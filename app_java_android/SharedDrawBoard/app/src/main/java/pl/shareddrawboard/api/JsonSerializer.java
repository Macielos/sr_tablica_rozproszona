package pl.shareddrawboard.api;

import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.shareddrawboard.domain.BoardUpdate;
import pl.shareddrawboard.domain.Point;

/**
 * Created by Arjan on 05.01.2017.
 */

public class JsonSerializer {

	public static String toJson(int x, int y, int oldX, int oldY) throws JSONException {
		return new JSONObject().put("type", "drag").put("x", x).put("y", y).put("oldX", oldX).put("oldY", oldY).toString();
	}

	public static String toJson(String sender, BoardUpdate boardUpdate) throws JSONException {
		JSONObject baseJsonObject = baseJson("drag", sender);
		JSONObject boardUpdateJsonObject = baseJsonObject.getJSONObject("args");
		JSONArray jsonPoints = new JSONArray();
		for (Point point : boardUpdate.getPointsDrawn()) {
			JSONObject jsonPoint = new JSONObject();
			jsonPoint.put("x", point.x);
			jsonPoint.put("y", point.y);
			jsonPoints.put(jsonPoint);
		}
		boardUpdateJsonObject.put("points", jsonPoints);
		boardUpdateJsonObject.put("brushColor", boardUpdate.getBrushColor());
		boardUpdateJsonObject.put("brushSize", boardUpdate.getBrushSize());
		return baseJsonObject.toString();
	}

	private static JSONObject baseJson(String action, String sender) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", action);
		jsonObject.put("sender", sender);
		jsonObject.put("args", new JSONObject());
		return jsonObject;
	}

	public static BoardUpdate fromJson(JSONObject jsonObject) throws JSONException {
		JSONObject boardUpdateJsonObject = jsonObject.getJSONObject("args");
		BoardUpdate boardUpdate = new BoardUpdate(boardUpdateJsonObject.getInt("brushColor"), boardUpdateJsonObject.getInt("brushSize"));
		JSONArray jsonPoints = boardUpdateJsonObject.getJSONArray("points");
		for (int i = 0; i < jsonPoints.length(); ++i) {
			JSONObject jsonPoint = jsonPoints.getJSONObject(i);
			boardUpdate.addPointDrawn(new Point(jsonPoint.getInt("x"), jsonPoint.getInt("y")));
		}
		return boardUpdate;
	}

	public static Pair<Point, Point> pointPairFromJson(JSONObject messageJsonObject) throws JSONException {
		return new Pair<>(
				new Point(messageJsonObject.getInt("x"), messageJsonObject.getInt("y")),
				new Point(messageJsonObject.getInt("oldX"), messageJsonObject.getInt("oldY")));
	}
}
