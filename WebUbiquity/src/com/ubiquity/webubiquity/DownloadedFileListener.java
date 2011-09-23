package com.ubiquity.webubiquity;

import java.io.File;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
import com.ubiquity.webubiquity.resource.FileDownloadResource;
import com.vaadin.Application;

@SuppressWarnings("serial")
public class DownloadedFileListener implements RefreshListener {

	private String fileName;
	private String deviceName;
	private long fileSize;
	private Application app;
	
	public DownloadedFileListener(String fileName, String deviceName, long fileSize, Application app) {
		this.deviceName = deviceName.trim();
		this.fileSize = fileSize;
		File f = new File(System.getProperty("user.home") + System.getProperty("file.separator") + app.getUser().toString() + System.getProperty("file.separator") + this.deviceName + System.getProperty("file.separator") + fileName);
		this.fileName = f.getAbsolutePath();
		this.app = app;
	}

	@Override
	public void refresh(Refresher source) {
		File f = new File(fileName);
		System.out.println(f.getAbsolutePath());
		System.out.println("F.LENGTH = " + f.length());
		System.out.println("FILESIZE = " + fileSize);
		if (f.exists() && f.length() == fileSize) {
			FileDownloadResource fdr = new FileDownloadResource(f, app);
			app.getMainWindow().open(fdr);
			System.out.println("File ready to be downloaded by the client");
			source.setEnabled(false);
			app.getMainWindow().removeComponent(source);
		}
	}
}
