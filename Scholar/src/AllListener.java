import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class AllListener implements ActionListener {

	private ScholarFrame frame;
	private JFileChooser fileChooser;
	
	public AllListener(ScholarFrame frame) {
		this.frame = frame;
		this.fileChooser = new JFileChooser();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String command = arg0.getActionCommand();
		if (command.equals("Remove")) {
			frame.removeSelectedFile();
		}
		else if (command.equals("Open")) {
			frame.openSelectedFile();
		}
		else if (command.equals("Open All")) {
			frame.openAllFiles();
		}
		else if (command.equals("Quit")) {
			if (!frame.getCurrentProject().isSaved()) {
				int option = JOptionPane.showConfirmDialog(frame.getContentPane(), "Save project before exiting?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (option == JOptionPane.NO_OPTION) {
					System.exit(0);
				}
				else if (option == JOptionPane.YES_OPTION) {
					if (frame.getCurrentProject().getName().isEmpty()) {
						int returnVal = fileChooser.showSaveDialog(frame.getContentPane());
					    if (returnVal == JFileChooser.APPROVE_OPTION) {
					        File file = fileChooser.getSelectedFile();
					        try {
								frame.getCurrentProject().saveProject(file.getName(), file.getCanonicalPath());
								frame.setTitle("Ubiquity Scholar - " + file.getName());
								System.exit(0);
					        } catch (IOException e) {
								JOptionPane.showMessageDialog(frame.getContentPane(), "The current project could not be saved!", "Error saving project!", JOptionPane.ERROR_MESSAGE);
								e.printStackTrace();
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
		else if (command.equals("Open Project")) {
			if (!frame.isDirty()) {
				int returnVal = fileChooser.showOpenDialog(frame);
			    if (returnVal == JFileChooser.APPROVE_OPTION) {
			        File file = fileChooser.getSelectedFile();
					Project project = new Project(file);
					frame.setCurrentProject(project);
					frame.clean();
			    }
			}
			else {
				int option = JOptionPane.showConfirmDialog(frame.getContentPane(), "Save current project?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (option == JOptionPane.NO_OPTION) {
					int returnVal = fileChooser.showOpenDialog(frame);
				    if (returnVal == JFileChooser.APPROVE_OPTION) {
				        File file = fileChooser.getSelectedFile();
						Project project = new Project(file);
						frame.setCurrentProject(project);
						frame.clean();
				    }
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
					        } catch (IOException e) {
								JOptionPane.showMessageDialog(frame.getContentPane(), "The current project could not be saved!", "Error saving project!", JOptionPane.ERROR_MESSAGE);
								e.printStackTrace();
							}
					    }
					}
					else {
						frame.getCurrentProject().saveProject(frame.getCurrentProject().getName(), frame.getCurrentProject().getSaveLocation());
						frame.clean();
					}
					int returnVal = fileChooser.showOpenDialog(frame);
				    if (returnVal == JFileChooser.APPROVE_OPTION) {
				        File file = fileChooser.getSelectedFile();
						Project project = new Project(file);
						frame.setCurrentProject(project);
						frame.clean();
				    }
				}
			}
		}
		else if (command.equals("Save Project")) {
			if (frame.isDirty()) {
				if (frame.getCurrentProject().getName().isEmpty()) {
					int returnVal = fileChooser.showSaveDialog(frame.getContentPane());
				    if (returnVal == JFileChooser.APPROVE_OPTION) {
				        File file = fileChooser.getSelectedFile();
				        try {
							frame.getCurrentProject().saveProject(file.getName(), file.getCanonicalPath());
							frame.setTitleString(file.getName());
							frame.clean();
				        } catch (IOException e) {
							JOptionPane.showMessageDialog(frame.getContentPane(), "The current project could not be saved!", "Error saving project!", JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
				    }
				}
				else {
					frame.getCurrentProject().saveProject(frame.getCurrentProject().getName(), frame.getCurrentProject().getSaveLocation());
					frame.clean();
				}
			}
		}
		else if (command.equals("New Project") || command.equals("Close Project")) {
			if (frame.isDirty()) {
				int option = JOptionPane.showConfirmDialog(frame.getContentPane(), "Save current project?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (option == JOptionPane.NO_OPTION) {
					Project project = new Project();
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
					        } catch (IOException e) {
								JOptionPane.showMessageDialog(frame.getContentPane(), "The current project could not be saved!", "Error saving project!", JOptionPane.ERROR_MESSAGE);
								e.printStackTrace();
							}
					    }
					}
					else {
						frame.getCurrentProject().saveProject(frame.getCurrentProject().getName(), frame.getCurrentProject().getSaveLocation());
						frame.clean();
					}
					Project project = new Project();
					frame.setCurrentProject(project);
					frame.clean();
				}
			}
			else {
				Project project = new Project();
				frame.setCurrentProject(project);
				frame.clean();
			}
		}
		else if (command.equals("About")) {
			JOptionPane.showMessageDialog(frame.getContentPane(), "Ubiquity Scholar © 2012 All rights reserved", "About Ubiquity Scholar", JOptionPane.INFORMATION_MESSAGE);
		}
		else if (command.equals("Hide")) {
			frame.setExtendedState(JFrame.ICONIFIED);
		}
	}

}
