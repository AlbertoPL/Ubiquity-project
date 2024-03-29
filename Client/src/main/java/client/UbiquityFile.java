package client;

import java.io.File;

@SuppressWarnings("serial")
public class UbiquityFile extends File {

	private String filetype;
	private int dbID;
	
	public UbiquityFile(String file) {
		super(file);
		filetype = "";
		dbID = -1; //this means its not in the database
	}
	
	public UbiquityFile(String file, String filetype) {
		super(file);
		this.filetype = filetype;
	}
	
	public void setDbID(int dbID) {
		this.dbID = dbID;
	}
	
	public String getFileType() {
		return filetype;
	}
	
	public int getDbID() {
		return dbID;
	}
}
