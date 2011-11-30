package com.ubiquity.ubiquitywebserver;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class DeviceWindow extends Window {

	private VerticalLayout mainLayout;
	
	public DeviceWindow() {
		super();
		init();
	}
	
	private void init() {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		
		this.setContent(mainLayout);
	}
	
}
