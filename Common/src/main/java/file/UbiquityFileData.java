package file;

import java.io.Serializable;

public class UbiquityFileData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8381832798289207139L;
	private long filelength;
	private String filename;
	private String filepath;
	
	public UbiquityFileData() {
		filelength = 0;
		filename = "";
		filepath = "";
	}
	
	public UbiquityFileData(String filename, String filepath, long filelength) {
		this.filename = filename;
		this.filepath = filepath;
		this.filelength = filelength;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getFilepath() {
		return filepath;
	}
	
	public long getFilelength() {
		return filelength;
	}
	
	
}
