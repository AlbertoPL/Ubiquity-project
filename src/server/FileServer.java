package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import message.Messageable;

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

	public static final int PORT = 4442;
	
	private boolean running;
	private ServerSocket fileSocket;
	private Server server;

	public FileServer(Server server) {
		running = false;
		this.server = server;
		try {
		    fileSocket = new ServerSocket(PORT);
		    
		} catch (IOException e) {
		    System.out.println("Could not listen on port: " + PORT);
		}
	}
	
	public void stop() {
		running = false;
	}

	@Override
	public void run() {
		running = true;
		Socket clientSocket = null;
		while (running) {
			try {
			    clientSocket = fileSocket.accept(); 
			    FileSender cfs = new FileSender(null, clientSocket.getOutputStream());
			    Thread t = new Thread(cfs);
			    t.start();
			} catch (IOException e) {
			    System.out.println("Accept failed: " + PORT);
			}
		}
		try {
			fileSocket.close();
		} catch (IOException e) {
		}
	}
	
}