package message;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Alberto Pareja-Lecaros
 *
 * Defines methods that clients and servers must implement in order to 
 * interpret messages received through a MessageHandler.
 *  
 */
public interface Messageable {

	/**
	 * 
	 * Determines a course of action based on the code passed and the state of
	 * the Messageable.
	 * 
	 * @param code - the integer code received from a network source
	 * @return None
	 */
	public void interpretCode(Message message);
	
	public int getPort();
	
	public String getHost();
	
	public InputStream getInputStream();
	
	public OutputStream getOutputStream();
	
	public InputStream getFileInputStream();
	
	public OutputStream getFileOutputStream();
	
	public boolean isConnected();
	
	public boolean isLoggedIn();
	
	public void receiverDisconnected();
	
	public String getDeviceName();
	
	public String getOsName();
	
	public String getRootFolder();
	
	public void fileReceivedCallback(String file, Message m);
}
