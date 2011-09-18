package com.ubiquity.webubiquity.resource;

import java.io.File;

import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.FileResource;

public class FileDownloadResource  extends FileResource{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6425954923091629730L;

	public FileDownloadResource(File sourceFile, Application application) {
		super(sourceFile, application);
    }
	
	public DownloadStream getStream() {
		return super.getStream();
		/*try {
			final DownloadStream ds = new DownloadStream(new FileInputStream(getSourceFile()), getMIMEType(), getFilename());
	        ds.setParameter("Content-Disposition", "attachment; filename="+ getFilename());
	        ds.setCacheTime(getCacheTime());
	        return ds;
	    } catch (final FileNotFoundException e) {
	        //TODO:do something
	        return null;
	    }*/
	}
}
