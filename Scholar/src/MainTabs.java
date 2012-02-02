import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
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
		
		fileInfo = new JPanel();
		
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
		attachWindow.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (attachWindow.getText().equals("Attach Window")) {
					String selectedWindow = TableDialog.showDialog(
                            parent,
                            attachWindow,
                            "Current Open Windows:",
                            "Choose window");
					if (!selectedWindow.isEmpty()) {
						ProjectFile file = parent.getCurrentProject().getProjectFile(selectedFileLocation);
						file.attachWindow();
						file.setWindowName(selectedWindow.substring(0, selectedWindow.indexOf('\n')));
						selectedWindow = selectedWindow.substring(selectedWindow.indexOf('\n') + 1);
						setSelectedFileInfo(selectedFileName, selectedFileSize, selectedFileLocation, selectedFileModified, true, true, selectedWindow.substring(0, selectedWindow.indexOf('\n')), selectedWindow.substring(selectedWindow.indexOf('\n') + 1));
						
						int width = -1;
						int height = -1;
						int leftPos = -1;
						int topPos = -1;
						if (!selectedWindow.substring(0, selectedWindow.indexOf('\n')).equalsIgnoreCase("minimized")) {
							leftPos = Integer.parseInt(selectedWindow.substring(0, selectedWindow.indexOf(',')));
							topPos = Integer.parseInt(selectedWindow.substring(selectedWindow.indexOf(',') + 1, selectedWindow.indexOf('\n')));
							selectedWindow = selectedWindow.substring(selectedWindow.indexOf('\n') + 1);
							width = Integer.parseInt(selectedWindow.substring(0, selectedWindow.indexOf(',')));
							height = Integer.parseInt(selectedWindow.substring(selectedWindow.indexOf(',') + 1));
						}
						else {
							selectedWindow = selectedWindow.substring(selectedWindow.indexOf('\n') + 1);
							width = Integer.parseInt(selectedWindow.substring(0, selectedWindow.indexOf(',')));
							height = Integer.parseInt(selectedWindow.substring(selectedWindow.indexOf(',') + 1));
						}
						file.setWindowSettings(height, width, leftPos, topPos);

						MainTabs.this.invalidate();
						MainTabs.this.validate();
						MainTabs.this.parent.setDirty();
					}
				}
				else {
					setSelectedFileInfo(selectedFileName, selectedFileSize, selectedFileLocation, selectedFileModified, true, false, "", "");
					MainTabs.this.parent.getCurrentProject().getProjectFile(selectedFileLocation).detachWindow();
					MainTabs.this.parent.setDirty();
				}
			}
			
		});
		attachWindow.setEnabled(false);
		fileInfo.add(attachWindow);

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
			attachWindow.setText("Detach Window");
		}
		else {
			attachWindow.setEnabled(windowAttachedEnabled);
			attachWindow.setText("Attach Window");
			windowDataText.setVisible(false);
		}
		
		this.setSelectedComponent(fileInfo);
	}
}
