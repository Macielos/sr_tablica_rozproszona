package pl.shareddrawboard.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Arjan on 05.01.2017.
 */

public class JsonSerializer {

	public String toJson(BoardUpdate boardUpdate) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonPoints = new JSONArray();
		for (Point point : boardUpdate.getPointsDrawn()) {
			JSONObject jsonPoint = new JSONObject();
			jsonPoint.put("x", point.x);
			jsonPoint.put("y", point.y);
			jsonPoints.put(jsonPoint);
		}
		jsonObject.put("points", jsonPoints);
		jsonObject.put("brushColor", boardUpdate.getBrushColor());
		jsonObject.put("brushSize", boardUpdate.getBrushSize());
		return jsonObject.toString();
	}

	public BoardUpdate fromJson(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		BoardUpdate boardUpdate = new BoardUpdate(jsonObject.getInt("brushColor"), jsonObject.getInt("brushSize"));
		JSONArray jsonPoints = jsonObject.getJSONArray("points");
		for (int i = 0; i < jsonPoints.length(); ++i) {
			JSONObject jsonPoint = jsonPoints.getJSONObject(i);
			boardUpdate.addPointDrawn(new Point(jsonPoint.getInt("x"), jsonPoint.getInt("y")));
		}
		return boardUpdate;
	}
}
