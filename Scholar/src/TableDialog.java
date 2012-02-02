/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;


@SuppressWarnings("serial")
public class TableDialog extends JDialog
                        implements ActionListener {
    private static TableDialog dialog;
    private static String value = "";
    private JTable table;
    private WindowTableModel windowTableModel;

    /**
     * Set up and show the dialog.  The first Component argument
     * determines which frame the dialog depends on; it should be
     * a component in the dialog's controlling frame. The second
     * Component argument should be null if you want the dialog
     * to come up with its left corner in the center of the screen;
     * otherwise, it should be the component on top of which the
     * dialog should appear.
     */
    public static String showDialog(Component frameComp,
                                    Component locationComp,
                                    String labelText,
                                    String title) {
        Frame frame = JOptionPane.getFrameForComponent(frameComp);
        dialog = new TableDialog(frame,
                                locationComp,
                                labelText,
                                title);
        dialog.checkOpenWindows();
        dialog.setVisible(true);
        return value;
    }

    private TableDialog(Frame frame,
                       Component locationComp,
                       String labelText,
                       String title) {
        super(frame, title, true);
        //Create and initialize the buttons.
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        //
        final JButton setButton = new JButton("Set");
        setButton.setActionCommand("Set");
        setButton.addActionListener(this);
        getRootPane().setDefaultButton(setButton);

        //main part of the dialog
        windowTableModel = new WindowTableModel();
		table = new JTable(windowTableModel);
		table.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		JScrollPane currentWindowsScrollPane = new JScrollPane(table);

        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel(labelText);
        label.setLabelFor(table);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(currentWindowsScrollPane);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);

        //Initialize values.
        //setValue(initialValue);
        pack();
        setLocationRelativeTo(locationComp);
    }

    //Handle clicks on the Set and Cancel buttons.
    public void actionPerformed(ActionEvent e) {
        if ("Set".equals(e.getActionCommand())) {
        	 //parent.getTabs().get
            TableDialog.value = (String)(table.getValueAt(table.getSelectedRow(), 0)) +"\n" +
            		(String)(table.getValueAt(table.getSelectedRow(), 1)) + "\n" +
            		(String)(table.getValueAt(table.getSelectedRow(), 2));
        }
        else {
        	TableDialog.value = "";
        }
        TableDialog.dialog.setVisible(false);
    }
    
    public void checkOpenWindows() {
		try {
	        String line;
	        Process p = Runtime.getRuntime().exec
	                ("openwindow.exe");
	        BufferedReader input =
	                new BufferedReader(new InputStreamReader(p.getInputStream()));
	        while ((line = input.readLine()) != null) {
	            //String xy = line.substring(5, line.indexOf("Win Name:") - 1);
	        	int x = Integer.parseInt(line.substring(0, line.indexOf(' ')));
	        	line = line.substring(line.indexOf(' ') + 1);
	        	int y = Integer.parseInt(line.substring(0 , line.indexOf(' ')));
	        	line = line.substring(line.indexOf(' ') + 1);
	        	int w = Integer.parseInt(line.substring(0, line.indexOf(' ')));
	        	line = line.substring(line.indexOf(' ') + 1);
	        	int h = Integer.parseInt(line.substring(0, line.indexOf(' ')));
	        	line = line.substring(line.indexOf(' ') + 1);
	            String name = line;
	        	if (x < 0 && y < 0) {
	        		line = "Minimized";
	        	}
	        	else {
	        		line = x + "," + y;
	        	}
	        	windowTableModel.addRow(new Object[]{name, line, w + "," + h, ""});
	        }
	        input.close();
	        p.destroy();
	    } catch (Exception err) {
	        err.printStackTrace();
	    }
	}
}
