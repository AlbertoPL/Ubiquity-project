package server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import file.FileReceiver;
import file.FileSender;

/**
 * 
 * @author Alberto Pareja-Lecaros
 * 
 *         The FileServer sends files to their appropriate destinations and,
 *         when necessary, polls clients for files that have been requested. One
 *         file server is created per client.
 * 
 */
public class FileReceiveServer implements Runnable, FileServer {

	private ServerSocket serverSocket;
	private final static int BUFFER = 2048;

	private DatabaseAdapter database;

	public FileReceiveServer() {
		database = new PostgresDatabaseAdapter();
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(4444);
		} catch (IOException e) {
			System.out.println("Could not listen on port: 4444");
			System.exit(-1);
		}
		Socket clientSocket = null;
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				final DataInputStream in = new DataInputStream(
						clientSocket.getInputStream());
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						byte[] mybytearray = new byte[BUFFER];
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
							String passwordHash = passwordHashbuilder
									.toString();

							int deviceNameSize = in.readInt();
							StringBuilder deviceNamebuilder = new StringBuilder();
							for (int x = 0; x < usernameSize; ++x) {
								deviceNamebuilder.append(in.readChar());
							}
							String deviceName = deviceNamebuilder.toString();

							int numberOfFiles = in.readInt();
							for (int i = 0; i < numberOfFiles; ++i) {

								int fileNameSize = in.readInt();
								StringBuilder filenamebuilder = new StringBuilder();
								for (int x = 0; x < fileNameSize; ++x) {
									filenamebuilder.append(in.readChar());
								}
								String filename = filenamebuilder.toString();
								// make the directories if they do not exist
								File f = new File(username
										+ System.getProperty("file.separator")
										+ deviceName
										+ System.getProperty("file.separator"));
								if (!f.exists()) {
									f.mkdirs();
								}
								f = new File(f.getAbsolutePath()
										+ System.getProperty("file.separator")
										+ filename);
								String localName = f.getAbsolutePath();
								System.out.println("Receiving: " + localName);
								FileOutputStream fos = new FileOutputStream(
										localName, false);
								BufferedOutputStream bos = new BufferedOutputStream(
										fos);
								long fileLength = in.readLong();
								long originalFileLength = fileLength;
								int bytesRead = 0;
								while (fileLength > 0) { // Index file being
															// sent
									if (fileLength > BUFFER) {
										bytesRead = in.read(mybytearray);
									} else {
										bytesRead = in.read(mybytearray, 0,
												(int) fileLength);
									}
									fileLength -= bytesRead;
									bos.write(mybytearray, 0, bytesRead);
									bos.flush();
								}
								bos.close();
								//database.storeFileInDatabase(username,
								//		filename, localName,
								//		originalFileLength, deviceName);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				});
				t.start();
			} catch (IOException e) {
				System.out.println("Accept failed: 4444");
			}
		}
	}

}