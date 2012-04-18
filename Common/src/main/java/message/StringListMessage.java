package message;

public class StringListMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = 139019888969456604L;
	private String[] list;
	private int metacode;
	
	public StringListMessage() {
		super();
	}
	
	public StringListMessage(int code, int metacode, String[] list) {
		super(code);
		this.metacode = metacode;
		this.list = list;
	}
	
	public String[] getList() {
		return list;
	}
	
	public int getMetacode() {
		return metacode;
	}
}
