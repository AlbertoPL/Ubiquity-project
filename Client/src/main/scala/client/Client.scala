package client;

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.PrintWriter
import java.lang.Override
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Properties
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.ConsumerCancelledException
import com.rabbitmq.client.QueueingConsumer
import com.rabbitmq.client.ShutdownSignalException
import interfaces.Serviceable
import message.FileMessage
import message.MessageCode
import message.MessageReceiver
import message.Messageable
import message.RequestMessage
import message.StringListMessage
import message.StringMapMessage
import util.BaseConversion
import com.rabbitmq.client.Connection

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
    properties.load(new FileInputStream("src/main/resources/client.properties"))
    //var c: Client = new Client
   // c.startWithDefaults(properties)
  //  var t: Thread = new Thread(c)
 //   t.start
 //   c.connect
  }
}

class Client(controller: Serviceable) extends Messageable {

  var rootFolder: String = _

 // var indexer: Indexer = _
  var username: String = _
  var password: String = _
  var userid: Int = _
  var database: Database = _
  var receiver: MessageReceiver = _
  var running: Boolean = _
  var clientViewController: Serviceable = controller
  var channel: Channel = _
  var BUFFER: Int = 2048*100
  var pathToRemoteFileStore: String = _
  var factory: ConnectionFactory = new ConnectionFactory()
  var connection: Connection = _
  //var fileMonitor: FileMonitor = _
  //var serverFactory: FtpServerFactory = _
  //var server: FtpServer = _
  ///var listenerFactory: ListenerFactory = _
  //var ssl: SslConfigurationFactory = _
  //var userManagerFactory: PropertiesUserManagerFactory = _
  //var connectionConfigFactory: ConnectionConfigFactory = _

def startWithDefaults(defaults: Properties) {
    port = Integer.parseInt(defaults.getProperty("port"));
    host = defaults.getProperty("host");
    pathToRemoteFileStore = System.getProperty("user.home") + 
    		System.getProperty("file.separator") + ".ubiquity" + 
    		System.getProperty("file.separator") + "projects" + 
    		System.getProperty("file.separator"); 
    
   // indexer = new Indexer(this);
   // fileMonitor = new FileMonitor(this);
    //fileHandler = new FileHandler(this);
    database = new Database();
    loggedIn = false;
    connected = false;

   // serverFactory = new FtpServerFactory();
    
    //connectionConfigFactory = new ConnectionConfigFactory();
    //connectionConfigFactory.setAnonymousLoginEnabled(false);
   // listenerFactory = new ListenerFactory();
   // listenerFactory.setPort(21); //TODO: hardcoded for now
   // listenerFactory.setServerAddress("localhost");
    
  //  ssl = new SslConfigurationFactory();
    // ssl.setKeystoreFile(new File("src/main/resources/ftpserver.jks"));
    // ssl.setKeystorePassword("password"); //TODO: all hardcoded...

    //listenerFactory.setSslConfiguration(ssl.createSslConfiguration());
    //listenerFactory.setImplicitSsl(false);

  //  serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
  //  serverFactory.addListener("default", listenerFactory.createListener());

  //  userManagerFactory = new PropertiesUserManagerFactory();
 //   serverFactory.setUserManager(new TestUserManagerFactory().createUserManager());

  //  server = serverFactory.createServer();

    // start the server
  //  server.start();
  }

  //  TODO: make this in the WebUbiquity project if Alberto ever figures it out
  //  public Client(String host, int port) {
  //    this.port = port;
  //    hostname = host;
  //    loggedIn = false;
  //    connected = false;
  //    connect();
  //  }

  /*override def interpretCode(message: Message) {
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
      case MessageCode.REQUEST_AUTH =>
       clientViewController.requestAuth();
      //TODO: Handle the case where the algorithm check fails (it shouldn't!)
      case MessageCode.ACCEPT_AUTH =>
        System.out.println("Successfully logged in!");
        loggedIn = true;
        val user = message.getPayload().substring(0, message.getPayload().indexOf(' '))
        val passhash = message.getPayload().substring(message.getPayload().indexOf(' ') + 1)
        clientViewController.loginSuccess(true, user, passhash, "Successful logon")
      case MessageCode.REJECT_AUTH =>
        //JOptionPane.showMessageDialog(null,
        //  "Username and/or password are incorrect!",
        //  "Authentication Failed", JOptionPane.ERROR_MESSAGE)
        val user = message.getPayload().substring(0, message.getPayload().indexOf(' '))
        val passhash = message.getPayload().substring(message.getPayload().indexOf(' ') + 1)
        clientViewController.loginSuccess(false, user, passhash, "Username and/or password are incorrect!") 
        clientViewController.requestAuth()
      //TODO: Handle the case where the algorithm check fails (it shouldn't!)
      case MessageCode.BLOCK_AUTH =>
        //JOptionPane.showMessageDialog(null,
        //  "Too many failed attempts to login! Please try again later.",
        //  "Account Locked", JOptionPane.ERROR_MESSAGE)
        val user = message.getPayload().substring(0, message.getPayload().indexOf(' '))
        val passhash = message.getPayload().substring(message.getPayload().indexOf(' ') + 1)
        clientViewController.loginSuccess(false, user, passhash, "Too many failed attempts to login! Please try again later.") 
      case MessageCode.REQUEST_DIRECTORY =>
        System.out.println("Directory listing requested for: " + message.getPayload());
        var sb: StringBuilder = new StringBuilder();
        if (message.getPayload().equalsIgnoreCase("root")) {
          val roots:Array[File] = File.listRoots();
          
          for (i<-0 until roots.length) {
        	//table.addItem(new Object[] {
        		//    roots[i],roots[i].getTotalSpace() - roots[i].getFreeSpace(),roots[i].lastModified()}, new Integer(i+2));
            System.out.println("Root[" + i + "] = " + roots(i))
            sb.append(roots(i))
            sb.append("\n")
          }
        }
        else {
          var root: File = new File(message.getPayload());
          root.listFiles().foreach( f=>sb.append(f)) 
        }
        m = new Message(MessageCode.SEND_DIRECTORY, sb.toString())

      case MessageCode.SEND_FILE =>
        //m = new Message(MessageCode.SEND_FILE, message.getPayload())
       //messageSender.enqueueMessage(m)
     //   sendFile(message.getPayload)
     /* case MessageCode.SERVER_INDEX_REQUEST_ACK =>
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
        t.start()*/
      case MessageCode.REQUEST_NAME_AND_OS =>
        userid = Integer.parseInt(message.getPayload());
        System.out.println(userid);
        m = new Message(MessageCode.SEND_NAME_AND_OS, osType + ";" + deviceName + ";" + macAddress + ";" + userid)
        messageSender.enqueueMessage(m)
      case _ =>
        System.err.println("INVALID MESSAGE CODE DETECTED: " + message.getCode())
    }
  }*/

 /* def login {
    System.out.println("Login")
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
      
      var passwordHashDigest =
        md.digest(String.valueOf(jpf.getPassword()).getBytes());
      passwordHash = BaseConversion.toHexString(passwordHashDigest);
      System.out.println(passwordHash);
      var m = new Message(MessageCode.SEND_AUTH, username + " " +
        BaseConversion.toHexString(passwordHashDigest));
      messageSender.enqueueMessage(m);
    }
  }*/
  
  def login(username:String, pass:String): Boolean = {
    var md: MessageDigest = null;
    try {
      md = MessageDigest.getInstance("SHA");
    } catch {
      case e: NoSuchAlgorithmException =>
        e.printStackTrace
      case ex: Exception =>
        ex.printStackTrace
    }
    if (md != null) {
      var passwordHash =
        md.digest(pass.getBytes());
      System.out.println(BaseConversion.toHexString(passwordHash));
      
	    if (connect) {
	      var input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	      var output = new PrintWriter(socket.getOutputStream(), true);
	      output.println(username)
	      output.println(BaseConversion.toHexString(passwordHash))
	      var userId = Integer.parseInt(input.readLine())
	      if (userId > 0) {
	    	  output.println(deviceName)
	    	  output.println(macAddress)
	    	  password = pass
	    	  this.username = username
	    	  subscribetoqueues
	    	  System.out.println("Logged in!")
	    	  userid = userId
	    	  return true
	      }
	      else {
	        System.out.println("Failed to authenticate!")
	        password = null
	        this.username = null
	        return false
	      }
	      // messageSender = new MessageSender(this)
	      //var t = new Thread(messageSender)
	    //  messageSender.start
	
	    //  receiver = new MessageReceiver(this)
	    //  receiver.start
	    } else {
	      System.out.println("Failed to authenticate somehow!")
	      return false
	    }
    }
    System.out.println("We shouldn't get here...")
    return false
  }
  
  def autologin() {
    var md: MessageDigest = null;
    try {
      md = MessageDigest.getInstance("SHA");
    } catch {
      case e: NoSuchAlgorithmException =>
        e.printStackTrace
    }
    if (md != null) {
      var passwordHash =
        md.digest(password.getBytes());
      System.out.println(BaseConversion.toHexString(passwordHash));
    
	      var input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	      var output = new PrintWriter(socket.getOutputStream(), true);
	      output.println(username)
	      output.println(BaseConversion.toHexString(passwordHash))
	      var userId = Integer.parseInt(input.readLine())
	      if (userId > 0) {
	    	  output.println(deviceName)
	    	  output.println(macAddress)
	    	  subscribetoqueues
	    	  true
	      }
	      else {
	        System.out.println("Failed to authenticate!")
	        username = null
	        password = null
	        false
	      }
	      // messageSender = new MessageSender(this)
	      //var t = new Thread(messageSender)
	    //  messageSender.start
	
	    //  receiver = new MessageReceiver(this)
	    //  receiver.start
	   
    }
  }
  
  def subscribetoqueues() {
    factory = new ConnectionFactory();
    factory.setUsername(username);
    var passwordHash = BaseConversion.toHexString(
	    MessageDigest.getInstance("SHA").digest(password.getBytes()))
	factory.setPassword(passwordHash);
	factory.setHost(host);
	connection = factory.newConnection();
	
	channel = connection.createChannel();
	channel.exchangeDeclare(username+passwordHash, "topic");
	channel.queueDeclare(username+passwordHash+deviceName+"MQclient", true, false, false, null);//durable, non-exclusive, non-autodelete
	channel.queueDeclare(username+passwordHash+deviceName+"FQclient", true, false, false, null);//durable, non-exclusive, non-autodelete
	channel.queueBind(username+passwordHash+deviceName+"MQclient", username+passwordHash, username+passwordHash+deviceName+"MQclient");
	channel.queueBind(username+passwordHash+deviceName+"FQclient", username+passwordHash, username+passwordHash+deviceName+"FQclient");
	
	//listen to our own queue for back messages
	var consumer = new QueueingConsumer(channel);
	var fileConsumer = new QueueingConsumer(channel);
	channel.basicConsume(username+passwordHash+deviceName+"MQclient", true, consumer);
	channel.basicConsume(username+passwordHash+deviceName+"FQclient", true, fileConsumer);
	
	//sign up to file queue to listen!
	 new Thread(new Runnable() {

				@Override
				def run() {
					while (connection.isOpen()) {
						
						try {
							var delivery = fileConsumer.nextDelivery();
						    var message = delivery.getBody();
						    var bis = new ByteArrayInputStream(message);
						    var in = new ObjectInputStream(bis);
						    var m = in.readObject();
						    in.close();
						    bis.close();
						    m match {
						      case fm: FileMessage =>
						        fm.getCode() match {
						          case MessageCode.FILE =>
						    	
							      try {
							        if (!new File(pathToRemoteFileStore).exists()) {
							          new File(pathToRemoteFileStore).mkdir();
							        }
						    		   var f = new File(pathToRemoteFileStore + fm.getFilename());
						    		   var buf = ByteBuffer.wrap(fm.getPayload());
						    		    // Create a writable file channel
						    		    var wChannel = new FileOutputStream(f, true).getChannel();
						
						    		    // Write the ByteBuffer contents; the bytes between the ByteBuffer's
						    		    // position and the limit is written to the file
						    		    wChannel.write(buf);
						    		    wChannel.truncate(fm.getFilelength());
						    		    System.out.println("Receiving file: " + fm.getFilename());
						    		    // Close the file
						    		    wChannel.close();
						    		    buf.clear();
					    		} catch {
					    		  case ioe: IOException =>
					    		    ioe.printStackTrace()
					    		}
						          case _ =>
						          	System.err.println("Invalid message code received! " + fm.getCode());	
						          
						        }
						      
						      case _ => throw new ClassCastException
						    }
						} catch {
						  case sse: ShutdownSignalException =>
							sse.printStackTrace
						  case cce: ConsumerCancelledException =>
						  	cce.printStackTrace
						  case ie: InterruptedException =>
						  	ie.printStackTrace
						  case ioe: IOException =>
						  	ioe.printStackTrace
						  case cnfe: ClassNotFoundException =>
						  	cnfe.printStackTrace
						}
					}
				}
		    	
		    }).start();
	
	//sign up to message queue to listen!
		    new Thread(new Runnable() {

				@Override
				def run() {
					while (connection.isOpen()) {
						
						try {
							var delivery = consumer.nextDelivery();
						    var message = delivery.getBody();
						    var bis = new ByteArrayInputStream(message);
						    var in = new ObjectInputStream(bis);
						    var m = in.readObject();
						    in.close();
						    bis.close();
						    System.out.println("Message on MQ received");
						    m match {
						      case fm: FileMessage =>
						        System.out.println("File message received on MQ");
						        var filename = fm.getFilename()
						        var filepath = fm.getFilepath()
						    	var returndevice = new String(fm.getPayload())
						    	var file = new File(filepath)
						        System.out.println("File being asked for: " + filepath)
						    	if (file.exists()) {
						    		new Thread(new Runnable() {
						    		  def run() {
						    		    var fin = new FileInputStream(file);
									    var bin = new BufferedInputStream(fin);
										var fileLength = file.length();
										
										var bytesRead = 0;
										var mybytearray = new Array[Byte](BUFFER);
	
									    while (fileLength > 0) {
									    	if (fileLength > BUFFER) {
										    	bytesRead = bin.read(mybytearray);
										    }
										    else {
										    	bytesRead = bin.read(mybytearray, 0, fileLength.asInstanceOf[Int]);
										    }
										    fileLength -= bytesRead;
										    var bos = new ByteArrayOutputStream();
											var out = new ObjectOutputStream(bos);   
											var newmessage = new FileMessage(MessageCode.FILE, file.getName(), file.getCanonicalPath(), file.length(), mybytearray);
					
										    out.writeObject(newmessage);
										    out.flush();
										    var yourBytes = bos.toByteArray();
										    out.close();
										    bos.close();
										    System.out.println("Sending to return device: " + returndevice);
											channel.basicPublish(username+passwordHash, username+passwordHash+returndevice+"FQclient", null, yourBytes);
									    }
						    		  }
						    		}).start()
						    	  
						    	}
						    	else {
						    	  System.out.println("No such file or directory for file: " + filepath + " that is being requested")
						    	}
						    
						      case slm: StringListMessage =>
						        System.out.println("StringList message received on MQ");
						        slm.getCode() match {
						          case MessageCode.REQUEST_FILES_OF_TYPE =>
						            System.out.println("Got a request files of type message!");
						          	var filelist = slm.getList()
						          	System.out.println("FILE ID CODE: " + slm.getMetacode());
						         //TODO: Come back to this or remove it entirely
						          	// 	controller.getFilesByTypeSuccess(slm.getMetacode(), filelist)
						          case _ =>
						          	System.err.println("Invalid message code received! " + slm.getCode());	
						        }
						      case smm: StringMapMessage =>
						        System.out.println("StringMap message received on MQ");
						        smm.getCode() match {
						          case MessageCode.REQUEST_FILES_OF_TYPE =>
						            System.out.println("Got a request files of type message!");
						          	var filemap = smm.getFileData()
						          	System.out.println("FILE ID CODE: " + smm.getMetacode());
						          	controller.getFilesByTypeSuccess(smm.getMetacode(), filemap)
						        }
						    }
						} catch {
						  case sse: ShutdownSignalException =>
							sse.printStackTrace
						  case cce: ConsumerCancelledException =>
						  	cce.printStackTrace
						  case ie: InterruptedException =>
						  	ie.printStackTrace
						  case ioe: IOException =>
						  	ioe.printStackTrace
						  case cnfe: ClassNotFoundException =>
						  	cnfe.printStackTrace
						}
					}
				}
		    	
		    }).start();
  } 

  //TODO: REDO THIS!
  def requestFile(filename: String) {
	  var m = new FileMessage(MessageCode.REQUEST_FILE, filename, filename, 0, null);
	  var bos = new ByteArrayOutputStream();
	  var out = new ObjectOutputStream(bos);   
	  out.writeObject(m);
	  out.flush();
	  var yourBytes = bos.toByteArray();
	  out.close();
	  bos.close();
	  var passwordHash = BaseConversion.toHexString(
	    MessageDigest.getInstance("SHA").digest(password.getBytes()))
	  channel.basicPublish(username+passwordHash, username+passwordHash+deviceName+"MQserver", null, yourBytes);
  }
  
  //TODO: Hacking this for ubiquity project files that arent being backed up but ARE being stored
  def backupFile(filename: String, filepath: String, filesize: Long) {
    System.out.println("File backup requested");	  
    var m = new FileMessage(MessageCode.BACKUP_FILE, filename, filepath, filesize, null);
    	  var bos = new ByteArrayOutputStream();
    	  var out = new ObjectOutputStream(bos);   
    	  out.writeObject(m);
    	  out.flush();
    	  var yourBytes = bos.toByteArray();
    	  out.close();
    	  bos.close();
    	  var passwordHash = BaseConversion.toHexString(
	    MessageDigest.getInstance("SHA").digest(password.getBytes()))
	      channel.basicPublish(username+passwordHash, username+passwordHash+deviceName+"MQserver", null, yourBytes)
    	  System.out.println("Message sent for back up!")
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
      println("We have connected")
      connected = true
      /*if (username != null) {
        autologin
      }*/
      connected
      
     // messageSender = new MessageSender(this)
      //var t = new Thread(messageSender)
    //  messageSender.start

    //  receiver = new MessageReceiver(this)
    //  receiver.start
    } else {
      false
    }
  }
  
  /*
  def sendFile(filepath: String) {
    val t = new Thread(new Runnable() {
      
      var sock: Socket = _;
      def run() {
       try {
    	   sock = new Socket(host, 4444);//TODO: Change hardcoded port
       } catch {
       	case uhe: UnknownHostException =>
       		uhe.printStackTrace
       	case ioe: IOException =>
       	ioe.printStackTrace
       }
       if (sock != null) {
         var out = new DataOutputStream(sock.getOutputStream())
         out.writeInt(1)
         out.writeChars("A") //TODO: Fix this hack. We should really only be passing user ID
         out.writeInt(passwordHash.length())
         out.writeChars(passwordHash)
         out.writeInt(deviceName.length())
         out.writeChars(deviceName);
         out.writeInt(1) //TODO: Only sending one file, should be modified for multiple
         out.flush()
         val f = new File(filepath);
							
			if (f.exists()) {
				out.writeInt(f.getName().length());
				out.flush();
				out.writeChars(f.getName());
				out.flush();
				val fin = new FileInputStream(f);
				val bin = new BufferedInputStream(fin);
				var fileLength = f.length();
				out.writeLong(fileLength);
				out.flush();
				var bytesRead: Int = 0
				val BUFFER = 2048
			  	var mybytearray = new Array[Byte](BUFFER);

				while (fileLength > 0) {
					if (fileLength > BUFFER) {
						bytesRead = bin.read(mybytearray);
					}
					else {
						bytesRead = bin.read(mybytearray, 0, fileLength.asInstanceOf[Int]);
					}
					fileLength -= bytesRead;
					out.write(mybytearray, 0, bytesRead);
					out.flush();
				}
				bin.close();
			}
       }
      }
      
    });
    t.start
  }*/
  
  /*def backupFile(filename : String, filepath : String, filesize : Long) {
    var m = new Message(MessageCode.BACKUP_FILE, filename + "\n" + filepath + "\n" + filesize);
    messageSender.enqueueMessage(m);
  }*/
  
  def getRemoteFile(filepath : String) {
    System.out.println("File to retrieve: " + filepath);
    var m = new FileMessage(MessageCode.REQUEST_FILE, filepath, filepath, 0, null);
	  var bos = new ByteArrayOutputStream();
	  var out = new ObjectOutputStream(bos);   
	  out.writeObject(m);
	  out.flush();
	  var yourBytes = bos.toByteArray();
	  out.close();
	  bos.close();
	  var passwordHash = BaseConversion.toHexString(
	    MessageDigest.getInstance("SHA").digest(password.getBytes()))
	  channel.basicPublish(username+passwordHash, username+passwordHash+deviceName+"MQserver", null, yourBytes);
  }
  
  def getFilesOfType(filetypeid : Int) {
    System.out.println("File type id to get: " + filetypeid);
    var m = new RequestMessage(MessageCode.REQUEST_FILES_OF_TYPE, userid, filetypeid);
	  var bos = new ByteArrayOutputStream();
	  var out = new ObjectOutputStream(bos);   
	  out.writeObject(m);
	  out.flush();
	  var yourBytes = bos.toByteArray();
	  out.close();
	  bos.close();
	  System.out.println("Code in message: " + m.getMetacode());
	  var passwordHash = BaseConversion.toHexString(
	    MessageDigest.getInstance("SHA").digest(password.getBytes()))
	  channel.basicPublish(username+passwordHash, username+passwordHash+deviceName+"MQserver", null, yourBytes);
  }
  
  def isConnected() = {
    connected;
  }

  def stop {
    running = false
    connected = false
    socket.close()
    connection.close()
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

  def run() {
    
    connect
    running = true

    //start indexer TODO: set indexer to be run periodically
    /*if (indexer != null) {
      var t = new Thread(indexer)
      if (!t.isAlive) {
        t = new Thread(indexer) //initialized twice at first, I know.
        t.start
        t.setPriority(Thread.MIN_PRIORITY)
      }
    }*/

    //start file monitor TODO: find out how JNotify breaks when it does
    /*if (fileMonitor != null) {
      var tt = new Thread(fileMonitor)
      if (!tt.isAlive) {
        tt = new Thread(fileMonitor)
        tt.start
        tt.setPriority(Thread.MAX_PRIORITY)
      }
    }*/

    while (running) {
      if (receiver != null) {
        //var m: FileMessage = receiver.dequeueMessage
      //  if (m != null) {
          //interpretCode(m)
      //  }
      }

      try {
        Thread.sleep(100)
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

  override def osType = {
    System.getProperty("os.name")
  }

  override def fileReceivedCallback(file: String, m: FileMessage) {
    System.out.println("File received: " + file);
    //TODO: Potentially other actions like talk to a GUI or make a native call
  }
  
  def getPathToRemoteFileStore = {
    pathToRemoteFileStore
  }
  
  override def macAddress = {
    var localMacAddress:String = ""
    try {
            var address: InetAddress = InetAddress.getLocalHost();

            /*
             * Get NetworkInterface for the current host and then read
             * the hardware address.
             */
            var ni:NetworkInterface = 
                    NetworkInterface.getByInetAddress(address);
            if (ni != null) {
                localMacAddress = ni.getHardwareAddress.toList.map(b => String.format("%02x",b.asInstanceOf[AnyRef])).mkString(":")     
            }
             else {
                	println("Address doesn't exist or is not accessible.");
                }
        } 
   catch {
      case e: UnknownHostException => e.printStackTrace
      case se: SocketException => se.printStackTrace
   }
    localMacAddress
  }
}
