package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import message.FileMessage;

public interface ReceiveMessageInterface extends Remote {
	
	public boolean sendMessageToClient(String username, String devicename, FileMessage m) throws RemoteException;
}