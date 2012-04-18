

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

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
		if (new File(System.getProperty("user.home") + System.getProperty("file.separator") + ".ubiquity" + System.getProperty("file.separator") + "UbiquityDB").exists()) {
			strUrl = "jdbc:derby:UbiquityDB;";//user=dbuser;password=dbuserpwd;";
			try {
			    dbConnection = DriverManager.getConnection(strUrl);
			} catch (SQLException sqle) {
			    sqle.printStackTrace();
			}
		}
		else {
			strUrl = "jdbc:derby:UbiquityDB;create=true";
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
	        statement.execute("CREATE table APP.LOGIN (ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),USERNAME LONG VARCHAR, PASSWORD LONG VARCHAR)");
	        statement.execute("CREATE table APP.PROJECT (ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),PROJECTNAME LONG VARCHAR,PROJECTPATH LONG VARCHAR)");
	        bCreatedTables = true;
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	    }
	    
	    return bCreatedTables;
	}
	
	public Map<String,String> getProjects() throws SQLException {
		Map<String, String> projects = new HashMap<String, String>();
		
		PreparedStatement statement = dbConnection
				.prepareStatement("SELECT * from APP.PROJECT");

		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			System.out.println("Project name: " + resultSet.getString("PROJECTNAME"));
			System.out.println("Project path: " + resultSet.getString("PROJECTPATH"));

			String name = resultSet.getString("PROJECTNAME");
			String path = resultSet.getString("PROJECTPATH");
			projects.put(name.substring(0, name.lastIndexOf(".uprj")), path);
		}
		
		return projects;
	}
	
	public boolean saveProject(String projectname, String projectpath) throws SQLException {
		boolean saved = false;
		PreparedStatement stmtSaveNewRecord = dbConnection.prepareStatement(
			    "INSERT INTO APP.PROJECT " +
			    "   (PROJECTNAME, PROJECTPATH) " +
			    "VALUES (?, ?)",
			    Statement.RETURN_GENERATED_KEYS);
		stmtSaveNewRecord.clearParameters();
	    stmtSaveNewRecord.setString(1, projectname);
	    stmtSaveNewRecord.setString(2, projectpath);
	    stmtSaveNewRecord.executeUpdate();
	    ResultSet results = stmtSaveNewRecord.getGeneratedKeys();
	    if (results.next()) {
	    	saved = true;
	    }
		return saved;
	}
	
	public void deleteProject(String projectpath) throws SQLException {
		PreparedStatement stmtDeleteRecord = dbConnection.prepareStatement(
			    "DELETE FROM APP.PROJECT WHERE PROJECTPATH LIKE ?");
		stmtDeleteRecord.clearParameters();
		stmtDeleteRecord.setString(1, projectpath);
		stmtDeleteRecord.executeUpdate();
	}
	
	public boolean saveLoginDetails(String username, String passwordHash) throws SQLException {
		boolean saved = false;
		PreparedStatement stmtSaveNewRecord = dbConnection.prepareStatement(
			    "INSERT INTO APP.LOGIN " +
			    "   (USERNAME, PASSWORD) " +
			    "VALUES (?, ?)",
			    Statement.RETURN_GENERATED_KEYS);
		stmtSaveNewRecord.clearParameters();
	    stmtSaveNewRecord.setString(1, username);
	    stmtSaveNewRecord.setString(2, passwordHash);
	    stmtSaveNewRecord.executeUpdate();
	    ResultSet results = stmtSaveNewRecord.getGeneratedKeys();
	    if (results.next()) {
	    	saved = true;
	    }
		return saved;
	}
	
	public boolean exportDatabase(String filename) throws SQLException {
		boolean exported = false;
		
		PreparedStatement ps = dbConnection.prepareStatement(
	    "CALL SYSCS_UTIL.SYSCS_EXPORT_TABLE (?,?,?,?,?,?)");
	    ps.setString(1,null);
	    ps.setString(2,"LOGIN");
	    ps.setString(3,System.getProperty("derby.system.home") + System.getProperty("file.separator") + filename);
	    ps.setString(4,System.getProperty("line.separator"));
	    ps.setString(5,null);
	    ps.setString(6,null);
	    ps.execute();
		exported = true;
		
		return exported;
	}
	
	public boolean isConnected() {
		return (dbConnection != null);
	}
}
