package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import file.FileReceiver;
import file.FileSender;

/**
 * 
 * @author Alberto Pareja-Lecaros
 *
 * The FileServer sends files to their appropriate destinations and, when 
 * necessary, polls clients for files that have been requested. One file server
 * is created per client.
 *
 */
public class FileServer implements Runnable {

	private ServerSocket fileSocket;
	private FileSender fileSender;
	private FileReceiver fileReceiver;

	public FileServer(ClientHandler handler) {
		fileSender = new FileSender(handler.getRootFolder());		
	    fileReceiver = new FileReceiver(handler.getRootFolder());
		
	    boolean success = false;
	    while (!success) {
		    try {
			    fileSocket = new ServerSocket(0);
			    success = true;
			} catch (IOException e) {
			    System.err.println("Could not listen on the port chosen: " + fileSocket.getLocalPort());
			}
	    }
	}
	
	public int getPort() {
		return fileSocket.getLocalPort();
	}

	@Override
	public void run() {
		Socket clientSocket = null;
		try {
		    clientSocket = fileSocket.accept(); 
		    
		    fileSender.setDataOutputStream(new DataOutputStream(clientSocket.getOutputStream()));
		    Thread t = new Thread(fileSender);
		    t.start();
			    
		    fileReceiver.setDataInputStream(new DataInputStream(clientSocket.getInputStream()));
		    t = new Thread(fileReceiver);
		    t.start();
		} catch (IOException e) {
		    System.out.println("Accept failed: " + fileSocket.getLocalPort());
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
			System.err.println("Closing socket...");
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}