package client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import message.Message;
import message.MessageCode;
import message.MessageReceiver;
import message.MessageSender;
import message.Messageable;
import util.BaseConversion;
import file.FileReceiver;
import file.FileSender;

/**
 * 
 * @author Alberto Pareja-Lecaros
 *
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
public class Client implements Messageable, Runnable {

	private final static String CLIENT_PROPERTIES = "client.properties";
		
	private Indexer indexer;
	private FileMonitor fileMonitor;
	private Database database;
	
	private MessageSender sender;
	private MessageReceiver receiver;
	private int port;
	private String hostname;
	private boolean running;
	private boolean connected;
	private Socket socket, fileSocket;
	private FileSender fileSender;
	private FileReceiver fileReceiver;
	
	/**
	 * Starts the indexer and the message handler on separate threads
	 * 
	 */
	public Client() {
		//get the host and port to use from external properties file
		Properties properties = new Properties();
		try {
		    properties.load(new FileInputStream (CLIENT_PROPERTIES));
		} catch (IOException e) {
			e.printStackTrace();
		}
		port = Integer.parseInt(properties.getProperty("port"));
		hostname = properties.getProperty("host");
		
		indexer = new Indexer(this);
		fileMonitor = new FileMonitor(this);
		database = new Database();
		
		connected = false;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getHost() { 
		return hostname;
	}
	
	@Override
	public void interpretCode(Message message) {
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
		 */
		int code = message.getCode();
		Thread t;
		switch(code) {
		case MessageCode.SERVER_REQUEST_AUTH:
			login();
			//TODO: Handle the case where the algorithm check fails (it shouldn't!)
			break;
		case MessageCode.SERVER_ACCEPT_AUTH:
			System.out.println("Successfully logged in!");		
			break;
		case MessageCode.SERVER_REJECT_AUTH:
			JOptionPane.showMessageDialog(null, "Username and/or password are incorrect!", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
			login();
			//TODO: Handle the case where the algorithm check fails (it shouldn't!)
			break;
		case MessageCode.SERVER_BLOCK_AUTH:
			JOptionPane.showMessageDialog(null, "Too many failed attempts to login! Please try again later.", "Account Locked", JOptionPane.ERROR_MESSAGE);
			break;
		case MessageCode.INDEX_REQUEST:
			if (fileSender == null || fileSender.isStopped()) {
				fileSender = new FileSender(this);
			}
			Message m = new Message(MessageCode.FILE_REQUEST, "index.dex"); //TODO: change hardcoded index file location
			fileSender.enqueueMessage(m);
			t = new Thread(fileSender);
			t.start();
			break;
		case MessageCode.FILE_REQUEST:
			if (fileSender == null || fileSender.isStopped()) {
				fileSender = new FileSender(this);
			}
			fileSender.enqueueMessage(message);
			t = new Thread(fileSender);
			t.start();
			break;
		case MessageCode.SENDING_INDEX://TODO: USES A NEW SOCKET STREAM, BUT PERHAPS SHOULD USE THE CLIENT'S FILESOCKET?
			if (fileReceiver == null || fileReceiver.isStopped()) {
				fileReceiver = new FileReceiver(this);
			}
			m = new Message(MessageCode.SENDING_FILE, "index.dex"); //TODO: change hardcoded index file
			fileReceiver.enqueueMessage(m);
			t = new Thread(fileReceiver);
			t.start();
			break;
		case MessageCode.SENDING_FILE://TODO: USES A NEW SOCKET STREAM, BUT PERHAPS SHOULD USE THE CLIENT'S FILESOCKET?
			if (fileReceiver == null || fileReceiver.isStopped()) {
				fileReceiver = new FileReceiver(this);
			}
			fileReceiver.enqueueMessage(message);
			t = new Thread(fileReceiver);
			t.start();
			break;
		case MessageCode.SERVER_INDEX_REQUEST_ACK:
			if (fileSocket == null || fileSocket.isClosed()) {
				try {
					fileSocket = new Socket(hostname, Integer.parseInt(message.getPayload()));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileSocket != null) {
				if (fileSender == null || fileSender.isStopped()) {
					try {
						fileSender = new FileSender(this, fileSocket.getOutputStream());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				m = new Message(MessageCode.FILE_REQUEST, "index.dex"); //TODO: change hardcoded index file location
				fileSender.enqueueMessage(m);
				t = new Thread(fileSender);
				t.start();
			}
			break;
		case MessageCode.SERVER_FILE_REQUEST_ACK:
			if (fileSocket == null || fileSocket.isClosed()) {
				try {
					fileSocket = new Socket(hostname, Integer.parseInt(message.getPayload()));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileSocket != null) {
				if (fileSender == null || fileSender.isStopped()) {
					try {
						fileSender = new FileSender(this, fileSocket.getOutputStream());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				fileSender.enqueueMessage(message);
				t = new Thread(fileSender);
				t.start();
			}
			break;
		default:
			System.err.println("INVALID MESSAGE CODE DETECTED: " + message.getCode());
		}
	}
	
	public void login() {
		String username = JOptionPane.showInputDialog("Username:");
		MessageDigest md = null;
		try {
			 md = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (md != null) {
			JLabel label = new JLabel("Please enter your password:");
			JPasswordField jpf = new JPasswordField();
			JOptionPane.showConfirmDialog(null,
			  new Object[]{label, jpf}, "Password:",
			  JOptionPane.OK_CANCEL_OPTION);
			byte[] passwordHash = md.digest(String.valueOf(jpf.getPassword()).getBytes());
			System.out.println(BaseConversion.toHexString(passwordHash));
			Message m = new Message(MessageCode.CLIENT_SEND_AUTH, username + " " + BaseConversion.toHexString(passwordHash));
			sender.enqueueMessage(m);
		}
	}
	
	private boolean connect() {
		try {
			socket = new Socket(hostname, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (socket != null) {
			sender = new MessageSender(this);
			Thread t = new Thread(sender);
			t.start();
			
			receiver = new MessageReceiver(this);
			t = new Thread(receiver);
			t.start();
			
			connected = true;
			return true;
		}
		return false;
	}

	public static void main(String... args) { //the client should be runnable, no?
		Client c = new Client(); //start the client	
		Thread t = new Thread(c);
		t.start();
		c.connect();
	}
	
	public void stop() {
		running = false;
	}
	
	public void receiverDisconnected() {
		try {
			Thread.sleep(1000 * 5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		connect();
	}

	@Override
	public void run() {
		running = true;
		
		//start indexer TODO: set indexer to be run periodically
		Thread t = new Thread(indexer);
		if (!t.isAlive()) {
			t = new Thread(indexer);//initialized twice at first, I know.
			t.start();
			t.setPriority(Thread.MIN_PRIORITY);
		}
		
		//start file monitor TODO: find out how JNotify breaks when it does
		Thread tt = new Thread(fileMonitor);
		if (!tt.isAlive()) {
			tt = new Thread(fileMonitor);
			tt.start();
			tt.setPriority(Thread.MAX_PRIORITY);
		}
		
		while (running) {
			/*if (!t.isAlive()) {
				t = new Thread(indexer);//initialized twice at first, I know.
				t.start();
				t.setPriority(Thread.MIN_PRIORITY);
			}*/
			if (receiver != null) {
				Message m = receiver.dequeueMessage();
				if (m != null) {
					interpretCode(m);
				}
			}
			
			try {
				Thread.sleep(1000 * 5);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			} //wait for 5 seconds
		}
	}

	@Override
	public InputStream getInputStream() {
		try {
			return socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public OutputStream getOutputStream() {
		try {
			return socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public MessageSender getMessageSender() {
		return sender;
	}
	
	@Override
	public InputStream getFileInputStream() {
		try {
			return fileSocket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public OutputStream getFileOutputStream() {
		try {
			return fileSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public Database getDatabase() {
		return database;
	}
}
