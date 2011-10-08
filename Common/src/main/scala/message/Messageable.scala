package message

import java.io._
import java.net._

/**
 * Defines methods that clients and servers must implement in order to 
 * interpret messages received through a MessageHandler.
 */
trait Messageable {
  
  var host: String =_
  var port: Int = _
  var connected: Boolean = _
  var loggedIn: Boolean = _
  var osName: String = _
  var socket: Socket = _
  var fileSocket: Socket = _
  var messageSender: MessageSender = _

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
  
  def inputStream: InputStream = {
    try {
      return socket.getInputStream
    } catch {
      case e: IOException => e.printStackTrace
    }
	null
  }
  
  def outputStream: OutputStream = {
    try {
      return socket.getOutputStream
    } catch {
      case e: IOException => e.printStackTrace
    }
	null
  }
  
  def fileInputStream = {
    try {
      fileSocket.getInputStream
    } catch {
      case e: IOException => e.printStackTrace
    }
    null
  }
  
  def fileOutputStream = {
    try {
      fileSocket.getOutputStream
    } catch {
      case e: IOException => e.printStackTrace
    }
    null
  }
  
  def deviceName: String
  
  def rootFolder: String
}
