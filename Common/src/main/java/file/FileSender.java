package file;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import message.Message;

public class FileSender implements Runnable {

	private boolean running;
	private final static int BUFFER = 2048;

	private List<Message> fileMessageQueue;
	private DataOutputStream out;
	
	public FileSender(String rootFolder) {
		fileMessageQueue = new ArrayList<Message>();
		running = false;
	}
	
	public void setDataOutputStream(DataOutputStream out) {
		this.out = out;
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
	}
	
	public boolean isStopped() {
		return !running;
	}
	
	@Override
	public void run() {
		running = true;
		
		if (running && out != null) {
			Message m = dequeueMessage();
			byte[] mybytearray = new byte[BUFFER];
			if (m != null) {
				try {
					File f = new File(m.getPayload());
					
					if (f.exists()) {
						out.writeInt(f.getName().length());
						out.flush();
						out.writeChars(f.getName());
						out.flush();
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
		}
		else {
			System.err.println("Not connected!");
		}
		stop();	
	}
}
