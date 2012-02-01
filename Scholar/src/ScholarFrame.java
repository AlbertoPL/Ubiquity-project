import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class ScholarFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BorderLayout layout;
	private JMenuBar menubar;
	private JMenu file;
	private JMenu options;
	private JMenu help;
	private JMenuItem quit;
	private JMenuItem newProject;
	private JMenuItem closeProject;
	private JMenuItem openProject;
	private JMenuItem saveProject;
	private JMenuItem about;
	private JMenuItem hide;
	
	private ProjectsPanel projectPanel;
	
	private FilesPanel filePanel;
	
	private JPanel buttonPanel;
	private FlowLayout buttonLayout;
	private JButton openAll;
	private JButton removeFile;
	private JButton openFile;
	private JButton shareFile;
	private JButton backupFile;
	private FilePanelListener allListener;
	
	private MainTabs tabs;

	private JPanel footer;
	private JLabel onlineStatus;
	
	private boolean isConnected = false;
	
	private String title = "";
	private boolean dirty = false;
	
	private Project currentProject;
	
	private Database database;
	
	private Map<String,String> projects;
	
	public ScholarFrame() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		layout = new BorderLayout();
		this.setLayout(layout);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize((int)(dim.width/1.5), (int)(dim.height/1.5));
		
		int w = this.getSize().width;
	    int h = this.getSize().height;
	    int x = (dim.width-w)/2;
	    int y = (dim.height-h)/2;
	    this.setLocation(x, y);

	    //data MUST be initialized after UI or things wont be properly initialized to display
	    //the data
	    initUI();
	    initData();
	    changeTitle();
	    
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener( new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
		        ScholarFrame frame = (ScholarFrame)e.getSource();

		        if (!frame.getCurrentProject().isSaved() || frame.isDirty()) {
					int option = JOptionPane.showConfirmDialog(frame.getContentPane(), "Save project before exiting?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if (option == JOptionPane.NO_OPTION) {
						System.exit(0);
					}
					else if (option == JOptionPane.YES_OPTION) {
						if (frame.getCurrentProject().getName().isEmpty()) {
							int returnVal = allListener.getFileChooser().showSaveDialog(frame.getContentPane());
						    if (returnVal == JFileChooser.APPROVE_OPTION) {
						        File file = allListener.getFileChooser().getSelectedFile();
						        try {
									frame.getCurrentProject().saveProject(file.getName(), file.getCanonicalPath());
									frame.setTitle("Ubiquity Scholar - " + file.getName());
									System.exit(0);
						        } catch (IOException ioe) {
									JOptionPane.showMessageDialog(frame.getContentPane(), "The current project could not be saved!", "Error saving project!", JOptionPane.ERROR_MESSAGE);
									ioe.printStackTrace();
								}
						    }
						}
						else {
							frame.getCurrentProject().saveProject(frame.getCurrentProject().getName(), frame.getCurrentProject().getSaveLocation());
							System.exit(0);
						}
					}
				}
				else {
					System.exit(0);
				}

		    }
		});
		this.setVisible(true);
	}
	
	private void initUI() {
		createMenu();
		createProjectList();
		createProjectFileList();
		createTabs();
		createFooter();
	}
	
	private void initData() {
		currentProject = new Project();
	    database = new Database();
	    database.connectToDB();
	    
	    try {
			projects = database.getProjects();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	    
	    if (projects != null) {
	    	for (String name: projects.keySet()) {
	    		projectPanel.addElement(name);
	    	}
	    }
	}
	
	private void createMenu() {
		menubar = new JMenuBar();
		file = new JMenu("File");
		file.setMnemonic('F');
		options = new JMenu("Options");
		options.setMnemonic('O');
		help = new JMenu("Help");
		help.setMnemonic('H');
		newProject = new JMenuItem("New Project");
		newProject.setMnemonic('N');
		openProject = new JMenuItem("Open Project");
		openProject.setMnemonic('O');
		saveProject = new JMenuItem("Save Project");
		saveProject.setMnemonic('S');
		closeProject = new JMenuItem("Close Project");
		closeProject.setMnemonic('C');
		quit = new JMenuItem("Quit");
		quit.setMnemonic('Q');
		about = new JMenuItem("About");
		about.setMnemonic('A');
		hide = new JMenuItem("Hide");
		hide.setMnemonic('d');
		
		menubar.add(file);
		menubar.add(options);
		menubar.add(help);
		
		file.add(newProject);
		file.addSeparator();
		file.add(openProject);
		file.add(saveProject);
		file.add(closeProject);
		file.addSeparator();
		file.add(quit);
		options.add(hide);
		help.add(about);
		
		allListener = new FilePanelListener(this);
		newProject.addActionListener(allListener);
		openProject.addActionListener(allListener);
		saveProject.addActionListener(allListener);
		closeProject.addActionListener(allListener);
		quit.addActionListener(allListener);
		hide.addActionListener(allListener);
		about.addActionListener(allListener);
		
		this.setJMenuBar(menubar);
	}
	
	private void createProjectList() {
		projectPanel = new ProjectsPanel(this);
		this.add(projectPanel, BorderLayout.WEST);
	}
	
	private void createProjectFileList() {
		
		filePanel = new FilesPanel(this);
		
	    this.add(filePanel, BorderLayout.EAST);
	}
	
	@SuppressWarnings("unused")
	private void createButtonBar() {
		buttonPanel = new JPanel();
		buttonLayout = new FlowLayout();
		openAll = new JButton("Open All");
		openAll.setMnemonic('A');
		openFile = new JButton("Open");
		openFile.setMnemonic('p');
		openFile.setEnabled(false);
		removeFile = new JButton("Remove");
		removeFile.setMnemonic('R');
		removeFile.setEnabled(false);
		backupFile = new JButton("Backup");
		backupFile.setMnemonic('B');
		backupFile.setEnabled(false);
		shareFile = new JButton("Share");
		shareFile.setMnemonic('S');
		shareFile.setEnabled(false);
		
		openAll.addActionListener(allListener);
		openFile.addActionListener(allListener);
		removeFile.addActionListener(allListener);
		backupFile.addActionListener(allListener);
		shareFile.addActionListener(allListener);
		
		buttonPanel.setLayout(buttonLayout);
		buttonPanel.add(openAll);
		buttonPanel.add(openFile);
		buttonPanel.add(removeFile);
		buttonPanel.add(backupFile);
		buttonPanel.add(shareFile);
		
		this.add(buttonPanel, BorderLayout.NORTH);
	}
	
	private void createTabs() {
		tabs = new MainTabs(this);
		
		this.add(tabs, BorderLayout.CENTER);
	}
	
	private void createFooter() {
		footer = new JPanel();
		onlineStatus = new JLabel("Offline");
		footer.add(onlineStatus);
		
		this.add(footer, BorderLayout.SOUTH);
	}
	
	public BorderLayout getLayout() {
		return layout;
	}

	public Project getCurrentProject() {
		return currentProject;
	}
	
	public void setCurrentProject(Project p) {
		currentProject = p;
		if (currentProject != null && currentProject.getProjectFiles() != null) {
			setTitleString(currentProject.getName());
			filePanel.removeAllElements();
			for (ProjectFile f: currentProject.getProjectFiles()) {
				filePanel.addElement(f.getFilePath());
			}
			changeTitle();
		}
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public void clean() {
		dirty = false;
		changeTitle();
	}
	
	public void setDirty() {
		dirty = true;
		changeTitle();
	}
	
	public void setTitleString(String title) {
		this.title = title;
	}
	
	private void changeTitle() {
		if (title.isEmpty()) {
			title = "New Project";
		}
		if (isDirty()) {
			setTitle("Ubiquity Scholar - *" + title);
		}
		else {
			setTitle("Ubiquity Scholar - " + title);
		}
		this.invalidate();
		this.validate();
	}
	
	public FilesPanel getFilePanel() {
		return filePanel;
	}
	
	public ProjectsPanel getProjectPanel() {
		return projectPanel;
	}
	
	public MainTabs getTabs() {
		return tabs;
	}
	
	public Database getDatabase() {
		return database;
	}
	
	public String getProjectPath(String projectname) {
		return projects.get(projectname);
	}
	
	public static void main(String... args) {
		new ScholarFrame();
	}
	
}
