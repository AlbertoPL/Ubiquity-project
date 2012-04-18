import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;


@SuppressWarnings("serial")
public class FileDownloadPanel extends JPanel {
	
	private JLabel downloadLabel;
	private JProgressBar downloadProgressBar;
	
	public FileDownloadPanel() {
		super();
		
		init();
	}
	
	private void init() {
		this.setLayout(new GridLayout(2,1));
		downloadLabel = new JLabel("Download Progress");
		downloadProgressBar = new JProgressBar();
		downloadProgressBar.setMinimum(0);
		downloadProgressBar.setValue(0);
		downloadProgressBar.setString("N/A");
		downloadProgressBar.setStringPainted(true);
		downloadProgressBar.setIndeterminate(true);
		
		add(downloadLabel);
		add(downloadProgressBar);
	}
	
	public void setFileDownloadString(String filepath) {
		downloadLabel.setText("Downloading " + filepath);
	}
	
	public void setProgressBarMax(long filelength) {
		downloadProgressBar.setIndeterminate(false);
		downloadProgressBar.setMaximum((int) filelength);
		downloadProgressBar.setString(null);

	}
	
	public void updateProgressBar(long currentFileLength) {
		downloadProgressBar.setValue((int) (currentFileLength));
		//downloadProgressBar.setString((int)(currentFileLength / downloadProgressBar.getMaximum()*100) + "%");
	}
	
	public void resetProgressBar() {
		downloadProgressBar.setValue(downloadProgressBar.getMinimum());
		downloadProgressBar.setString("N/A");
		downloadLabel.setText("Download Progress");
		downloadProgressBar.setIndeterminate(true);

	}
}
