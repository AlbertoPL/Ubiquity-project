package login;

/**
 * The DatabaseAdapter acts as a bridge between
 * the server code and the database.
 */
interface DatabaseAdapter {

	public boolean login(String username, String passwordHash);
	
	public boolean lookupDevice(String device, String username);
	
	public boolean addDevice(String device, String os, String username);
}
