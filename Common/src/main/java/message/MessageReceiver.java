package message;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class MessageReceiver implements Runnable {

	private boolean running;
	private boolean connected;
	
	private Messageable master;
	
	private List<Message> messageQueue;
	private ObjectInputStream in;
	
	private final static int TIMEOUT_TIMER_MS = 3000;
	
	public MessageReceiver(Messageable c) {
		master = c;
		messageQueue = new ArrayList<Message>();
		running = false;
		connected = false;
		if (in == null) {
			try {
				in = new ObjectInputStream(master.inputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public MessageReceiver(Messageable c, InputStream i) {
		try {
			in = new ObjectInputStream(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
		master = c;
		messageQueue = new ArrayList<Message>();
		running = false;
		connected = false;
	}
	
	private void enqueueMessage(Message m) {
		messageQueue.add(m);
	}
	
	public Message dequeueMessage() {
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
		connected = master.connected();
		while (running) {
			while (master.connected() && connected && in != null) {
				try {
					Message m = (Message) in.readObject();
					if (m.getCode() == -1) {
						connected = false;
					}
					else {
						System.out.println("Receiving message: " + m.getCode());
						enqueueMessage(m);
					}
				}
				catch (IOException e) {
					e.printStackTrace();
					connected = false; //reconnect
					//tell the master runnable that it's over
					master.receiverDisconnected();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(TIMEOUT_TIMER_MS);
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			} //sleep 3 seconds
		}
		//tell the client handler that the message receiver has failed.
	}
}
