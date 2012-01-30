import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



public class ProjectFile {

	private String filename;
	private String filepath;
	
	private String windowName;
	private int windowHeight;
	private int windowWidth;
	private int windowLeftPos;
	private int windowTopPos;
	private boolean windowAttached;
	
	private ProjectFile() {
		filename = "";
		filepath = "";
		windowHeight = -1;
		windowWidth = -1;
		windowLeftPos = -1;
		windowTopPos = -1;
		windowAttached = false;
	}
	
	public ProjectFile(String filename, String filepath) {
		this();
		this.filename = filename;
		this.filepath = filepath;
	}
	
	public void setWindowSettings(int windowHeight, int windowWidth, int windowLeftPos, int windowTopPos) {
		this.windowHeight = windowHeight;
		this.windowWidth = windowWidth;
		this.windowLeftPos = windowLeftPos;
		this.windowTopPos = windowTopPos;
		attachWindow();
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
	
	public double getFileSize() {
		return new File(filepath).length();
	}
	
	public Date getFileModified() {
		return new Date(new File(filepath).lastModified());
	}
	
	public String getFormattedFileModified() {
		DateFormat formatter =  new SimpleDateFormat("MM-dd-yyyy hh:MM:ss");
		return formatter.format(new File(filepath).lastModified());
	}
	
	public void setWindowName(String windowName) {
		this.windowName = windowName;
	}
	
	public String getWindowName() {
		return windowName;
	}
	
	public int getWindowHeight() {
		return windowHeight;
	}
	
	public int getWindowWidth() {
		return windowWidth;
	}
	
	public int getWindowLeftPos() {
		return windowLeftPos;
	}
	
	public int getWindowTopPos() {
		return windowTopPos;
	}
	
	public boolean isWindowAttached() {
		return windowAttached;
	}
	
	public void attachWindow() {
		this.windowAttached = true;
	}
}
