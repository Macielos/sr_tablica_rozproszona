package pl.shareddrawboard;

import android.graphics.Color;

import pl.shareddrawboard.api.BoardUpdate;
import pl.shareddrawboard.api.Point;

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
/*		for (int j = 10; j < 20; ++j) {
			for (int i = 10; i < 20; ++i) {
				board[j][i] = Color.BLACK;
			}
		}*/
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
			update(point, boardUpdate.getBrushColor());
		}
	}

	public void update(Point point, int color) {
		if (isWithinBoard(point.x, point.y)) {
			board[point.y][point.x] = color;
		}
	}

	private boolean isWithinBoard(int x, int y) {
		return x >= 0 && y >= 0 && x < width && y < height;
	}
}
