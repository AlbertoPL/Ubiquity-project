package message;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MessageSender implements Runnable {

	private boolean running;
	private boolean connected;
	
	private Messageable master;
	
	private List<Message> messageQueue;
	private ObjectOutputStream out;
	
	private final static int TIMEOUT_TIMER_MS = 3000;
	
	public MessageSender(Messageable c) {
		master = c;
		messageQueue = new ArrayList<Message>();
		running = false;
		connected = false;
		if (out == null) {
			try {
				out = new ObjectOutputStream(master.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
	
	public MessageSender(Messageable c, OutputStream o) { //called by servers
		try {
			out = new ObjectOutputStream(o);
		} catch (IOException e) {
			e.printStackTrace();
		}
		master = c;
		messageQueue = new ArrayList<Message>();
		running = false;
		connected = false;
	}
	
	public void enqueueMessage(Message m) {
		messageQueue.add(m);
	}
	
	private Message dequeueMessage() {
		if (messageQueue.isEmpty()) {
			return null;
		}
		return messageQueue.remove(0);
	}
	
	public void stop() {
		running = false;
	}
	
	@Override
	public void run() {
		running = true;
		while (running) {
			while (master.isConnected() && out != null) {
				Message m = dequeueMessage();
				if (m != null) {
					try {
						System.out.println("Sending message: " + m.getCode());
						out.writeObject(m);
					} catch (IOException e) {
						e.printStackTrace();
						connected = false;
					}
				}
				else {
					//sleep a bit
					try {
						Thread.sleep(TIMEOUT_TIMER_MS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(1000 * 3);
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			} //sleep 3 seconds
		}
	}
}
