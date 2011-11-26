package com.ubiquity.ubiquitywebclient;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

import javax.swing.filechooser.FileSystemView;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class FileViewLayout extends VerticalLayout {
    
	private TabSheet tabs;
	private Table table;
	private Button goUpOneLevel;
	private Table sharedFiles;
	
    HashSet<Object> markedRows = new HashSet<Object>();

    static final Action ACTION_MARK = new Action("Mark");
    static final Action ACTION_UNMARK = new Action("Unmark");
    static final Action ACTION_LOG = new Action("Save");
    static final Action[] ACTIONS_UNMARKED = new Action[] { ACTION_MARK,
            ACTION_LOG };
    static final Action[] ACTIONS_MARKED = new Action[] { ACTION_UNMARK,
            ACTION_LOG };
    
    private String currentPath = "";

    private String host = "";
    
    private File[] roots;
    
    private FileSystemView filesys;
    
    public FileViewLayout() {
    	
    	
    	try {
    		host = InetAddress.getLocalHost().getHostName();
    	}
    	catch(UnknownHostException e) {
    		System.out.println("Unknown host!");
    	}
    	tabs = new TabSheet();
    	
    	goUpOneLevel = new Button("Go Back");
    	goUpOneLevel.setEnabled(false);
    	goUpOneLevel.addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				if (currentPath.contains(System.getProperty("file.separator")) && currentPath.lastIndexOf(System.getProperty("file.separator"))!=currentPath.length()-1) {
					if (currentPath.indexOf(System.getProperty("file.separator"))== currentPath.lastIndexOf(System.getProperty("file.separator"))) {
						currentPath = currentPath.substring(0, currentPath.lastIndexOf(System.getProperty("file.separator")) + 1);
					}
					else {
						currentPath = currentPath.substring(0, currentPath.lastIndexOf(System.getProperty("file.separator")));
					}
					table.setCaption(currentPath);
					File root = new File(currentPath);
                    System.out.println("Current Path: " + currentPath);
	                table.removeAllItems();
	                int count = 0;
	                for (File f: root.listFiles()) {
	                	table.addItem(new Object[] {
	                		    f.getName(),f.getTotalSpace() - f.getFreeSpace(),f.lastModified()}, new Integer(count++));
	                }
				}
				else {
			    	goUpOneLevel.setEnabled(false);
					currentPath = "";
                    System.out.println("Current Path: " + currentPath);
					table.setCaption(host);
	                table.removeAllItems();
					table.addItem(new Object[] {
			        		System.getProperty("user.home"),new File(System.getProperty("user.home")).getTotalSpace() - new File(System.getProperty("user.home")).getFreeSpace(),new File(System.getProperty("user.home")).lastModified()}, new Integer(0));
			        table.addItem(new Object[] {
			        		filesys.getHomeDirectory().getName(),filesys.getHomeDirectory().getTotalSpace() - filesys.getHomeDirectory().getFreeSpace(),filesys.getHomeDirectory().lastModified()}, new Integer(1));
			        for (int i=0; i<roots.length; i++) {
			        	table.addItem(new Object[] {
			        		    roots[i],roots[i].getTotalSpace() - roots[i].getFreeSpace(),roots[i].lastModified()}, new Integer(i+2));
			            System.out.println("Root[" + i + "] = " + roots[i]);
			        }
				}
				
			}
		});
    	
    	VerticalLayout files = new VerticalLayout();
    	
    	files.addComponent(goUpOneLevel);
    	
    	table = new Table(host);
        files.addComponent(table);

        // set a style name, so we can style rows and cells
        table.setStyleName("iso3166");

        // size
        table.setSizeFull();

        // selectable
        table.setSelectable(true);
        table.setMultiSelect(false);
        table.setImmediate(true); // react at once when something is selected

        // turn on column reordering and collapsing
        table.setColumnReorderingAllowed(true);
        table.setColumnCollapsingAllowed(true);

        // set column headers
        table.addContainerProperty("Name", String.class,  null);
        table.addContainerProperty("Size (kb)",  Long.class,  null);
        table.addContainerProperty("Date Modified",       Long.class, null);
        table.addGeneratedColumn("Actions", new Table.ColumnGenerator() {
            public Component generateCell(Table source, Object itemId,
                    Object columnId) {
            	HorizontalLayout buttons = new HorizontalLayout();
                final Item item = table.getItem(itemId);
                final File root = new File(currentPath + System.getProperty("file.separator") + (String) item.getItemProperty("Name").getValue());
                Button download = null;
                Button share = null;
                if (!root.isDirectory()) {
                	download = new Button("Download");
                	download.addListener(new Button.ClickListener() {
    					
    					@Override
    					public void buttonClick(ClickEvent event) {
    	    				FileDownloadResource f = new FileDownloadResource(root, FileViewLayout.this.getApplication());
    	    				FileViewLayout.this.getApplication().getMainWindow().open(f);
    					}
    				});
                	share = new Button("Share");
                	share.addListener(new Button.ClickListener() {
						
						@Override
						public void buttonClick(ClickEvent event) {
							final Window dialog = new Window("Enter user name to share with");
					        dialog.setModal(true);
					        FileViewLayout.this.getWindow().addWindow(dialog);
					        TextField user = new TextField("Username");
					        Button ok = new Button("Share!");
					        ok.addListener(new Button.ClickListener() {

								@Override
								public void buttonClick(ClickEvent event) {
									//save user-file pair in DB
								}
					        	
					        });
					        dialog.addComponent(user);
					        dialog.addComponent(ok);
						}
					});
                }
                if (download != null) {
                	buttons.addComponent(download);
                }
                if (share != null) {
                	buttons.addComponent(share);
                }
                return buttons;
            }

        });
        
        table.setColumnHeaders(new String[] { "Name", "Size", "Date Modified", "Actions" });

        // Icons for column headers
       

        // Column alignment
       

        // Column width
       

        // Collapse one column - the user can make it visible again
        
        //data
        roots = File.listRoots();
        
        filesys = FileSystemView.getFileSystemView();

        table.addItem(new Object[] {
        		System.getProperty("user.home"),new File(System.getProperty("user.home")).getTotalSpace() - new File(System.getProperty("user.home")).getFreeSpace(),new File(System.getProperty("user.home")).lastModified()}, new Integer(0));
        table.addItem(new Object[] {
        		filesys.getHomeDirectory().getName(),filesys.getHomeDirectory().getTotalSpace() - filesys.getHomeDirectory().getFreeSpace(),filesys.getHomeDirectory().lastModified()}, new Integer(1));
        for (int i=0; i<roots.length; i++) {
        	table.addItem(new Object[] {
        		    roots[i],roots[i].getTotalSpace() - roots[i].getFreeSpace(),roots[i].lastModified()}, new Integer(i+2));
            System.out.println("Root[" + i + "] = " + roots[i]);
        }

        // show row header w/ icon
        table.setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY);

        // Actions (a.k.a context menu)
        table.addActionHandler(new Action.Handler() {
            public Action[] getActions(Object target, Object sender) {
                if (markedRows.contains(target)) {
                    return ACTIONS_MARKED;
                } else {
                    return ACTIONS_UNMARKED;
                }
            }

            public void handleAction(Action action, Object sender, Object target) {
                if (ACTION_MARK == action) {
                    markedRows.add(target);
                    table.refreshRowCache();
                } else if (ACTION_UNMARK == action) {
                    markedRows.remove(target);
                    table.refreshRowCache();
                } else if (ACTION_LOG == action) {
                    table.getItem(target);
                }

            }

        });

        // style generator
        table.setCellStyleGenerator(new CellStyleGenerator() {
            public String getStyle(Object itemId, Object propertyId) {
                if (propertyId == null) {
                    // no propertyId, styling row
                    return (markedRows.contains(itemId) ? "marked" : null);
                } 
                else {
                    // no style
                    return null;
                }

            }

        });

        // listen for valueChange, a.k.a 'select' and update the label
        table.addListener(new Table.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // in multiselect mode, a Set of itemIds is returned,
                // in singleselect mode the itemId is returned directly
                if (null == event.getProperty().getValue()) {
                   // selected.setValue("No selection");
                } else {
                    File root = new File(currentPath + System.getProperty("file.separator") + (String) table.getContainerProperty(table.getValue(), "Name").getValue());
                    if (root.isDirectory() && root.listFiles() != null) {
                    	goUpOneLevel.setEnabled(true);
	                    currentPath = root.getAbsolutePath();
	                    System.out.println("Current Path: " + currentPath);
                    	table.setCaption(currentPath);
	                    table.removeAllItems();
	                    int count = 0;
	                    for (File f: root.listFiles()) {
	                    	table.addItem(new Object[] {
	                    		    f.getName(),f.getTotalSpace() - f.getFreeSpace(),f.lastModified()}, new Integer(count++));
	                    }
                    }
                	// selected.setValue("Selected: " + table.getValue());
                }
            }
        });

        tabs.addTab(files, "My Files");
        
        sharedFiles = new Table("Shared Files");
        sharedFiles.setSizeFull();
        tabs.addTab(sharedFiles, "Shared Files");
        
        this.addComponent(tabs);
    }

       

}
