package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ListenerInterface extends Remote {

	public void registerListener(RemoteFileStreamListener r) throws RemoteException;

}
