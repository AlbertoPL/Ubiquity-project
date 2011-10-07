package server

import java.io._
import java.net.Socket
import java.util.List

import scala.collection.JavaConversions._

import message._

object ClientHandler {
  val maxLoginTries = 3 
}

class ClientHandler(var socket: Socket) extends Runnable with Messageable {

  var host: String = _
  var port: Int = _
  var osName: String = _
  var connected: Boolean = false
  var loggedIn: Boolean = false
  var messageSender: MessageSender = _
  
  var running: Boolean
  var receiver: MessageReceiver
  var fileServer: FileServer
  var fileSocket: Socket
  var deviceName: String
  var osType: String
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
      if (code == MessageCode.CLIENT_SEND_AUTH) {
        var payload = message.getPayload
        if (login(payload)) {
          m = new Message(MessageCode.REQUEST_NAME_AND_OS, null)
          username = payload.substring(0, payload.indexOf(" "))
        }
        else {
          loginTries += 1
          if (loginTries >= ClientHandler.maxLoginTries) {
            m = new Message (MessageCode.SERVER_BLOCK_AUTH, null)
          }
          else {
            m = new Message(MessageCode.SERVER_REJECT_AUTH, null)
          }
        }
        messageSender.enqueueMessage(m)
      }
      else if (code == MessageCode.NAME_AND_OS) {
        var payload = message.getPayload
        var os = payload.substring(0, payload.indexOf(':')).trim
        var name = payload.substring(payload.indexOf(':') + 1).trim
        System.out.println("NAME: " + name)
        if (Server.validOsTypes.contains(os)) {
          deviceName = name
          osType = os
          loggedIn = true
          fileServer = new FileServer(this)
          messageSender.enqueueMessage(new Message(MessageCode.SERVER_ACCEPT_AUTH, null))
        }
        else {
          Server.validOsTypes.foreach(println)
          System.err.println(os)
          payload = message.getCode() + " " + message.getPayload()
          m = new Message(MessageCode.DEVICE_NOT_SUPPORTED, payload)
          messageSender.enqueueMessage(m)
        }
      }
      else {
        var payload = message.getCode() + " " + message.getPayload()
        m = new Message(MessageCode.NOT_LOGGED_IN, payload)
        messageSender.enqueueMessage(m)
      }
    }
    else {
      code match {
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
      }
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
    running = true
    messageSender = new MessageSender(this)
      var t = new Thread(messageSender)
      t.start
      
      //XXX this seems wrong. using t over again?
      receiver = new MessageReceiver(this)
      t = new Thread(receiver)
      t.start
      
      connected = true
      System.out.println("Request authentication")
      var m = new Message(MessageCode.SERVER_REQUEST_AUTH, null)
      messageSender.enqueueMessage(m)
      
      while (running) {
        m = receiver.dequeueMessage
        if (m != null) {
          interpretCode(m)
        }
        else {
          try {
            Thread.sleep(1000 * 3)
          }
          catch {
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
    m.getCode() match {
    case MessageCode.INDEX => database.storeIndexInDatabase(username, file, deviceName)
    case MessageCode.CACHE => ()
    case MessageCode.BACKUP => ()
    }
    
  }
  
  def userExists(username: String) = database.userExists(username)
  
  def allUserFiles = database.selectAllFilesFromUser(username)
  
  def login(payload: String) = database.login(payload.substring(0, 
      payload.indexOf(" ")), payload.substring(payload.indexOf(" ")+ 1))
  
  def betaSignup(username: String, email: String) =
    database.betaSignup(username, email)
  
  def fileSize(filepath: String, nameOfDevice: String) =
    database.getFileSize(username, filepath, nameOfDevice)
}
