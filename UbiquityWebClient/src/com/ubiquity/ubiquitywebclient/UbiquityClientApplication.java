package com.ubiquity.ubiquitywebclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import com.vaadin.Application;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class UbiquityClientApplication extends Application {
	
	private boolean running;
	private String passwordHash;
	private LoginForm login;
	private boolean loggedIn;
	private FileViewLayout mainLayout;
	private Window mainWindow;
	private Thread dnsUpdate;
	private Database database;
	
	@Override
	public void init() {
		mainWindow = new Window("Ubiquity");
		mainLayout = new FileViewLayout();
		//mainWindow.setContent(layout);
		setMainWindow(mainWindow);
	

		dnsUpdate = new Thread(new Runnable() {
	
				@Override
				public void run() {
					running = true;
					while(running) {
						if (!isInternetReachable()) {
							try {
								Thread.sleep(1000 * 60 * 15);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						//send update and sleep for a while
						sendUpdate();
						
						try {
							Thread.sleep(1000 * 60 * 15);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} //15 minute wait
					}
				}
				
			});
		
		database = new Database();
		
		showLoginForm();
	    }
	
	private boolean login(String username) {
		  Socket clientSocket;
		  boolean success = false;
		try {
			clientSocket = new Socket("testubiquity.info", 14445);
			PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(),true);
			  outToServer.println(username);
			  outToServer.println(this.passwordHash);
			  outToServer.println(InetAddress.getLocalHost().getHostName());
			  outToServer.println(System.getProperty("os.name"));
			  BufferedReader inFromClient = new BufferedReader(new InputStreamReader(
					  clientSocket.getInputStream()));
			  String successString = inFromClient.readLine();
			  if (successString.equalsIgnoreCase("true")) {
				  success = true;
			  }
			  clientSocket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
		  
	}
	
	private boolean sendUpdate() {
		  Socket clientSocket;
		try {
			clientSocket = new Socket("testubiquity.info", 14443);
			PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
				outToServer.println((String)this.getUser());
			  outToServer.println(this.passwordHash);
			  URL whatismyip = new URL("http://www.whatismyip.org");
			  BufferedReader in = new BufferedReader(new InputStreamReader(
			                  whatismyip.openStream()));

			  String ip = in.readLine(); //you get the IP as a String

			  outToServer.println(ip);
			  outToServer.println(InetAddress.getLocalHost().getHostName());
			  in.close();
			  clientSocket.close();
			  return true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		  
	}

	//checks for connection to the internet through dummy request
    private boolean isInternetReachable()
    {
            try {
                    //make a URL to a known source
                    URL url = new URL("http://www.testubiquity.info");

                    //open a connection to that source
                    HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();

                    //trying to retrieve data from the source. If there
                    //is no connection, this line will fail
                    Object objData = urlConnect.getContent();

            } catch (UnknownHostException e) {
                    e.printStackTrace();
                    return false;
            }
            catch (IOException e) {
                    e.printStackTrace();
                    return false;
            }
            return true;
    }
    
    private void showLoginForm() {
		VerticalLayout layout = new VerticalLayout();
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
					
					String name = event.getLoginParameter("username");
					UbiquityClientApplication.this.passwordHash = BaseConversion.toHexString(md.digest(event.getLoginParameter("password").getBytes()));
					
					if (!database.isConnected()) {
						database.connectToDB();
					}
					boolean correctUser = false;
					
					try {
						if (database.checkUserExists()) {
							correctUser = database.checkCorrectUser(name, passwordHash);
						}
						else {
							correctUser = database.setUser(name, passwordHash);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
					if (correctUser) {
						loggedIn = login(name);
						if (loggedIn) {
							setUser(name);
							loggedIn = true;
							dnsUpdate.start();
							mainWindow.setContent(UbiquityClientApplication.this.mainLayout);
						}
						else {
							//TODO: MAKE THIS BETTER
							UbiquityClientApplication.this.close();
						}
					}
					else {
						//TODO: MAKE THIS BETTER
						UbiquityClientApplication.this.close();
					}
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
            }
        });
        layout.addComponent(login);
        mainWindow.setContent(layout);
	}
	
    public String getPasswordHash() {
    	return passwordHash;
    }
	
}
