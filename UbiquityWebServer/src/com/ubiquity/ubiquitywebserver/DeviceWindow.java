package com.ubiquity.ubiquitywebserver;

import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class DeviceWindow extends Window {

	private VerticalLayout mainLayout;
	
	public DeviceWindow(List<String> devices) {
		super();
		init(devices);
	}
	
	private void init(List<String> devices) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		
		for (String device: devices) {
			Button deviceButton = new Button(device);
			deviceButton.addListener(new Button.ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					//ListWindow.this.getApplication().getMainWindow().open( new ExternalResource("http://" + ListWindow.this.getApplication().getUser() + ".testubiquity.info:8080/Ubiquity"));
				}
			});
			mainLayout.addComponent(deviceButton);
		}
		
		this.setContent(mainLayout);
	}
	
}
