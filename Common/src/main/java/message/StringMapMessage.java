package message;

import java.util.concurrent.ConcurrentMap;

import file.UbiquityFileData;

public class StringMapMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6573920293060527752L;

	private UbiquityFileData[] filedata;
	private int metacode;
	
	public StringMapMessage() {
		super();
	}
	
	public StringMapMessage(int code, int metacode, UbiquityFileData[] filedata) {
		super(code);
		this.metacode = metacode;
		this.filedata = filedata;
	}
	
	public UbiquityFileData[] getFileData() {
		return filedata;
	}
	
	public int getMetacode() {
		return metacode;
	}
	
}
