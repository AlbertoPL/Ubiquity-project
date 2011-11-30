package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import message.Message;

public interface ReceiveMessageInterface extends Remote {
	public boolean sendMessageToClient(String username, String devicename, Message m) throws RemoteException;
}