package message;


public class FileMessage extends Message  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3530216227410845168L;
	private String filename;
	private String filepath;
	private long filelength;
	private byte[] payload;
	
	/**
	 * Constructor which initializes the code and payload. The protocol
	 * defaults to TCP.
	 * 
	 * @param code - the integer code representing the type of message
	 * @param payload - the data contained within this message
	 */
	public FileMessage(int code, String filename, String filepath, long filelength, byte[] payload) {
		super(code);
		this.filename = filename;
		this.filepath = filepath;
		this.filelength = filelength;
		this.payload = payload;
	}
	

	
	/**
	 * Returns the data contained within this message
	 * 
	 * @return payload - the data contained within this message
	 */
	public byte[] getPayload() {
		return payload;
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
