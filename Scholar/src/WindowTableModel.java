import javax.swing.table.DefaultTableModel;


@SuppressWarnings("serial")
public class WindowTableModel extends DefaultTableModel {

	public WindowTableModel() {
		super();
		init();
	}
	
	private void init() {
		this.addColumn("Window Name");
		this.addColumn("Window Position (X, Y)");
		this.addColumn("Window Size");
	}
	
	@Override
	public boolean isCellEditable(int x, int y) {
		return false;
	}
}
