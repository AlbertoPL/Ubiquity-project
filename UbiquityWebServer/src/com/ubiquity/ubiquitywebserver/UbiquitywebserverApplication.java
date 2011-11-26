package com.ubiquity.ubiquitywebserver;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class UbiquitywebserverApplication extends Application {
	
	private boolean loggedIn;
	private ListWindow mainWindow;
	private NewAccountPojoForm newAccountForm;
	private LoginForm login;
	private DatabaseAdapter database;
	
	@Override
	public void init() {
		mainWindow = new ListWindow("Ubiquity Server");
		database = new PostgresDatabaseAdapter();
		setMainWindow(mainWindow);
		loggedIn = false;
		showLoginForm();
	}
	
	private void showFileTable() {
		mainWindow.setContent();
	}

	public boolean login(String username, String passwordHash) {
		return database.login(username, passwordHash);
	}
	
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
					
					loggedIn = login(name,passwordHash);
					if (loggedIn) {
						setUser(name);
						loggedIn = true;
						showFileTable();
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
