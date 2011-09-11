package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


/**
 * 
 * @author Alberto Pareja-Lecaros
 *
 * The Server implements Messageable so that the Server defines methods 
 * necessary for handling messages passed to it from the MessageHandler. 
 * The Server is then responsible for starting up all server functions, 
 * including the BackupAdapter, the DatabaseAdapter, and the FileServer.
 * 
 */
public class Server implements Runnable {

	private final static String SERVER_PROPERTIES = "server.properties";
	
	private int port;
	private boolean running;
	private List<ClientHandler> clientHandlers;
	private ServerSocket serverSocket;
	private FileServer fileServer;
	public static List<?> validOsTypes;
	
	public Server() {
		PropertiesConfiguration properties = new PropertiesConfiguration();
		try {
		    properties.load(new FileInputStream (SERVER_PROPERTIES));
			port = properties.getInt("port");
			validOsTypes = properties.getList("validOs");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		running = true;
		clientHandlers = new ArrayList<ClientHandler>();
		
		try {
		    serverSocket = new ServerSocket(port);
		    
		} catch (IOException e) {
		    System.out.println("Could not listen on port: " + port);
		}
	}

	@Override
	public void run() {
		
		Socket clientSocket = null;
		while (running) {
			try {
			    clientSocket = serverSocket.accept(); 
			    
			    ClientHandler client = new ClientHandler(clientSocket);
			    Thread t = new Thread(client);
			    t.start();
			    
			    clientHandlers.add(client);
			    
			} catch (IOException e) {
			    System.out.println("Accept failed: " + port);
			}
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
		}
	
		for (ClientHandler c: clientHandlers) {
			c.stop();
		}
	}
	
	public static void main(String... args) {
		Server s = new Server();
		Thread t = new Thread(s);
		t.start();
	}

}
