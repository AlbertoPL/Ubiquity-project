import file.UbiquityFileData;
import interfaces.Serviceable;
import interfaces.View;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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


public class ScholarFrame extends JFrame implements View {

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
	
	private ConnectionPanel connectionPanel;
	
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
	
	private String title = "";
	private boolean dirty = false;
	
	private Project currentProject;
	
	private Database database;
	
	private Map<String,String> projects;
	
	private Map<String, UbiquityFileData> projectFileMetadataMap;
	
	private Properties properties;
	
	private Serviceable controller;
	
	private boolean connected = false;
	
	public ScholarFrame(Serviceable controller) {
		
		this.controller = controller;
		
		properties = new Properties();
		try {
			properties.load(new FileInputStream("scholar.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
									frame.addProjectToMap(file.getName(), file.getCanonicalPath());
									if (frame.isConnected()) {
										frame.getController().backupFile(file.getName(), file.getCanonicalPath(), file.length());
									}
									System.exit(0);
						        } catch (IOException ioe) {
									JOptionPane.showMessageDialog(frame.getContentPane(), "The current project could not be saved!", "Error saving project!", JOptionPane.ERROR_MESSAGE);
									ioe.printStackTrace();
								}
						    }
						}
						else {
							frame.getCurrentProject().saveProject(frame.getCurrentProject().getName(), frame.getCurrentProject().getSaveLocation());
							if (frame.isConnected()) {
								frame.getController().backupFile(frame.getCurrentProject().getName(), frame.getCurrentProject().getSaveLocation(), new File(frame.getCurrentProject().getSaveLocation()).length());
							}
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
		createHeader();
		createProjectList();
		createProjectFileList();
		createTabs();
		createFooter();
	}
	
	private void initData() {
		currentProject = new Project();
	    database = new Database();
	    database.connectToDB();
	    projectFileMetadataMap = new HashMap<String, UbiquityFileData>();
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
	
	private void createHeader() {
		connectionPanel = new ConnectionPanel(this);
		this.add(connectionPanel, BorderLayout.NORTH);
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
			setTitleString(currentProject.getName().substring(0, currentProject.getName().lastIndexOf(".uprj")));
			filePanel.removeAllElements();
			//projectFileMetadataMap.clear();
			for (ProjectFile f: currentProject.getProjectFiles()) {
				filePanel.addElement(f.getFilePath());
				projectFileMetadataMap.put(f.getFilePath(), new UbiquityFileData(f.getFileName(), f.getFilePath(), f.getFileSize()));
			}
			changeTitle();
		}
	}
	
	public void login() {
		String userPass = LoginDialog.showDialog(this, this, "Login", "Login");
		controller.login(userPass.substring(0, userPass.indexOf(':')), userPass.substring(userPass.indexOf(':') + 1));
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
	
	public Serviceable getController() {
		return controller;
	}
	
	public String getOnlineStatus() {
		return onlineStatus.getText();
	}
	
	public void setOnlineStatus(String status) {
		onlineStatus.setText(status);
	}
	
	public void loginSuccess(boolean success, String username, String passwordHash, String message) {
		setConnected(success);
		if (success) {
			connectionPanel.setConnected(success, username);
			setOnlineStatus("Online");
			getProjectFiles();
		}
		else {
			System.out.println(message);
			setOnlineStatus("Offline");
		}
		invalidate();
		validate();
	}
	
	public void fileBackupSuccess(boolean success, String filepath) {
		if (success) {
			currentProject.getProjectFile(filepath).setBackupStatus(success);
		}
		else {
			//TODO: Make error more robust
			System.out.println("File " + filepath + " was not able to be backed up");
		}
	}
	
	public void fileShareSuccess(boolean success, String filepath, List<String> users) {
		if (success) {
			for (String u: users) {
				currentProject.getProjectFile(filepath).addSharedWith(u);
			}
		}
		else {
			//TODO: Make error more robust
			System.out.println("File " + filepath + " was not able to be shared!");
		}
	}
	
	/*public void backupProjectFile(String name, String filepath) {
		controller.backupFile(name, filepath+".uprj", new File(filepath+".uprj").length());
	}*/
	
	public void getProjectFiles() {
		controller.getFilesByType(2); //TODO: 2 is ubiquity project. It is hardcoded. Should go in some properties file
	}
	
	public void getFilesByTypeSuccess(int filetypeid, UbiquityFileData[] files) {
		System.out.println("File type id in getFilesByTypeSuccess: " + filetypeid);
		switch(filetypeid) {
		case 2: //ubiquity project files
			for(int x = 0; x < files.length; ++x) {
				System.out.println("Name: " + files[x].getFilename());
				System.out.println("File: " + files[x].getFilepath());
				String path = files[x].getFilepath();
				String name = files[x].getFilename();
				System.out.println("FILENAME: " + name);
				System.out.println("FILEPATH: " + path);
				if (!projects.containsValue(path)) {
					System.out.println("Project did not contain path: " + path);
					projects.put(name.substring(0, name.lastIndexOf(".uprj")), path);
					projectPanel.addElement(name.substring(0, name.lastIndexOf(".uprj")));
					projectPanel.invalidate();
					projectPanel.validate();
					projectFileMetadataMap.put(path, files[x]);
				}
			}
			invalidate();
			validate();
		}
	}
	
	public long getFilePathLength(String filepath) {
		return projectFileMetadataMap.get(filepath).getFilelength();
	}
	
	public void setConnected(boolean status) {
		connected = status;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public void addProjectToMap(String name, String path) {
		if (!projects.containsKey(name.substring(0, name.lastIndexOf(".uprj")))) {
			projects.put(name.substring(0, name.lastIndexOf(".uprj")), path);
		}
	}
	
	public static void main(String... args) {
	/*	Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("client.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Client c = new Client();
		c.startWithDefaults(properties);*/
		//new ScholarFrame();
		//Thread t = new Thread(c);
		//t.start();
	}
	
}
