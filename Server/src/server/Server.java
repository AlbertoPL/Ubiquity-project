package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import message.Message;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import remote.RmiServer;


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
	
	@SuppressWarnings("unused")
	private RmiServer rmiserver;
	
	private int port;
	private boolean running;
	private List<ClientHandler> clientHandlers;
	private ServerSocket serverSocket;
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
		
		try {
			rmiserver = new RmiServer(this);
		} catch (RemoteException e) {
			e.printStackTrace();
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
	
	public boolean sendMessageToClient(String username, String devicename, Message m) {
		for (ClientHandler c: clientHandlers) {
			if (c.getUsername().equals(username) && c.getDeviceName().equals(devicename))  {
				c.getMessageSender().enqueueMessage(m);
				return true;
			}
		}
		return false;
	}
	
	public static void main(String... args) {
		Server s = new Server();
		Thread t = new Thread(s);
		t.start();
	}

}
