package message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MessageSender implements Runnable {

	private boolean running;
	
	private Messageable master;
	
	private List<Message> messageQueue;
	private ObjectOutputStream out;
	
	private final static int TIMEOUT_TIMER_MS = 3000;
	
	public MessageSender(Messageable c) {
		master = c;
		messageQueue = new ArrayList<Message>();
		running = false;
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
				if (m != null && master.isLoggedIn()) {
					try {
						System.out.println("Sending message: " + m.getCode());
						out.writeObject(m);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				//ok to send if we're asking for authentication or sending name and os, should move list elsewhere.
				else if (m != null && (m.getCode() == MessageCode.SERVER_REQUEST_AUTH || m.getCode() == MessageCode.CLIENT_SEND_AUTH || m.getCode() == MessageCode.SERVER_ACCEPT_AUTH || m.getCode() == MessageCode.SERVER_REJECT_AUTH || m.getCode() == MessageCode.SERVER_BLOCK_AUTH || m.getCode() == MessageCode.REQUEST_NAME_AND_OS || m.getCode() == MessageCode.NAME_AND_OS || m.getCode() == MessageCode.DEVICE_NOT_SUPPORTED || m.getCode() == MessageCode.NOT_LOGGED_IN)) {
					try {
						System.out.println("Sending message: " + m.getCode());
						out.writeObject(m);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else if (m != null && !master.isLoggedIn()) {
					System.err.println("Will resend message later");
					enqueueMessage(m);
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
