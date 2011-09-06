package message;

public class MessageCode {

	public static final int SERVER_REQUEST_AUTH = 0; //being asked for authentication
	public static final int SERVER_ACCEPT_AUTH = 1; //authentication was accepted
	public static final int SERVER_REJECT_AUTH = 2; //authentication was rejected
	public static final int SERVER_BLOCK_AUTH = 3; //authentication is disabled
	
	public static final int INDEX_REQUEST = 4; //requesting index
	public static final int FILE_REQUEST = 5; //requesting file
	public static final int SENDING_INDEX = 6; //index is about to be sent
	public static final int SENDING_FILE = 7; //file is about to be sent

	public static final int CLIENT_SEND_AUTH = 8;//authentication about to be sent

	public static final int SERVER_INDEX_REQUEST_ACK = 9;
	public static final int SERVER_FILE_REQUEST_ACK = 10;
	
	public static final int NOT_LOGGED_IN = 11; //client is not logged in
	
}
