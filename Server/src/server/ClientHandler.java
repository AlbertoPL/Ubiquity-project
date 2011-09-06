package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import file.FileReceiver;

import message.Message;
import message.MessageCode;
import message.MessageReceiver;
import message.MessageSender;
import message.Messageable;

public class ClientHandler implements Messageable, Runnable {

	private boolean connected;
	private boolean running;
	private boolean loggedIn;
	private MessageSender sender;
	private MessageReceiver receiver;
	private FileReceiver fileReceiver;
	private Socket socket, fileSocket;
	private int loginTries;
	
	private final static int MAX_LOGIN_TRIES = 3;
	
	public ClientHandler(Socket socket) {
		this.socket = socket;
		loginTries = 0;
	}
	
	@Override
	public void interpretCode(Message message) {
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
		int code = message.getCode();
		Thread t; //declared in case its needed
		Message m; //declared in case its needed
		
		if (!loggedIn) {
			if (code == MessageCode.CLIENT_SEND_AUTH) {
				String payload = message.getPayload();
				//hardcoded for now
				if ("Crowtche".equals(payload.substring(0, payload.indexOf(" "))) 
						&& "6147273FFC253ABF34954F15203A1E47D9854BEC".equals(payload.
								substring(payload.indexOf(" ")+ 1))) {
					m = new Message(MessageCode.SERVER_ACCEPT_AUTH, null);
					loggedIn = true;
				}
				else {
					loginTries++;
					if (loginTries >= MAX_LOGIN_TRIES) {
						m = new Message (MessageCode.SERVER_BLOCK_AUTH, null);
					}
					else {
						m = new Message(MessageCode.SERVER_REJECT_AUTH, null);
					}
				}
				sender.enqueueMessage(m);
			}
			else {
				String payload = message.getCode() + " " + message.getPayload();
				m = new Message(MessageCode.NOT_LOGGED_IN, payload);
			}
		}
		else {
			switch(code) {
			case MessageCode.INDEX_REQUEST:
				m = new Message(MessageCode.SERVER_INDEX_REQUEST_ACK, String.valueOf(FileServer.PORT));
				sender.enqueueMessage(m);
				break;
			case MessageCode.FILE_REQUEST:
				m = new Message(MessageCode.SERVER_FILE_REQUEST_ACK, String.valueOf(FileServer.PORT));
				sender.enqueueMessage(m);
				break;
			case MessageCode.SENDING_INDEX:
				if (fileReceiver == null) {
					fileReceiver = new FileReceiver(this);
				}
				m = new Message(MessageCode.SENDING_FILE, "index.dex"); //TODO: change hardcoded index file
				fileReceiver.enqueueMessage(m);
				if (fileReceiver.isStopped()) {
					t = new Thread(fileReceiver);
					t.start();
				}
				break;
			case MessageCode.SENDING_FILE:
				if (fileReceiver == null) {
					fileReceiver = new FileReceiver(this);
				}
				fileReceiver.enqueueMessage(message);
				if (fileReceiver.isStopped()) {
					t = new Thread(fileReceiver);
					t.start();
				}
				break;
			}
		}
	}

	@Override
	public int getPort() {
		return 0;
	}

	@Override
	public String getHost() {
		return null;
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
	
	@Override
	public boolean isConnected() {
		return connected;
	}

	public void stop() {
		running = false;
		connected = false;
		sender.stop();
		receiver.stop();
	}
	
	public void receiverDisconnected() {
		stop();
	}
		
	@Override
	public void run() {
		running = true;
		sender = new MessageSender(this);
	    Thread t = new Thread(sender);
	    t.start();
	    
	    receiver = new MessageReceiver(this);
	    t = new Thread(receiver);
	    t.start();
	    
	    connected = true;
	    //authenticate the client before proceeding
	    System.out.println("Request authentication");
	    Message m = new Message(MessageCode.SERVER_REQUEST_AUTH, null);
	    sender.enqueueMessage(m);
	    
	    while (running) {
	    	m = receiver.dequeueMessage();
	    	if (m != null) {
	    		interpretCode(m);
	    	}
	    	else {
	    		try {
	    			Thread.sleep(1000 * 3);
	    		}
	    		catch(InterruptedException e) {
	    			e.printStackTrace();
	    		}
	    	}
	    }
	}

	@Override
	public boolean isLoggedIn() {
		return loggedIn;
	}

}
