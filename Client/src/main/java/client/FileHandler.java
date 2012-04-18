package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import message.FileMessage;
import file.FileReceiver;
import file.FileSender;

/**
 * 
 * @author Alberto Pareja-Lecaros
 *
 * The FileHandler handles the sending and receiving of messages, queuing the 
 * files that need to be sent out.
 *
 */
public class FileHandler implements Runnable {

	private FileSender fileSender;
	private FileReceiver fileReceiver;
	
	private String hostname;
	private int port;
	private boolean sending;
	private boolean receiving;
	
	
	public FileHandler(Client client) {
		fileSender = new FileSender(client.rootFolder());		
	    fileReceiver = new FileReceiver(client.rootFolder());
	    hostname = client.host();
	    sending = false;
	    receiving = false;
	}
	
	public void setSending() {
		sending = true;
	}
	
	public void setReceiving() {
		receiving = true;
	}
	
	//set port always before starting the thread
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setFileToSendMetadata(FileMessage m) {
		fileSender.enqueueMessage(m);
	}

	@Override
	public void run() {
		Socket clientSocket = null;
		try {
		    clientSocket = new Socket(hostname, port);
		    
		    Thread t = null;
		    if (sending) {
		    	fileSender.setDataOutputStream(new DataOutputStream(clientSocket.getOutputStream()));
		    	t = new Thread(fileSender);
		    	t.start();
		    }
			    
		    if (receiving) {
		    	fileReceiver.setDataInputStream(new DataInputStream(clientSocket.getInputStream()));
		    	t = new Thread(fileReceiver);
		    	t.start();
		    }
		} catch (IOException e) {
		    System.out.println("Accept failed: " + port);
		}
		do {
			try {
				Thread.sleep(5000); //keep waiting while either is still going
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		while (!fileSender.isStopped() || !fileReceiver.isStopped());
			
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		sending = false;
		receiving = false;
	}
	
}
