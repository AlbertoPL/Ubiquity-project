package file;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import message.Message;
import message.Messageable;

public class FileSender implements Runnable {

	private boolean running;
	private boolean connected;
	private ServerSocket serverSocket;
	private Socket socket;
	private int filePort = 4442; //must be changed to a property later
	private final static int BUFFER = 2048;
	private final static int CONNECT_TRIES = 3;
	
	private Messageable master;
	
	private List<Message> fileMessageQueue;
	private DataOutputStream out;
		
	//only to be used if not a server
	public FileSender(Messageable c) {
		master = c;
		fileMessageQueue = new ArrayList<Message>();
		running = true;
		connected = false;
		try {
		    serverSocket = new ServerSocket(filePort);
		    
		} catch (IOException e) {
		    System.out.println("Could not listen on port: " + filePort);
		}
	}
	
	public FileSender(Messageable c, OutputStream o) {
		master = c;
		fileMessageQueue = new ArrayList<Message>();
		running = true;
		connected = false;
		out = new DataOutputStream(o);
	}
	
	private boolean connect() {
		try {
			socket = serverSocket.accept();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (socket != null) {			
			connected = true;
			try {
				out = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
	
	public void enqueueMessage(Message m) {
		fileMessageQueue.add(m);
	}
	
	private Message dequeueMessage() {
		if (fileMessageQueue.isEmpty()) {
			running = false;
			return null;
		}
		return fileMessageQueue.remove(0);
	}
	
	public void stop() {
		running = false;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isStopped() {
		return !running;
	}
	
	@Override
	public void run() {
		int connectTries = 0;
		if (out == null) {
			while (!connect() && connectTries < CONNECT_TRIES) {
				connectTries++;
			}
		}
		if (connectTries < CONNECT_TRIES) {
			connected = true;
		}
		while (connected && !fileMessageQueue.isEmpty() && out != null) {
			Message m = dequeueMessage();
			byte[] mybytearray = new byte[BUFFER];
			try {
				File f = new File(m.getPayload());
				if (f.exists()) {
					FileInputStream fin = new FileInputStream(f);
				    BufferedInputStream bin = new BufferedInputStream(fin);
					long fileLength = f.length();
					out.writeLong(fileLength);
					out.flush();
					int bytesRead;
				    while (fileLength > 0) {
				    	if (fileLength > BUFFER) {
					    	bytesRead = bin.read(mybytearray);
					    }
					    else {
					    	bytesRead = bin.read(mybytearray, 0, (int)fileLength);
					    }
					    fileLength -= bytesRead;
					    out.write(mybytearray, 0, bytesRead);
					    out.flush();
				    }
				    bin.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		stop();
	}
}
