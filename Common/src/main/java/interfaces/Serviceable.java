package interfaces;

import java.util.List;

import file.UbiquityFileData;

public interface Serviceable {

	//view calls backupFile so that the server proxy sends a message to backup a particular file
	public void backupFile(String filename, String filepath, long filesize);
	
	//view calls getFilesOfType so that the server proxy sends a message to get all files of a
	//particular type
	public void getFilesByType(int filetypeid);
	
	//getFilesByType success is called by the client when files have been retrieved
	public void getFilesByTypeSuccess(int filetypeid, UbiquityFileData[] files);
	
	//server proxy calls backupSuccess to let the view knows that the backup of a particular 
	//file succeeded
	public void backupSuccess(boolean success, String filepath);
	
	//view calls shareFile so that the server proxy sends a message to share a particular file
	public void shareFile(String filename, String filepath, long filelength, String usernameToShareWith);
	
	//server proxy calls shareSuccess to let the view know that the sharing of a particular
	//file succeeded
	public void shareSuccess(boolean success, String filepath, List<String> users);

	public void requestAuth();
	
	//view calls login for passing credentials
	public void login(String username, String passwordHash);
	
	//server proxy calls loginSuccess to let the view know the status of the login
	//the message contains error codes and other metadata
	public void loginSuccess(boolean success, String username, String passwordHash, String message);

	//view calls connect when trying to manually connect to the service
	public boolean connect();
	
	//view calls disconnect when manually disconnecting from the service
	public boolean disconnect();
	
	//serviceable calls reconnect upon an accidental disconnect
	public void reconnect();
	
	//view calls getRemoteFile to get a file from another device sent over
	public void getRemoteFile(String filepath, int ownerid);
	
	//view calls this to get the location of stored remote files
	public String getRemoteFileStore();
}
