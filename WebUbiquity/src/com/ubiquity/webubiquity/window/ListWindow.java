package com.ubiquity.webubiquity.window;

import com.github.wolfie.refresher.Refresher;
import com.ubiquity.webubiquity.DownloadedFileListener;
import com.ubiquity.webubiquity.WebUbiquityApplication;
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
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
		
		VerticalLayout secondComponent = new VerticalLayout();
		
		optionButtons = new HorizontalLayout();
		downloadFile = new Button("Download File");
		shareFile = new Button("Share File");
		
		downloadFile.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (tabs.getSelectedTab().equals(tab1)) {
					WebUbiquityApplication app = ((WebUbiquityApplication) ListWindow.this.getApplication());
					Object itemId = fileTable.getValue();
					Item item = fileTable.getItem(itemId);
					
					//TODO: check if server has the file already
					
					boolean success = app.downloadFile((String)app.getUser(), item.getItemProperty("Device").toString().trim(), item.getItemProperty("File Path").toString());
					if (success) {
						System.out.println("Downloading file: " + item.getItemProperty("File Path").toString() + " from " + item.getItemProperty("Device").toString().trim());
						Refresher refresher = new Refresher();
						long filesize = app.getFileSize(item.getItemProperty("File Path").toString().trim(),item.getItemProperty("Device").toString().trim());
						refresher.addListener(new DownloadedFileListener(item.getItemProperty("File Name").toString(),item.getItemProperty("Device").toString(),filesize, ListWindow.this.getApplication()));
						ListWindow.this.addComponent(refresher);
					}
					else {
						//REPLACE WITH WINDOW NOTIFICATION
						System.out.println("Cannot download file! (Is the machine on and connected to the Internet?)");
					}
				}
			}
			
		});
		
		optionButtons.addComponent(downloadFile);
		optionButtons.addComponent(shareFile);
		downloadFile.setEnabled(false);
		shareFile.setEnabled(false);
		
		secondComponent.addComponent(optionButtons);
		
		tabs = new TabSheet();
		tab1 = new VerticalLayout();
		tab1.setMargin(true);
		tab2 = new VerticalLayout();
		tab2.setMargin(true);
		tab3 = new VerticalLayout();
		tab3.setMargin(true);
		
		tabs.addTab(tab1, "Your files", null);
		tabs.addTab(tab2, "Files shared with you", null);
		tabs.addTab(tab3, "Files you are sharing", null);
		tabs.addListener(new SelectedTabChangeListener() {

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				TabSheet tabsheet = event.getTabSheet();
		        tabsheet.getTab(tabsheet.getSelectedTab());				
			}
			
		});

		
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
		
		fileTable.addListener(new ItemClickListener() {

			@Override
			public void itemClick(ItemClickEvent event) {
				//TODO: For now, only download one file at a time, this will change
				if (!fileTable.isSelected(fileTable.getNullSelectionItemId()) && 
						!fileTable.isMultiSelect()) {
					shareFile.setEnabled(true);
					downloadFile.setEnabled(true);
				}
				else {
					shareFile.setEnabled(false);
					downloadFile.setEnabled(false);
				}
			}
			
		});
		
		tab1.addComponent(fileTable);
		
		sharedToYouFileTable = new Table("Files shared with you");
		sharedToYouFileTable.setSizeFull();
		sharedToYouFileTable.setImmediate(true);
		sharedToYouFileTable.setSelectable(true);
		
		// turn on column reordering and collapsing
		sharedToYouFileTable.setColumnReorderingAllowed(true);

        // set column headers
        //table.setContainerDataSource(newDataSource)
		sharedToYouFileTable.addContainerProperty("File Name", String.class, null);
		sharedToYouFileTable.addContainerProperty("File Type",  String.class, null);
		sharedToYouFileTable.addContainerProperty("Shared By", String.class, null);
		sharedToYouFileTable.setVisibleColumns(new String[] { "File Name", "File Type", "Shared By"});
		sharedToYouFileTable.setColumnHeaders(new String[] { "File Name", "File Type", "Shared By"});
		sharedToYouFileTable.setColumnExpandRatio("File Name", 40);
		sharedToYouFileTable.setColumnExpandRatio("File Type", 30);
		sharedToYouFileTable.setColumnExpandRatio("Shared By", 30);
		tab2.addComponent(sharedToYouFileTable);
		
		sharedByYouFileTable = new Table("Files shared by you");
		sharedByYouFileTable.setSizeFull();
		sharedByYouFileTable.setImmediate(true);
		sharedByYouFileTable.setSelectable(true);
		
		// turn on column reordering and collapsing
		sharedByYouFileTable.setColumnReorderingAllowed(true);

        // set column headers
        //table.setContainerDataSource(newDataSource)
		sharedByYouFileTable.addContainerProperty("Device", String.class, null);
		sharedByYouFileTable.addContainerProperty("File Name", String.class, null);
		sharedByYouFileTable.addContainerProperty("File Path",  String.class, null);
		sharedByYouFileTable.addContainerProperty("File Type",  String.class, null);
		sharedByYouFileTable.addContainerProperty("Shared With",  String.class, null);
		sharedByYouFileTable.setVisibleColumns(new String[] { "Device", "File Name", "File Path", "File Type", "Shared With"});
		sharedByYouFileTable.setColumnHeaders(new String[] { "Device", "File Name", "File Path", "File Type", "Shared With"});
		sharedByYouFileTable.setColumnExpandRatio("Device", 10);
		sharedByYouFileTable.setColumnExpandRatio("File Name", 25);
		sharedByYouFileTable.setColumnExpandRatio("File Path", 45);
		sharedByYouFileTable.setColumnExpandRatio("File Type", 10);
		sharedByYouFileTable.setColumnExpandRatio("Shared With", 10);
		tab3.addComponent(sharedByYouFileTable);
		
		secondComponent.addComponent(tabs);
		
		splitter.setSecondComponent(secondComponent);
		
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
