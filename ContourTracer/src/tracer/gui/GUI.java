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

package tracer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.MouseInputAdapter;

import org.apache.batik.swing.JSVGCanvas;

import trace.core.ImageTracer;
import tracer.main.OptionsListener;

public class GUI extends JPanel {
	private static final long serialVersionUID = -6823507468116509577L;

	private ImageComponent sourceImage;
	private ImageZoomComponent imageZoomComponent;

	private JSVGCanvas svgCanvas;

	private int zoomAmountPerNotch;
	private OptionsListener optionsListener;
	private JSlider factorSlider;
	private JSlider minimumSlider;
	private JSlider maximumSlider;

	private double factorValue;
	private double minimumValue;
	private double maximumValue;
	private JLabel factorValueLabel;
	private JLabel minimumValueLabel;
	private JLabel maximumValueLabel;
	
	public GUI() {
		this.setLayout(new BorderLayout());
		setOpaque(true);
		setFocusable(true);
		
		zoomAmountPerNotch = 2;
		this.optionsListener = new OptionsListener(this);
		sourceImage = new ImageComponent();
		imageZoomComponent = new ImageZoomComponent(sourceImage);
		
		svgCanvas = new JSVGCanvas();
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		svgCanvas.setMinimumSize(new Dimension(400,400));
		svgCanvas.setSize(400, 400);
		svgCanvas.setSVGDocument(ImageTracer.getInstance().getCurveBuilder().getDocument());
		
		buildOptionsPanel();
		buildImagePanel();

		setupListeners();
	}
	
	private void buildOptionsPanel(){
		JPanel optionsPanel = new JPanel();
		optionsPanel.setMaximumSize(new Dimension(100, 100));
		optionsPanel.setLayout( new BoxLayout(optionsPanel, BoxLayout.Y_AXIS) );
		
		// slider
		JLabel factorLabel = new JLabel("Factor: ");
		JLabel minimumLabel = new JLabel("Minimum: ");
		JLabel maximumLabel = new JLabel("Maximum: ");
		
		this.factorSlider = new JSlider(1,200);
		this.minimumSlider = new JSlider(1,100);
		this.maximumSlider = new JSlider(1,100);
		this.factorSlider.setName("factor");
		this.minimumSlider.setName("minimum");
		this.maximumSlider.setName("maximum");
		
		this.factorValue = 1.33;
		this.minimumValue = 0.55;
		this.maximumValue = 1.0;
		
		this.factorValueLabel = new JLabel();
		this.factorValueLabel.setText(Double.toString(factorValue));
		this.minimumValueLabel = new JLabel();
		this.minimumValueLabel.setText(Double.toString(minimumValue));
		this.maximumValueLabel = new JLabel();
		this.maximumValueLabel.setText(Double.toString(maximumValue));
		
		JPanel factorPanel = new JPanel();
		factorPanel.setLayout( new BoxLayout(factorPanel, BoxLayout.X_AXIS) );
		factorPanel.add(factorLabel);
		factorPanel.add(factorValueLabel);	
		optionsPanel.add(factorPanel);
		optionsPanel.add(factorSlider);
		
		JPanel minimumPanel = new JPanel();
		minimumPanel.setLayout( new BoxLayout(minimumPanel, BoxLayout.X_AXIS) );
		minimumPanel.add(minimumLabel);
		minimumPanel.add(minimumValueLabel);	
		optionsPanel.add(minimumPanel);
		optionsPanel.add(minimumSlider);
		
		JPanel maximumPanel = new JPanel();
		maximumPanel.setLayout( new BoxLayout(maximumPanel, BoxLayout.X_AXIS) );
		maximumPanel.add(maximumLabel);
		maximumPanel.add(maximumValueLabel);	
		optionsPanel.add(maximumPanel);
		optionsPanel.add(maximumSlider);
		
		add(optionsPanel,BorderLayout.LINE_START);
	}
	
	private void buildImagePanel(){
		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(new GridLayout(2,2));

		JPanel sourceImagePanel = new JPanel();
		sourceImagePanel.setLayout( new BoxLayout(sourceImagePanel, BoxLayout.Y_AXIS) );
		sourceImagePanel.add(sourceImage);
		sourceImagePanel.add(new JLabel("Original"));
		
		JPanel imageZoomPanel = new JPanel();
		imageZoomPanel.setLayout( new BoxLayout(imageZoomPanel, BoxLayout.Y_AXIS) );
		imageZoomPanel.add(imageZoomComponent);
		imageZoomPanel.add(new JLabel("Optimal polygons"));
		
		JPanel vectorImagePanel = new JPanel();
		vectorImagePanel.setLayout( new BoxLayout(vectorImagePanel, BoxLayout.Y_AXIS) );
		vectorImagePanel.add(svgCanvas);
		vectorImagePanel.add(new JLabel("Vectorized imaged"));
		
		imagePanel.add(sourceImagePanel);
		imagePanel.add(imageZoomPanel);
		imagePanel.add(vectorImagePanel);
		
		add(imagePanel,BorderLayout.CENTER);
	}
	
	private void setupListeners(){
		this.factorSlider.addChangeListener(optionsListener);
		this.minimumSlider.addChangeListener(optionsListener);
		this.maximumSlider.addChangeListener(optionsListener);
		
		// Create a listener for up||dates based on mouse dragging.
		MouseInputAdapter zoomUpdater = new MouseInputAdapter() {
			public void mousePressed(MouseEvent e) {
				update(e);
			}

			public void mouseDragged(MouseEvent e) {
				update(e);
			}

			public void mouseWheelMoved(MouseWheelEvent e) {
				int notches = e.getWheelRotation();
				int sectionSize = sourceImage.getViewSectionSize();
				if (notches < 0) {
					if (sectionSize - zoomAmountPerNotch > ImageComponent.MIN_SECTION_SIZE)
						sectionSize -= zoomAmountPerNotch;
				} else {
					if (sectionSize + zoomAmountPerNotch < sourceImage.getMaxSectionSize())
						sectionSize += zoomAmountPerNotch;
				}
				sourceImage.setViewSectionSize(sectionSize);
				imageZoomComponent.update();
			}

			private void update(MouseEvent e) {
				sourceImage.setSectionPostion(e.getPoint());
				imageZoomComponent.update();
			}
		};
		
		sourceImage.addMouseListener(zoomUpdater);
		sourceImage.addMouseMotionListener(zoomUpdater);
		sourceImage.addMouseWheelListener(zoomUpdater);
	}
	
	public void updateSVGCanvas(){
		svgCanvas.getUpdateManager().getUpdateRunnableQueue().
	    invokeLater(new Runnable() {
	        public void run() {
	        	ImageTracer.getInstance().getCurveBuilder().buildCurves();
	        }
	    });
	}
	
	public ImageComponent getSourceImage() {
		return sourceImage;
	}
	
	public ImageZoomComponent getImageZoomComponent() {
		return imageZoomComponent;
	}
	
	public double getFactorValue() {
		return factorValue;
	}

	public void setFactorValue(double factorValue) {
		this.factorValue = factorValue;
		this.factorValueLabel.setText(Double.toString(factorValue));
	}

	public double getMinimumValue() {
		return minimumValue;
	}

	public void setMinimumValue(double minimumValue) {
		this.minimumValue = minimumValue;
		this.minimumValueLabel.setText(Double.toString(minimumValue));
	}

	public double getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(double maximumValue) {
		this.maximumValue = maximumValue;
		this.maximumValueLabel.setText(Double.toString(maximumValue));
	}
	
	public JSVGCanvas getSvgCanvas() {
		return svgCanvas;
	}	
}
