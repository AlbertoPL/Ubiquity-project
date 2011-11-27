package fileshare;

/**
 * The DatabaseAdapter acts as a bridge between
 * the server code and the database.
 */
interface DatabaseAdapter {

	public boolean login(String username, String passwordHash);
	
	public boolean shareFile(String filename, String filepath, String filetype, String username, String device, String shareWith);
}
