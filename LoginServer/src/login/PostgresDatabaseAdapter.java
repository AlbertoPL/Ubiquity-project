package login;

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
	public boolean lookupDevice(String device, String username) {
		boolean result = false;
		
		Connection conn = null;
		PreparedStatement checkdevice = null;

		try {
			conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
					props);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}		
		
		String checkdeviceString = "SELECT * FROM device WHERE \"name\" = ? AND \"user\" = ?";

		if (conn != null) {
			try {
				checkdevice = conn.prepareStatement(checkdeviceString);
				conn.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

			try {
				checkdevice.setString(1, device);
				checkdevice.setString(2, username);
				ResultSet rs = checkdevice.executeQuery();
				conn.commit();
				if (rs.next()) {
					result = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			try {
				if (checkdevice != null) {
					checkdevice.close();
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
	public boolean addDevice(String device, String os, String username) {
		boolean result = false;
		Connection conn = null;
		PreparedStatement adddevice = null;

		try {
			conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
					props);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}		
		
		String adddeviceString = "INSERT INTO device(\"name\", os, \"user\") VALUES (?, ?, ?)";
		
		try {
			adddevice = conn.prepareStatement(adddeviceString);
			conn.setAutoCommit(false);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		try {
			adddevice.setString(1, device);
			adddevice.setString(2, os);
			adddevice.setString(3, username);
			int i = adddevice.executeUpdate();
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
		
		try {
			if (adddevice != null) {
				adddevice.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
}