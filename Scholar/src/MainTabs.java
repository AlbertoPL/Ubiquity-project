import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;


@SuppressWarnings("serial")
public class MainTabs extends JTabbedPane {

	private JPanel fileInfo;
	
	private String selectedFileName;
	private String selectedFileSize;
	private String selectedFileLocation;
	private String selectedFileModified;
	private JLabel fileDataText;
	
	private String backupStatus;
	private String shareStatus;
	private JLabel fileStatus;
	
	private ScholarFrame parent;
	
	public MainTabs(ScholarFrame parent) {
		super();
		
		this.parent = parent;
		
		init();
	}
	
	private void init() {
		
		fileInfo = new JPanel();
		
		selectedFileName = "";
		selectedFileSize = "";
		selectedFileLocation = "";
		selectedFileModified = "";
		String text = "File Name: " + selectedFileName + "<br/><br/>File Size: " + selectedFileSize + " bytes<br/><br/>File Location: " + selectedFileLocation + "<br/><br/>Last Modified By: " + selectedFileModified + "<br/><br/><br/>";
		fileDataText = new JLabel("<html><div style=\"text-align: left;\">" + text + "</html>");
		fileInfo.add(fileDataText);

		this.addTab("Selected File", fileInfo);
	}
	
	public void setSelectedFileInfo(String filename, String filesize, String filelocation, String filemodified, boolean backupStatus, String shareStatus) {
		this.selectedFileName = filename;
		this.selectedFileSize = filesize;
		this.selectedFileLocation = filelocation;
		this.selectedFileModified = filemodified;
		String text = "File Name: " + selectedFileName + "<br/><br/>File Size: " + selectedFileSize + " bytes<br/><br/>File Location: " + selectedFileLocation + "<br/><br/>Last Modified By: " + selectedFileModified + "<br/><br/><br/>";
		fileDataText.setText("<html><div style=\"text-align: left;\">" + text + "</html>");
		
		this.setSelectedComponent(fileInfo);
	}
}
