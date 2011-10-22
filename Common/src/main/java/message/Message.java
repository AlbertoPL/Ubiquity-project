package message;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = -6621680135548538902L;
	private int code;
	private String payload;
	
	/**
	 * Constructor which initializes the code and payload. The protocol
	 * defaults to TCP.
	 * 
	 * @param code - the integer code representing the type of message
	 * @param payload - the data contained within this message
	 */
	public Message(int code, String payload) {
		this.code = code;
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
	public String getPayload() {
		return payload;
	}
}
