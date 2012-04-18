package message;

public class MessageCode {
	
	//SERVER SEND CODES
	public static final int REQUEST_AUTH = 0; //ask client for authentication
	public static final int ACCEPT_AUTH = 1; //accept authentication
	public static final int REJECT_AUTH = 2; //reject authentication
	public static final int BLOCK_AUTH = 3; //block authentication
	
	public static final int REQUEST_DIRECTORY = 4; //request a directory listing
	public static final int REQUEST_FILE = 5; //request a file
	
	public static final int REQUEST_NAME_AND_OS = 6; //request name and os
	
	public static final int DEVICE_NOT_SUPPORTED = 7; //tell client device is not supported
	public static final int NOT_LOGGED_IN = 8; //client is not logged in
	
	
	
	//CLIENT SEND CODES
	public static final int SEND_AUTH = 0; //send server authentication
	public static final int SEND_DIRECTORY = 4; //send directory
	public static final int SEND_FILE = 5; //send file
	public static final int SEND_NAME_AND_OS = 6; //send name and OS
	
	public static final int BACKUP_FILE = 9; //backup a file
	
	public static final int REQUEST_FILES_OF_TYPE = 10; //request files of a certain filetype
	
	public static final int FILE = 11; //a file message, contains file data
	/*public static final int SERVER_REQUEST_AUTH = 0; //being asked for authentication
	public static final int SERVER_ACCEPT_AUTH = 1; //authentication was accepted
	public static final int SERVER_REJECT_AUTH = 2; //authentication was rejected
	public static final int SERVER_BLOCK_AUTH = 3; //authentication is disabled
	*/
	
	/*public static final int INDEX_REQUEST = 4; //requesting index
	public static final int SENDING_INDEX = 4; //index is about to be sent
	
	public static final int FILE_REQUEST = 5; //requesting file
	public static final int SENDING_FILE = 5; //file is about to be sent
	//same number indicates same behavior, should probably just be renamed

	public static final int CLIENT_SEND_AUTH = 8;//authentication about to be sent

	public static final int SERVER_INDEX_REQUEST_ACK = 9;
	public static final int SERVER_FILE_REQUEST_ACK = 10;
	
	public static final int NOT_LOGGED_IN = 11; //client is not logged in
	
	//public static final int REQUEST_NAME_AND_OS = 12; //server needs to know
	public static final int NAME_AND_OS = 13; //client sends name and OS*/
	
	//public static final int DEVICE_NOT_SUPPORTED = 14;
	
	//callback messages from a received file
	/*public static final int INDEX = 15;
	public static final int CACHE = 16;
	public static final int BACKUP = 17;*/
	
	public static String printCode(int code, boolean isClient) {
		switch(code) {
		case REQUEST_AUTH:
			return "Authorization requested";
		case ACCEPT_AUTH:
			return "Authorization accepted";
		case REJECT_AUTH:
			return "Authorization rejected";
		case BLOCK_AUTH:
			return "Authorization blocked";
		case REQUEST_DIRECTORY:
			return "Requesting directory";
		case REQUEST_FILE:
			return "Requesting file";
		case REQUEST_NAME_AND_OS:
			return "Requesting name and OS";
		case DEVICE_NOT_SUPPORTED:
			return "Device not supported";
		case NOT_LOGGED_IN:
			return "Not logged in";
		default:
			return "";
		}
	}
}
