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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import tracer.main.PolyBuildListener;
import tracer.utils.Contour;
import tracer.utils.Vector2D;

public class PolygonBuilder {


	private HashMap<Integer, Point[]> sourceVertices;

	private HashMap<Integer, int[]> straightPathPivots;

	private Vector<PolyBuildListener> polyBuildListeners;

	private Vector2D constraintVec1;
	private Vector2D constraintVec2;

	public PolygonBuilder() {
		this.polyBuildListeners = new Vector<PolyBuildListener>();

		constraintVec1 = new Vector2D();
		constraintVec2 = new Vector2D();
		
		//copy vertices of contours into array data structure for faster look-ups
		sourceVertices = new HashMap<Integer,Point[]>();
		
		//create pivots array to store maximum straight paths
		straightPathPivots = new HashMap<Integer, int[]>();
	}
	
	private void reset(){
		sourceVertices.clear();
		straightPathPivots.clear();
		
		constraintVec1.x = 0;
		constraintVec1.y = 0;
		
		constraintVec2.x = 0;
		constraintVec2.y = 0;
	}
	
	public Contour getPolygons(Contour contours)
	{
		//reset all data before searching
		reset();
		
		//copy contour point into array data structure for faster lookup during search
		Iterator<Integer> contourIt = contours.getContourIterator();
		Iterator<Point> contourPointIt;
		while(contourIt.hasNext()){
			int contourID = contourIt.next();
			contourPointIt = contours.getPointIterator(contourID);
			Point contourVertices[] = new Point[contours.getContourSize(contourID)];
			
			for (int i = 0; i < contourVertices.length; ++i) {
				contourVertices[i] = contourPointIt.next();
			}
			
			//add pivots array to store maximum straight paths for each contour
			int pivots[] = new int[contourVertices.length];
			
			sourceVertices.put(contourID, contourVertices);
			
			straightPathPivots.put(contourID, pivots);
			
			searchStraightPaths(contourID);
			
			
		}
		
		//search straight paths, valid polygon segments and the optimal polygon for each contour 		
		return findOptimalPolygon();
	}

	private void searchStraightPaths(int contourID) {
		
		Point contourVertices[] = sourceVertices.get(contourID);
		Point examinedVertex = null;
		Point previousVertex = null;
		boolean directionsChanged[] = new boolean[4];;

		// go through all source vertices and find their maximum straight paths
		for (int currVertexIndex = 0; currVertexIndex < contourVertices.length; ++currVertexIndex) {

			boolean isStraightPathFinished = false;

			// reset constraints
			constraintVec1.x = 0;
			constraintVec1.y = 0;
			constraintVec2.x = 0;
			constraintVec2.y = 0;
			
			
			// reset directions
			directionsChanged[0] = false;
			directionsChanged[1] = false;
			directionsChanged[2] = false;
			directionsChanged[3] = false;

			// with each step increase the examined index and check if there is
			// still a valid straight path
			int examinedIndex = (currVertexIndex + 1)
					% (contourVertices.length);

			while (!isStraightPathFinished) {

				
				// get a reference to the examined vertex to check for direction
				// changes
				examinedVertex = contourVertices[examinedIndex];
				if (examinedIndex == 0) {
					previousVertex = contourVertices[contourVertices.length - 1];
				} else {
					previousVertex = contourVertices[examinedIndex - 1];
				}

				// there is a valid straight path between the examined index and
				// current index if all vertices are within the 0.5 maxNorm
				// distance
				// check against constraint
				Vector2D direction = new Vector2D();

				direction.x = contourVertices[examinedIndex].x
						- contourVertices[currVertexIndex].x;
				direction.y = contourVertices[examinedIndex].y
						- contourVertices[currVertexIndex].y;
				
				if ((Vector2D.cross(constraintVec1, direction) < 0)
						|| (Vector2D.cross(constraintVec2, direction) > 0)) {

					// store max path index
					straightPathPivots.get(contourID)[currVertexIndex] = examinedIndex;
					isStraightPathFinished = true;
				}

				updateConstraint(direction);

				// register direction change
				// right
				if (examinedVertex.x > previousVertex.x)
					directionsChanged[0] = true;
				// up
				if (examinedVertex.y < previousVertex.y)
					directionsChanged[1] = true;
				// left
				if (examinedVertex.x < previousVertex.x)
					directionsChanged[2] = true;
				// down
				if (examinedVertex.y > previousVertex.y)
					directionsChanged[3] = true;

				// If there are no more than three direction changes it is no
				// longer a straight path
				// direction changes
				int directionChanges = 0;
				for (int i = 0; i < 4; ++i) {
					if (directionsChanged[i] == true) {
						++directionChanges;
					}
				}

				if (directionChanges > 3) {
					isStraightPathFinished = true;
					straightPathPivots.get(contourID)[currVertexIndex] = examinedIndex;
				}

				examinedIndex = (examinedIndex + 1)
						% (contourVertices.length);
			}
		}
	}
	
	private void updateConstraint(Vector2D direction) {
		// constraint 1
		Vector2D constraintTmp1 = new Vector2D();
		Vector2D constraintTmp2 = new Vector2D();

		if (!(java.lang.Math.abs(direction.x) <= 1 && java.lang.Math
				.abs(direction.y) <= 1)) {
			if (direction.y >= 0 && (direction.y > 0 || direction.x < 0)) {
				constraintTmp1.x = direction.x + 1;
			} else {
				constraintTmp1.x = direction.x - 1;
			}

			if (direction.x <= 0 && (direction.x < 0 || direction.y < 0)) {
				constraintTmp1.y = direction.y + 1;
			} else {
				constraintTmp1.y = direction.y - 1;
			}

			if (Vector2D.cross(constraintVec1, constraintTmp1) >= 0) {
				constraintVec1 = constraintTmp1;
			}

			if (direction.y <= 0 && (direction.y < 0 || direction.x < 0)) {
				constraintTmp2.x = direction.x + 1;
			} else {
				constraintTmp2.x = direction.x - 1;
			}

			if (direction.x >= 0 && (direction.x > 0 || direction.y < 0)) {
				constraintTmp2.y = direction.y + 1;
			} else {
				constraintTmp2.y = direction.y - 1;
			}

			if (Vector2D.cross(constraintVec2, constraintTmp2) <= 0) {
				constraintVec2 = constraintTmp2;
			}
		}
	}

	private Contour findOptimalPolygon() {
		
		Contour optimalPolygons = new Contour();
		
		Iterator<Integer> contourIt = sourceVertices.keySet().iterator();
		
		while(contourIt.hasNext()){
			int contourID = contourIt.next();
			
			LinkedList<Integer> currentPolygon = new LinkedList<Integer>();
			Point contourVertices[] = sourceVertices.get(contourID);
			int pivots[] = straightPathPivots.get(contourID);

			for (int polygonStartIndex = 0; polygonStartIndex < contourVertices.length-1; ++polygonStartIndex) {

				currentPolygon.clear();

				boolean isPolygonClosed = false;
				int segmentStartIndex = polygonStartIndex;
				int segmentEndIndex = (segmentStartIndex + 1)
						% (contourVertices.length-1);

				// add first index
				currentPolygon.add(segmentStartIndex);

				while (!isPolygonClosed) {

					boolean isValidSegment = true;
					// find the largest possible segment, beginning at the segment
					// start index
					// the end gets increased until the segment is no longer valid
					while (isValidSegment) {

						// calculate the cyclic difference of the examined index and
						// the current index
						int cyclicDifference = getCyclicDifference(contourID,
								segmentStartIndex, segmentEndIndex);

						int extendedSegmentStart = (segmentStartIndex - 1)
								% (contourVertices.length-1);
						if (extendedSegmentStart < 0)
							extendedSegmentStart += contourVertices.length;
						int extendedSegmentEnd = (segmentEndIndex + 1)
								% (contourVertices.length-1);

						int extendedSegmentDifference = getCyclicDifference(contourID,
								extendedSegmentStart, extendedSegmentEnd);
						int pivotDifference = getCyclicDifference(contourID,
								extendedSegmentStart, pivots[extendedSegmentStart]-1);

						if (segmentEndIndex == polygonStartIndex) {
							isPolygonClosed = true;
							break;
						}
						
						if(pivotDifference==0){
							
						}

						if (cyclicDifference > contourVertices.length - 3) {
							System.out.println("Segment + " + segmentStartIndex
									+ " " + segmentEndIndex
									+ " violates cyclic requirement");
							isValidSegment = false;
							break;
						}

						if (extendedSegmentDifference > pivotDifference) {
							isValidSegment = false;
							break;
						}

						segmentEndIndex = (segmentEndIndex + 1)
								% (contourVertices.length);
					}

					// add to current polygon
					currentPolygon.add(segmentEndIndex);

					segmentStartIndex = segmentEndIndex;
				}

				if (optimalPolygons.getContourSize(contourID) == 0 || optimalPolygons.getContourSize(contourID) > currentPolygon.size()) {
					System.out.println("Found new optimal polygon with "
							+ currentPolygon.size() + " points");
					//optimalPolygon = currentPolygon;
					//polygonVertices.addContour(contourID, currentPolygon);

					// clear old poly vertices
					optimalPolygons.clear(contourID);

					Iterator<Integer> polyIndexIt = currentPolygon.iterator();
					while (polyIndexIt.hasNext()) {
						optimalPolygons.addPoint(contourID,
								contourVertices[polyIndexIt.next()]);
					}
				}
			}
		}
		
		return optimalPolygons;
	}

	private int getCyclicDifference(int contourID, int startIndex, int endIndex) {

		Point contourVertices[] = sourceVertices.get(contourID);
		
		// calculate the cyclic difference of the examined index and the current
		// index
		int cyclicDifference;
		if (startIndex <= endIndex) {
			cyclicDifference = endIndex - startIndex;
		} else {
			cyclicDifference = endIndex - startIndex
					+ contourVertices.length;
		}

		return cyclicDifference;
	}

//	private boolean isStraightPath(int contourID, int startIndex, int endIndex) {
//
//		Point contourVertices[] = sourceVertices.get(contourID);
//		
//		if (startIndex >= 0 && endIndex <= contourVertices.length - 1) {
//			// calculate the cyclic difference of the examined index and the
//			// current index
//			int cyclicDifference = getCyclicDifference(contourID, startIndex, endIndex);
//
//			// there is a valid straight path between the examined index and
//			// current index if all vertices are within the 0.5 maxNorm distance
//			// do not check examined index and current index as they obviously
//			// define the line
//			for (int i = 1; i < cyclicDifference; ++i) {
//
//				int pathStartIndex = startIndex
//						% (contourVertices.length - 1);
//				int indexToCheck = (startIndex + i)
//						% (contourVertices.length - 1);
//
//				double pointDistance = Math.distanceToSegment(
//						contourVertices[pathStartIndex],
//						contourVertices[endIndex],
//						contourVertices[indexToCheck]);
//
//				if (pointDistance > 0.5) {
//					return false;
//				}
//			}
//		}
//		return true;
//	}

	public void addListener(PolyBuildListener newListener) {
		polyBuildListeners.add(newListener);
	}

	public void removeListener(PolyBuildListener oldListener) {
		polyBuildListeners.remove(oldListener);
	}
}
