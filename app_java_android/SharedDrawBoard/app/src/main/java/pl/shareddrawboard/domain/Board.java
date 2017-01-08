package pl.shareddrawboard.domain;

import android.graphics.Color;

import pl.shareddrawboard.domain.BoardUpdate;
import pl.shareddrawboard.domain.Point;

/**
 * Created by Arjan on 07.01.2017.
 */
public class Board {

	private int width;
	private int height;
	private int[][] board;

	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		board = new int[height][];
		for (int j = 0; j < height; ++j) {
			board[j] = new int[width];
		}
		for (int j = 0; j < height; ++j) {
			for (int i = 0; i < width; ++i) {
				board[j][i] = Color.WHITE;
			}
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getField(int x, int y) {
		return board[y][x];
	}

	public void update(BoardUpdate boardUpdate) {
		for (Point point : boardUpdate.getPointsDrawn()) {
			update(point, boardUpdate.getBrushSize(), boardUpdate.getBrushColor());
		}
	}

	public void update(Point point, int size, int color) {
		for(int i = point.x - size/2; i < point.x + size/2; ++i) {
			for(int j = point.y - size/2; j < point.y + size/2; ++j) {
				updatePoint(i, j, color);
			}
		}
	}

	private void updatePoint(int x, int y, int color) {
		if (isWithinBoard(x, y)) {
			board[y][x] = color;
		}
	}

	private boolean isWithinBoard(int x, int y) {
		return x >= 0 && y >= 0 && x < width && y < height;
	}
}
