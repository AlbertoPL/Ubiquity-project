package server

import java.lang.InterruptedException
import java.lang.Runnable
import java.lang.String
import java.lang.System
import java.lang.Thread
import java.net.Socket

import scala.collection.JavaConversions.asScalaBuffer

import message.Message
import message.MessageCode
import message.MessageReceiver
import message.MessageSender
import message.Messageable

object ClientHandler {
  val maxLoginTries = 3
}

class ClientHandler(var s: Socket) extends Runnable with Messageable {

  socket = s
  loggedIn = false
  var running: Boolean = _
  var receiver: MessageReceiver = _
  var fileServer: FileServer = _
  var deviceName: String = _
  var osType: String = _
  var username: String = _
  var loginTries: Int = 0
  var database: DatabaseAdapter = new PostgresDatabaseAdapter

  def interpretCode(message: Message) {
    /**
     * NOTE: Codes that have (same as #) will be the renumbered to #
     *
     * Codes client will receive:
     * 8. Client sends authentication
     * 9. Client sends index file (same as 6)
     * 10. Client sends file (same as 7)
     * 11. Client requests master index (other indices) (same as 4)
     * 12. Client requests a file (same as 5)
     */
    var code: Int = message.getCode
    var t: Thread = null //declared in case its needed
    var m: Message = null //declared in case its needed

    if (!loggedIn) {
      if (code == MessageCode.SEND_AUTH) {
        var payload = message.getPayload
        var userid: Int = login(payload)
        if (userid > 0) {
          m = new Message(MessageCode.REQUEST_NAME_AND_OS, String.valueOf(userid))
          username = payload.substring(0, payload.indexOf(" "))
        } else {
          loginTries += 1
          if (loginTries >= ClientHandler.maxLoginTries) {
            m = new Message(MessageCode.BLOCK_AUTH, null)
          } else {
            m = new Message(MessageCode.REJECT_AUTH, null)
          }
        }
        messageSender.enqueueMessage(m)
      } else if (code == MessageCode.SEND_NAME_AND_OS) {
        var payload = message.getPayload
        System.out.println("PAYLOAD: " + payload)
        var os = payload.substring(0, payload.indexOf(';')).trim
        payload = payload.substring(payload.indexOf(';') + 1);
        var name = payload.substring(0, payload.indexOf(';')).trim
        payload = payload.substring(payload.indexOf(';') + 1);
        var macaddr = payload.substring(0, payload.indexOf(';')).trim
        payload = payload.substring(payload.indexOf(';') + 1);
        var userid = Integer.parseInt(payload);
        System.out.println("NAME: " + name)
        if (Server.validOsTypes.contains(os)) {
          deviceName = name
          osType = os
          loggedIn = true
          fileServer = new FileServer(this)
          messageSender.enqueueMessage(new Message(MessageCode.ACCEPT_AUTH, null))
          insertDevice(deviceName, macaddr, userid)
        } else {
          Server.validOsTypes.foreach(println)
          System.err.println(os)
          payload = message.getCode() + " " + message.getPayload()
          m = new Message(MessageCode.DEVICE_NOT_SUPPORTED, payload)
          messageSender.enqueueMessage(m)
        }
      } else {
        var payload = message.getCode() + " " + message.getPayload()
        m = new Message(MessageCode.NOT_LOGGED_IN, payload)
        messageSender.enqueueMessage(m)
      }
    } else {
   /*   code match {
        case MessageCode.INDEX_REQUEST =>
          t = new Thread(fileServer)
          t.start
          m = new Message(MessageCode.SERVER_INDEX_REQUEST_ACK, String.valueOf(fileServer.getPort()) + " " + message.getPayload())
          messageSender.enqueueMessage(m)
        case MessageCode.FILE_REQUEST =>
          t = new Thread(fileServer)
          t.start
          m = new Message(MessageCode.SERVER_FILE_REQUEST_ACK, String.valueOf(fileServer.getPort()) + " " + message.getPayload())
          messageSender.enqueueMessage(m)
      }*/
    }
  }

  def stop() {
    running = false
    connected = false
    messageSender.stop
    receiver.stop
  }

  override def receiverDisconnected = stop

  override def run() {
    System.out.println("Connection succeeded");
    running = true
    connected = true

    messageSender = new MessageSender(this)
    //var t = new MessageSender(messageSender)
    messageSender.start
    System.out.println("Connection still succeeded")

    //XXX this seems wrong. using t over again?
    receiver = new MessageReceiver(this)
    receiver.start

    System.out.println("Request authentication")
    var m = new Message(MessageCode.REQUEST_AUTH, null)
    messageSender.enqueueMessage(m)

    while (running) {
      m = receiver.dequeueMessage
      if (m != null) {
        interpretCode(m)
      } else {
        try {
          Thread.sleep(100)
        } catch {
          case e: InterruptedException => e.printStackTrace
        }
      }
    }
    stop
  }

  override def rootFolder =
    username + System.getProperty("file.separator") +
      deviceName + System.getProperty("file.separator")

  override def fileReceivedCallback(file: String, m: Message) {
    // TODO: Determine if this is an index file, if so, update the database
    //Otherwise, do whatever is necessary with the file such as sending it
    //to another datastore
    /*m.getCode() match {
      case MessageCode.INDEX => database.storeIndexInDatabase(username, file, deviceName)
      case MessageCode.CACHE => ()
      case MessageCode.BACKUP => ()
    }*/

  }

  def userExists(username: String) = database.userExists(username)

  def allUserFiles = database.selectAllFilesFromUser(username)

  def login(payload: String) = database.login(payload.substring(0,
    payload.indexOf(" ")), payload.substring(payload.indexOf(" ") + 1))

  def betaSignup(username: String, email: String) =
    database.betaSignup(username, email)

  def fileSize(filepath: String, nameOfDevice: String) =
    database.getFileSize(username, filepath, nameOfDevice)

  def setUsername(name: String) {
    username = name
  }
  
  def insertDevice(devicename: String, macaddr:String, userid:Int) =
    database.insertDevice(devicename, macaddr, userid)
  
  override def macAddress = "Hi"
}
