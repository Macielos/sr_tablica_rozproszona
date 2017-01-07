package pl.shareddrawboard.api;

/**
 * Created by Arjan on 29.12.2016.
 */

public class Point {

	public final int x;
	public final int y;

	public Point(float x, float y) {
		this((int) x, (int) y);
	}

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "[" + x + ", " + y + "]";
	}
}
