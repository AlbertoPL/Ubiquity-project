package com.ubiquity.webubiquity;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import message.Message;
import message.MessageCode;
import remote.ReceiveMessageInterface;
import scala.collection.immutable.Vector;
import server.ClientHandler;
import util.BaseConversion;

import com.ubiquity.webubiquity.form.NewAccountPojoForm;
import com.ubiquity.webubiquity.window.ListWindow;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

public class WebUbiquityApplication extends Application {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3948762438314239641L;

	private boolean loggedIn;
	private ListWindow mainWindow;
	private LoginForm login;
	private ClientHandler client;
	private NewAccountPojoForm newAccountForm;
	
	private ReceiveMessageInterface rmiServer;
	private String rmiServerAddress;
	private int rmiServerPort;
	
	private Registry registry;
	
	private String rootFolder;
	
	@Override
	public void init() {
		mainWindow = new ListWindow("Web Ubiquity Application");
		setMainWindow(mainWindow);

		//RMI initialization
		try {
			rmiServerAddress=InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} //TODO: hardcoded for now
		rmiServerPort=10500; //TODO: hardcoded for now
	    try{
	        // get the �registry�
	        registry=LocateRegistry.getRegistry(
	        		rmiServerAddress,
	            (new Integer(rmiServerPort)).intValue()
	        );
	        // look up the remote object
	        rmiServer=
	           (ReceiveMessageInterface)(registry.lookup("rmiServer"));
	    }
	    catch(RemoteException e){
	        e.printStackTrace();
	    }
	    catch(NotBoundException e){
	        e.printStackTrace();
	    }
		
		client = new ClientHandler(null); //used for simple interactions with ClientHandler methods only, should be refactored
		rootFolder = "." + System.getProperty("file.separator"); //TODO: Hardcoded for now
		
		loggedIn = false;
		//ask for login first!
		showLoginForm();
	}
	
	public String getRootFolder() {
		return rootFolder;
	}
	
	private void showFileTable() {
		mainWindow.setContent();
	}
	
	public boolean userExists(String username) {
		return client.userExists(username);
	}
	
	private void populateTable() {
		Vector<String[]> files = client.allUserFiles();
		
		int count = 0;
		if (files != null) {
			scala.collection.Iterator<String[]> iter = files.iterator();
			while (iter.hasNext()) {
				mainWindow.getTable().addItem(iter.next(),new Integer(count++));
			}
		}	
	}
	
	public long getFileSize(String filepath, String deviceName) {
		return client.fileSize(filepath, deviceName);
	}
	
	public boolean betaSignup(String username, String email) {
		return client.betaSignup(username, email);
	}
	
	public boolean downloadFile(String username, String device, String filepath) {
		if (rmiServer == null) {
			try{
		        // get the �registry�
		        registry=LocateRegistry.getRegistry(
		            rmiServerAddress,
		            (new Integer(rmiServerPort)).intValue());
		        // look up the remote object
		        rmiServer=
		           (ReceiveMessageInterface)(registry.lookup("rmiServer"));
		    }
		    catch(RemoteException e){
		        e.printStackTrace();
		    }
		    catch(NotBoundException e){
		        e.printStackTrace();
		    }
		}
		boolean success = false;
		try {
			success = rmiServer.sendMessageToClient(username, device, new Message(MessageCode.FILE_REQUEST, filepath));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return success;
	}
	
	@SuppressWarnings("serial")
	private void showLoginForm() {
		VerticalLayout layout = new VerticalLayout();
		login = new LoginForm();
		Button newAccount = new Button("Create new account");
		newAccount.setStyleName(BaseTheme.BUTTON_LINK);
		newAccount.setDescription("Create new account");
		newAccount.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				newAccountForm = new NewAccountPojoForm();
				mainWindow.setContent(newAccountForm);
			}
			
		}); // react to clicks

        login.setWidth("100%");
        login.setHeight("300px");
        login.addListener(new LoginForm.LoginListener() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 6245053537269550510L;

			@Override
			public void onLogin(LoginEvent event) {
				
				try {
					MessageDigest md = MessageDigest.getInstance("SHA");
					
					String name = event.getLoginParameter("username");
					String passwordHash = BaseConversion.toHexString(md.digest(event.getLoginParameter("password").getBytes()));
					
					loggedIn = client.login(name + " " + passwordHash);
					if (loggedIn) {
						client.setUsername(name);
						setUser(name);
						loggedIn = true;
						showFileTable();
						populateTable();
					}
					else {
						//TODO: Authentication failed!
					}
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
            }
        });
        layout.addComponent(login);
        layout.addComponent(newAccount);
        mainWindow.setContent(layout);
	}

}
