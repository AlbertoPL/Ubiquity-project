package server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import scala.collection.Iterator;
import scala.collection.immutable.Vector;

public class FileSendServer implements Runnable, FileServer {

	private ServerSocket serverSocket;
	private final static int BUFFER = 2048;
	private DatabaseAdapter database;

	public FileSendServer() {
		database = new PostgresDatabaseAdapter();
	}
	
	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(4445);
		} catch (IOException e) {
			System.out.println("Could not listen on port: 4445");
			System.exit(-1);
		}
		Socket clientSocket = null;
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				final DataInputStream in = new DataInputStream(clientSocket.getInputStream());
				final DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

				Thread t = new Thread(new Runnable() {

					private void sendFile(String file) {
						byte[] mybytearray = new byte[BUFFER];
						try {
							File f = new File(file);
							
							if (f.exists()) {
								out.writeInt(f.getName().length());
								out.flush();
								out.writeChars(f.getName());
								out.flush();
								FileInputStream fin = new FileInputStream(f);
							    BufferedInputStream bin = new BufferedInputStream(fin);
								long fileLength = f.length();
								out.writeLong(fileLength);
								out.flush();
								int bytesRead;
							    while (fileLength > 0) {
							    	if (fileLength > BUFFER) {
								    	bytesRead = bin.read(mybytearray);
								    }
								    else {
								    	bytesRead = bin.read(mybytearray, 0, (int)fileLength);
								    }
								    fileLength -= bytesRead;
								    out.write(mybytearray, 0, bytesRead);
								    out.flush();
							    }
							    bin.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					@Override
					public void run() {
						try {
							int usernameSize = in.readInt();
							StringBuilder usernamebuilder = new StringBuilder();
							for (int x = 0; x < usernameSize; ++x) {
								usernamebuilder.append(in.readChar());
							}
							String username = usernamebuilder.toString();
							
							int passwordHashSize = in.readInt();
							StringBuilder passwordHashbuilder = new StringBuilder();
							for (int x = 0; x < usernameSize; ++x) {
								passwordHashbuilder.append(in.readChar());
							}
							String passwordHash = passwordHashbuilder.toString();

							//TODO: ACTUAL verification of this client
							
							int typeId = in.readInt();
							//if type id is -1, either ignore or grab all files
							int fileId = in.readInt();
							//grab file location from database and send the file, if -1 is sent, send ALL files
							//if typeId is also -1, other get all files of type
							if (typeId == -1 && fileId == -1) {
								Vector<String[]> files = database.selectAllFilesFromUser(username);
								Iterator i = files.iterator();
								while (i.hasNext()) {
									String[] fileInfo = (String[]) i.next();
									String filepath = fileInfo[2];//2 happens to be the position in the array 
									//where the path is returned from the db.
									sendFile(filepath);
								}
							}
							else if(typeId == -1 && fileId >= 0) {
								//grab single file
							}
							else if (typeId >=0) {
								//grab all files of type
							}

						//	FileOutputStream fos = new FileOutputStream(localName,
					//				false);
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
				}); 
				t.start();
			}
			catch(IOException e) {
				System.out.println("Accept failed: 4445");
			}
		}
	}

}
