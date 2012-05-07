import file.UbiquityFileData;
import interfaces.Serviceable;
import interfaces.View;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import client.Client;

/**
 * Eventually this will be replaced with a fully Scala solution
 * TODO: Must add logon functionality
 * 
 * @author Alberto
 *
 */
public class ClientViewController implements Serviceable {

	private Client client;
	private View scholar;
	
	//store these in memory in case we need automatic reconnects and whatnot
	private String username;
	private String passwordHash;
	
	public ClientViewController() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("client.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		client = new Client(this);
		client.startWithDefaults(properties);
		scholar = new ScholarFrame(this);
		
		username = "";
		passwordHash = "";
	}

	@Override
	public void backupFile(String filename, String filepath, long filesize) {
		System.out.println("Backing up: " + filename);
		System.out.println("Backing up: " +filepath);
		client.backupFile(filename, filepath, filesize);
	}
	
	@Override
	public void getFilesByType(int filetypeid) {
		client.getFilesOfType(filetypeid);
	}
	
	/*@Override
	public void getFilesByTypeSuccess(int filetypeid, String[] files) {
		scholar.getFilesByTypeSuccess(filetypeid, files);
	}*/
	
	@Override
	public void getFilesByTypeSuccess(int filetypeid, UbiquityFileData[] files) {
		scholar.getFilesByTypeSuccess(filetypeid, files);
	}
	
	@Override
	public void shareFile(String filename, String filepath, long filelength, String usernameToShareWith) {
		client.shareFile(filename, filepath, filelength, usernameToShareWith);
	}
	
	@Override
	public void backupSuccess(boolean success, String filepath) {
		if (success) {
			scholar.fileBackupSuccess(true, filepath);
		}
		else {
			scholar.fileBackupSuccess(false, filepath);
		}
	}

	@Override
	public void shareSuccess(boolean success, String filepath, List<String> users) {
		if (success) {
			scholar.fileShareSuccess(true, filepath, users);
		}
		else {
			scholar.fileShareSuccess(false, filepath, users);
		}
	}
	
	@Override
	public void login(String username, String passwordHash) {
		boolean loggedIn = client.login(username, passwordHash);
		loginSuccess(loggedIn, username, passwordHash, String.valueOf(loggedIn));
	}

	@Override
	public void loginSuccess(boolean success, String username, String passwordHash, String message) {
		if (success) {
			this.username = username;
			this.passwordHash = passwordHash;
		}
		scholar.loginSuccess(success, username, passwordHash, message);
	}

	@Override
	public boolean connect() {
		client.connect();
		return client.isConnected();
	}

	@Override
	public void requestAuth() {
		scholar.login();
	}
	
	@Override
	public boolean disconnect() {
		client.stop();
		return true;
	}
	
	@Override
	public void reconnect() {
		client.connect();
		if (client.isConnected()) {
			login(username, passwordHash);
		}
	}
	
	@Override
	public void getRemoteFile(String filepath) {
		client.getRemoteFile(filepath);
	}
	
	@Override
	public String getRemoteFileStore() {
		return client.getPathToRemoteFileStore();
	}
	
	/**
	 * arguments include the actual program to be run
	 * 
	 * @param args
	 */
	public static void main(String... args) {
		new ClientViewController();
	}
}
