package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class UbiquityServer implements Runnable {

	private ServerSocket serverSocket;
	
	public static void main(String... args) {
		UbiquityServer server = new UbiquityServer();
		new Thread(server).start();
	}
	
	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(4445);
		} catch (IOException e) {
			System.out.println("Could not listen on port: 4445");
			System.exit(-1);
		}
		
		Socket clientSocket = null;
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				ClientHandler c = new ClientHandler(clientSocket);
				Thread t = new Thread(c);
				t.start();
			}
			catch (IOException e) {
				
			}
		}
	}

	
	
	
}
