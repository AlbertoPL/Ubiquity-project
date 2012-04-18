import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


@SuppressWarnings("serial")
public class ProjectsPanel extends JPanel {

	private DefaultListModel projectList;
	private JList projectJList;
	private Font displayFont;
	private JScrollPane projectScrollPane;
	private ButtonPanel buttonPanel;
	
	private ScholarFrame parent;
	
	private String selectedProjectPath = "";
	private String selectedProjectName = "";
	
	private ProjectsPanel() {
		super();
	}
	
	public ProjectsPanel(ScholarFrame parent) {
		this();
	
		this.parent = parent;
		
		init();
	}
	
	private void init() {
		
		this.setName("Projects");
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createTitledBorder("Projects"));
		
		projectList = new DefaultListModel();
		projectJList = new JList(projectList);
		projectJList.setToolTipText("List of your projects");
		displayFont = new Font("Serif", Font.BOLD, 12);
		projectJList.setFont(displayFont);
		projectScrollPane = new JScrollPane(projectJList);
		projectScrollPane.setToolTipText("List of your projects");
		
		projectJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		projectJList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				int firstSelIx = projectJList.getSelectedIndex();
				if (firstSelIx >= 0) {
					buttonPanel.setOpenText("Open");
					buttonPanel.setRemoveText("Remove");
					buttonPanel.setShareText("Share");
					buttonPanel.setBackupText("Backup");
					buttonPanel.setOpenEnabled(true);
					buttonPanel.setRemoveEnabled(true);
					buttonPanel.setShareEnabled(true);
					buttonPanel.setBackupEnabled(true);
					
					File file = new File(parent.getProjectPath((String) projectList.get(firstSelIx)));
					
					if (file.exists()) {
						try {
							selectedProjectPath = file.getCanonicalPath();
							selectedProjectName = (String) projectList.get(firstSelIx);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else {
						System.out.println("File doesn't exist");
						selectedProjectPath = parent.getProjectPath((String) projectList.get(firstSelIx));
						selectedProjectName = (String) projectList.get(firstSelIx);
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
					parent.getTabs().setSelectedFileInfo("", "", "", "", false, "Not shared");
					selectedProjectPath = "";
					selectedProjectName = "";
				}
				parent.invalidate();
				parent.validate();
			}
			
		});
		
		buttonPanel = new ButtonPanel();
		buttonPanel.addActionListener(new ProjectPanelListener(parent));
		
		this.add(projectScrollPane);
		this.add(buttonPanel);
		
	}
	
	public void addElement(String projectname) {
		projectList.addElement(projectname);
	}
	
	public void removeSelectedProject() {
		projectList.remove(projectJList.getSelectedIndex());
		selectedProjectPath = "";
	}
	
	public String getSelectedProjectPath() {
		return selectedProjectPath;
	}
	
	public String getSelectedProjectName() {
		return selectedProjectName;
	}
}
