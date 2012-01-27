import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;


@SuppressWarnings("serial")
public class MainTabs extends JTabbedPane {

	private JPanel openWindows;
	private JPanel fileInfo;
	
	private JTextArea currentWindows;
	private JScrollPane currentWindowsScrollPane;
	
	private String selectedFileName;
	private String selectedFileSize;
	private String selectedFileLocation;
	private String selectedFileModified;
	private JLabel fileDataText;

	
	public MainTabs() {
		super();
		
		init();
	}
	
	private void init() {
		openWindows = new JPanel();
		openWindows.setLayout(new BorderLayout());
		
		fileInfo = new JPanel();
		
		currentWindows = new JTextArea();
		currentWindows.setText("No windows open");
		currentWindows.setEditable(false);
		currentWindowsScrollPane = new JScrollPane(currentWindows);
		
		openWindows.add(currentWindowsScrollPane, BorderLayout.CENTER);
		
		selectedFileName = "";
		selectedFileSize = "";
		selectedFileLocation = "";
		selectedFileModified = "";
		String text = "File Name: " + selectedFileName + "<br/><br/>File Size: " + selectedFileSize + " bytes<br/><br/>File Location: " + selectedFileLocation + "<br/><br/>Last Modified By: " + selectedFileModified;
		fileDataText = new JLabel("<html><div style=\"text-align: left;\">" + text + "</html>");
		fileInfo.add(fileDataText);
		
		this.addTab("Open Windows", openWindows);
		this.addTab("Selected File", fileInfo);
	}
	
	public void setSelectedFileInfo(String filename, String filesize, String filelocation, String filemodified) {
		this.selectedFileName = filename;
		this.selectedFileSize = filesize;
		this.selectedFileLocation = filelocation;
		this.selectedFileModified = filemodified;
		String text = "File Name: " + selectedFileName + "<br/><br/>File Size: " + selectedFileSize + " bytes<br/><br/>File Location: " + selectedFileLocation + "<br/><br/>Last Modified By: " + selectedFileModified;
		fileDataText.setText("<html><div style=\"text-align: left;\">" + text + "</html>");
		
		this.setSelectedComponent(fileInfo);
	}
	
}
