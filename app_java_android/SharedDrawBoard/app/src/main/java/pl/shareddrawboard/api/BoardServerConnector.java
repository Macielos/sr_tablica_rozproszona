package pl.shareddrawboard.api;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import pl.shareddrawboard.NetworkUtils;

/**
 * Created by Arjan on 08.01.2017.
 */

public class BoardServerConnector {

	private static final String TAG = BoardServerConnector.class.getSimpleName();

	private AsyncHttpClient client = new AsyncHttpClient();

	public void createBoard(String name, String ip, AsyncHttpResponseHandler responseHandler) throws JSONException, UnsupportedEncodingException {
		client.post(null, NetworkUtils.SERVER_URL, null, new ByteArrayEntity(newCreateBoardJson(name, ip).toString().getBytes("UTF-8")), "application/json", responseHandler);
	}

	private JSONObject newCreateBoardJson(String name, String ip) throws JSONException {
		JSONObject boardJsonObject = new JSONObject();
		boardJsonObject.put("boardName", name);
		boardJsonObject.put("ipAddress", "http://"+ip+":"+NetworkUtils.DEFAULT_PORT);

		return boardJsonObject;
	}

	public void getBoards(AsyncHttpResponseHandler responseHandler) {
		client.get(null, NetworkUtils.SERVER_URL, null, responseHandler);
	}
}
