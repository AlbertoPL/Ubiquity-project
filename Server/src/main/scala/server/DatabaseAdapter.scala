package server
import file.UbiquityFileData

/**
 * The DatabaseAdapter acts as a bridge between
 * the server code and the database.
 */
trait DatabaseAdapter {

  def storeIndexInDatabase(username: String, filename: String,
    deviceName: String)
  def storeFileInDatabase(userid: Int, filename: String, filepath: String, filesize: Long, devicename: String)
  def getAllFilesOfType(userid: Int, fileid: Int): Array[UbiquityFileData];
  def login(username: String, passwordHash: String): Int
  def selectAllFilesFromUser(username: String): Vector[Array[String]]
  def userExists(username: String): Boolean
  def betaSignup(username: String, email: String): Boolean
  def getFileSize(username: String, filepath: String,
    deviceName: String): Long
  def insertDevice(devicename: String, macaddr: String, userid: Int)
  def getDevices(userid: Int): Array[String]
}
