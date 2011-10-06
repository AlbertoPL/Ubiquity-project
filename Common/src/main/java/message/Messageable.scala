package message;

import java.io._

/**
 * Defines methods that clients and servers must implement in order to 
 * interpret messages received through a MessageHandler.
 */
trait Messageable {
  
  var host: String
  var port: Int
  var connected: Boolean
  var loggedIn: Boolean
  var osName: String
  var rootFolder: String

  /**
   * 
   * Determines a course of action based on the code passed and the state of
   * the Messageable.
   * 
   * @param code - the integer code received from a network source
   * @return None
   */
  def interpretCode(message: Message): Unit
  def receiverDisconnected: Unit
  def fileReceivedCallback(filename: String, message: Message): Unit
  
  def inputStream: InputStream
  def outputStream: OutputStream 
  def fileInputStream: FileInputStream
  def fileOutputStream: FileOutputStream
  def deviceName: String
}
