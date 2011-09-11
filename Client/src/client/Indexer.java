package client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import message.Message;
import message.MessageCode;



/**
 * 
 * @author Alberto Pareja-Lecaros
 *
 * The Indexer both traverses the file system, indexing each file, and also 
 * monitors the file system for changes. These updates are sent to the client 
 * to deal with.
 *
 */
public class Indexer implements Runnable {

	private Client client;
	
	private List<String> music;
	private List<String> videos;
	private List<String> documents;
	
	
	public Indexer(Client c) {
		music = new ArrayList<String>();
		videos = new ArrayList<String>();
		documents = new ArrayList<String>();   
		client = c;
	}
	
	/**
	 * TODO: Replace if-else conditions with properties that are loaded, that will make dynamic
	 * adding of new types easier. Also, this solution is too naive, should be checking common
	 * signatures for their appropriate file types
	 * @param root
	 */
	private void index(File root) {
		if (root.canRead() && !root.getName().equalsIgnoreCase(".ubiquity")) {
			try {
				if (root.listFiles() != null) {
					for (File f: root.listFiles()) {
						if (f.isDirectory()) {
							index(f);
						}
						else if (f.getName().contains(".")){
							
							String filename = f.getAbsolutePath() + System.getProperty("file.separator") + f.getName();
							//System.out.println(filename);
	
							if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".mp3")) {
								if (!music.contains(filename)) {
									music.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "music");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".wav")) {
								if (!music.contains(filename)) {
									music.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "music");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".mid")) {
								if (!music.contains(filename)) {
									music.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "music");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".doc")) {
								if (!documents.contains(filename)) {
									documents.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "documents");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".odt")) {
								if (!documents.contains(filename)) {
									documents.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "documents");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".ppt")) {
								if (!documents.contains(filename)) {
									documents.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "documents");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".xls")) {
								if (!documents.contains(filename)) {
									documents.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "documents");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".pptx")) {
								if (!documents.contains(filename)) {
									documents.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "documents");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".xlsx")) {
								if (!documents.contains(filename)) {
									documents.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "documents");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".docx")) {
								if (!documents.contains(filename)) {
									documents.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "documents");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".mpeg")) {
								if (!videos.contains(filename)) {
									videos.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "videos");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".avi")) {
								if (!videos.contains(filename)) {
									videos.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "videos");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".mkv")) {
								if (!videos.contains(filename)) {
									videos.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "videos");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".mp4")) {
								if (!videos.contains(filename)) {
									videos.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "videos");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".ogg")) {
								if (!videos.contains(filename)) {
									videos.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "videos");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".ogm")) {
								if (!videos.contains(filename)) {
									videos.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "videos");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".flv")) {
								if (!videos.contains(filename)) {
									videos.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "videos");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".m4v")) {
								if (!videos.contains(filename)) {
									videos.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "videos");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".f4v")) {
								if (!videos.contains(filename)) {
									videos.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "videos");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							else if (filename.contains(".") && filename.substring(filename.lastIndexOf('.')).equalsIgnoreCase(".ifo")) {
								if (!videos.contains(filename)) {
									videos.add(filename);
									UbiquityFile uf = new UbiquityFile(filename, "videos");
									try {
										uf.setDbID(client.getDatabase().addNewFileToDB(uf));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			}
			catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		
		//start at user home, then index
		System.getProperty("user.home");
		
		File root = new File(System.getProperty("user.home"));
		
		//try to connect to the database
		if (!client.getDatabase().isConnected()) {
			client.getDatabase().connectToDB();
		}
		
		index(root);
		
		//just for debugging purposes
		spitToFile();
		
		//export database
		try {
			client.getDatabase().exportDatabase("dbexport.dex");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//enqueue the database file 
		if (client.getMessageSender() != null) {
			Message m = new Message(MessageCode.SENDING_INDEX, System.getProperty("derby.system.home") + System.getProperty("file.separator") + "dbexport.dex");
			client.getMessageSender().enqueueMessage(m);
		}
	}
	
	private void spitToFile() {
		try {
			BufferedWriter outputStream = 
			    new BufferedWriter(new FileWriter("indextest.dex"));
			for (String s: music) {
				outputStream.write(s + System.getProperty("line.separator"));
				outputStream.flush();
			}
			for (String s: videos) {
				outputStream.write(s + System.getProperty("line.separator"));
				outputStream.flush();
			}
			for (String s: documents) {
				outputStream.write(s + System.getProperty("line.separator"));
				outputStream.flush();
			}
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}
