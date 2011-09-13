package com.ubiquity.webubiquity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import server.ClientHandler;
import util.BaseConversion;

import com.ubiquity.webubiquity.window.ListWindow;
import com.vaadin.Application;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;

public class WebUbiquityApplication extends Application {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3948762438314239641L;

	private boolean loggedIn;
	private ListWindow mainWindow;
	private LoginForm login;
	
	@Override
	public void init() {
		mainWindow = new ListWindow("Web Ubiquity Application");
		setMainWindow(mainWindow);

		loggedIn = false;
		//ask for login first!
		showLoginForm();
	}
	
	private void showFileTable() {
		mainWindow.setContent();
	}
	
	private void populateTable(ClientHandler c) {
		List<Object[]> files = c.getAllUserFiles();
		
		int count = 0;
		if (files != null) {
			for (Object[] o: files) {
			mainWindow.getTable().addItem(o,new Integer(count++));
			}
		}	
	}
	
	private void showLoginForm() {
		login = new LoginForm();
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
					
					ClientHandler c = new ClientHandler(null);
					
					String name = event.getLoginParameter("username");
					String passwordHash = BaseConversion.toHexString(md.digest(event.getLoginParameter("password").getBytes()));
					
					loggedIn = c.login(name + " " + passwordHash);
					if (loggedIn) {
						c.setUsername(name);
						setUser(name);
						loggedIn = true;
						showFileTable();
						populateTable(c);
					}
					else {
						//TODO: Authentication failed!
					}
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
            }
        });
        mainWindow.setContent(login);
	}

}
