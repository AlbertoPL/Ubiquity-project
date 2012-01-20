import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
	
	private DefaultListModel fileList;
	private JList fileJList;
	private JScrollPane fileScrollPane;
	
	@SuppressWarnings("unused")
	private FileDrop fileDrop;
	
	private JPanel buttonPanel;
	private FlowLayout buttonLayout;
	private JButton openAll;
	private JButton removeFile;
	private JButton openFile;
	private JButton shareFile;
	private JButton backupFile;
	
	private JLabel fileDataText;
	private String selectedFileName;
	private String selectedFileSize;
	private String selectedFileLocation;
	private String selectedFileModified;
	
	private JTextArea notes;
	private JScrollPane notesScrollPane;
	
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
		
	    init();
	    
		this.setTitle("Ubiquity Scholar");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	private void init() {
		createMenu();
		createProjectFileList();
		createButtonBar();
		createNotesArea();
		createMetadata();
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
		
		this.setJMenuBar(menubar);
	}
	
	private void createProjectFileList() {
		fileList = new DefaultListModel();
		fileJList = new JList(fileList);
		fileJList.setVisibleRowCount(this.getHeight()/20);
		Font displayFont = new Font("Serif", Font.BOLD, 12);
		fileJList.setFont(displayFont);
		fileScrollPane = new JScrollPane(fileJList);
		
		fileDrop = new FileDrop( System.out, fileScrollPane, /*dragBorder,*/ new FileDrop.Listener() {   
			public void filesDropped( java.io.File[] files ) {
				for( int i = 0; i < files.length; i++ ) {   
					try {
						fileList.addElement(files[i].getCanonicalPath() + "\n" );
					}   // end try
					catch( java.io.IOException e ) {
						e.printStackTrace();
					}
				}   // end for: through each dropped file
			}   // end filesDropped
		}); // end FileDrop.Listener
		
	    this.add(fileScrollPane, BorderLayout.WEST);
	}
	
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
		
		buttonPanel.setLayout(buttonLayout);
		buttonPanel.add(openAll);
		buttonPanel.add(openFile);
		buttonPanel.add(removeFile);
		buttonPanel.add(backupFile);
		buttonPanel.add(shareFile);
		
		this.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	private void createNotesArea() {
		notes = new JTextArea();
		notes.setEditable(false);
		notesScrollPane = new JScrollPane(notes);
		
		this.add(notesScrollPane, BorderLayout.CENTER);
	}
	
	private void createMetadata() {
		selectedFileName = "";
		selectedFileSize = "";
		selectedFileLocation = "";
		selectedFileModified = "";
		String text = "File Name: " + selectedFileName + "<br/><br/>File Size: " + selectedFileSize + " bytes<br/><br/>File Location: " + selectedFileLocation + "<br/><br/>Last Modified By: " + selectedFileModified;
		fileDataText = new JLabel("<html><div style=\"text-align: left;\">" + text + "</html>");
		
		this.add(fileDataText, BorderLayout.EAST);
	}
	
	public BorderLayout getLayout() {
		return layout;
	}

	public static void main(String... args) {
		new ScholarFrame();
	}
	
}
