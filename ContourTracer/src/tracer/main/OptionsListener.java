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

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import trace.core.CurveBuilder;
import trace.core.ImageTracer;
import tracer.gui.GUI;

public class OptionsListener implements ChangeListener {
	private GUI gui;

	public OptionsListener(GUI gui) {
		this.gui = gui;
	}

	public void stateChanged(ChangeEvent e) {
		JSlider sourceSlider = (JSlider) e.getSource();
		CurveBuilder svgBuilder = ImageTracer.getInstance().getCurveBuilder();

		if (sourceSlider.getName() == "factor") {
			gui.setFactorValue(sourceSlider.getValue() / 100.0d);
			svgBuilder.setFactor(sourceSlider.getValue() / 100.0d);
			gui.updateSVGCanvas();
		}

		if (sourceSlider.getName() == "minimum") {
			gui.setMinimumValue(sourceSlider.getValue() / 100.0d);
			svgBuilder.setMinimumAngle(sourceSlider.getValue() / 100.0d);
			gui.updateSVGCanvas();
		}

		if (sourceSlider.getName() == "maximum") {
			gui.setMaximumValue(sourceSlider.getValue() / 100.0d);
			svgBuilder.setMaximumAngle(sourceSlider.getValue() / 100.0d);
			gui.updateSVGCanvas();
		}
	}
}
