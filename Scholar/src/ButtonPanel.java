import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class ButtonPanel extends JPanel {
	
	private JButton open;
	private JButton remove;
	private JButton backup;
	private JButton share;
	
	public ButtonPanel() {
		super();
		
		init();
	}
	
	private void init() {
		this.setLayout(new FlowLayout());
		open = new JButton("Open");
		open.setEnabled(false);
		remove = new JButton("Remove");
		remove.setEnabled(false);
		backup = new JButton("Backup");
		backup.setEnabled(false);
		share = new JButton("Share");
		share.setEnabled(false);
	
		add(open);
		add(remove);
		add(backup);
		add(share);
	}
	
	public void addActionListener(ActionListener listener) {
		open.addActionListener(listener);
		remove.addActionListener(listener);
		backup.addActionListener(listener);
		share.addActionListener(listener);
	}
	
	public void setOpenText(String newText) {
		open.setText(newText);
	}
	
	public void setRemoveText(String newText) {
		remove.setText(newText);
	}
	
	public void setBackupText(String newText) {
		backup.setText(newText);
	}
	
	public void setShareText(String newText) {
		share.setText(newText);
	}
	
	public void setOpenEnabled(boolean value) {
		open.setEnabled(value);
	}
	
	public void setRemoveEnabled(boolean value) {
		remove.setEnabled(value);
	}
	
	public void setBackupEnabled(boolean value) {
		backup.setEnabled(value);
	}
	
	public void setShareEnabled(boolean value) {
		share.setEnabled(value);
	}
}
