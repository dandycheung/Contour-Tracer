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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import trace.core.ImageTracer;
import tracer.main.TraceListener;
import tracer.utils.Contour;

public class ImageZoomComponent extends JComponent implements TraceListener {

	private static final long serialVersionUID = -1857902074286586118L;
	private final static int MIN_SIZE = 300;
	private static final int GRID_STROKE_WIDTH = 1;
	private static final Color GRID_COLOR = Color.BLACK;
	private final static int MAX_GRID_DRAW = 40;

	private static final int CONTOUR_STROKE_WIDTH = 2;
	private static final Color CONTOUR_OUTER_COLOR = Color.BLUE;
	private static final Color CONTOUR_INNER_COLOR = Color.RED;

	private static final int POLYSEGMENT_STROKE_WIDTH = 2;
	private static final int POLYSEGMENT_VERTEX_SIZE = 5;
	private static final Color POLYSEGMENT_VERTEX_COLOR = Color.GREEN;

	private static final Color POLYSEGMENT_INNER_COLOR = Color.ORANGE;
	private static final Color POLYSEGMENT_OUTER_COLOR = Color.CYAN;

	private BufferedImage sourceImage;
	private int size;

	private ImageComponent imageComponent;
	// source section coordinates
	private int sourceX1, sourceY1, sourceX2, sourceY2;
	private int pixelAmount;
	private float pixelSize;

	private Contour outerContour;
	private Contour innerContour;
	private Contour outerPolygons;
	private Contour innerPolygons;
	
	private boolean drawContoursEnabled;
	private boolean drawPolygonEnabled;
	

	public ImageZoomComponent(final ImageComponent imageComp) {

		this.imageComponent = imageComp;
		// 1px simple border
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

		drawContoursEnabled = false;
		drawPolygonEnabled = true;
	}
	
	public void reload(){
		if(imageComponent.getImage() != null){
			this.sourceImage = imageComponent.getImage();
			
			if (sourceImage.getHeight() < MIN_SIZE) {
				this.setMinimumSize(new Dimension(MIN_SIZE, MIN_SIZE));
				this.size = MIN_SIZE;

			} else {
				this.setMinimumSize(new Dimension(sourceImage.getHeight(),
						sourceImage.getHeight()));
				this.size = sourceImage.getHeight();
			}

			// view section coordinates
			this.sourceX1 = imageComponent.getViewSection().x;
			this.sourceY1 = imageComponent.getViewSection().y;
			this.sourceX2 = imageComponent.getViewSection().x
					+ imageComponent.getViewSection().width;
			this.sourceY2 = imageComponent.getViewSection().y
					+ imageComponent.getViewSection().height;

			// Calculate pixel amount & size in relationship to view section
			pixelAmount = sourceX2 - sourceX1;
			pixelSize = size / (float) pixelAmount;

			repaint();
		}
	}

	public void paintComponent(Graphics g) {
		if(sourceImage != null){
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

			g2.drawImage(sourceImage, 0, 0, size, size, sourceX1, sourceY1,
					sourceX2, sourceY2, null);
			drawGrid(g2);
			if (drawContoursEnabled == true && outerContour != null && innerContour != null) {
				drawContours(g2);
			}

			if (drawPolygonEnabled == true) {
				//debugDraw(g2);
				drawPolygons(g2);
			}
			g2.dispose();
		}
	}

	private void drawGrid(Graphics2D g2) {
		// grid
		g2.setColor(GRID_COLOR);
		g2.setStroke(new BasicStroke(GRID_STROKE_WIDTH));
		if (pixelAmount < MAX_GRID_DRAW) {
			for (int i = 0; i <= pixelAmount; ++i)
				g2.drawLine((int) (i * pixelSize), 0, (int) (i * pixelSize),
						size);
			for (int i = 0; i <= pixelAmount; ++i)
				g2.drawLine(0, (int) (i * pixelSize), size,
						(int) (i * pixelSize));
		}
	}

	private void drawContours(Graphics2D g2) {
		g2.setStroke(new BasicStroke(CONTOUR_STROKE_WIDTH));
		g2.setColor(CONTOUR_OUTER_COLOR);

		for (Iterator<Integer> outerContourIt = outerContour
				.getContourIterator(); outerContourIt.hasNext();) {
			int contourID = outerContourIt.next();

			Iterator<Point> outerPointsIterator = outerContour
					.getPointIterator(contourID);

			Point startPoint = null;
			Point endPoint = null;

			for (Iterator<Point> contourIt = outerPointsIterator; contourIt
					.hasNext();) {
				startPoint = endPoint;
				endPoint = contourIt.next();
				if (startPoint != null) {

					// is start pos within view section
					boolean isStartPosVisible = startPoint.x >= sourceX1
							&& startPoint.x <= sourceX2
							&& startPoint.y >= sourceY1
							&& startPoint.y <= sourceY2;
					boolean isEndPosVisible = endPoint.x >= sourceX1
							&& endPoint.x <= sourceX2 && endPoint.y >= sourceY1
							&& endPoint.y <= sourceY2;

					if (isStartPosVisible && isEndPosVisible) {
						g2.drawLine(
								(int) ((startPoint.x - sourceX1) * pixelSize),
								(int) ((startPoint.y - sourceY1) * pixelSize),
								(int) ((endPoint.x - sourceX1) * pixelSize),
								(int) ((endPoint.y - sourceY1) * pixelSize));
					}
				}
			}
		}

		g2.setColor(CONTOUR_INNER_COLOR);
		for (Iterator<Integer> innterContourIt = innerContour
				.getContourIterator(); innterContourIt.hasNext();) {
			int contourID = innterContourIt.next();

			Iterator<Point> innerPointsIterator = innerContour
					.getPointIterator(contourID);

			Point startPoint = null;
			Point endPoint = null;

			for (Iterator<Point> contourIt = innerPointsIterator; contourIt
					.hasNext();) {
				startPoint = endPoint;
				endPoint = contourIt.next();
				if (startPoint != null) {

					// is start pos within view section
					boolean isStartPosVisible = startPoint.x >= sourceX1
							&& startPoint.x <= sourceX2
							&& startPoint.y >= sourceY1
							&& startPoint.y <= sourceY2;
					boolean isEndPosVisible = endPoint.x >= sourceX1
							&& endPoint.x <= sourceX2 && endPoint.y >= sourceY1
							&& endPoint.y <= sourceY2;

					if (isStartPosVisible && isEndPosVisible) {
						g2.drawLine(
								(int) ((startPoint.x - sourceX1) * pixelSize),
								(int) ((startPoint.y - sourceY1) * pixelSize),
								(int) ((endPoint.x - sourceX1) * pixelSize),
								(int) ((endPoint.y - sourceY1) * pixelSize));
					}
				}
			}
		}
	}

	private void drawPolygons(Graphics2D g2) {
		//link the poly data structure of the tracer the image component to display the polygons
		outerPolygons = ImageTracer.getInstance().getOuterPolygons();
		innerPolygons = ImageTracer.getInstance().getInnerPolygons();
		
		if(innerPolygons != null){
			//go through all inner polygons and draw their vertices
			for (Iterator<Integer> polygonIt = innerPolygons.getContourIterator(); polygonIt
					.hasNext();) {
				int polygonID = polygonIt.next();

				Iterator<Point> vertexIt = innerPolygons.getPointIterator(polygonID);
				Point polySegmentStart = null;
				Point polySegmentEnd = null;

				while (vertexIt.hasNext()) {
					polySegmentStart = polySegmentEnd;
					polySegmentEnd = vertexIt.next();

					if (polySegmentStart != null) {
						// is start pos within view section
						boolean isStartPosVisible = polySegmentStart.x >= sourceX1
								&& polySegmentStart.x <= sourceX2
								&& polySegmentStart.y >= sourceY1
								&& polySegmentStart.y <= sourceY2;
						boolean isEndPosVisible = polySegmentEnd.x >= sourceX1
								&& polySegmentEnd.x <= sourceX2
								&& polySegmentEnd.y >= sourceY1
								&& polySegmentEnd.y <= sourceY2;

						if (isStartPosVisible && isEndPosVisible) {

							g2.setStroke(new BasicStroke(POLYSEGMENT_STROKE_WIDTH));
							g2.setColor(POLYSEGMENT_INNER_COLOR);
							g2
									.drawLine(
											(int) ((polySegmentStart.x - sourceX1) * pixelSize),
											(int) ((polySegmentStart.y - sourceY1) * pixelSize),
											(int) ((polySegmentEnd.x - sourceX1) * pixelSize),
											(int) ((polySegmentEnd.y - sourceY1) * pixelSize));

							g2.setColor(POLYSEGMENT_VERTEX_COLOR);
							g2
									.fillOval(
											(int) ((polySegmentStart.x - sourceX1) * pixelSize)
													- POLYSEGMENT_VERTEX_SIZE / 2,
											(int) ((polySegmentStart.y - sourceY1) * pixelSize)
													- POLYSEGMENT_VERTEX_SIZE / 2,
											POLYSEGMENT_VERTEX_SIZE,
											POLYSEGMENT_VERTEX_SIZE);
						}
					}
				}
			}
		}
		
		if(outerPolygons != null){
			//go through all outer polygons and draw their vertices
			for (Iterator<Integer> polygonIt = outerPolygons.getContourIterator(); polygonIt
					.hasNext();) {
				int polygonID = polygonIt.next();

				Iterator<Point> vertexIt = outerPolygons.getPointIterator(polygonID);
				Point polySegmentStart = null;
				Point polySegmentEnd = null;

				while (vertexIt.hasNext()) {
					polySegmentStart = polySegmentEnd;
					polySegmentEnd = vertexIt.next();

					if (polySegmentStart != null) {
						// is start pos within view section
						boolean isStartPosVisible = polySegmentStart.x >= sourceX1
								&& polySegmentStart.x <= sourceX2
								&& polySegmentStart.y >= sourceY1
								&& polySegmentStart.y <= sourceY2;
						boolean isEndPosVisible = polySegmentEnd.x >= sourceX1
								&& polySegmentEnd.x <= sourceX2
								&& polySegmentEnd.y >= sourceY1
								&& polySegmentEnd.y <= sourceY2;

						if (isStartPosVisible && isEndPosVisible) {

							g2.setStroke(new BasicStroke(POLYSEGMENT_STROKE_WIDTH));
							g2.setColor(POLYSEGMENT_OUTER_COLOR);
							g2
									.drawLine(
											(int) ((polySegmentStart.x - sourceX1) * pixelSize),
											(int) ((polySegmentStart.y - sourceY1) * pixelSize),
											(int) ((polySegmentEnd.x - sourceX1) * pixelSize),
											(int) ((polySegmentEnd.y - sourceY1) * pixelSize));

							g2.setColor(POLYSEGMENT_VERTEX_COLOR);
							g2
									.fillOval(
											(int) ((polySegmentStart.x - sourceX1) * pixelSize)
													- POLYSEGMENT_VERTEX_SIZE / 2,
											(int) ((polySegmentStart.y - sourceY1) * pixelSize)
													- POLYSEGMENT_VERTEX_SIZE / 2,
											POLYSEGMENT_VERTEX_SIZE,
											POLYSEGMENT_VERTEX_SIZE);
						}
					}
				}
			}
		}
	}


	public void update() {
		sourceX1 = imageComponent.getViewSection().x;
		sourceY1 = imageComponent.getViewSection().y;
		sourceX2 = imageComponent.getViewSection().x
				+ imageComponent.getViewSection().width;
		sourceY2 = imageComponent.getViewSection().y
				+ imageComponent.getViewSection().height;

		// Calculate pixel amount & size in relationship to view section
		pixelAmount = sourceX2 - sourceX1;
		pixelSize = size / (float) pixelAmount;

		repaint();
	}

	public void setOuterContour(Contour outerContour) {
		this.outerContour = outerContour;
	}

	public void setInnerContour(Contour innerContour) {
		this.innerContour = innerContour;
	}

	public void setInnerPolygons(Contour polygons) {
		this.innerPolygons = polygons;
	}
	
	public void setOuterPolygons(Contour polygons) {
		this.outerPolygons = polygons;
	}


	public Dimension getPreferredSize() {

		return new Dimension(size, size);
	}

	public void onAdjacentPixelsChanged() {

	}

	public void onCurrentPixelChanged(final Point pixel) {
		System.out.println("Current Pixel is now X:" + pixel.x + "Y: "
				+ pixel.y);
		//currentPixel = pixel;
		repaint();
	}

	public void onContourChanged() {
		repaint();
	}

	public void onPolyContourChanged() {
		repaint();
	}

	public void onLineSegmentChanged() {
		repaint();
	}
}
