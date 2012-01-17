package com.ubiquity.ubiquitywebserver;

import java.util.List;

/**
 * The DatabaseAdapter acts as a bridge between
 * the server code and the database.
 */
interface DatabaseAdapter {

 public int login(String username, String passwordHash);
	
 public List<String> getDevices(int userid);
 
}
