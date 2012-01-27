import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;


@SuppressWarnings("serial")
public class MainTabs extends JTabbedPane {

	private JPanel openWindows;
	private JPanel fileInfo;
	
	private WindowTableModel windowTableModel;
	private JTable currentWindows;
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
		
		windowTableModel = new WindowTableModel();
		currentWindows = new JTable(windowTableModel);
		currentWindowsScrollPane = new JScrollPane(currentWindows);
		
		openWindows.add(currentWindowsScrollPane, BorderLayout.CENTER);
		try {
        String line;
        Process p = Runtime.getRuntime().exec
                ("openwindow.exe");
        BufferedReader input =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
            String xy = line.substring(5, line.indexOf("Win Name:") - 1);
        	int x = Integer.parseInt(xy.substring(0, xy.indexOf(',')));
        	int y = Integer.parseInt(xy.substring(xy.indexOf(',') + 1));
            String name = line.substring(line.indexOf("Win Name:") + 10);
        	if (x < 0 && y < 0) {
        		xy = "Minimized";
        	}
        	windowTableModel.addRow(new String[]{name, xy, null});
        }
        input.close();
        p.destroy();
    } catch (Exception err) {
        err.printStackTrace();
    }
		
		
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
