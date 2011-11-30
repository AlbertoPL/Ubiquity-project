package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import message.Message;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import remote.RmiServer;

/**
 * The Server implements Messageable so that the Server defines methods
 * necessary for handling messages passed to it from the MessageHandler.
 * The Server is then responsible for starting up all server functions,
 * including the BackupAdapter, the DatabaseAdapter, and the FileServer.
 */
object Server {
  val serverPropertiesFilename = "src/main/resources/server.properties"
  var validOsTypes: List[_] = _

  def main(args: Array[String]) {
    var s = new Server
    s.init
    var t = new Thread(s)
    t.start

    var rmi = new RmiServer(s);
  }
}

class Server extends Runnable {

  var port: Int = _
  var running: Boolean = _
  var clientHandlers: Seq[ClientHandler] = _
  var serverSocket: ServerSocket = _

  def init {
    var properties = new PropertiesConfiguration();
    try {
      properties.load(new FileInputStream(Server.serverPropertiesFilename));
      port = properties.getInt("port");
      Server.validOsTypes = properties.getList("validOs")
    } catch {
      case ioe: IOException => ioe.printStackTrace
      case ce: ConfigurationException => ce.printStackTrace
    }
    running = true;
    clientHandlers = Vector.empty

    try {
      serverSocket = new ServerSocket(port)
    } catch {
      case ioe: IOException => println("Could not listen on port: " + port)
    }
  }

  override def run {
    var clientSocket: Socket = null
    while (running) {
      try {
        clientSocket = serverSocket.accept

        var client = new ClientHandler(clientSocket)
        var t = new Thread(client)
        t.start

        clientHandlers :+= client

      } catch {
        case ioe: IOException => println("Accept failed: " + port)
      }
    }
    try {
      serverSocket.close
    } catch {
      case ioe: IOException => ioe.printStackTrace
    }

    clientHandlers.foreach(_.stop)
  }

  def sendMessageToClient(username: String, deviceName: String, m: Message): Boolean = {
    clientHandlers = clientHandlers.filter(_.connected)
    var ret = clientHandlers.filter { handler =>
      handler.username == username && handler.deviceName == deviceName
    } headOption match {
      case Some(handler) =>
        handler.messageSender.enqueueMessage(m)
        return true
      case None =>
        return false
    }
    ret
  }
}
