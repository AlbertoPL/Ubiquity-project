package server;

import java.net.Socket;

import file.FileReceiver;
import file.FileSender;

public class FileHandler implements Runnable {

	private Socket s;
	private FileSender fileSender;
	private FileReceiver fileReceiver;
	private String fileReceiveDirectory;
	
	public FileHandler(Socket s, String fileReceiveDirectory) {
		this.s = s;
		this.fileReceiveDirectory = fileReceiveDirectory;
	}
	
	@Override
	public void run() {
		fileSender = new FileSender("");		
	    fileReceiver = new FileReceiver(fileReceiveDirectory);
	}

}
