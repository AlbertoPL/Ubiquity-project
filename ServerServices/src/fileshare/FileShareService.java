package fileshare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class FileShareService implements Runnable {

	private ServerSocket welcomeSocket;
	private DatabaseAdapter database;
	private boolean running;
	
	public FileShareService() {
		try {
			welcomeSocket = new ServerSocket(14444);
			database = new PostgresDatabaseAdapter();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String... args) {
		FileShareService fileshareServer = new FileShareService();
		Thread t = new Thread(fileshareServer);
		t.start();
	}

	public void stop() {
		running = false;
		if (welcomeSocket != null && !welcomeSocket.isClosed()) {
			try {
				welcomeSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		running = true;
		while (running) {
			Socket connectionSocket = null;
			try {
				connectionSocket = welcomeSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (connectionSocket != null) {
				BufferedReader inFromClient = null;
				try {
					inFromClient = new BufferedReader(new InputStreamReader(
							connectionSocket.getInputStream()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (inFromClient != null) {
					try {
						String username = inFromClient.readLine();
						String passwordHash = inFromClient.readLine();
						String filename = inFromClient.readLine();
						String filepath = inFromClient.readLine();
						String filetype = inFromClient.readLine();
						String device = inFromClient.readLine();
						String shareWith = inFromClient.readLine();
						if (database.login(username, passwordHash)) {
							boolean success = database.shareFile(filename, filepath, filetype, username, device, shareWith);
							if (success) {
								System.out.println("User: " + username + " is now sharing " + 
									filepath + " -> " + filename + " with " + shareWith);
							}
							else {
								System.out.println("Share file from " + username + " to " + shareWith + " failed!");
							}
						}
						else {
							System.out.println("Login for " + username + " failed!");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
	}
	
}
