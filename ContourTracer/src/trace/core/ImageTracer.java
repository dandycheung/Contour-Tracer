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


package trace.core;
import tracer.gui.ImageComponent;
import tracer.utils.Contour;

/** The ImageTracer searches the contour of a binary image, finds the optimal polygon for each contour
 * and finally translates each polygon into a curve
 * The ImageTracer is a Singleton class to allow global access to trace components
 */
public class ImageTracer {
	
	private static Potracer contourTracer = new Potracer();
	private static PolygonBuilder polyBuilder = new PolygonBuilder();
	private static CurveBuilder curveBuilder = new CurveBuilder();
	
	private static Contour outerPolygons;
	private static Contour innerPolygons;

	private ImageTracer() {}

	public Potracer getContourTracer() {
		return contourTracer;
	}

	private static class Holder {
		private static final ImageTracer INSTANCE = new ImageTracer();
	}
	public static ImageTracer getInstance() {
		return Holder.INSTANCE;
	}
	
	public void setImage(final ImageComponent sourceImage){
		contourTracer.setImage(sourceImage);
	}
	
	public void trace(){
		contourTracer.trace();
		
		outerPolygons = polyBuilder.getPolygons(contourTracer.getOuterContour());
		innerPolygons = polyBuilder.getPolygons(contourTracer.getInnerContour());	
	}
	
	public Contour getOuterPolygons() {
		return outerPolygons;
	}

	public Contour getInnerPolygons() {
		return innerPolygons;
	}
	
	public CurveBuilder getCurveBuilder() {
		return curveBuilder;
	}

}
