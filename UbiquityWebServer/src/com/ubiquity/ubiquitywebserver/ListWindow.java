package com.ubiquity.ubiquitywebserver;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Link;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TabSheet;
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
	private Table sharedToYouFileTable;
	private Table sharedByYouFileTable;
	private VerticalLayout footer;
	
	private HorizontalLayout optionButtons;
	private Button downloadFile;
	private Button shareFile;
	
	private TabSheet tabs;
	private VerticalLayout tab1;
	private VerticalLayout tab2;
	private VerticalLayout tab3;
	
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
	
	@SuppressWarnings("serial")
	private void initBody() {
		body = new HorizontalLayout();
		body.setSizeFull();
		Button device = new Button("Your device");
		device.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				ListWindow.this.getApplication().getMainWindow().open( new ExternalResource("http://" + ListWindow.this.getApplication().getUser() + ".testubiquity.info:8080/Ubiquity"));
			}
		});
		body.addComponent(device);
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
