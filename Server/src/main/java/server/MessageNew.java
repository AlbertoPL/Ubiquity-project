package server;

import java.io.Serializable;

public class MessageNew implements Serializable {
	private static final long serialVersionUID = -6621680135548538902L;
	private int code;
	private String filename;
	private long filelength;
	private byte[] payload;
	
	/**
	 * Constructor which initializes the code and payload. The protocol
	 * defaults to TCP.
	 * 
	 * @param code - the integer code representing the type of message
	 * @param payload - the data contained within this message
	 */
	public MessageNew(int code, String filename, long filelength, byte[] payload) {
		this.code = code;
		this.filename = filename;
		this.filelength = filelength;
		this.payload = payload;
	}
	
	/**
	 * Returns the code this message represents
	 * 
	 * @return code - the integer code that represents what this message is for
	 */
	public int getCode() {
		return code;
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
	
	public long getFilelength() {
		return filelength;
	}
}
