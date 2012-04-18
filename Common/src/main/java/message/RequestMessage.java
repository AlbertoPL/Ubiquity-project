package message;

public class RequestMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3455186213348301045L;
	private int userid;
	private int metacode;
	
	public RequestMessage() {
		super();
	}
	
	public RequestMessage(int code, int userid, int metacode) {
		super(code);
		this.userid = userid;
		this.metacode = metacode;
	}
	
	public int getUserid() {
		return userid;
	}
	
	public int getMetacode() {
		return metacode;
	}
	
}
