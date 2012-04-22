import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class ConnectionPanel extends JPanel {

	private JButton connect;
	private JLabel connectionStatus;
	
	
	public ConnectionPanel(final ScholarFrame parent) {
		
		connect = new JButton("Connect");
		connect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (connect.getText().equalsIgnoreCase("Connect")) {
					connect.setEnabled(false);
					parent.setOnlineStatus("Connecting...");
					int timer = 5;
					int numberofTries = 3;
					parent.login();
					/*while (!parent.getController().connect() && numberofTries > 0) {
						int originalTimer = timer;
						while (timer > 0) {
							parent.setOnlineStatus("Connect failed: Trying again in " + timer);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							timer--;
						}
						numberofTries--;
						timer = originalTimer*2;
					}
					if (numberofTries <= 0) {
						parent.setOnlineStatus("Offline");
						System.out.println("Connection failed after 3 tries");
						connect.setEnabled(true);
						//TODO: Better notification
					}*/
					//else {
					//	connect.setEnabled(false);
					//}
				}
				else if (connect.getText().equalsIgnoreCase("Disconnect")) {
					connect.setText("Connect");
					parent.getController().disconnect();
					parent.setOnlineStatus("Offline");
					parent.setConnected(false);
					connectionStatus.setText("Not Connected");
				}
			}
		});
		connectionStatus = new JLabel("Not connected");
		this.add(connect);
		this.add(connectionStatus);
	}
	
	public void setConnected(boolean connected, String username) {
		System.out.println("Connected = " + connected);
		if (connected) {
			connect.setText("Disconnect");
			connectionStatus.setText("Connected as " + username);
		}
		else {
			disconnected();
		}
		connect.setEnabled(true);
	}
	
	public void disconnected() {
		connect.setText("Connect");
	}
	
	
	
}
