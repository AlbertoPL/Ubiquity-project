package server

/**
 * The DatabaseAdapter acts as a bridge between 
 * the server code and the database.
 */
trait DatabaseAdapter {

	def storeIndexInDatabase(username: String, filename: String, 
	    deviceName: String)
	def login(username: String, passwordHash: String): Boolean
	def selectAllFilesFromUser(username: String): Vector[Array[String]]
	def userExists(username: String): Boolean
	def betaSignup(username: String, email: String): Boolean
	def getFileSize(username: String, filepath: String, 
	    deviceName: String): Long
}
