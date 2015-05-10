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

public class Vector2D {
	public double x;
	public double y;
	
	public Vector2D(){
		x=0;
		y=0;
	}
	
	public Vector2D(final double x, final double y){
		this.x = x;
		this.y = y;
	}
	
	public static double dot(Vector2D v1, Vector2D v2){
		return v1.x * v2.x + v1.y * v1.y;
	}
	
	public static double cross(Vector2D v1, Vector2D v2){
		return v1.x * v2.y - v1.y * v2.x;
	}
	
	public double length(){
		return java.lang.Math.sqrt(x * x + y * y);
	}
	
	public void normalize(){
		double length=length();
		this.x /= length;
		this.y /= length;
	}
	
	public void add(final Vector2D vec){
		x += vec.x;
		y += vec.y;
	}
	
	public void subtract(final Vector2D vec){
		x -= vec.x;
		y -= vec.y;
	}
	
	public void multiply(final double scalar){
		x *= scalar;
		y *= scalar;
	}
	
	public void multiply(final Vector2D vec){
		x *= vec.x;
		y *= vec.x;
	}
	
	
	public void divide(final double scalar){
		x /= scalar;
		y /= scalar;
	}

}
