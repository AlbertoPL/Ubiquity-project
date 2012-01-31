import java.awt.Desktop;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


@SuppressWarnings("serial")
public class FilesPanel extends JPanel {

	private DefaultListModel fileList;
	private JList fileJList;
	private JScrollPane fileScrollPane;
	private ScholarFrame parent;
	private Font displayFont;
	
	private ButtonPanel buttonPanel;
	
	@SuppressWarnings("unused")
	private FileDrop fileDrop;
	
	private FilesPanel() {
		super();
	}
	
	public FilesPanel(ScholarFrame parent) {
		this();
		
		this.parent = parent;
		init();
	}
	
	private void init() {
		this.setName("Files");
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createTitledBorder("Files"));
		
		fileList = new DefaultListModel();
		fileJList = new JList(fileList);
		displayFont = new Font("Serif", Font.BOLD, 12);
		fileJList.setFont(displayFont);
		fileScrollPane = new JScrollPane(fileJList);
		
		fileDrop = new FileDrop( System.out, fileScrollPane, new FileDrop.Listener() {   
			public void filesDropped( java.io.File[] files ) {
				for( int i = 0; i < files.length; i++ ) {   
					try {
						fileList.addElement(files[i].getCanonicalPath());
						parent.getCurrentProject().addProjectFile(files[i].getName(), files[i].getCanonicalPath());
						if (!parent.isDirty()) {
							parent.setDirty();
						}
					}
					catch( java.io.IOException e ) {
						e.printStackTrace();
					}
				}
			}
		});
		
		fileJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		fileJList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				int firstSelIx = fileJList.getSelectedIndex();
				if (firstSelIx >= 0) {
					buttonPanel.setOpenText("Open");
					buttonPanel.setRemoveText("Remove");
					buttonPanel.setShareText("Share");
					buttonPanel.setBackupText("Backup");
					buttonPanel.setOpenEnabled(true);
					buttonPanel.setRemoveEnabled(true);
					buttonPanel.setShareEnabled(true);
					buttonPanel.setBackupEnabled(true);
					
					String filename = (String) fileList.get(firstSelIx);
					ProjectFile file = parent.getCurrentProject().getProjectFile(filename);
					//File file = new File(filename);
					
					Date lastmodified = file.getFileModified();
					DateFormat formatter =  new SimpleDateFormat("dd-MM-yyyy hh-MM-ss");
					String formattedDate = formatter.format(lastmodified);
					
					if (fileJList.getSelectedIndices().length > 1) {
						parent.getTabs().setSelectedFileInfo("Multiple files selected", "", "", "", false, false, "", "");
					}
					else {
						if (file.getWindowLeftPos() < 0 && file.getWindowTopPos() < 0) {
							parent.getTabs().setSelectedFileInfo(file.getFileName(), String.valueOf(file.getFileSize()), file.getFilePath(), formattedDate, true, file.isWindowAttached(), "Minimized", file.getWindowWidth() +"," + file.getWindowHeight());
						}
						else {
							parent.getTabs().setSelectedFileInfo(file.getFileName(), String.valueOf(file.getFileSize()), file.getFilePath(), formattedDate, true, file.isWindowAttached(), file.getWindowLeftPos() + "," + file.getWindowTopPos(), file.getWindowWidth() +"," + file.getWindowHeight());
						}
					}
				}
				else {
					buttonPanel.setOpenText("Open All");
					buttonPanel.setRemoveText("Remove");
					buttonPanel.setShareText("Share");
					buttonPanel.setBackupText("Backup");
					buttonPanel.setRemoveEnabled(false);
					buttonPanel.setShareEnabled(false);
					buttonPanel.setBackupEnabled(false);
					parent.getTabs().setSelectedFileInfo("", "", "", "", false, false, "", "");
				}
				parent.invalidate();
				parent.validate();
			}
			
		});
		
		buttonPanel = new ButtonPanel();
		buttonPanel.setOpenText("Open All");
		buttonPanel.setOpenEnabled(true);
		buttonPanel.addActionListener(new FilePanelListener(parent));
		
		this.add(fileScrollPane);
		this.add(buttonPanel);
	}
	
	public DefaultListModel getListModel() {
		return fileList;
	}
	
	public void removeSelectedFile() {
		parent.getCurrentProject().removeProjectFile((String) fileList.get(fileJList.getSelectedIndex()));
		fileList.remove(fileJList.getSelectedIndex());
		fileJList.invalidate();
		fileJList.validate();
		parent.setDirty();
	}
	
	public void openSelectedFile() {
		for (Integer i: fileJList.getSelectedIndices()) {
			try {
				File f = new File((String) fileList.get(i));
				Desktop.getDesktop().open( f);
				ProjectFile pf = parent.getCurrentProject().getProjectFile(f.getCanonicalPath());
				String windowName = pf.getWindowName();
				int height = pf.getWindowHeight();
				int width = pf.getWindowWidth();
				int leftPos = pf.getWindowLeftPos();
				int topPos = pf.getWindowTopPos();
				
				String foundWindow = "";
				do {
					Process p = Runtime.getRuntime().exec
			                ("openwindow.exe");
					String line;
					BufferedReader input =
			                new BufferedReader(new InputStreamReader(p.getInputStream()));
			        while ((line = input.readLine()) != null) {
			            //String xy = line.substring(5, line.indexOf("Win Name:") - 1);
			        	line = line.substring(line.indexOf(' ') + 1);
			        	line = line.substring(line.indexOf(' ') + 1);
			        	line = line.substring(line.indexOf(' ') + 1);
			        	line = line.substring(line.indexOf(' ') + 1);
			            String name = line;
			            if (name.equals(windowName)) {
			            	foundWindow = name;
			            	break;
			            }
			        }
			        input.close();
			        p.destroy();
				}
				while (foundWindow.isEmpty());
				@SuppressWarnings("unused")
				Process p = Runtime.getRuntime().exec
		                ("openwindow.exe " + "\"" + windowName + "\"" + " " + leftPos + " " + topPos + " " + width + " " + height);
			}
			catch (IOException e) {
				JOptionPane.showMessageDialog(parent.getContentPane(), "No default program to open " + (String) fileList.get(fileJList.getSelectedIndex()) + " exists!", "Can't open file!", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}
	
	public void openAllFiles() {
		for (Object file: fileList.toArray()) {
			if (file instanceof String) {
				try {
					File f = new File((String) file);
						Desktop.getDesktop().open( f );
						ProjectFile pf = parent.getCurrentProject().getProjectFile(f.getCanonicalPath());
						String windowName = pf.getWindowName();
						int height = pf.getWindowHeight();
						int width = pf.getWindowWidth();
						int leftPos = pf.getWindowLeftPos();
						int topPos = pf.getWindowTopPos();
						
						String foundWindow = "";
						do {
							Process p = Runtime.getRuntime().exec
					                ("openwindow.exe");
							String line;
							BufferedReader input =
					                new BufferedReader(new InputStreamReader(p.getInputStream()));
					        while ((line = input.readLine()) != null) {
					            //String xy = line.substring(5, line.indexOf("Win Name:") - 1);
					        	line = line.substring(line.indexOf(' ') + 1);
					        	line = line.substring(line.indexOf(' ') + 1);
					        	line = line.substring(line.indexOf(' ') + 1);
					        	line = line.substring(line.indexOf(' ') + 1);
					            String name = line;
					            if (name.equals(windowName)) {
					            	foundWindow = name;
					            	break;
					            }
					        }
					        input.close();
					        p.destroy();
						}
						while (!foundWindow.isEmpty());
						
						@SuppressWarnings("unused")
						Process p = Runtime.getRuntime().exec
				                ("openwindow.exe " + "\"" + windowName + "\"" + " " + leftPos + " " + topPos + " " + width + " " + height);
				}
				catch (IOException e) {
					JOptionPane.showMessageDialog(parent.getContentPane(), "No default program to open " + (String) fileList.get(fileJList.getSelectedIndex()) + " exists!", "Can't open file!", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		}
	}
	
	public void removeAllElements() {
		fileList.removeAllElements();
	}
	
	public void addElement(String file) {
		fileList.addElement(file);
	}
}
