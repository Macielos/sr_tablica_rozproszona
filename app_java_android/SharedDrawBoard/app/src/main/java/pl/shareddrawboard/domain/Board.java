package pl.shareddrawboard.domain;

import android.graphics.Color;
import android.util.Pair;

import pl.shareddrawboard.domain.BoardUpdate;
import pl.shareddrawboard.domain.Point;

/**
 * Created by Arjan on 07.01.2017.
 */
public class Board {

	private int width;
	private int height;
	private int[][] board;

	private String name;
	private String ip;
	private String id;
	private String ipAddress;

	public Board(String name, String ip, String id) {
		this(name, ip, id, 200, 150);
	}

	public Board(String name, String ip, String id, int width, int height) {
		this.name = name;
		this.ip = ip;
		this.id = id;

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

	public void update(Pair<Point, Point> pointPair) {
		updatePoint(pointPair.first.x, pointPair.first.y, Color.BLACK);
		updatePoint(pointPair.second.x, pointPair.second.y, Color.BLACK);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Board board = (Board) o;

		return id != null ? id.equals(board.id) : board.id == null;

	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	public String getName() {
		return name;
	}

	public String getIp() {
		return ip;
	}

}
