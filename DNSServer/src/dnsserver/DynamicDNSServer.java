package dnsserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class DynamicDNSServer implements Runnable {

	private ServerSocket welcomeSocket;
	private DatabaseAdapter database;
	
	private boolean running;
	
	public DynamicDNSServer() {
		try {
			welcomeSocket = new ServerSocket(14443);
			database = new PostgresDatabaseAdapter();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String... args) {
		DynamicDNSServer dnsServer = new DynamicDNSServer();
		Thread t = new Thread(dnsServer);
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
						String clientIP = inFromClient.readLine();
						String device = inFromClient.readLine();
						device = device.trim().replace(" ", "-");
						if (database.login(username, passwordHash)) {
							System.out.println("Received IP: " + clientIP + " from " + device);
							try {
								Runtime.getRuntime().exec("php /opt/dynamic-dns/update.php " + clientIP + " " + username + "." + device + ".testubiquity.info");
							} catch (IOException e) {
								e.printStackTrace();
							} 
						}
						else {
							System.out.println("Login for " + username + "@" + clientIP + " failed!");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
	}
}
