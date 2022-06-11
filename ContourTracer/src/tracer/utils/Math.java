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

package tracer.utils;

import java.awt.Point;
import java.awt.geom.Point2D;

public class Math {	
	/** Calculates the distance between two points
	 */
	public static double distance(Point2D.Double A, Point2D.Double B) {
		double d1 = A.x - B.x;
		double d2 = A.y - B.y;
		
		return java.lang.Math.sqrt(d1 * d1 + d2 * d2);
	}
	
	/** Calculates the distance from C to the line AB
	 */
	public static double pointLineDistance(Point2D.Double A, Point2D.Double B, Point2D.Double C) {
		Vector2D vecAB = new Vector2D(B.x - A.x, B.y - A.y);
		Vector2D vecAC = new Vector2D(C.x - A.x, C.y - A.y);
		
		double distance = Vector2D.cross(vecAB, vecAC) / distance(A, B);
		
		return java.lang.Math.abs(distance);
	}
	
//	/** Calculates the distance from C to the line AB
//	 */
//	public static double pointLineDistance(Point A, Point B, Point C) {
//		Vector2D vecAB = new Vector2D(B.x - A.x, B.y - A.y);
//		Vector2D vecAC = new Vector2D(C.x - A.x, C.y - A.y);
//		
//		double distance = Vector2D.cross(vecAB, vecAC) / distance(A, B);
//		
//		return java.lang.Math.abs(distance);
//	}
	
	 public static double distanceToSegment(Point p1, Point p2, Point p3) {
		final double xDelta = p2.x - p1.x;
		final double yDelta = p2.y - p1.y;

		if ((xDelta == 0) && (yDelta == 0)) {
			throw new IllegalArgumentException("p1 and p2 cannot be the same point");
		}

		final double u = ((p3.x - p1.x) * xDelta + (p3.y - p1.y) * yDelta)
				/ (xDelta * xDelta + yDelta * yDelta);

		final Point2D closestPoint;
		if (u < 0) {
			closestPoint = p1;
		} else if (u > 1) {
			closestPoint = p2;
		} else {
			closestPoint = new Point2D.Double(p1.getX() + u * xDelta, p1.getY() + u * yDelta);
		}

		return maxNormDistance(closestPoint.getX(), closestPoint.getY(),  p3.x, p3.y);
	}
	
	public static double maxNormDistance(double aX, double aY, double bX, double bY) {
		double a = java.lang.Math.abs(aX - bX);
		double b = java.lang.Math.abs(aY - bY);
		
		if (a > b) {
			return a;
		} else {
			return b;
		}
	}
}
