import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;


public class ProjectPanelListener implements ActionListener {

	private ScholarFrame frame;
	private JFileChooser fileChooser;
	
	public ProjectPanelListener(ScholarFrame frame) {
		this.frame = frame;
		this.fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Ubiquity Projects", "uprj");
		fileChooser.setFileFilter(filter);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String command = arg0.getActionCommand();
		if (command.equals("Remove")) {
			int option = JOptionPane.showConfirmDialog(frame.getContentPane(), "Are you sure you want to delete this project?", "Delete Project", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.YES_OPTION) {
				try {
					frame.getDatabase().deleteProject(frame.getProjectPanel().getSelectedProjectPath());
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if (frame.getProjectPanel().getSelectedProjectPath().equals(frame.getCurrentProject().getSaveLocation())) {
					Project project = new Project();
					frame.setCurrentProject(project);
					frame.clean();
				}
				File f = new File(frame.getProjectPanel().getSelectedProjectPath());
				f.delete();
				frame.getProjectPanel().removeSelectedProject();
			}
		}
		else if (command.equals("Open")) {
			String path = frame.getProjectPanel().getSelectedProjectPath();
			System.out.println("PATH BEING OPENED: " + path);
			if (!path.equals(frame.getCurrentProject().getSaveLocation())) {
				if (!frame.isDirty()) {
					File f = new File(path);
					if (f.exists()) {
						//Create the file, then 
					    Project project = new Project(f);
						frame.setCurrentProject(project);
						frame.clean();
					}
					else if (frame.isConnected()){
						frame.getController().getRemoteFile(path);
						
						do {
							f = new File(frame.getController().getRemoteFileStore() + frame.getProjectPanel().getSelectedProjectName() + ".uprj");
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						while (f.length() < frame.getFilePathLength(path));
						Project project = new Project(f);
						frame.setCurrentProject(project);
						frame.clean();
					}
					else {
						System.out.println("File doesn't exist, but can't be retrieved because we're not connected");
					}
				}
				else {
					int option = JOptionPane.showConfirmDialog(frame.getContentPane(), "Save current project?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if (option == JOptionPane.NO_OPTION) {
						File f = new File(path);
						if (f.exists()) {
							//Create the file, then 
						    Project project = new Project(f);
							frame.setCurrentProject(project);
							frame.clean();
						}
						else if (frame.isConnected()){
							frame.getController().getRemoteFile(path);
							f = new File(frame.getController().getRemoteFileStore() + frame.getProjectPanel().getSelectedProjectName() + ".uprj");
							while (f.length() < frame.getFilePathLength(path)) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							Project project = new Project(f);
							frame.setCurrentProject(project);
							frame.clean();
						}
						else {
							System.out.println("File doesn't exist, but can't be retrieved because we're not connected");
						}
					}
					else if (option == JOptionPane.YES_OPTION){
						if (frame.getCurrentProject().getName().isEmpty()) {
							int returnVal = fileChooser.showSaveDialog(frame.getContentPane());
						    if (returnVal == JFileChooser.APPROVE_OPTION) {
						        File file = fileChooser.getSelectedFile();
						        try {
									frame.getCurrentProject().saveProject(file.getName(), file.getCanonicalPath());
									frame.setTitleString(file.getName().substring(0, file.getName().lastIndexOf(".uprj")));
									frame.clean();
									frame.getDatabase().saveProject(file.getName(), file.getCanonicalPath());
									frame.getProjectPanel().addElement(file.getName().substring(0, file.getName().lastIndexOf(".uprj")));
									frame.addProjectToMap(file.getName(), file.getCanonicalPath());
									if (frame.isConnected()) {
										frame.getController().backupFile(file.getName(), file.getCanonicalPath(), file.length());
									}
						        } catch (IOException e) {
									JOptionPane.showMessageDialog(frame.getContentPane(), "The current project could not be saved!", "Error saving project!", JOptionPane.ERROR_MESSAGE);
									e.printStackTrace();
								} catch (SQLException e) {
									e.printStackTrace();
								}
						    }
						}
						else {
							frame.getCurrentProject().saveProject(frame.getCurrentProject().getName(), frame.getCurrentProject().getSaveLocation());
							frame.clean();
							if (frame.isConnected()) {
								frame.getController().backupFile(frame.getCurrentProject().getName(), frame.getCurrentProject().getSaveLocation(), new File(frame.getCurrentProject().getSaveLocation()).length());
							}
						}
						File f = new File(path);
						if (f.exists()) {
							//Create the file, then 
						    Project project = new Project(f);
							frame.setCurrentProject(project);
							frame.clean();
						}
						else if (frame.isConnected()){
							frame.getController().getRemoteFile(path);
							f = new File(frame.getController().getRemoteFileStore() + frame.getProjectPanel().getSelectedProjectName() + ".uprj");
							while (f.length() < frame.getFilePathLength(path)) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							Project project = new Project(f);
							frame.setCurrentProject(project);
							frame.clean();
						}
						else {
							System.out.println("File doesn't exist, but can't be retrieved because we're not connected");
						}
					}
				}
			}
		}
		else if (command.equals("Open All")) {
			//frame.getFilePanel().openAllFiles();
		}
	}

	public JFileChooser getFileChooser() {
		return fileChooser;
	}
	
}
