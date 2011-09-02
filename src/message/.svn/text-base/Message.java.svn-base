package message;

import java.io.Serializable;

public class Message implements Serializable {

	private int code;
	private String payload;
	private String protocol;
	
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
		protocol = "TCP"; //default is TCP
	}
	
	/**
	 * Constructor which takes the code, payload, and protocol for this message
	 * 
	 * @param code - the integer code representing the type of message
	 * @param payload - the data contained within this message
	 * @param protocol - the protocol to be used for transmission as a string
	 */
	public Message(int code, String payload, String protocol) {
		this(code, payload);
		this.protocol = protocol;
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
	
	/**
	 * Returns the string representation of the protocol to be used for 
	 * transmitting this message.
	 * 
	 * @return protocol - the protocol to be used for transmitting this message
	 */
	public String getProtocol() {
		return protocol;
	}
}
