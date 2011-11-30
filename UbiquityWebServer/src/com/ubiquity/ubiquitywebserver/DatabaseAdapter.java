package com.ubiquity.ubiquitywebserver;

/**
 * The DatabaseAdapter acts as a bridge between
 * the server code and the database.
 */
interface DatabaseAdapter {

 public boolean login(String username, String passwordHash);
	
}
