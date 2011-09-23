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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PostgresDatabaseAdapter implements DatabaseAdapter {

	Properties props = new Properties();
	
	public PostgresDatabaseAdapter() {
		props.setProperty("user","postgres");
		props.setProperty("password","bob");
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean login(final String username, final String passwordHash) {

		System.out.println("Logging user " + username + " in");
		boolean result = false;
				
		Connection conn = null;
		PreparedStatement login = null;
				
		try {
			conn = DriverManager.getConnection("jdbc:postgresql:ubiquity", props);
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
			    
				try {
					conn = DriverManager.getConnection("jdbc:postgresql:ubiquity", props);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				
				String insertString = "INSERT INTO file(\"name\", device, path, \"user\", \"type\", size) values(?,?,?,?,?,?)";
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
								line = read.readLine().trim();
								long size = Long.parseLong(line);
								//firstPath = firstPath.replace("\\", "\\\\");
								
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
										insertIndexFiles.setLong(6, size);
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
	
	@Override
	public List<Object[]> selectAllFilesFromUser(String username) {
		List<Object[]> objects = new ArrayList<Object[]>();
		Connection conn = null;
	    PreparedStatement getIndex = null;
	    
		try {
			conn = DriverManager.getConnection("jdbc:postgresql:ubiquity", props);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		String getString = "SELECT * FROM file WHERE \"user\" = ?"; 
		
		if (conn != null) {
			try {
				getIndex = conn.prepareStatement(getString);
				conn.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			try {
				getIndex.setString(1, username);
				ResultSet rs = getIndex.executeQuery();
				conn.commit();
				while (rs.next()) {
					String path = rs.getString("path");
					path = path.replace("\\\\", "\\");
					path = path.trim();
					objects.add(new Object[] {
							rs.getString("device"), rs.getString("name").trim(), path, rs.getString("type").trim()}); 
				}
			} catch (SQLException e) {
				e.printStackTrace();
				try {
					System.err.println("Transaction is being rolled back: check firstPath");
					conn.rollback();
				} 
				catch(SQLException excep) {
					excep.printStackTrace();
			    }
			}
		}
		try {
			if (getIndex != null) { 
				getIndex.close(); 
			}
		    if (conn != null) {
		    	conn.close();
		    }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return objects;
	}

	@Override
	public boolean userExists(String username) {
		boolean exists = false;
		Connection conn = null;
	    PreparedStatement getUser = null;
	    
		try {
			conn = DriverManager.getConnection("jdbc:postgresql:ubiquity", props);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		String getString = "SELECT * FROM \"user\" WHERE \"name\" = ?"; 
		String getBetaString = "SELECT * FROM betasignup WHERE username = ?";
		
		if (conn != null) {
			try {
				getUser = conn.prepareStatement(getString);
				conn.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			try {
				getUser.setString(1, username);
				ResultSet rs = getUser.executeQuery();
				conn.commit();
				if (rs.next()) {
					exists = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				try {
					System.err.println("Transaction is being rolled back: check username");
					conn.rollback();
				} 
				catch(SQLException excep) {
					excep.printStackTrace();
			    }
			}
			try {
				getUser = conn.prepareStatement(getBetaString);
				conn.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			try {
				getUser.setString(1, username);
				ResultSet rs = getUser.executeQuery();
				conn.commit();
				if (rs.next()) {
					exists = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				try {
					System.err.println("Transaction is being rolled back: check username");
					conn.rollback();
				} 
				catch(SQLException excep) {
					excep.printStackTrace();
			    }
			}
			
		}
		try {
			if (getUser != null) { 
				getUser.close(); 
			}
		    if (conn != null) {
		    	conn.close();
		    }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return exists;
	}

	@Override
	public boolean betaSignup(String username, String email) {
		boolean success = false;
		Connection conn = null;
	    PreparedStatement insertString = null;
	    
		try {
			conn = DriverManager.getConnection("jdbc:postgresql:ubiquity", props);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		String getString = "INSERT INTO betasignup (username, email) values(?,?)"; 
		
		if (conn != null) {
			try {
				insertString = conn.prepareStatement(getString);
				conn.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			try {
				insertString.setString(1, username);
				insertString.setString(2, email);
				insertString.executeUpdate();
				conn.commit();
				success = true;
			} catch (SQLException e) {
				e.printStackTrace();
				try {
					System.err.println("Transaction is being rolled back: check username or email");
					conn.rollback();
				} 
				catch(SQLException excep) {
					excep.printStackTrace();
			    }
			}
		}
		try {
			if (insertString != null) { 
				insertString.close(); 
			}
		    if (conn != null) {
		    	conn.close();
		    }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return success;
	}

	@Override
	public long getFileSize(String username, String filepath, String deviceName) {
		long size = 0;
		Connection conn = null;
	    PreparedStatement selectString = null;
	    
		try {
			conn = DriverManager.getConnection("jdbc:postgresql:ubiquity", props);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		String getString = "SELECT size FROM file where \"user\"=? and path=? and device=?"; 
		
		if (conn != null) {
			try {
				selectString = conn.prepareStatement(getString);
				conn.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			try {
				selectString.setString(1, username);
				selectString.setString(2, filepath);
				selectString.setString(3, deviceName);
				ResultSet rs = selectString.executeQuery();
				conn.commit();
				if (rs.next()) {
					size =  rs.getLong(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				try {
					System.err.println("Transaction is being rolled back: check username or email");
					conn.rollback();
				} 
				catch(SQLException excep) {
					excep.printStackTrace();
			    }
			}
		}
		try {
			if (selectString != null) { 
				selectString.close(); 
			}
		    if (conn != null) {
		    	conn.close();
		    }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return size;
	}

}
