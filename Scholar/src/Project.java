import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.gson.Gson;


public class Project {

	private String name;
	private String saveLocation;
	private List<String> projectFiles;
	private boolean saved;
	
	//default blank
	public Project() {
		projectFiles = new ArrayList<String>();
		saved = false;
		name = "";
		saveLocation = "";
	}
	
	//load
	public Project(File projectData) {
		Gson gson = new Gson();
		try {
			 
			BufferedReader br = new BufferedReader(
				new FileReader(projectData));
	 
			//convert the json string back to object
			Project project = gson.fromJson(br, Project.class);
	 
			this.saved = true;
			this.name = project.name;
			this.saveLocation = project.saveLocation;
			this.projectFiles = project.projectFiles;
	 
		} catch (IOException e) {
			try {
				JOptionPane.showMessageDialog(null, projectData.getCanonicalPath() + " is not a valid scholar project file!", "Not a valid project", JOptionPane.ERROR_MESSAGE);
			} catch (HeadlessException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, projectData.getAbsolutePath() + System.getProperty("file.separator") + projectData.getName() + " is not a valid scholar project file!", "Not a valid project", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (Exception e) {
			try {
				JOptionPane.showMessageDialog(null, projectData.getCanonicalPath() + " is not a valid scholar project file!", "Not a valid project", JOptionPane.ERROR_MESSAGE);
			} catch (HeadlessException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, projectData.getAbsolutePath() + System.getProperty("file.separator") + projectData.getName() + " is not a valid scholar project file!", "Not a valid project", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
	
	public void addProjectFile(String projectFile) {
		projectFiles.add(projectFile);
	}
	
	public List<String> getProjectFiles() {
		return projectFiles;
	}
	
	public void removeProjectFile(String projectFile) {
		projectFiles.remove(projectFile);
	}
	
	public boolean saveProject(String name, String saveLocation) {
		Gson gson = new Gson();
		
		String prevName = this.name;
		String prevSaveLocation = this.saveLocation;
		this.name = name;
		this.saveLocation = saveLocation;
		saved = true;
		
		// convert java object to JSON format,
		// and returned as JSON formatted string
		String json = gson.toJson(this);
	 
		try {
			//write converted json data to a file
			
			FileWriter writer = new FileWriter(saveLocation + ".uprj");
			writer.write(json);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
			this.name = prevName;
			this.saveLocation = prevSaveLocation;
			saved = false;
		}
		
		return saved;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSaveLocation() {
		return saveLocation;
	}
	
	public boolean isSaved() {
		return saved;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}