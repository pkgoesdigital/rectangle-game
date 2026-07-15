import java.awt.Color;
import java.util.Random;

import edu.princeton.cs.introcs.StdDraw;

public class MovingRectangle {

	//initializing private variables for rectangles
	private double xCoord, yCoord;
	private double width, height;
	// x velocity & y velocity
	private double xv, yv;
	private Color randomColor;
	private boolean isFrozen;
	private int remainingClicks;
	private int r, g, b;

	//create 5 rectangle objects & draw on canvas
	// NOTE: StdDraw.filledRectangle takes HALF width and HALF height, so
	// these fields are half-extents — which is why collidesWith/hasMouse
	// below compare against xCoord +/- width rather than width/2.
	public MovingRectangle(double xCoord, double yCoord, double width,
			double height, double xv, double yv, int remainingClicks) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.width = width;
		this.height = height;
		this.xv = xv;
		this.yv = yv;
		this.isFrozen = false;
		this.remainingClicks = remainingClicks;
		// Give it a colour up front. Without this, r/g/b stay 0 and every
		// rectangle is drawn black until it happens to hit a wall.
		randomColor();
	}

	//create new random object for rng of r, g, b values
	private final Random rng = new Random();

	public void randomColor() {
		r = rng.nextInt(256);
		g = rng.nextInt(256);
		b = rng.nextInt(256);
		randomColor = new Color(r, g, b);
	}

	//exposed so tests can check the colour without opening a window
	public Color getColor() {
		return randomColor;
	}

	public void draw() {
		if (isFrozen) {
			StdDraw.setPenColor(StdDraw.RED);
			StdDraw.filledRectangle(xCoord, yCoord, width, height);
		}
		else {
			StdDraw.setPenColor(r, g, b);
			StdDraw.filledRectangle(xCoord, yCoord, width, height);
			//displaying number of remaining clicks for each rectangle
			StdDraw.setPenColor(StdDraw.WHITE);
			StdDraw.setPenRadius(0.01);
			StdDraw.text(xCoord, yCoord, " " + remainingClicks);
		}
	}

	public void animate() {
		//check boolean to see if rectangle isFrozen
		if (isFrozen) {
			return;
		}
		//changing velocities, color of rectangle objects, and yCoord xCoord
		xCoord += xv;
		yCoord += yv;
		if (xCoord + width > 100) {
			//negative velocity stops movement of whatever value xv is
			xv *= -1;
			xCoord = 100 - width;
			//randomColor method to change rectangle color when unfrozen
			randomColor();
		}
		else if (xCoord - width < 0) {
			xv *= -1;
			xCoord = 0 + width;
			randomColor();
		}
		if (yCoord - height < 0) {
			yv *= -1;
			yCoord = 0 + height;
			randomColor();
		}
		else if (yCoord + height > 100) {
			yv *= -1;
			yCoord = 100 - height;
			randomColor();
		}

	}

	public void freeze() {
		isFrozen = true;
	}

	//checks isFrozen boolean
	public void unfreeze() {
		if (!isFrozen) {
			return;
		}
		//give it a fresh click count for its next life
		resetRemainingClicks();
		//changes isFrozen to false if isFrozen is true
		isFrozen = false;
	}

	//use to check if rectangle isFrozen
	public boolean isFrozen() {
		return isFrozen;
	}

	//reset clicks when rectangles become unfrozen
	public void resetRemainingClicks() {
		remainingClicks = rng.nextInt(3) + 1;
	}

	//setter method
	public void setRemainingClicks(int clicks) {
		remainingClicks = clicks;
	}

	//getter method
	public int getRemainingClicks() {
		return remainingClicks;
	}

	//position and velocity getters — the game itself never needs these, but
	//they let the tests check movement and bouncing without opening a window
	public double getX() {
		return xCoord;
	}

	public double getY() {
		return yCoord;
	}

	public double getXVelocity() {
		return xv;
	}

	public double getYVelocity() {
		return yv;
	}

	//checks values for rectangle objects to keep randomly generated rectangle dimensions on the StdDraw canvas
	public boolean hasMouse(double a, double b) {
		return (a > xCoord - width) && (a < xCoord + width) //left & right edges
				&& (b > yCoord - height) && (b < yCoord + height); //bottom & top edges
	}

	//checks dimensions of other rectangles so MovingRectangle objects unfreeze with collision
	public boolean collidesWith(MovingRectangle other)   {
		return xCoord - width < other.xCoord + other.width && 
				xCoord + width > other.xCoord - other.width && 
				yCoord - height < other.yCoord + other.height && 
				yCoord + height > other.yCoord - other.height;
	}
}