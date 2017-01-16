package pl.shareddrawboard.api;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;
import pl.shareddrawboard.NetworkUtils;

/**
 * Created by Arjan on 08.01.2017.
 */

public class BoardRetriever {

	private static final String TAG = BoardRetriever.class.getSimpleName();

	private AsyncHttpClient client = new AsyncHttpClient();

	public void createBoard(String name, String ip, AsyncHttpResponseHandler responseHandler) throws JSONException {
		client.post(null, NetworkUtils.SERVER_URL, null, new RequestParams("newHost", newCreateBoardJson(name, ip).toString()), "application/json", responseHandler);
	}

	private JSONObject newCreateBoardJson(String name, String ip) throws JSONException {
		JSONObject boardJsonObject = new JSONObject();
		boardJsonObject.put("boardName", name);
		boardJsonObject.put("ipAddress", ip);

		return boardJsonObject;
	}

}
