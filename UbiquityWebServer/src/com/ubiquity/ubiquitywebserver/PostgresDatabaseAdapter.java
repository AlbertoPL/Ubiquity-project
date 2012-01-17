package com.ubiquity.ubiquitywebserver;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
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
	public int login(final String username, final String passwordHash) {

		System.out.println("Logging user " + username + " in");
		int id = -1;

		Connection conn = null;
		PreparedStatement login = null;

		try {
			conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
					props);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		String loginString = "SELECT \"userid\" FROM \"users\" WHERE \"username\" = ? " +
			      "AND \"password\" = ?";

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
					id = rs.getInt(1);
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
		return id;
	}

	@Override
	public List<String> getDevices(int userid) {
		Connection conn = null;
		PreparedStatement getDevices = null;
		List<String> devices = new ArrayList<String>();

		try {
			conn = DriverManager.getConnection("jdbc:postgresql:ubiquity",
					props);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}		

		CallableStatement getdevicesProc;
		try {
			getdevicesProc = conn.prepareCall("{ ? = call getdevices(?) }");
			getdevicesProc.registerOutParameter(1, Types.OTHER);
			getdevicesProc.setInt(2, userid);

			getdevicesProc.execute();
			Object table = getdevicesProc.getObject(1);
			getdevicesProc.close();  
			System.out.println(table.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}