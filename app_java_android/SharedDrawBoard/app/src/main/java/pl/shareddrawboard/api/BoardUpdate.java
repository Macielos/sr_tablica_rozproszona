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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BoardUpdate that = (BoardUpdate) o;

		if (brushColor != that.brushColor) return false;
		return pointsDrawn.equals(that.pointsDrawn);

	}

	@Override
	public int hashCode() {
		int result = pointsDrawn.hashCode();
		result = 31 * result + brushColor;
		return result;
	}
}
