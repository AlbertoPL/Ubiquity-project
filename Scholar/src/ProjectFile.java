import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class ProjectFile {

	private String filename;
	private String filepath;
	private long filelength;
	
	private boolean backedUp;
	private List<String> sharedWith;
	
	
	private ProjectFile() {
		filename = "";
		filepath = "";
		filelength = 0;
		backedUp = false;
		sharedWith = new ArrayList<String>();
	}
	
	public ProjectFile(String filename, String filepath) {
		this();
		this.filename = filename;
		this.filepath = filepath;
		filelength = new File(filepath).length();
	}
	
	public void setBackupStatus(boolean backedUp) {
		this.backedUp = backedUp;
	}
	
	public void addSharedWith(String username) {
		sharedWith.add(username);
	}
	
	public void removeSharedWith(String username) {
		sharedWith.remove(username);
	}
	
	public File getFile() {
		return new File(filepath);
	}
	
	public String getFileName() {
		return filename;
	}
	
	public String getFilePath() {
		return filepath;
	}
	
	public long getFileSize() {
		return filelength;
	}
	
	public Date getFileModified() {
		return new Date(new File(filepath).lastModified());
	}
	
	public String getFormattedFileModified() {
		DateFormat formatter =  new SimpleDateFormat("MM-dd-yyyy hh:MM:ss");
		return formatter.format(new File(filepath).lastModified());
	}
	
	public boolean isBackedUp() {
		return backedUp;
	}
	
	public List<String> getSharedWith() {
		return sharedWith;
	}
}
