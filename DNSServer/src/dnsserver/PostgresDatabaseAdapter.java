package dnsserver;

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
}