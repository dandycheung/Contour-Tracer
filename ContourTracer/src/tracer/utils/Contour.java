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
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class Contour {

	Hashtable<Integer, LinkedList<Point>> contours;

	public Contour() {

		contours = new Hashtable<Integer, LinkedList<Point>>();

	}

	public void addPoint(final int contourID, final Point p) {
			LinkedList<Point> contour;	
			if(contours.containsKey(contourID)){
				contour = contours.get(contourID);
			} else {
				contour = new LinkedList<Point>();
				contours.put(contourID, contour);
			}
			contour.add(p);
	}
	
	public void addContour(final int contourID, final LinkedList<Point> contour){
		contours.put(contourID, contour);
	}

	public Iterator<Point> getPointIterator(final int contourID) {
		if (contours.containsKey(contourID)) {
			return contours.get(contourID).iterator();
		} else { 
			return Collections.<Point> emptyList().iterator();
		}
	}
	
	public Iterator<Integer> getContourIterator(){
			return contours.keySet().iterator();
	}
	
	public int getSize(){
		return contours.size();
	}
	
	public int getContourSize(final int contourID){
		if (contours.containsKey(contourID)){
			return contours.get(contourID).size();
		} else {
			return 0;
		}
	}
	
	public void clear(final int contourID){
		if (contours.containsKey(contourID)){
			contours.get(contourID).clear();
		}
	}
	
	public boolean hasContour(final int contourID){
		return contours.containsKey(contourID);
	}
}
