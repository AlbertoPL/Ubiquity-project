package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import message.Message;
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
	
	public FileHandler(Client client) {
		fileSender = new FileSender(client.getRootFolder());		
	    fileReceiver = new FileReceiver(client, client.getRootFolder());
	    hostname = client.getHost();
	}
	
	//set port always before starting the thread
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setFileToSendMetadata(Message m) {
		fileSender.enqueueMessage(m);
	}

	@Override
	public void run() {
		Socket clientSocket = null;
		try {
		    clientSocket = new Socket(hostname, port);
		    
		    fileSender.setDataOutputStream(new DataOutputStream(clientSocket.getOutputStream()));
		    Thread t = new Thread(fileSender);
		    t.start();
			    
		    fileReceiver.setDataInputStream(new DataInputStream(clientSocket.getInputStream()));
		    t = new Thread(fileReceiver);
		    t.start();
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
	}
	
}
