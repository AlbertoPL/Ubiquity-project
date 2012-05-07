package message;

public class ShareFileMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7902193109998585885L;
	private String filename;
	private String filepath;
	private String userToShareWith;
	private long filelength;
	
	public ShareFileMessage(int code, String filename, String filepath, long filelength, String userToShareWith) {
		super(code);
		this.filename = filename;
		this.filepath = filepath;
		this.userToShareWith = userToShareWith;
		this.filelength = filelength;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getFilepath() {
		return filepath;
	}
	
	public String getUserToShareWith() {
		return userToShareWith;
	}
	
	public long getFilelength() {
		return filelength;
	}
}
