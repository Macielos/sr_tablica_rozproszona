package pl.shareddrawboard.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arjan on 29.12.2016.
 */

public class BoardUpdate {

	private final List<Point> pointsDrawn = new ArrayList<>();
	private int brushColor;
	private int brushSize;

	public BoardUpdate(int brushColor, int brushSize) {
		this.brushColor = brushColor;
		this.brushSize = brushSize;
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

	public int getBrushSize() {
		return brushSize;
	}

	public void setBrushSize(int brushSize) {
		this.brushSize = brushSize;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BoardUpdate that = (BoardUpdate) o;

		if (brushColor != that.brushColor) return false;
		if (brushSize != that.brushSize) return false;
		return pointsDrawn.equals(that.pointsDrawn);

	}

	@Override
	public int hashCode() {
		int result = pointsDrawn.hashCode();
		result = 31 * result + brushColor;
		result = 31 * result + brushSize;
		return result;
	}
}
