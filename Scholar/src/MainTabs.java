import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.JButton;
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
	
	private JButton refreshOpenWindows;
	
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
		checkOpenWindows();
		
		refreshOpenWindows = new JButton("Refresh Windows");
		refreshOpenWindows.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				checkOpenWindows();
			}
			
		});
		openWindows.add(refreshOpenWindows, BorderLayout.SOUTH);
		
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
	
	private void checkOpenWindows() {
		while (windowTableModel.getRowCount()>0){
			windowTableModel.removeRow(0);
		}
		try {
	        String line;
	        Process p = Runtime.getRuntime().exec
	                ("openwindow.exe");
	        BufferedReader input =
	                new BufferedReader(new InputStreamReader(p.getInputStream()));
	        while ((line = input.readLine()) != null) {
	            //String xy = line.substring(5, line.indexOf("Win Name:") - 1);
	        	int x = Integer.parseInt(line.substring(0, line.indexOf(' ')));
	        	line = line.substring(line.indexOf(' ') + 1);
	        	int y = Integer.parseInt(line.substring(0 , line.indexOf(' ')));
	        	line = line.substring(line.indexOf(' ') + 1);
	        	int w = Integer.parseInt(line.substring(0, line.indexOf(' ')));
	        	line = line.substring(line.indexOf(' ') + 1);
	        	int h = Integer.parseInt(line.substring(0, line.indexOf(' ')));
	        	line = line.substring(line.indexOf(' ') + 1);
	            String name = line;
	        	if (x < 0 && y < 0) {
	        		line = "Minimized";
	        	}
	        	else {
	        		line = x + "," + y;
	        	}
	        	windowTableModel.addRow(new String[]{name, line, w + "," + h, null});
	        }
	        input.close();
	        p.destroy();
	    } catch (Exception err) {
	        err.printStackTrace();
	    }
	}
}
