package com.ubiquity.webubiquity.window;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ListWindow extends Window {

	private MenuBar menu;
	private MenuItem file;
	private MenuItem options;
	private MenuItem help;
	
	private VerticalLayout mainLayout;
	private VerticalLayout header;
	private HorizontalLayout body;
	private HorizontalSplitPanel splitter;
	private VerticalLayout fileInfoPanel;
	private Tree deviceTree;
	private Table fileTable;
	private VerticalLayout footer;
	
	private final int SPLIT_POS = 20;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2575708029678884515L;

	public ListWindow() {
		init();
	}
	
	public ListWindow(String title) {
		super(title);
		init();
	}
	
	public Table getTable() {
		return fileTable;
	}
	
	private void init() {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		
		initMenu();
		initHeader();
		initBody();
		initFooter();
		
		mainLayout.addComponent(menu);
		mainLayout.addComponent(header);
		mainLayout.addComponent(body);
		mainLayout.addComponent(footer);
		
		mainLayout.setExpandRatio(menu, 5);
		mainLayout.setExpandRatio(header, 10);
		mainLayout.setExpandRatio(body, 75);
		mainLayout.setExpandRatio(footer, 10);
		
		this.setContent(mainLayout);
	}
	
	private void initMenu() {
		menu = new MenuBar();
		menu.setSizeUndefined();
		
		file = menu.addItem("File", null);
		file.addItem("Log Out", menuCommand);
		options = menu.addItem("Options", null);
		help = menu.addItem("Help", null);
	}
	
	private void initHeader() {
		header = new VerticalLayout();
		header.setSizeFull();
		//logo and such perhaps, or maybe the menu should go into the header.
	}
	
	private void initBody() {
		body = new HorizontalLayout();
		body.setSizeFull();
		
		splitter = new HorizontalSplitPanel();
		splitter.setSizeFull();
		splitter.setLocked(true);
		splitter.setSplitPosition(SPLIT_POS, Sizeable.UNITS_PERCENTAGE);

		fileInfoPanel = new VerticalLayout();
		fileInfoPanel.setSizeFull();
		
		deviceTree = new Tree("Devices");
		deviceTree.setImmediate(true);
		//TODO: Filling out the tree requires figuring out which devices are registered to the user
		
		//TODO: Fix this hacked mess
		fileInfoPanel.addComponent(deviceTree);
		fileInfoPanel.addComponent(new Label("<hr />",Label.CONTENT_XHTML)); 
		fileInfoPanel.addComponent(new Label("File Name: "));
		fileInfoPanel.addComponent(new Label());
		fileInfoPanel.addComponent(new Label("File Location: "));
		fileInfoPanel.addComponent(new Label());
		fileInfoPanel.addComponent(new Label("File Type: "));
		fileInfoPanel.addComponent(new Label());
		fileInfoPanel.addComponent(new Embedded("File Image",null));
		
		splitter.setFirstComponent(fileInfoPanel);
		
		fileTable = new Table("Files");
		fileTable.setSizeFull();
		fileTable.setImmediate(true);
		fileTable.setSelectable(true);
		
		// turn on column reordering and collapsing
		fileTable.setColumnReorderingAllowed(true);

        // set column headers
        //table.setContainerDataSource(newDataSource)
		fileTable.addContainerProperty("Device", String.class, null);
		fileTable.addContainerProperty("File Name", String.class, null);
		fileTable.addContainerProperty("File Path",  String.class, null);
		fileTable.addContainerProperty("File Type",  String.class, null);
		fileTable.setVisibleColumns(new String[] { "Device", "File Name", "File Path", "File Type"});
		fileTable.setColumnHeaders(new String[] { "Device", "File Name", "File Path", "File Type"});
		fileTable.setColumnExpandRatio("Device", 10);
		fileTable.setColumnExpandRatio("File Name", 25);
		fileTable.setColumnExpandRatio("File Path", 55);
		fileTable.setColumnExpandRatio("File Type", 10);
		splitter.setSecondComponent(fileTable);
		
		body.addComponent(splitter);
	}

	private void initFooter() {
		footer = new VerticalLayout();
		footer.setSizeFull();
		//copyright info and such
	}
	
	public void setContent() {
		this.setContent(mainLayout);
	}
	
	private Command menuCommand = new Command() {
        /**
		 * 
		 */
		private static final long serialVersionUID = 4195800588008842737L;

		public void menuSelected(MenuItem selectedItem) {
            if (selectedItem.getText().equalsIgnoreCase("Log Out")) {
            	ListWindow.this.getApplication().close();
            }
            else {
            	//TODO: Replace with meaningful actions
            	getWindow().showNotification("Action " + selectedItem.getText());
            }
		}
    };
}
