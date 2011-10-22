package client;

import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Properties
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPasswordField
import message.Message
import message.MessageCode
import message.MessageReceiver
import message.MessageSender
import message.Messageable
import util.BaseConversion
import javax.swing.JComponent

/**
 * A client implements a Messageable interface, which defines methods the
 * client must implement in order to interpret messages received through the
 * MessageHandler. The client then is responsible for starting up all client
 * services, the Indexer, the MessageHandler, and the FileHandler, as well as
 * keeping track of all message codes. The client also makes sure to stay
 * connected to the server and keeps the username and password stored such that
 * when the client must reconnect with the server for whatever reason, the
 * reconnect happens seamlessly.
 *
 */
object Client {

  def main(args: Array[String]) {
    val properties = new Properties
    properties.load(new FileInputStream("client.properties"))
    var c: Client = new Client
    c.startWithDefaults(properties)
    var t: Thread = new Thread(c)
    t.start
    c.connect
  }
}

class Client extends Runnable with Messageable {

  var rootFolder: String = _

  var indexer: Indexer = _
  var database: Database = _
  var receiver: MessageReceiver = _
  var running: Boolean = _
  var fileHandler: FileHandler = _
  var fileMonitor: FileMonitor = _

  /**
   * Starts the indexer and the message handler on separate threads
   *
   */
  def startWithDefaults(defaults: Properties) {
    port = Integer.parseInt(defaults.getProperty("port"));
    host = defaults.getProperty("host");

    indexer = new Indexer(this);
    fileMonitor = new FileMonitor(this);
    fileHandler = new FileHandler(this);
    database = new Database();
    loggedIn = false;
    connected = false;
  }

  //  TODO: make this in the WebUbiquity project if Alberto ever figures it out
  //  public Client(String host, int port) {
  //    this.port = port;
  //    hostname = host;
  //    loggedIn = false;
  //    connected = false;
  //    connect();
  //  }

  override def interpretCode(message: Message) {
    /**
     * Codes client will receive:
     * 0. Server asks for authentication
     * 1. Server authorizes authentication
     * 2. Server rejects authentication
     * 3. Server blocks authentication (too many tries)
     * 4. Server requests the latest index (same as client)
     * 5. Server requests a file (same as client)
     * 6. Server sends master index (same as client)
     * 7. Server sends a file (same as client)
     * 11. Server says client is not logged in
     */
    var code = message.getCode()
    var t: Thread = null
    var m: Message = null
    code match {
      case MessageCode.NOT_LOGGED_IN =>
        var newCode = Integer.parseInt(message.getPayload().substring(0,
          message.getPayload().indexOf(" ")))
        m = new Message(newCode, message.getPayload().substring(
          message.getPayload().indexOf(" ") + 1))
        messageSender.enqueueMessage(m)
      case MessageCode.DEVICE_NOT_SUPPORTED =>
        System.out.println("This device is not supported by the server!")
        connected = false
      case MessageCode.SERVER_REQUEST_AUTH =>
        login
      //TODO: Handle the case where the algorithm check fails (it shouldn't!)
      case MessageCode.SERVER_ACCEPT_AUTH =>
        System.out.println("Successfully logged in!");
        loggedIn = true;
      case MessageCode.SERVER_REJECT_AUTH =>
        JOptionPane.showMessageDialog(null,
          "Username and/or password are incorrect!",
          "Authentication Failed", JOptionPane.ERROR_MESSAGE)
        login
      //TODO: Handle the case where the algorithm check fails (it shouldn't!)
      case MessageCode.SERVER_BLOCK_AUTH =>
        JOptionPane.showMessageDialog(null,
          "Too many failed attempts to login! Please try again later.",
          "Account Locked", JOptionPane.ERROR_MESSAGE)
      case MessageCode.INDEX_REQUEST => None
      case MessageCode.FILE_REQUEST =>
        m = new Message(MessageCode.SENDING_FILE, message.getPayload())
        messageSender.enqueueMessage(m)
      case MessageCode.SERVER_INDEX_REQUEST_ACK =>
        fileHandler.setPort(Integer.parseInt(message.getPayload().substring(0,
          message.getPayload().indexOf(' '))))
        m = new Message(MessageCode.SENDING_FILE, message.getPayload().substring(
          message.getPayload().indexOf(' ') + 1))
        fileHandler.setFileToSendMetadata(m)
        fileHandler.setSending()
        t = new Thread(fileHandler)
        t.start()
      case MessageCode.SERVER_FILE_REQUEST_ACK =>
        fileHandler.setPort(Integer.parseInt(message.getPayload().substring(0,
          message.getPayload().indexOf(' '))))
        m = new Message(MessageCode.SENDING_FILE, message.getPayload().substring(
          message.getPayload().indexOf(' ') + 1))
        fileHandler.setFileToSendMetadata(m);
        fileHandler.setSending()
        t = new Thread(fileHandler)
        t.start()
      case MessageCode.REQUEST_NAME_AND_OS =>
        m = new Message(MessageCode.NAME_AND_OS, osName + ":" + deviceName)
        messageSender.enqueueMessage(m)
      case _ =>
        System.err.println("INVALID MESSAGE CODE DETECTED: " + message.getCode())
    }
  }

  def login {
    var username = JOptionPane.showInputDialog("Username:");
    var md: MessageDigest = null;
    try {
      md = MessageDigest.getInstance("SHA");
    } catch {
      case e: NoSuchAlgorithmException =>
        e.printStackTrace
    }
    if (md != null) {
      var label = new JLabel("Please enter your password:");
      var jpf = new JPasswordField();
      JOptionPane.showConfirmDialog(null, Array(label, jpf), "Password:",
        JOptionPane.OK_CANCEL_OPTION);
      var passwordHash =
        md.digest(String.valueOf(jpf.getPassword()).getBytes());
      System.out.println(BaseConversion.toHexString(passwordHash));
      var m = new Message(MessageCode.CLIENT_SEND_AUTH, username + " " +
        BaseConversion.toHexString(passwordHash));
      messageSender.enqueueMessage(m);
    }
  }

  def connect = {
    try {
      socket = new Socket(host, port);
    } catch {
      case uhe: UnknownHostException =>
        uhe.printStackTrace
      case ioe: IOException =>
        ioe.printStackTrace
    }

    if (socket != null) {
      messageSender = new MessageSender(this)
      var t = new Thread(messageSender)
      t.start

      receiver = new MessageReceiver(this)
      t = new Thread(receiver)
      t.start

      connected = true
      connected
    } else {
      false
    }
  }

  def stop {
    running = false;
  }

  override def receiverDisconnected() {
    try {
      Thread.sleep(1000 * 5);
    } catch {
      case e: InterruptedException =>
        e.printStackTrace
    }
    connect
  }

  override def run() {
    running = true

    //start indexer TODO: set indexer to be run periodically
    if (indexer != null) {
      var t = new Thread(indexer)
      if (!t.isAlive) {
        t = new Thread(indexer) //initialized twice at first, I know.
        t.start
        t.setPriority(Thread.MIN_PRIORITY)
      }
    }

    //start file monitor TODO: find out how JNotify breaks when it does
    if (fileMonitor != null) {
      var tt = new Thread(fileMonitor)
      if (!tt.isAlive) {
        tt = new Thread(fileMonitor)
        tt.start
        tt.setPriority(Thread.MAX_PRIORITY)
      }
    }

    while (running) {
      if (receiver != null) {
        var m: Message = receiver.dequeueMessage
        if (m != null) {
          interpretCode(m)
        }
      }

      try {
        Thread.sleep(1000 * 5)
      } catch {
        case e: InterruptedException => e.printStackTrace
      }
    }
  }

  override def deviceName = {
    try {
      InetAddress.getLocalHost().getHostName
    } catch {
      case e: UnknownHostException => {
        e.printStackTrace
        ""
      }
    }
  }

  override def fileReceivedCallback(file: String, m: Message) {
    System.out.println("File received: " + file);
    //TODO: Potentially other actions like talk to a GUI or make a native call
  }
}
