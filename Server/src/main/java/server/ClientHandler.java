package server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import message.FileMessage;
import message.Message;
import message.MessageCode;
import message.RequestMessage;
import message.ShareFileMessage;
import message.StringMapMessage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import file.UbiquityFileData;

public class ClientHandler implements Runnable {

	private Socket client;
	private DatabaseAdapter database;
	private ConnectionFactory factory;
	
	public ClientHandler(Socket s) {
		client = s;
		database = new PostgresDatabaseAdapter();
		factory = new ConnectionFactory();
	}
	
	@Override
	public void run() {
		BufferedReader input;
		PrintWriter output;
		try {
			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			output = new PrintWriter(client.getOutputStream(), true);
			String username = input.readLine();
			String passwordHash = input.readLine();
			Thread.sleep(2000);
			int userId = database.login(username, passwordHash);
			output.println(userId);		
			if (userId > 0) {
				System.out.println("User " + username + " logged in!");
				//get the device name
				String devicename = input.readLine();
				String macAddr = input.readLine();
				//TODO: check if the device is registered
				//for now, we'll just always insert the device
				if (macAddr.isEmpty()) {
					macAddr = "00:00:00:00:00:00";
				}
				database.insertDevice(devicename, macAddr, userId);
				
				handleMessageQueue(username, passwordHash, devicename, userId);
			}
			else {
				System.err.println("User " + username + " failed to authenticate!");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Error reading input from client for logging in");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	private void handleMessageQueue(final String username, final String passwordHash, final String devicename, final int userId) {
		//subscribe to this client's message queue
		System.out.println("Subscribing to queues");
		factory.setUsername(username);
		factory.setPassword(passwordHash);
		factory.setHost("localhost"); //this needs to change to one found in a property file
		Connection connection;
		try {
			connection = factory.newConnection();
			final Channel channel = connection.createChannel();
			channel.exchangeDeclare(username+passwordHash, "topic");
			channel.queueDeclare(username+passwordHash+devicename+"MQserver", true, false, false, null);//durable, non-exclusive, non-autodelete
			channel.queueDeclare(username+passwordHash+devicename+"FQserver", true, false, false, null);//durable, non-exclusive, non-autodelete
			
			channel.queueBind(username+passwordHash+devicename+"MQserver", username+passwordHash, username+passwordHash+devicename+"MQserver");
			channel.queueBind(username+passwordHash+devicename+"FQserver", username+passwordHash, username+passwordHash+devicename+"FQserver");
			
		    System.out.println(" [*] Waiting for messages.");
		    final QueueingConsumer consumer = new QueueingConsumer(channel);
		    final QueueingConsumer fileConsumer = new QueueingConsumer(channel);

		    channel.basicConsume(username+passwordHash+devicename+"MQserver", true, consumer);
			channel.basicConsume(username+passwordHash+devicename+"FQserver", true, fileConsumer);

		    //sign up to message queue
		    new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						QueueingConsumer.Delivery delivery;
						try {
							delivery = consumer.nextDelivery();
						    byte[] message = delivery.getBody();
						    ByteArrayInputStream bis = new ByteArrayInputStream(message);
						    ObjectInput in = new ObjectInputStream(bis);
						    Message m = (Message) in.readObject();
						    System.out.println("MQ received a message");
						    in.close();
						    bis.close();
						    switch (m.getCode()) {
						    case MessageCode.REQUEST_FILE:
						    	//TODO: have file id as well in case we want to filter devices by file id
						    	//TODO: if the file is backed up, retrieve from our servers
						    	String filename = ((FileMessage)m).getFilename();
						    	String filepath = ((FileMessage)m).getFilepath();
						    	String[] devices = database.getDevices(userId);
						    	//TODO: iterate through devices, signing up as a publisher of the correct one/all of them
						    	for (String device: devices) {
						    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
									ObjectOutput out = new ObjectOutputStream(bos);   
									Message newMessage = new FileMessage(MessageCode.REQUEST_FILE, filename, filepath, 0, devicename.getBytes());

								    out.writeObject(newMessage);
								    out.flush();
								    byte[] yourBytes = bos.toByteArray();
								    out.close();
								    bos.close();
									channel.basicPublish(username+passwordHash, username+passwordHash+device+"MQclient", null, yourBytes);
						    	}
						    	break;
						    case MessageCode.REQUEST_FILES_OF_TYPE:
						    	int filetypeid = ((RequestMessage) m).getMetacode();
						    	int userid = ((RequestMessage) m).getUserid();
						    	UbiquityFileData[] files = database.getAllFilesOfType(userid, filetypeid);
						    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
								ObjectOutput out = new ObjectOutputStream(bos);   
								Message newMessage = new StringMapMessage(MessageCode.REQUEST_FILES_OF_TYPE, filetypeid, files);
								
							    out.writeObject(newMessage);
							    out.flush();
							    byte[] yourBytes = bos.toByteArray();
							    out.close();
							    bos.close();
								System.out.println("METACODE TO SEND TO CLIENT: " + ((StringMapMessage)newMessage).getMetacode());
							    channel.basicPublish(username+passwordHash, username+passwordHash+devicename+"MQclient", null, yourBytes);
								break;
						    case MessageCode.BACKUP_FILE:
						    	filename = ((FileMessage)m).getFilename();
						    	filepath = ((FileMessage)m).getFilepath();
						    	long filelength = ((FileMessage)m).getFilelength();
						    	database.storeFileInDatabase(userId, filename, filepath, filelength, devicename);
						    	break;
						    case MessageCode.SHARE_FILE:
						    	filename = ((ShareFileMessage)m).getFilename();
						    	filepath = ((ShareFileMessage)m).getFilepath();
						    	filelength = ((ShareFileMessage)m).getFilelength();
						    	String userToShareWith = ((ShareFileMessage)m).getUserToShareWith();
						    	database.shareFile(userId, userToShareWith, filepath, filelength, devicename);
						    	break;
						    default:
						    	System.err.println("Invalid message code received! " + m.getCode());	
						    }
						} catch (ShutdownSignalException e) {
							e.printStackTrace();
						} catch (ConsumerCancelledException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
		    	
		    }).start();
		    
		    //sign up to file queue (if user has paid, will allow for transfers)
		    new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						QueueingConsumer.Delivery delivery;
						try {
							delivery = fileConsumer.nextDelivery();
						    byte[] message = delivery.getBody();
						    ByteArrayInputStream bis = new ByteArrayInputStream(message);
						    ObjectInput in = new ObjectInputStream(bis);
						    Message m = (Message) in.readObject();
						    System.out.println("FQ received a message");
						    in.close();
						    bis.close();
						    switch (m.getCode()) {
						    case MessageCode.BACKUP_FILE:
						    	String filename = ((FileMessage)m).getFilename();
						    	File f = new File(filename);
					    		   ByteBuffer buf = ByteBuffer.wrap(((FileMessage)m).getPayload());
					    		    // Create a writable file channel
					    		    FileChannel wChannel = new FileOutputStream(f, true).getChannel();

					    		    // Write the ByteBuffer contents; the bytes between the ByteBuffer's
					    		    // position and the limit is written to the file
					    		    wChannel.write(buf);
					    		    System.out.println("Receiving file: " + filename);
					    		    // Close the file
					    		    wChannel.close();
					    		    buf.clear();
						    	break;
						    }
						}
						catch (ShutdownSignalException sse) {
							sse.printStackTrace();
						}
						catch (ConsumerCancelledException cce) {
						  	cce.printStackTrace();
						}
						catch (InterruptedException ie) {
						  	ie.printStackTrace();
						}
						catch (IOException ioe) {
						  	ioe.printStackTrace();
						}
						catch (ClassNotFoundException cnfe) {
							cnfe.printStackTrace();
						}
					}
				}
		    }).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	   
	}
	
}
