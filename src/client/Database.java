package client;

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
	    // Decide on the db system directory: <userhome>/.addressbook/
	    String userHomeDir = System.getProperty("user.home", ".");
	    String systemDir = userHomeDir + "/.ubiquity";

	    // Set the db system directory.
	    System.setProperty("derby.system.home", systemDir);
	}
	
	public void connectToDB() {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		String strUrl = "";
		if (new File(System.getProperty("user.home") + "/.ubiquity/UbiquityIndex").exists()) {
			strUrl = "jdbc:derby:UbiquityIndex;user=dbuser;password=dbuserpwd;";
			try {
			    dbConnection = DriverManager.getConnection(strUrl);
			} catch (SQLException sqle) {
			    sqle.printStackTrace();
			}
		}
		else {
			strUrl = "jdbc:derby:UbiquityIndex;user=dbuser;password=dbuserpwd;create=true";
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
	        statement.execute("CREATE table APP.FILE (ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),FILENAME VARCHAR(256), FILEPATH VARCHAR(1024),FILETYPE VARCHAR(32),FILESIZE INTEGER)");
	        bCreatedTables = true;
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	    }
	    
	    return bCreatedTables;
	}
	
	public int addNewFileToDB(UbiquityFile f) throws SQLException {
		PreparedStatement stmtSaveNewRecord = dbConnection.prepareStatement(
			    "INSERT INTO FILE " +
			    "   (FILENAME, FILEPATH, FILETYPE, " +
			    "    FILESIZE) " +
			    "VALUES (?, ?, ?, ?)",
			    Statement.RETURN_GENERATED_KEYS);
		
		int id = -1;
	    stmtSaveNewRecord.clearParameters();
	    stmtSaveNewRecord.setString(1, f.getName());
	    stmtSaveNewRecord.setString(2, f.getPath());
	    stmtSaveNewRecord.setString(3, f.getFileType());
	    stmtSaveNewRecord.setLong(4, f.length()/1024); //size of file in kB
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
	}
	
	public boolean isConnected() {
		return (dbConnection != null);
	}
}
