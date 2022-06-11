/*
The MIT License (MIT)

Copyright (c) 2011 Andre Groeschel

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package tracer.main;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import trace.core.ImageTracer;
import tracer.gui.GUI;

public class MainWindow extends JFrame {
	private static final long serialVersionUID = 4375973878073035272L;
	private static final String TITLE = "Potrace Contour Tracing";
	private static final String INITIAL_OPEN = "data/zange.png";
	
	private GUI gui;

	public MainWindow() {
		super(TITLE);
		
		gui = new GUI();
		
		// load the default image
		File input = new File(INITIAL_OPEN);
		if (!input.canRead()) // file not found, choose another image
			input = openFile(); 
		
		gui.getSourceImage().loadImage(input);
		gui.getImageZoomComponent().reload();
		ImageTracer.getInstance().setImage(gui.getSourceImage());

		setContentPane(gui);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// window size and position
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		setSize(screenSize.width / 2, screenSize.height / 2);
		setLocation((screenSize.width - getWidth()) / 2,
				(screenSize.height - getHeight()) / 2);
		setLocationRelativeTo(null);

		// menu bar
		setupMenuBar();

		setVisible(true);
		
		ImageTracer.getInstance().trace();
	}

	private void setupMenuBar() {
		JMenuBar menu = new JMenuBar();

		// File menu & entries
		JMenu fileMenu = new JMenu("File");
		menu.add(fileMenu);
		Action fileOpenAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			{
				putValue(Action.NAME, "Open");
				putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, 0);
			}

			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if (input != null) {
					gui.getSourceImage().loadImage(input);
					gui.getImageZoomComponent().reload();
					ImageTracer.getInstance().setImage(gui.getSourceImage());
					ImageTracer.getInstance().trace();
					ImageTracer.getInstance().getCurveBuilder().findCurves();
					gui.getSvgCanvas().repaint();
				}
			}
		};
		
		Action fileSaveAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			{
				putValue(Action.NAME, "Save");
				putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, 0);
			}
			
			public void actionPerformed(ActionEvent e) {
				ImageTracer.getInstance().getCurveBuilder().save();
			}
		};
		
		Action exitAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			{
				putValue(Action.NAME, "Exit");
				putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, 0);
			}
			
			public void actionPerformed(ActionEvent e) {
				 System.exit( 0 ); 
			}
		};
		
		fileMenu.add(fileOpenAction);
		fileMenu.add(fileSaveAction);
		fileMenu.add(exitAction);
		
		setJMenuBar(menu);
	}
	
	private File openFile() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Images (*.jpg, *.png, *.gif, *.bmp)", "jpg", "png", "gif", "bmp");
		chooser.setFileFilter(filter);
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getCrossPlatformLookAndFeelClassName());
				} catch (Exception e) {
				}
				new MainWindow();
				ImageTracer.getInstance().getCurveBuilder().findCurves();
			}
		});
	}
}
