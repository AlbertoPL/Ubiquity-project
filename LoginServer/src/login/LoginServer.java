package login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class LoginServer implements Runnable {
	private ServerSocket welcomeSocket;
	private DatabaseAdapter database;
	private boolean running;
	
	public LoginServer() {
		try {
			welcomeSocket = new ServerSocket(14445);
			database = new PostgresDatabaseAdapter();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String... args) {
		LoginServer loginServer = new LoginServer();
		Thread t = new Thread(loginServer);
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
						String device = inFromClient.readLine();
						String os = inFromClient.readLine();
						PrintWriter outToClient  = new PrintWriter(connectionSocket.getOutputStream(),true);
						if (database.login(username, passwordHash)) {
							boolean success = database.lookupDevice(device, username);
							if (success) {
								System.out.println("User: " + username + " is now accessing device " + device);
							}
							else {
								System.out.println("User: " + username + " is now adding a new device " + device);
								database.addDevice(device, os, username);
								try {
									Runtime.getRuntime().exec("php /opt/dynamic-dns/insert.php " + username + "." + device + ".testubiquity.info");
								} catch (IOException e) {
									e.printStackTrace();
								} 
							}
							outToClient.println("true");
						}
						else {
							System.out.println("Login for " + username + " failed!");
							outToClient.println("false");
						}
						outToClient.close();
						inFromClient.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
	}
}
