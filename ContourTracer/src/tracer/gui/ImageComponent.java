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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

public class ImageComponent extends JComponent {
	private static final long serialVersionUID = -7037485372677445280L;

	private final static int MIN_WIDTH = 200;
	private final static int MIN_HEIGHT = 200;
	public final static int MIN_SECTION_SIZE = 1;

	private BufferedImage image;
	private int imageWidth;
	private int imageHeight;
	private int pixels[];
	
	private int sectionSize;
	private int maxSectionSize;

	private Rectangle viewSection;
	private boolean isViewSectionVisible;
	
	public ImageComponent() {
		this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		// 1px simple border
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
	}
	
	public void loadImage(final File imagePath) {
		try {
			// BufferedImage newImage = ImageIO.read(imagePath);
			image = ImageIO.read(imagePath);

			this.imageWidth = image.getWidth();
			this.imageHeight = image.getHeight();
			resetViewSection();
			repaint();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					"Bild konnte nicht geladen werden.", "Fehler",
					JOptionPane.ERROR_MESSAGE);
			image = new BufferedImage(MIN_WIDTH, MIN_HEIGHT,
					BufferedImage.TYPE_INT_ARGB);
			this.imageWidth = MIN_WIDTH;
			this.imageHeight = MIN_HEIGHT;
		}
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
//        g2.setComposite(AlphaComposite.getInstance(
//                AlphaComposite.SRC_OVER, 1.0f));	
		g2.drawImage(image, null, 0, 0);
		if (isViewSectionVisible) {
			g2.drawRect(viewSection.x,viewSection.y, viewSection.width, viewSection.height);
		}

		g2.dispose();
	}
	
	public Dimension getPreferredSize() {
		if (image != null)
			return new Dimension(image.getWidth(), image.getHeight());
		else
			return new Dimension(MIN_WIDTH, MIN_HEIGHT);
	}
	
	public int getImageWidth() {
		return this.imageWidth;
	}
	
	public int getImageHeight() {
		return this.imageHeight;
	}
	
	public void setSectionPostion(final Point p) {
		// check boundaries
		if (p.x >= 0 && p.y >= 0 && p.x <= imageWidth && p.y <= imageHeight) {
			viewSection.x = p.x - viewSection.width / 2;
			viewSection.y = p.y - viewSection.height / 2;
		}
		repaint();
	}
	
	public Rectangle getViewSection() {
		return viewSection;
	}
	
	public int getViewSectionSize() {
		return viewSection.width;
	}
	
	public void setViewSectionSize(int newSize) {
		viewSection.width = newSize;
		viewSection.height = newSize;
		repaint();
	}
	
	public int[] getPixels() {
		// get reference to internal pixels array
		if (pixels == null) {
			pixels = new int[imageWidth * imageHeight];
			image.getRGB(0, 0, imageWidth, imageHeight, pixels, 0, imageWidth);
		} else if(pixels.length != imageWidth * imageHeight) {
			pixels = new int[imageWidth * imageHeight];
			image.getRGB(0, 0, imageWidth, imageHeight, pixels, 0, imageWidth);
		}
		return pixels;
	}
	
	public void setPixels(int[] pixels) {
		// set pixels with same dimension
		setPixels(pixels, imageWidth, imageHeight);
	}
	
	public void setPixels(int[] pixels, int width, int height) {
		// set pixels with arbitrary dimension
		if (pixels == null || pixels.length != width * height)
		    throw new IndexOutOfBoundsException();
	
		if (width != imageWidth || height != imageHeight) {
			// image dimension changed
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			imageWidth = width;
			imageHeight = height;
			pixels = null;
		}
		
		image.setRGB(0, 0, width, height, pixels, 0, width);
		
		if (pixels != null && pixels != this.pixels) {
			// update internal pixels array
			System.arraycopy(pixels, 0, this.pixels, 0, java.lang.Math.min(pixels.length, this.pixels.length));
		}

		this.invalidate();
		this.repaint();
	}

	public boolean isViewSectionVisible() {
		return isViewSectionVisible;
	}

	public void setViewSectionVisible(boolean isViewSectionVisible) {
		this.isViewSectionVisible = isViewSectionVisible;
	}
	
	public int getMaxSectionSize() {
		return maxSectionSize;
	}
	
	private void resetViewSection(){
		if (imageWidth <= imageHeight) {
			this.sectionSize = imageWidth/2;
			this.maxSectionSize = imageWidth;
		} else {
			this.sectionSize = imageHeight/2;
			this.maxSectionSize = imageHeight;
		}

		this.isViewSectionVisible = true;
		this.viewSection = new Rectangle(new Point(0,0),new Dimension(sectionSize, sectionSize));
	}
}
