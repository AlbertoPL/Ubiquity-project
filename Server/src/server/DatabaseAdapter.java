package server;

import java.util.List;

/**
 * 
 * @author Alberto Pareja-Lecaros
 *
 * The DatabaseAdapter acts as a bridge between the server code and the database.
 * 
 */
public interface DatabaseAdapter {

	public void storeIndexInDatabase(final String username, final String filename, final String deviceName);
	public boolean login(final String username, final String passwordHash);
	public List<Object[]> selectAllFilesFromUser(String username);
}
