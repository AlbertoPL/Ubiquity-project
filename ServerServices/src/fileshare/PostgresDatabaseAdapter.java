package fileshare;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;


class PostgresDatabaseAdapter implements DatabaseAdapter {

	Properties props = new Properties();

	public PostgresDatabaseAdapter() {
		props.setProperty("user", "postgres");
		props.setProperty("password", "bob");
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
			conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
					props);
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
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public boolean shareFile(String filename, String filepath, String filetype,
			String username, String device, String shareWith) {
		boolean result = false;
		boolean shareWithExists = false;

		Connection conn = null;
		PreparedStatement shareFile = null;

		try {
			conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
					props);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}		
		
		PreparedStatement checkuser = null;

		String checkuserString = "SELECT \"name\" FROM \"user\" WHERE \"name\" = ?";


		//String shareFileString = "SELECT \"name\" FROM \"user\" WHERE \"name\" = ? AND \"password\" = ?";
		String shareFileString = "INSERT INTO sharedFile(\"name\", path, \"type\", \"owner\", " +
				"device, sharedwith) VALUES (?, ?, ?, ?, ?, ?)";
		
		if (conn != null) {
			try {
				checkuser = conn.prepareStatement(checkuserString);
				conn.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			try {
				checkuser.setString(1, username);
				ResultSet rs = checkuser.executeQuery();
				conn.commit();
				if (rs.next()) {
					shareWithExists = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			if (shareWithExists) {
				try {
					shareFile = conn.prepareStatement(shareFileString);
					conn.setAutoCommit(false);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
	
				try {
					shareFile.setString(1, filename);
					shareFile.setString(2, filepath);
					shareFile.setString(3, filetype);
					shareFile.setString(4, username);
					shareFile.setString(5, device);
					shareFile.setString(6, shareWith);
					int i = shareFile.executeUpdate();
					conn.commit();
					if (i > 0) {
						result = true;
					}
				} catch (SQLException e) {
					e.printStackTrace();
					try {
	                    System.err.println(
	                      "Transaction is being rolled back: check inputs");
	                    conn.rollback();
	                  } catch (SQLException e2){
	                    e2.printStackTrace();
	                  }
				}
			}
			
			try {
				if (shareFile != null) {
					shareFile.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}