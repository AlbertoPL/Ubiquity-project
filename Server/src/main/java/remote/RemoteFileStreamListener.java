package remote;

import java.rmi.Remote;

public class RemoteFileStreamListener implements Remote {

	protected long id = -1;
	protected byte[] data; 
	
	private final static int BUFFER = 2048;
	
	public RemoteFileStreamListener() {
		data = new byte[BUFFER];
	}
	
	public final void setId(int id) {
		if (id < 0) {
			this.id = id;
		}
	}
	
	public byte[] getByteArray() {
		return data;
	}
}
