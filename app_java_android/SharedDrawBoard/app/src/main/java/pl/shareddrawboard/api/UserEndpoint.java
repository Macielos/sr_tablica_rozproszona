package pl.shareddrawboard.api;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.shareddrawboard.NetworkUtils;

/**
 * Created by Arjan on 15.01.2017.
 */

public class UserEndpoint {

	//tylko do testow, poki nie dziala serwer do znajdowania userow
	public static final List<UserEndpoint> allUsers = initTestUserList();

	private static List<UserEndpoint> initTestUserList() {
		try {
			return Arrays.asList(
					new UserEndpoint(new URI("ws://192.168.1.106:"+ NetworkUtils.DEFAULT_PORT), "abc"),   //samsung
					new UserEndpoint(new URI("ws://192.168.1.102:"+NetworkUtils.DEFAULT_PORT), "xyz"),   //hammer
					new UserEndpoint(new URI("ws://10.0.2.15:"+NetworkUtils.DEFAULT_PORT), "qwerty")     //emulator
					);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return new ArrayList<>(0);
		}
	}

	private final String TAG;

	private WebSocketClient client;
	private final URI address;
	private String name;

	public UserEndpoint(URI address, String name) {
		this.address = address;
		this.name = name;

		TAG = getClass().getSimpleName()+":"+name;
	}

	public boolean connect() throws InterruptedException {
		if(client != null) {
			client.closeBlocking();
		}
		client = new WebSocketClient(address) {

			@Override
			public void onOpen(ServerHandshake handshake) {
				Log.i(TAG, "client connected " + handshake);
			}

			@Override
			public void onClose(int code, String reason, boolean remote) {
				Log.i(TAG, "connection is closed with code "+code+" because "+reason+", remote: "+remote);
			}

			@Override
			public void onMessage(String message) {
				Log.i(TAG, "message from server: "+message);
			}

			@Override
			public void onError(Exception ex) {
				Log.e(TAG, "got remote error", ex);
			}
		};
		return client.connectBlocking();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public URI getAddress() {
		return address;
	}

	@Override
	public String toString() {
		return name+" ["+address + "]";
	}

	public void send(String message) {
		if(client.getConnection() != null && client.getConnection().isOpen()) {
			client.send(message);
		} else {
			Log.e(TAG, "Can't send msg, client not connected");
		}
	}
}
