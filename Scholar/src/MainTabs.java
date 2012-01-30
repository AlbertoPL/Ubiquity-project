import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


@SuppressWarnings("serial")
public class MainTabs extends JTabbedPane {

	private JPanel openWindows;
	private JPanel fileInfo;
	
	
	private WindowTableModel windowTableModel;
	private JTable currentWindows;
	private JScrollPane currentWindowsScrollPane;
	
	private JButton refreshOpenWindows;
	private JButton attachFile;
	private JPanel windowButtonBar;
	
	private String selectedFileName;
	private String selectedFileSize;
	private String selectedFileLocation;
	private String selectedFileModified;
	private JLabel fileDataText;
	
	private String windowPosition;
	private String windowSize;
	private JLabel windowDataText;
	
	private JButton attachWindow;
	
	private ScholarFrame parent;
	
	public MainTabs(ScholarFrame parent) {
		super();
		
		this.parent = parent;
		
		init();
	}
	
	private void init() {
		openWindows = new JPanel();
		openWindows.setLayout(new BorderLayout());
		
		fileInfo = new JPanel();
		
		windowButtonBar = new JPanel();
		
		windowTableModel = new WindowTableModel();
		currentWindows = new JTable(windowTableModel);
		currentWindows.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		currentWindows.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (currentWindows.getSelectedRow() >= 0) {
					attachFile.setEnabled(true);
				}
				else {
					attachFile.setEnabled(false);
				}
			}
			
		});
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
		windowButtonBar.add(refreshOpenWindows);
		
		attachFile = new JButton("Attach Window to File");
		attachFile.setEnabled(false);
		attachFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int row = currentWindows.getSelectedRow();
				if (row >= 0) {
					JFileChooser fileChooser = new JFileChooser();
					int returnVal = fileChooser.showDialog(MainTabs.this, "Attach File");
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fileChooser.getSelectedFile();
				        String filepath = "";
						try {
							filepath = file.getCanonicalPath();
							currentWindows.getModel().setValueAt(filepath, row, 3);
							ProjectFile projectFile = new ProjectFile(file.getName(), filepath);
							projectFile.attachWindow();
							projectFile.setWindowName((String) currentWindows.getModel().getValueAt(row, 0));
							String windowPos = (String) currentWindows.getModel().getValueAt(row, 1);
							String windowSize = (String) currentWindows.getModel().getValueAt(row, 2);
							int leftPos = Integer.parseInt(windowPos.substring(0, windowPos.indexOf(',')));
							int topPos = Integer.parseInt(windowPos.substring(windowPos.indexOf(',') + 1));
							int width = Integer.parseInt(windowSize.substring(0, windowSize.indexOf(',')));
							int height = Integer.parseInt(windowSize.substring(windowSize.indexOf(',') + 1));
							projectFile.setWindowSettings(height, width, leftPos, topPos);
							parent.getFilePanel().getListModel().addElement(filepath);
							parent.getCurrentProject().addProjectFile(projectFile);
							parent.setDirty();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		windowButtonBar.add(attachFile);
		
		openWindows.add(windowButtonBar, BorderLayout.SOUTH);
		
		selectedFileName = "";
		selectedFileSize = "";
		selectedFileLocation = "";
		selectedFileModified = "";
		String text = "File Name: " + selectedFileName + "<br/><br/>File Size: " + selectedFileSize + " bytes<br/><br/>File Location: " + selectedFileLocation + "<br/><br/>Last Modified By: " + selectedFileModified + "<br/><br/><br/>";
		fileDataText = new JLabel("<html><div style=\"text-align: left;\">" + text + "</html>");
		fileInfo.add(fileDataText);
		
		String windowText = "Window Position: " + windowPosition + "<br/><br/>Window Size: " + windowSize;
		windowDataText = new JLabel("<html><div style=\"text-align: left;\">" + windowText + "</html>");
		windowDataText.setVisible(false);
		fileInfo.add(windowDataText);
		
		attachWindow = new JButton("Attach Window");
		attachWindow.setEnabled(false);
		fileInfo.add(attachWindow);
		
		this.addTab("Open Windows", openWindows);
		this.addTab("Selected File", fileInfo);
	}
	
	public void setSelectedFileInfo(String filename, String filesize, String filelocation, String filemodified, boolean windowAttachedEnabled, boolean windowAttached, String windowPosition, String windowSize) {
		this.selectedFileName = filename;
		this.selectedFileSize = filesize;
		this.selectedFileLocation = filelocation;
		this.selectedFileModified = filemodified;
		String text = "File Name: " + selectedFileName + "<br/><br/>File Size: " + selectedFileSize + " bytes<br/><br/>File Location: " + selectedFileLocation + "<br/><br/>Last Modified By: " + selectedFileModified + "<br/><br/><br/>";
		fileDataText.setText("<html><div style=\"text-align: left;\">" + text + "</html>");
		
		this.windowPosition = windowPosition;
		this.windowSize = windowSize;
		String windowText = "Window Position: " + windowPosition + "<br/><br/>Window Size: " + windowSize;
		windowDataText.setText("<html><div style=\"text-align: left;\">" + windowText + "</html>");
		
		if (windowAttached) {
			windowDataText.setVisible(true);
			attachWindow.setVisible(true);
			attachWindow.setEnabled(true);
		}
		else {
			attachWindow.setEnabled(windowAttachedEnabled);
			windowDataText.setVisible(false);
		}
		
		this.setSelectedComponent(fileInfo);
	}
	
	public WindowTableModel getOpenWindows() {
		return windowTableModel;
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
	        	windowTableModel.addRow(new Object[]{name, line, w + "," + h, ""});
	        }
	        input.close();
	        p.destroy();
	    } catch (Exception err) {
	        err.printStackTrace();
	    }
	}
}
