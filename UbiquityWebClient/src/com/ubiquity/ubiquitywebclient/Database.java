package com.ubiquity.ubiquitywebclient;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

	Connection dbConnection;

	public Database() {
		dbConnection = null;
		setDBSystemDir();
	}
	
	private void setDBSystemDir() {
	    // Decide on the db system directory: <userhome>/.ubiquity/
	    String userHomeDir = System.getProperty("user.home", ".");
	    String systemDir = userHomeDir + System.getProperty("file.separator") + ".ubiquity";

	    // Set the db system directory.
	    System.setProperty("derby.system.home", systemDir);
	}
	
	public String getDBExportPath() {
		return System.getProperty("derby.system.home") + System.getProperty("file.separator") + "dbexport.dex";
	}
	
	public void connectToDB() {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		String strUrl = "";
		if (new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".ubiquity" + System.getProperty("file.separator") + "Ubiquity").exists()) {
			strUrl = "jdbc:derby:Ubiquity;";//user=dbuser;password=dbuserpwd;";
			try {
			    dbConnection = DriverManager.getConnection(strUrl);
			} catch (SQLException sqle) {
			    sqle.printStackTrace();
			}
		}
		else {
			strUrl = "jdbc:derby:Ubiquity;create=true";
			try {
			    dbConnection = DriverManager.getConnection(strUrl);
			    createTables(dbConnection);
			} catch (SQLException sqle) {
			    sqle.printStackTrace();
			}
		}
		
	}
	
	private boolean createTables(Connection dbConnection) {
	    boolean bCreatedTables = false;
	    Statement statement = null;
	    try {
	        statement = dbConnection.createStatement();
	        statement.execute("CREATE table APP.ACCOUNT (NAME VARCHAR(80) PRIMARY KEY,PASSHASH VARCHAR(80))");
	        bCreatedTables = true;
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	    }
	    
	    return bCreatedTables;
	}
	
	public boolean checkUserExists() throws SQLException {
		Statement stmtSaveNewRecord = dbConnection.createStatement(); 
		ResultSet rs = stmtSaveNewRecord.executeQuery("SELECT * FROM APP.ACCOUNT"); 
		if (rs.next()) {
			 return true;
		}
		return false;
	}
	
	public boolean checkCorrectUser(String username, String passwordHash) throws SQLException {
		PreparedStatement stmtSaveNewRecord = dbConnection.prepareStatement(
			    "SELECT * FROM APP.ACCOUNT WHERE NAME=? AND PASSHASH=?");
		stmtSaveNewRecord.clearParameters();
		stmtSaveNewRecord.setString(1, username);
		stmtSaveNewRecord.setString(2, passwordHash);
		ResultSet rs = stmtSaveNewRecord.executeQuery();
		if (rs.next()) {
			 return true;
		}
		return false;
	}
	
	public boolean setUser(String username, String passwordHash) throws SQLException {

		PreparedStatement stmtSaveNewRecord = dbConnection.prepareStatement(
			    "INSERT INTO APP.ACCOUNT " +
			    "   (NAME, PASSHASH) " +
			    "VALUES (?, ?)",
			    Statement.RETURN_GENERATED_KEYS);
	    stmtSaveNewRecord.clearParameters();
	    stmtSaveNewRecord.setString(1, username);
	    stmtSaveNewRecord.setString(2, passwordHash);
	    stmtSaveNewRecord.executeUpdate();
	    ResultSet results = stmtSaveNewRecord.getGeneratedKeys();
	    if (results.next()) {
	       return true;
	    }
		return false;
	}
	/*public int addNewFileToDB(UbiquityFile f) throws SQLException {
		PreparedStatement stmtSaveNewRecord = dbConnection.prepareStatement(
			    "INSERT INTO APP.FILE " +
			    "   (FILENAME, FILEPATH, FILETYPE, " +
			    "    FILESIZE) " +
			    "VALUES (?, ?, ?, ?)",
			    Statement.RETURN_GENERATED_KEYS);
		
		int id = -1;
	    stmtSaveNewRecord.clearParameters();
	    stmtSaveNewRecord.setString(1, f.getName());
	    stmtSaveNewRecord.setString(2, f.getPath());
	    stmtSaveNewRecord.setString(3, f.getFileType());
	    stmtSaveNewRecord.setLong(4, f.length()); //size of file in bytes
	    stmtSaveNewRecord.executeUpdate();
	    ResultSet results = stmtSaveNewRecord.getGeneratedKeys();
	    if (results.next()) {
	        id = results.getInt(1);
	    }
	    return id;
	}
	
	public boolean editFileInDB(UbiquityFile f) throws SQLException {
		PreparedStatement stmtUpdateExistingRecord = dbConnection.prepareStatement(
			    "UPDATE APP.FILE " +
			    "SET FILENAME = ?, " +
			    "    FILEPATH = ?, " +
			    "    FILETYPE = ?, " +
			    "    FILESIZE = ? " +
			    "WHERE ID = ?");
		
		boolean bEdited = false;
		stmtUpdateExistingRecord.clearParameters();
		stmtUpdateExistingRecord.setString(1, f.getName());
		stmtUpdateExistingRecord.setString(2, f.getPath());
		stmtUpdateExistingRecord.setString(3, f.getFileType());
	    stmtUpdateExistingRecord.setLong(4, f.length()/1024); //size of file in kB
	    stmtUpdateExistingRecord.setInt(5, f.getDbID());
	    stmtUpdateExistingRecord.executeUpdate();
	    bEdited = true;
	    return bEdited;
	}
	
	public boolean deleteFileFromDB(UbiquityFile f) throws SQLException {
		PreparedStatement stmtDeleteAddress = dbConnection.prepareStatement(
		        "DELETE FROM APP.FILE " +
		        "WHERE ID = ?");
		boolean bDeleted = false;

	    stmtDeleteAddress.clearParameters();
	    stmtDeleteAddress.setInt(1, f.getDbID());
	    stmtDeleteAddress.executeUpdate();
	    bDeleted = true;
	    return bDeleted;
	}*/
	
	public boolean isConnected() {
		return (dbConnection != null);
	}
}
