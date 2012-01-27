import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;


@SuppressWarnings("serial")
public class ProjectsPanel extends JPanel {

	private DefaultListModel projectList;
	private JList projectJList;
	private Font displayFont;
	private JScrollPane projectScrollPane;
	private ButtonPanel buttonPanel;
	
	public ProjectsPanel() {
		super();
	
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
		
		buttonPanel = new ButtonPanel();
		
		this.add(projectScrollPane);
		this.add(buttonPanel);
		
	}
}
