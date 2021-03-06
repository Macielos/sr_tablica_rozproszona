package pl.shareddrawboard.api;

import android.util.Log;
import android.util.Pair;
import android.view.View;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import pl.shareddrawboard.NetworkUtils;
import pl.shareddrawboard.domain.Board;
import pl.shareddrawboard.domain.BoardUpdate;
import pl.shareddrawboard.domain.Point;
import pl.shareddrawboard.domain.StateContainer;

/**
 * Created by Arjan on 29.12.2016.
 */
public class DrawboardServer extends WebSocketServer {

	private static final String TAG = DrawboardServer.class.getSimpleName();

	private final Board board;
	private View view;

	private Map<String, UserEndpoint> activeUsers = new HashMap<>();

	private final boolean log;
	private boolean master;

	public DrawboardServer(Board board) throws ConnectException {
		this(board, true, false);
	}

	public DrawboardServer(Board board, boolean log, boolean master) throws ConnectException {
		super(new InetSocketAddress(NetworkUtils.getIP(), NetworkUtils.DEFAULT_PORT));
		this.board = board;
		this.log = log;
		this.master = master;
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		Log.i(TAG, "new connection: " + handshake.getResourceDescriptor()+" from "+conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		Log.i(TAG, "connection: "+conn+" is closed with code "+code+" because "+reason+", remote: "+remote);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		Log.i(TAG, "message from "+conn.getRemoteSocketAddress().getAddress().getHostAddress()+", length "+message.length());
		try {
			JSONObject messageJsonObject = new JSONObject(message);
			//TODO wypierdzielic te nazwy pol do stalych
			switch(messageJsonObject.getString("type")) {
				case "drag":
					Pair<Point, Point> pointPair = JsonSerializer.pointPairFromJson(messageJsonObject);
					//BoardUpdate boardUpdate = JsonSerializer.fromJson(messageJsonObject);
					board.update(pointPair);
					view.postInvalidate();
					if(master) {
						//StateContainer.instance.getClientPool().sendBoardUpdate(boardUpdate);
					}
					break;
				default:
					Log.w(TAG, "invalid msg type for json: "+messageJsonObject);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		Log.e(TAG, "got error from "+conn.getRemoteSocketAddress().getAddress().getHostAddress(), ex);
	}

	public void setMaster(boolean master) {
		this.master = master;
	}

	public void setView(View view) {
		this.view = view;
	}

}
