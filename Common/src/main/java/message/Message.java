package message;

import java.io.Serializable;

public abstract class Message implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int code;
	
	public Message() {
		
	}
	
	public Message(int code) {
		this.code = code;
	}
	
	/**
	 * Returns the code this message represents
	 * 
	 * @return code - the integer code that represents what this message is for
	 */
	public int getCode() {
		return code;
	}
	
}
