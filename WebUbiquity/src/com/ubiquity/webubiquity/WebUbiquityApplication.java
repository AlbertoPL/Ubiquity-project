package com.ubiquity.webubiquity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

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
	
	@Override
	public void init() {
		mainWindow = new ListWindow("Web Ubiquity Application");
		setMainWindow(mainWindow);

		client = new ClientHandler(null);
		
		loggedIn = false;
		//ask for login first!
		showLoginForm();
	}
	
	private void showFileTable() {
		mainWindow.setContent();
	}
	
	public boolean userExists(String username) {
		return client.userExists(username);
	}
	
	private void populateTable() {
		List<Object[]> files = client.getAllUserFiles();
		
		int count = 0;
		if (files != null) {
			for (Object[] o: files) {
			mainWindow.getTable().addItem(o,new Integer(count++));
			}
		}	
	}
	
	public boolean betaSignup(String username, String email) {
		return client.betaSignup(username, email);
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
