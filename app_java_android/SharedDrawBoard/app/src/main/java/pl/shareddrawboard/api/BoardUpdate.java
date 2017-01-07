package pl.shareddrawboard.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arjan on 29.12.2016.
 */

public class BoardUpdate {

	private final List<Point> pointsDrawn = new ArrayList<>();
	private int brushColor;

	public BoardUpdate(int brushColor) {
		this.brushColor = brushColor;
	}

	public void addPointDrawn(Point point) {
		pointsDrawn.add(point);
	}

	public List<Point> getPointsDrawn() {
		return pointsDrawn;
	}

	public int getBrushColor() {
		return brushColor;
	}

	public void setBrushColor(int color) {
		this.brushColor = color;
	}
}
