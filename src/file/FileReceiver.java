package file;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import message.Message;
import message.Messageable;

public class FileReceiver implements Runnable {

	private boolean running;
	private boolean connected;
	private Socket socket;
	private int filePort = 4442; //must be changed to a property later
	private final static int BUFFER = 2048;
	
	private Messageable master;
	
	private List<Message> fileMessageQueue;
	private DataInputStream in;
	
	private final static int TIMEOUT_TIMER_MS = 3000;
	
	public FileReceiver(Messageable c) {
		master = c;
		fileMessageQueue = new ArrayList<Message>();
		running = false;
		connected = false;
		
	}
	
	private boolean connect() {
		try {
			socket = new Socket(master.getHost(), filePort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (socket != null) {			
			connected = true;
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
	}
	
	public boolean isStopped() {
		return !running;
	}
	
	@Override
	public void run() {
		running = true;
		int connectTries = 0;
		while (!connect() && connectTries < 3) {
			connectTries++;
		}
		try {
			in = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (connected && !fileMessageQueue.isEmpty() && in != null) {
			Message m = dequeueMessage();
			byte[] mybytearray = new byte[BUFFER];
			try {
				File f = new File(m.getPayload());
				String localName = f.getName();
				FileOutputStream fos = new FileOutputStream(localName,
						false);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				long fileLength = in.readLong();
				int bytesRead = 0;
				while (fileLength > 0) { // Index file being sent
					if (fileLength > BUFFER) {
						bytesRead = in.read(mybytearray);
					} 
					else {
						bytesRead = in.read(mybytearray, 0, (int) fileLength);
					}
					fileLength -= bytesRead;
					bos.write(mybytearray, 0, bytesRead);
					bos.flush();
				}
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		stop();
	}
}
