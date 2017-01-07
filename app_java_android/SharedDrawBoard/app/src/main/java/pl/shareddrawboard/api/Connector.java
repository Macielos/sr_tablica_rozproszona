package pl.shareddrawboard.api;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pl.shareddrawboard.Board;

/**
 * Created by Arjan on 29.12.2016.
 */

public class Connector {

	private class User {
		private final String host;
		private final int port;

		public User(String host, int port) {
			this.host = host;
			this.port = port;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			User user = (User) o;

			if (port != user.port) return false;
			return host != null ? host.equals(user.host) : user.host == null;

		}

		@Override
		public int hashCode() {
			int result = host != null ? host.hashCode() : 0;
			result = 31 * result + port;
			return result;
		}

		@Override
		public String toString() {
			return host + ":" + port;
		}
	}

	private class BoardUpdateListener extends Thread {

		ServerSocket listenerSocket;
		User myUser;
		InputStream otherUserDrawStream;
		private boolean running = true;

		public BoardUpdateListener(User myUser) throws IOException {
			this.myUser = myUser;
		}

		@Override
		public void run() {
			while (running) {
				boolean ok = false;
				byte[] buffer = new byte[8192];
				while (!ok) {
					try {
						listenerSocket = new ServerSocket(myUser.port);
						ok = true;
					} catch (IOException e) {
						Log.e(TAG, "failed to start listening for user " + myUser);
						e.printStackTrace();
					}
					if (!ok) {
						try {
							sleep(2000L);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				while (ok) {
					try {
						Log.i(TAG, "listening on port "+myUser.port);
						Socket socket = listenerSocket.accept();
						otherUserDrawStream = socket.getInputStream();
						int readBytes = otherUserDrawStream.read(buffer);
						Log.i(TAG, "read " + readBytes + " bytes");
						byte[] filledBytes = Arrays.copyOf(buffer, readBytes);
						BoardUpdate boardUpdate = jsonSerializer.fromJson(new String(filledBytes));
						board.update(boardUpdate);
					} catch (IOException e) {
						e.printStackTrace();
						ok = false;
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static final String TAG = Connector.class.getSimpleName();

	private final List<User> users = Arrays.asList(new User("localhost", 28000), new User("localhost", 28001));
	private final User me;

	private final ThreadPoolExecutor sendBoardUpdateExecutor = newExecutor();

	private final List<BoardUpdateListener> boardUpdateListeners = new ArrayList<>();

	private final JsonSerializer jsonSerializer = new JsonSerializer();
	private final Board board;

	private ThreadPoolExecutor newExecutor() {
		return new ThreadPoolExecutor(4, 16, 0, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<Runnable>());
	}

	public Connector(String host, int port, Board board) {
		this.board = board;
		this.me = new User(host, port);

		Log.i(TAG, "creating listener for user "+me);
		try {
			boardUpdateListeners.add(new BoardUpdateListener(me));
		} catch (IOException e) {
			Log.e(TAG, "failed to create server socket for user " + me + ", exception is " + e);
			e.printStackTrace();
		}
	}

	public void startListeners() {
		for (BoardUpdateListener boardUpdateListener : boardUpdateListeners) {
			boardUpdateListener.start();
		}
	}

	public void sendBoardUpdate(final BoardUpdate boardUpdate) {
		for (final User user : users) {
			if (!user.equals(me)) {
				sendBoardUpdateExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try (Socket socket = new Socket(user.host, user.port)) {
							String json = jsonSerializer.toJson(boardUpdate);

							OutputStream outputStream = socket.getOutputStream();
							//outputStream.write(("" + json.length()).getBytes());
							outputStream.write(json.getBytes());

						} catch (JSONException e) {
							Log.e(TAG, "failed to parse json from " + boardUpdate + ": " + e);
							e.printStackTrace();
						} catch (IOException e) {
							Log.e(TAG, "failed to send board update: " + e);
							e.printStackTrace();
						}
					}
				});
			}
		}
	}

}
