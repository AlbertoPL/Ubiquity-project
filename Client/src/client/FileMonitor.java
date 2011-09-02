package client;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class FileMonitor implements Runnable {

	private int watchID;
	private Client client;
	
	public FileMonitor(Client c) {
		client = c;
	}
	
	@Override
	public void run() {
		String path = System.getProperty("user.home");

	    // watch mask, specify events you care about,
	    // or JNotify.FILE_ANY for all events.
	    int mask = JNotify.FILE_CREATED  | 
	               JNotify.FILE_DELETED  | 
	               JNotify.FILE_MODIFIED | 
	               JNotify.FILE_RENAMED;

	    // watch subtree?
	    boolean watchSubtree = true;

	    // add actual watch
	    try {
			watchID = JNotify.addWatch(path, mask, watchSubtree, new Listener());
		} catch (JNotifyException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}
	
	public int getWatchID() {
		return watchID;
	}
	
	class Listener implements JNotifyListener {
	    public void fileRenamed(int wd, String rootPath, String oldName,
	        String newName) {
	      print("renamed " + rootPath + " : " + oldName + " -> " + newName);
	    }
	    public void fileModified(int wd, String rootPath, String name) {
	      print("modified " + rootPath + " : " + name);
	    }
	    public void fileDeleted(int wd, String rootPath, String name) {
	      print("deleted " + rootPath + " : " + name);
	    }
	    public void fileCreated(int wd, String rootPath, String name) {
	      print("created " + rootPath + " : " + name);
	    }
	    void print(String msg) {
	      System.err.println(msg);
	    }
	}
}
