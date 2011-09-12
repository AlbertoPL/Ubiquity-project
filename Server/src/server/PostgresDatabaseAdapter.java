package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class PostgresDatabaseAdapter implements DatabaseAdapter {

	@Override
	public boolean login(final String username, final String passwordHash) {

		System.out.println("Logging user " + username + " in");
		boolean result = false;
				
		Connection conn = null;
		PreparedStatement login = null;
		
		Properties props = new Properties();
		props.setProperty("user","postgres");
		props.setProperty("password","bob");
				
		try {
			conn = DriverManager.getConnection("jdbc:postgresql:Ubiquity", props);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
				
		String loginString = "SELECT \"name\" FROM \"user\" WHERE \"name\" = ? AND \"password\" = ?";
				
		if (conn != null) {
			try {
				login = conn.prepareStatement(loginString);
				conn.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
				
			try {
				login.setString(1, username);
				login.setString(2, passwordHash);
				ResultSet rs = login.executeQuery();
				conn.commit();
				if (rs.next()) {
					result = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			try {
				if (login != null) { 
					login.close(); 
				}
			    if (conn != null) {
			    	conn.close();
			    }
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
	@Override
	public void storeIndexInDatabase(final String username, final String filename, final String deviceName) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println("Storing index in database");
				long timeTaken1 = System.currentTimeMillis();
			
				Connection conn = null;
			    PreparedStatement insertIndexFiles = null;
			    PreparedStatement checkIndex = null;
			    
				Properties props = new Properties();
				props.setProperty("user","postgres");
				props.setProperty("password","bob");
				try {
					conn = DriverManager.getConnection("jdbc:postgresql:Ubiquity", props);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				
				String insertString = "INSERT INTO file(\"name\", device, path, \"user\", \"type\") values(?,?,?,?,?)";
				String checkString = "SELECT path FROM file WHERE path = ?"; 
				
				
				if (conn != null) {
					try {
						insertIndexFiles = conn.prepareStatement(insertString);
						checkIndex = conn.prepareStatement(checkString);
						conn.setAutoCommit(false);
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
						
					BufferedReader read = null;
					try {
						read = new BufferedReader(new FileReader(filename));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (read != null) {
						String line = "";
						try {
							while ((line = read.readLine()) != null) {
								line = read.readLine();
								String nameOfFile = line.substring(1, line.length() - 1);
								line = read.readLine();
								String firstPath = line.substring(1, line.length() - 1);
								line = read.readLine();
								String fileType = line.substring(1, line.length() - 1);
								read.readLine();
								firstPath = firstPath.replace("\\", "\\\\");
								
								//first check to see if the path already exists in the database
								
								try {
									checkIndex.setString(1, firstPath);
									ResultSet rs = checkIndex.executeQuery();
									conn.commit();
									if (!rs.next()) {
										insertIndexFiles.setString(1, nameOfFile);
										insertIndexFiles.setString(2, deviceName);
										insertIndexFiles.setString(3, firstPath);
										insertIndexFiles.setString(4, username);
										insertIndexFiles.setString(5, fileType);
										insertIndexFiles.executeUpdate();
										conn.commit();
									}
								} catch (SQLException e1) {
									System.out.println(firstPath);
									e1.printStackTrace();
									try {
										System.err.println("Transaction is being rolled back: check firstPath");
										conn.rollback();
									} 
									catch(SQLException excep) {
										excep.printStackTrace();
								    }
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (ArrayIndexOutOfBoundsException e) {
							e.printStackTrace();
						}
						try {
							read.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				try {
					if (insertIndexFiles != null) { 
						insertIndexFiles.close(); 
					}
				    if (conn != null) {
				    	conn.close();
				    }
				} catch (SQLException e) {
					e.printStackTrace();
				}
				long timeTaken2 = System.currentTimeMillis();
				System.out.println("Time taken to index everything (seconds): " + ((timeTaken2-timeTaken1)/1000.0));
			    //System.out.println(client.getInetAddress().getHostAddress() + ":Index file received");
			}
			
		});
		t.start();
	}

}
