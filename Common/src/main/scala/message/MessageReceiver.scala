package message
import scala.actors.Actor
import java.io.ObjectInputStream
import java.util.List
import java.util.ArrayList
import java.io.InputStream
import java.io.IOException

object MessageReceiver {
  val timeout = 100

  def build(c: Messageable, i: InputStream) = {
    val mr = new MessageReceiver(c)
    if (i == null)
      new ObjectInputStream(mr.master.inputStream)
    else
      new ObjectInputStream(i)
    mr
  }
}

class MessageReceiver(c: Messageable) extends Actor {
  var running: Boolean = false
  var connected: Boolean = false

  var master: Messageable = c
  var messageQueue: List[FileMessage] = new ArrayList[FileMessage]
  var in: ObjectInputStream = new ObjectInputStream(master.inputStream)

  def enqueueMessage(m: FileMessage) {
    messageQueue.add(m);
  }

  def FileMessage =
    if (messageQueue.isEmpty) {
      null
    } else {
      messageQueue.remove(0);
    }

  def stop() {
    running = false;
  }

  override def act() {
    running = true;
    connected = master.connected;
    while (running) {
      while (master.connected && connected && in != null) {
        try {
          var m: FileMessage = in.readObject.asInstanceOf[FileMessage]
          System.out.println("Message received");
          if (m.getCode == -1) {
            connected = false;
          } else {
            System.out.println("Receiving message: " + m.getCode);
            enqueueMessage(m);
          }
        } catch {
          case ioe: IOException =>
            ioe.printStackTrace
            connected = false; //reconnect
            //tell the master runnable that it's over
            master.receiverDisconnected
          case cnfe: ClassNotFoundException => cnfe.printStackTrace()
        }
      }
      try {
        Thread.sleep(MessageReceiver.timeout)
      } catch {
        case ie: InterruptedException => ie.printStackTrace
      } //sleep 3 seconds
    }
    System.out.println("We're disconnected...");

    //tell the client handler that the message receiver has failed.
  }
}