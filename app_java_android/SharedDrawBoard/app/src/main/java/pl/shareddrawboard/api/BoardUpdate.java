package pl.shareddrawboard.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arjan on 29.12.2016.
 */

public class BoardUpdate {

	private final List<Point> pixelsDrawn = new ArrayList<>();
	private int brushColor;

	public BoardUpdate(int brushColor) {
		this.brushColor = brushColor;
	}

	public void addPixelDrawn(Point point) {
		pixelsDrawn.add(point);
	}

	public List<Point> getPixelsDrawn() {
		return pixelsDrawn;
	}

	public int getBrushColor() {
		return brushColor;
	}

	public void setBrushColor(int color) {
		this.brushColor = color;
	}
}
