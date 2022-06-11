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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Vector;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import tracer.utils.Contour;
import tracer.utils.Math;

public class CurveBuilder {
	private SVGDocument document;
	private Element svgRoot;

	private final static int SOURCE_IMG_WIDTH = 200;
	private final static int SOURCE_IMG_HEIGHT = 200;

	private final static int VIEWBOX_WIDTH = 100;
	private final static int VIEWBOX_HEIGHT = 100;

	private final static String BACKGROUND_COLOR = "white";

	// private final static String SOURCE_POLYGON_STROKE_WIDTH = "0.1";
	// private final static String SOURCE_POLYGON_COLOR = "blue";

	private final static String CURVE_STROKE_WIDTH = "0.5";
	// private final static String CURVE_STROKE_COLOR = "green";
	private final static String CURVE_FILL_COLOR = "black";

	// private final static String CURVEPOINT_COLOR = "red";
	// private final static String CURVEPOINT_SIZE = "0.5";
	// private final static String CONTROLPOINT_COLOR = "blue";
	// private final static String CONTROLPOINT_SIZE = "0.3";

	private double factor;
	private double minimumAngle;
	private double maximumAngle;

	private Element curves[];

	private Vector<Point[]> polygonsVertices;
	private Vector<Point2D.Double[]> centerPoints;

	private Contour outerPolygons;
	private Contour innerPolygons;

	int curveAmount;

	String svgNS;

	public CurveBuilder() {
		// default bezier control point parameters
		this.factor = 4 / (double) 3;
		this.minimumAngle = 0.55;
		this.maximumAngle = 1;

		// create SVG document
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		document = (SVGDocument) impl.createDocument(svgNS, "svg", null);

		// Get the root element (the 'svg' element).
		svgRoot = document.getDocumentElement();

		// Set the width and height attributes on the root 'svg' element.
		svgRoot.setAttributeNS(null, "width", "100%");
		svgRoot.setAttributeNS(null, "height", "100%");
		svgRoot.setAttributeNS(null, "viewBox", "0 0 " + VIEWBOX_WIDTH + " "
				+ VIEWBOX_HEIGHT);
	}

	public void findCurves() {
		// reset data structures
		polygonsVertices = new Vector<Point[]>();
		centerPoints = new Vector<Point2D.Double[]>();

		outerPolygons = ImageTracer.getInstance().getOuterPolygons();
		innerPolygons = ImageTracer.getInstance().getInnerPolygons();

		curveAmount = outerPolygons.getSize() + innerPolygons.getSize();

		// copy the points found by the PolygonBuilder into another data
		// structure (merge inner and outer polygons)
		// we need to do the curve calculation with double precision
		for (int i = 0; i < curveAmount; ++i) {
			if (outerPolygons.hasContour(i)) {
				Iterator<Point> pointIt = outerPolygons.getPointIterator(i);
				Point vertices[] = new Point[outerPolygons.getContourSize(i)];

				for (int j = 0; pointIt.hasNext(); ++j) {
					vertices[j] = pointIt.next();
				}

				polygonsVertices.add(vertices);
			}

			if (innerPolygons.hasContour(i)) {
				Iterator<Point> pointIt = innerPolygons.getPointIterator(i);
				Point vertices[] = new Point[innerPolygons.getContourSize(i)];

				for (int j = 0; pointIt.hasNext(); ++j) {
					vertices[j] = pointIt.next();
				}

				polygonsVertices.add(vertices);
			}
		}

		// create the svg document, all elements are created at this stage but
		// not filled with data yet
		buildDocument();
		calculateCenterPoints();
		buildCurves();

	}

	private void buildDocument() {
		if (curves != null) {
			for (int i = 0; i < curves.length; ++i) {
				svgRoot.removeChild(curves[i]);
			}
		}

		// make sure inner and outer polygons are added to the DOM in the
		// correct order
		curves = new Element[curveAmount];

		for (int i = 0; i < curveAmount; ++i) {
			if (outerPolygons.hasContour(i)) {
				curves[i] = document.createElementNS(svgNS, "path");

				curves[i].setAttribute("stroke", "none");
				curves[i].setAttribute("fill", CURVE_FILL_COLOR);
				curves[i].setAttribute("stroke-width", CURVE_STROKE_WIDTH);

				svgRoot.appendChild(curves[i]);
			}

			if (innerPolygons.hasContour(i)) {
				curves[i] = document.createElementNS(svgNS, "path");

				curves[i].setAttribute("stroke", "none");
				curves[i].setAttribute("fill", BACKGROUND_COLOR);
				curves[i].setAttribute("stroke-width", CURVE_STROKE_WIDTH);
				svgRoot.appendChild(curves[i]);
			}
		}
	}

	private void calculateCenterPoints() {
		// iterate over polygons and calculate their center points
		Iterator<Point[]> polygonIt = polygonsVertices.iterator();

		while (polygonIt.hasNext()) {
			Point currentPolygon[] = polygonIt.next();
			Point2D.Double centerPoints[] = new Point2D.Double[currentPolygon.length];

			// build center points between source vertices
			for (int i = 0; i < currentPolygon.length; ++i) {
				int currentVertex = i % currentPolygon.length;
				int nextVertex = (i + 1) % currentPolygon.length;

				double centerPointX = (currentPolygon[currentVertex].getX() + currentPolygon[nextVertex]
						.getX()) / 2;
				double centerPointY = (currentPolygon[currentVertex].getY() + currentPolygon[nextVertex]
						.getY()) / 2;
				centerPoints[i] = new Point2D.Double(centerPointX, centerPointY);
			}

			this.centerPoints.add(centerPoints);
		}
	}

	public void buildCurves() {
		// iterate over polygons and calculate the curve's control points
		for (int i = 0; i < polygonsVertices.size(); ++i) {
			Point[] vertices = polygonsVertices.get(i);
			Point2D.Double centerPoints[] = this.centerPoints.get(i);

			boolean isNewCurveSegment = true;
			String curveCoordinates = new String();

			// calculate control points of curve
			curveCoordinates = curveCoordinates.concat("M "
					+ Integer.toString((int) (centerPoints[0].x
							/ SOURCE_IMG_WIDTH * VIEWBOX_WIDTH))
					+ " "
					+ Integer.toString((int) (centerPoints[0].y
							/ SOURCE_IMG_HEIGHT * VIEWBOX_HEIGHT)));

			for (int j = 0; j < centerPoints.length; ++j) {
				int centerPointIndex = j % centerPoints.length;

				// calculate distance from corner vertex to the line that
				// connects two center points
				Point2D.Double curveSegmentStart = centerPoints[centerPointIndex];
				Point2D.Double curveSegmentEnd = centerPoints[(j + 1)
						% centerPoints.length];
				Point2D.Double cornerVertex = new Point2D.Double(
						vertices[(j + 1) % vertices.length].x,
						vertices[(j + 1) % vertices.length].y);

				double distance = Math.pointLineDistance(curveSegmentStart,
						curveSegmentEnd, cornerVertex);
				double angle = factor * (distance - 0.5) / distance;

				// System.out.println("Angle: " + angle + " Distance: " +
				// distance + " Factor: " + factor);

				if (angle < minimumAngle) {
					angle = minimumAngle;
				}

				// draw lines from curveSegmentStart->cornerVertex and
				// cornerVertex->curveSegmentEnd
				if (angle > maximumAngle) {
					curveCoordinates = curveCoordinates.concat(" L "
							+ java.lang.Double.toString((curveSegmentStart.x
									/ SOURCE_IMG_WIDTH * VIEWBOX_WIDTH))
							+ " "
							+ java.lang.Double.toString((curveSegmentStart.y
									/ SOURCE_IMG_HEIGHT * VIEWBOX_HEIGHT))
							+ " "
							+ java.lang.Double.toString((cornerVertex.x
									/ SOURCE_IMG_WIDTH * VIEWBOX_WIDTH))
							+ " "
							+ java.lang.Double.toString((cornerVertex.y
									/ SOURCE_IMG_HEIGHT * VIEWBOX_HEIGHT))
							+ " L "
							+ java.lang.Double.toString((cornerVertex.x
									/ SOURCE_IMG_WIDTH * VIEWBOX_WIDTH))
							+ " "
							+ java.lang.Double.toString((cornerVertex.y
									/ SOURCE_IMG_HEIGHT * VIEWBOX_HEIGHT))
							+ " "
							+ java.lang.Double.toString((curveSegmentEnd.x
									/ SOURCE_IMG_WIDTH * VIEWBOX_WIDTH))
							+ " "
							+ java.lang.Double.toString((curveSegmentEnd.y
									/ SOURCE_IMG_HEIGHT * VIEWBOX_HEIGHT)));
					isNewCurveSegment = true;
				} else {
					Point2D.Double controlPoint1 = lerp(curveSegmentStart,
							cornerVertex, angle);
					Point2D.Double controlPoint2 = lerp(curveSegmentEnd,
							cornerVertex, angle);

					if (isNewCurveSegment) {
						curveCoordinates = curveCoordinates.concat(" C");
						isNewCurveSegment = false;
					}

					curveCoordinates = curveCoordinates.concat(" "
							+ java.lang.Double.toString((controlPoint1.x
									/ SOURCE_IMG_WIDTH * VIEWBOX_WIDTH))
							+ " "
							+ java.lang.Double.toString((controlPoint1.y
									/ SOURCE_IMG_HEIGHT * VIEWBOX_HEIGHT))
							+ " "
							+ java.lang.Double.toString((controlPoint2.x
									/ SOURCE_IMG_WIDTH * VIEWBOX_WIDTH))
							+ " "
							+ java.lang.Double.toString((controlPoint2.y
									/ SOURCE_IMG_HEIGHT * VIEWBOX_HEIGHT))
							+ " "
							+ java.lang.Double.toString((curveSegmentEnd.x
									/ SOURCE_IMG_WIDTH * VIEWBOX_WIDTH))
							+ " "
							+ java.lang.Double.toString((curveSegmentEnd.y
									/ SOURCE_IMG_HEIGHT * VIEWBOX_HEIGHT)));
				}
			}

			curves[i].setAttribute("d", curveCoordinates);
		}
	}

	private Point2D.Double lerp(Point2D.Double start, Point2D.Double end, double factor) {
		Point2D.Double result = new Point2D.Double();
		result.setLocation((1 - factor) * start.x + factor * end.x,
				(1 - factor) * start.y + factor * end.y);
		return result;
	}

	public SVGDocument getDocument() {
		return document;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public double getMinimumAngle() {
		return minimumAngle;
	}

	public void setMinimumAngle(double minimumAngle) {
		this.minimumAngle = minimumAngle;
	}

	public double getMaximumAngle() {
		return maximumAngle;
	}

	public void setMaximumAngle(double maximumAngle) {
		this.maximumAngle = maximumAngle;
	}

	public void save() {
		try {
			OutputStream outputStream = new FileOutputStream("test.svg");
			Writer out = new OutputStreamWriter(outputStream);
			DOMUtilities.writeDocument(document, out);
			out.flush();
			out.close();
		} catch (Exception e) {
		}
	}
}
