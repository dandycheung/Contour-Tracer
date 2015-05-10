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
import java.util.Iterator;
import java.util.Vector;

import tracer.gui.ImageComponent;
import tracer.main.TraceListener;
import tracer.utils.Contour;

public class Potracer {

	private ImageComponent sourceImage;
	private Contour outerContour;
	private Contour innerContour;

	private int adjacentPixelIndices[];
	private int contourID;

	private int sourcePixels[];
	private int destinationPixels[];

	private int orientation;

	private Vector<TraceListener> traceListeners;

	public Potracer() {
		this.sourceImage = null;

		this.outerContour = new Contour();
		this.innerContour = new Contour();

		this.adjacentPixelIndices = new int[4];

		traceListeners = new Vector<TraceListener>();

		this.orientation = 0;
	}
	
	public void setImage(final ImageComponent sourceImage){
		this.sourceImage = sourceImage;
		this.sourcePixels = sourceImage.getPixels();
		this.destinationPixels = java.util.Arrays.copyOf(sourcePixels, sourcePixels.length);
		
		this.outerContour = new Contour();
		this.innerContour = new Contour();
		this.contourID = 0;
		
		trace();
	}

	public void trace() {
		if(this.sourceImage != null){
			for (int i = 0; i < destinationPixels.length - 1; ++i) {
				// first foreground pixel
				if (destinationPixels[i] != 0xffffffff) {
					Point currentVertex = new Point(i % sourceImage.getImageWidth(), i / sourceImage.getImageWidth());

					// check if we hit outer or inner contour
					if (sourcePixels[i] == 0xff000000
							&& sourcePixels[i - 1] == 0xffffffff) {
						followContour(currentVertex, false);
					} else {
						followContour(currentVertex, true);
					}
				}
			}

			// inform listeners
			for (Iterator<TraceListener> listenerIt = traceListeners.iterator(); listenerIt
					.hasNext();) {
				TraceListener listener = listenerIt.next();
				listener.onContourChanged();
			}
		}
	}

	private void followContour(final Point startVertex, boolean isInnerContour) {
		// store first vertex (used to determine if the end of the contour has
		// been reached)
		Point firstVertex = new Point(startVertex);
		Point currentVertex = new Point(startVertex.x, startVertex.y);

		boolean isEndReached = false;
		
		//walk along contour until we reach the first vertex again
		while (!isEndReached) {
			
			//add the new point
			if (isInnerContour) {
				innerContour.addPoint(contourID, new Point(currentVertex.x,
						currentVertex.y));
			} else {
				outerContour.addPoint(contourID, new Point(currentVertex.x,
						currentVertex.y));
			}
			
			// vertex index to pixel index
			int pixelIndex = currentVertex.y * sourceImage.getImageWidth()
					+ currentVertex.x;

			adjacentPixelIndices[0] = pixelIndex - sourceImage.getImageWidth()
					- 1;
			adjacentPixelIndices[1] = pixelIndex - sourceImage.getImageWidth();
			adjacentPixelIndices[2] = pixelIndex - 1;
			adjacentPixelIndices[3] = pixelIndex;
			
			boolean newVertexFound = false;

			//try each direction
			while (!newVertexFound) {
				
				orientation = (orientation + 3) % 4;

				switch (orientation) {
				// right
				case 0:
					if(isInBounds(adjacentPixelIndices[3]) && isInBounds(adjacentPixelIndices[1])){
						if (destinationPixels[adjacentPixelIndices[3]] == 0xffffffff
								&& destinationPixels[adjacentPixelIndices[1]] == 0xff000000) {
							newVertexFound = true;
							++currentVertex.x;
						}
					}
					break;
				// up
				case 1:
					if(isInBounds(adjacentPixelIndices[1]) && isInBounds(adjacentPixelIndices[0])){
						if (destinationPixels[adjacentPixelIndices[1]] == 0xffffffff
								&& destinationPixels[adjacentPixelIndices[0]] == 0xff000000) {
							newVertexFound = true;
							--currentVertex.y;
						}
					}
					break;
				// left
				case 2:
					if(isInBounds(adjacentPixelIndices[0]) && isInBounds(adjacentPixelIndices[2])){
						if (destinationPixels[adjacentPixelIndices[0]] == 0xffffffff
								&& destinationPixels[adjacentPixelIndices[2]] == 0xff000000) {
							newVertexFound = true;
							--currentVertex.x;
						}
					}
					break;
				// down
				case 3:
					if(isInBounds(adjacentPixelIndices[2]) && isInBounds(adjacentPixelIndices[3])){
						if (destinationPixels[adjacentPixelIndices[2]] == 0xffffffff
								&& destinationPixels[adjacentPixelIndices[3]] == 0xff000000) {
							newVertexFound = true;
							++currentVertex.y;
						}
					}
					break;
				default:
					break;
				}
			}
			
			if(currentVertex.x == firstVertex.x && currentVertex.y == firstVertex.y){
				isEndReached = true;
				//add the last point to close contour
				if (isInnerContour) {
					innerContour.addPoint(contourID, new Point(currentVertex.x,
							currentVertex.y));
				} else {
					outerContour.addPoint(contourID, new Point(currentVertex.x,
							currentVertex.y));
				}
			}
		}

		// invert pixels inside contour
		invertPixelInContour(contourID, isInnerContour);
		++contourID;

	}

	private void invertPixelInContour(int contourID, boolean isInnerContour) {
		Iterator<Point> pointIt;

		if (isInnerContour) {
			pointIt = innerContour.getPointIterator(contourID);
		} else {
			pointIt = outerContour.getPointIterator(contourID);
		}

		Point previousPoint = null;
		Point currentPoint = null;

		while (pointIt.hasNext()) {
			previousPoint = currentPoint;
			currentPoint = pointIt.next();

			if (previousPoint != null) {
				if (currentPoint.y > previousPoint.y) {
					int pixelIndex = previousPoint.y
							* sourceImage.getImageWidth()
							+ previousPoint.x;

					invertRow(pixelIndex);
				} else if (currentPoint.y < previousPoint.y) {
					int pixelIndex = currentPoint.y
							* sourceImage.getImageWidth() + currentPoint.x;

					invertRow(pixelIndex);
				}
			}
		}
	}

	private void invertRow(int startIndex) {
		for (int i = startIndex; i % sourceImage.getImageWidth() < sourceImage
				.getImageWidth() - 1; ++i) {
			// destinationPixels[i] = (byte)(255-destinationPixels[i]);

			if (destinationPixels[i] == 0xff000000) {
				destinationPixels[i] = 0xffffffff;
			} else {
				destinationPixels[i] = 0xff000000;
			}
		}
	}

	public Contour getInnerContour() {
		return this.innerContour;
	}

	public Contour getOuterContour() {
		return this.outerContour;
	}

	public void addTraceListener(TraceListener listener) {
		traceListeners.add(listener);
	}

	public void removeTraceListener(TraceListener listener) {
		traceListeners.remove(listener);
	}
	
	private boolean isInBounds(int pixelIndex){
		if(pixelIndex >= 0 && pixelIndex <= destinationPixels.length){
			return true;
		} else {
			return false;
		}
	}

}
