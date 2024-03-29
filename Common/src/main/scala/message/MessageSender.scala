package message

import java.io.IOException
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.List
import scala.actors.Actor
import scala.actors.Actor._

object MessageSender {
  val timeout = 3000

  def build(c: Messageable, o: OutputStream) = {
    val ms = new MessageSender(c)
    if (o == null)
      new ObjectOutputStream(ms.master.outputStream)
    else
      new ObjectOutputStream(o)
    ms
  }
}

class MessageSender(c: Messageable) extends Actor {

  var running: Boolean = false
  var master: Messageable = c
  var messageQueue: List[Message] = new ArrayList[Message]
  var out: ObjectOutputStream = new ObjectOutputStream(master.outputStream)

  def enqueueMessage(m: Message) {
    messageQueue.add(m);
  }

  def dequeueMessage =
    if (messageQueue.isEmpty()) {
      null
    } else {
      messageQueue.remove(0);
    }

  def stop() {
    running = false;
  }

  override def act() {
    running = true

    while (running) {
      while (master.connected && out != null) {
        var m = dequeueMessage
        if (m != null && master.loggedIn) {
          try {
            System.out.println("Sending message: " + m.getCode());
            out.writeObject(m);
          } catch {
            case ioe: IOException => ioe.printStackTrace
          }
        } //ok to send if we're asking for authentication or sending name and os, should move list elsewhere.
        else if (m != null && (m.getCode() == MessageCode.SERVER_REQUEST_AUTH || m.getCode() == MessageCode.CLIENT_SEND_AUTH || m.getCode() == MessageCode.SERVER_ACCEPT_AUTH || m.getCode() == MessageCode.SERVER_REJECT_AUTH || m.getCode() == MessageCode.SERVER_BLOCK_AUTH || m.getCode() == MessageCode.REQUEST_NAME_AND_OS || m.getCode() == MessageCode.NAME_AND_OS || m.getCode() == MessageCode.DEVICE_NOT_SUPPORTED || m.getCode() == MessageCode.NOT_LOGGED_IN)) {
          try {
            System.out.println("Sending message: " + m.getCode());
            out.writeObject(m);
          } catch {
            case ioe: IOException => ioe.printStackTrace
          }
        } else if (m != null && !master.loggedIn) {
          System.err.println("Will resend message later");
          enqueueMessage(m);
        } else {
          //sleep a bit
          try {
            Thread.sleep(MessageSender.timeout)
          } catch {
            case ie: InterruptedException => ie.printStackTrace
          }
        }
      }
      try {
        Thread.sleep(MessageSender.timeout)
      } catch {
        case ie: InterruptedException => ie.printStackTrace
      }
    }
  }
}
