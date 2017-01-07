package pl.shareddrawboard.api;

import android.util.Log;
import android.view.View;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.IllegalFormatException;
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
				byte[] buffer = new byte[81920];
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
					Socket socket = null;
					try {
						Log.i(TAG, "listening for board updates on port "+myUser.port);
						socket = listenerSocket.accept();
						otherUserDrawStream = socket.getInputStream();

						BoardUpdate boardUpdate = jsonSerializer.fromJson(readString(otherUserDrawStream));
						board.update(boardUpdate);
						view.postInvalidate();
					} catch (IOException e) {
						e.printStackTrace();
						ok = false;
						if(listenerSocket != null) {
							try {
								listenerSocket.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					} finally {
						if(socket != null) {
							try {
								socket.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	private static final String TAG = Connector.class.getSimpleName();

	private static final String CONNECTION_ERROR = "did not find ip, probably not connected to the internet";
	private static final int DEFAULT_PORT = 28000;

	private final User user1 = new User("192.168.1.106", DEFAULT_PORT);
	private final User user2 = new User("192.168.1.102", DEFAULT_PORT);

	private final List<User> users = Arrays.asList(user1, user2);
	private final User me;

	private final ThreadPoolExecutor sendBoardUpdateExecutor = newExecutor();

	private final List<BoardUpdateListener> boardUpdateListeners = new ArrayList<>();

	private final JsonSerializer jsonSerializer = new JsonSerializer();
	private final Board board;
	private final View view;

	private boolean log = true;

	private ThreadPoolExecutor newExecutor() {
		return new ThreadPoolExecutor(4, 16, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
	}

	public Connector(Board board, View view) throws ConnectException {
		this(getIP(), DEFAULT_PORT, board, view);
	}

	public Connector(String host, int port, Board board, View view) {
		this(host, port, board, view, true);
	}

	public Connector(String host, int port, Board board, View view, boolean log) {
		this.board = board;
		this.view = view;
		this.me = new User(host, port);
		this.log = log;

		if(log) {
			Log.i(TAG, "creating listener for user " + me);
		}
		try {
			boardUpdateListeners.add(new BoardUpdateListener(me));
		} catch (IOException e) {
			Log.e(TAG, "failed to create server socket for user " + me + ", exception is " + e);
			e.printStackTrace();
		}
	}

	private static String getIP() throws ConnectException {
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			NetworkInterface ni;
			while (nis.hasMoreElements()) {
				ni = nis.nextElement();
				if (!ni.isLoopback()/*not loopback*/ && ni.isUp()/*it works now*/) {
					for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
						//filter for ipv4/ipv6
						if (ia.getAddress().getAddress().length == 4) {
							//4 for ipv4, 16 for ipv6
							return ia.getAddress().getHostAddress();
						}
					}
				}
			}
		} catch (SocketException e) {
			Log.e(TAG, CONNECTION_ERROR);
			throw new ConnectException(CONNECTION_ERROR);
		}
		throw new ConnectException(CONNECTION_ERROR);
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
						Log.i(TAG, "sending "+boardUpdate.getPointsDrawn().size() + " points to "+user);
						Socket socket = null;
						try {
							socket = new Socket(user.host, user.port);
							String json = jsonSerializer.toJson(boardUpdate);

							OutputStream outputStream = socket.getOutputStream();
							writeString(outputStream, json);
							Log.i(TAG, "sent "+boardUpdate.getPointsDrawn().size() + " points to "+user);
						} catch (JSONException e) {
							Log.e(TAG, "failed to parse json from " + boardUpdate + ": " + e);
							e.printStackTrace();
						} catch (IOException e) {
							Log.e(TAG, "failed to send board update: " + e);
							e.printStackTrace();
						} finally {
							if(socket != null) {
								try {
									socket.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				});
			}
		}
	}

	/**
	 * Struktura wiadomości:
	 * - 4 bajty - integer - rozmiar jsona
	 * - właściwy json o rozmiarze jw
	 */
	String readString(InputStream inputStream) throws IOException {
		//czytamy rozmiar jsona
		int bufferSize = 4;
		byte[] buffer = new byte[bufferSize];
		int readBytes = inputStream.read(buffer, 0, bufferSize);
		if(log) {
			Log.i(TAG, "read " + readBytes + " bytes of size");
		}
		if(readBytes != bufferSize) {
			throw new RuntimeException("wrong json size length: got "+readBytes+" bytes but expected "+bufferSize);
		}

		//konwersja byte[] na int
		ByteBuffer bb = ByteBuffer.allocate(bufferSize).put(buffer, 0, bufferSize);
		bb.position(0);
		int jsonSize = bb.getInt();

		//czytamy reszte
		buffer = new byte[jsonSize];
		readBytes = inputStream.read(buffer, 0, jsonSize);
		if(readBytes != jsonSize) {
			throw new RuntimeException("wrong json length: got "+readBytes+" bytes but expected "+jsonSize);
		}
		if(log) {
			Log.i(TAG, "read " + readBytes + " bytes of msg");
		}
		return new String(buffer);
	}

	void writeString(OutputStream outputStream, String json) throws IOException {
		byte[] jsonSizeAsBytes = ByteBuffer.allocate(4).putInt(json.getBytes().length).array();
		outputStream.write(jsonSizeAsBytes);
		if(log) {
			Log.i(TAG, "wrote " + jsonSizeAsBytes.length + " bytes of size");
		}
		outputStream.write(json.getBytes());
		if(log) {
			Log.i(TAG, "wrote " + json.getBytes().length + " bytes of msg");
		}
		outputStream.flush();
	}

}
