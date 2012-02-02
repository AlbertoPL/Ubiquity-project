import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


public class ProjectPanelListener implements ActionListener {

	private ScholarFrame frame;
	private JFileChooser fileChooser;
	
	public ProjectPanelListener(ScholarFrame frame) {
		this.frame = frame;
		this.fileChooser = new JFileChooser();
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
			String path = frame.getProjectPanel().getSelectedProjectPath() + ".uprj";

			if (!path.equals(frame.getCurrentProject().getSaveLocation())) {
				if (!frame.isDirty()) {
			
				    Project project = new Project(new File(path));
					frame.setCurrentProject(project);
					frame.clean();
				}
				else {
					int option = JOptionPane.showConfirmDialog(frame.getContentPane(), "Save current project?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if (option == JOptionPane.NO_OPTION) {
							Project project = new Project(new File(path));
							frame.setCurrentProject(project);
							frame.clean();
					}
					else if (option == JOptionPane.YES_OPTION){
						if (frame.getCurrentProject().getName().isEmpty()) {
							int returnVal = fileChooser.showSaveDialog(frame.getContentPane());
						    if (returnVal == JFileChooser.APPROVE_OPTION) {
						        File file = fileChooser.getSelectedFile();
						        try {
									frame.getCurrentProject().saveProject(file.getName(), file.getCanonicalPath());
									frame.setTitleString(file.getName());
									frame.clean();
									frame.getDatabase().saveProject(file.getName(), file.getCanonicalPath());
									frame.getProjectPanel().addElement(file.getName());
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
						}
						Project project = new Project(new File(path));
						frame.setCurrentProject(project);
						frame.clean();
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
