package pl.shareddrawboard.api;

import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pl.shareddrawboard.domain.Board;
import pl.shareddrawboard.domain.BoardUpdate;

/**
 * Created by Arjan on 29.12.2016.
 *
 * stara impl na socketach, potem sie wywali
 */
@Deprecated
public class PlainSocketConnector {

	private class User {
		private final String host;
		private final int port;
		private String name;

		private boolean connected = true;

		public User(String host, int port, String name) {
			this.host = host;
			this.port = port;
			this.name = name;
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

		User myUser;
		InputStream otherUserDrawStream;
		private boolean running = true;

		public BoardUpdateListener(User myUser) throws IOException {
			this.myUser = myUser;
		}

		@Override
		public void run() {
			while (running) {
				try (ServerSocket listenerSocket = new ServerSocket(myUser.port)) {
					Log.i(TAG, "listening for board updates on port "+myUser.port);
					final Socket socket = listenerSocket.accept();
					if(socket != null) {
						boardUpdateExecutor.execute(new Runnable() {
							@Override
							public void run() {
								try {
									otherUserDrawStream = socket.getInputStream();
									JSONObject messageJsonObject = new JSONObject(readString(otherUserDrawStream));
									//TODO wypierdzielic te nazwy pol do stalych
									switch(messageJsonObject.getString("action")) {
										case "draw":
											BoardUpdate boardUpdate = jsonSerializer.fromJson(messageJsonObject);
											board.update(boardUpdate);
											view.postInvalidate();
											break;
										default:
											Log.w(TAG, "invalid msg type for json: "+messageJsonObject);
									}

								} catch (IOException | JSONException e) {
									e.printStackTrace();
								} finally {
									try {
										socket.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static final String TAG = PlainSocketConnector.class.getSimpleName();

	private static final String CONNECTION_ERROR = "did not find ip, probably not connected to the internet";
	private static final int DEFAULT_PORT = 28000;
	private static final int CONNECT_RETRIES_ALLOWED = 3;

	private final User user1 = new User("192.168.1.106", DEFAULT_PORT, "abc");
	private final User user2 = new User("192.168.1.102", DEFAULT_PORT, "xyz");
	private final User user3 = new User("10.0.2.15", DEFAULT_PORT, "qwerty"); //192.168.1.110

	//tylko do testow, poki nie dziala serwer do znajdowania userow
	private final List<User> allUsers = Arrays.asList(/*user1, user2, */user3);

	private final User me;

	private final ThreadPoolExecutor boardUpdateExecutor = newExecutor();
	private final Map<User, ThreadPoolExecutor> userSendBoardUpdateExecutors = new HashMap<>();

	private final List<BoardUpdateListener> boardUpdateListeners = new ArrayList<>();

	private final JsonSerializer jsonSerializer = new JsonSerializer();
	private final Board board;
	private View view;

	private boolean log = true;

	/* TODO
	- usuwanie usera po N nieudanych probach nie chce dzialac
	- dodac jakich mechanizm synchronizacji tablicy jak komus zmuli lacze
	 */
	private ThreadPoolExecutor newExecutor() {
		return new ThreadPoolExecutor(8, 8, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
	}

	public PlainSocketConnector(String name, Board board) throws ConnectException {
		this(getIP(), DEFAULT_PORT, name, board, true);
	}

	public PlainSocketConnector(String host, int port, String name, Board board, boolean log) {
		this.board = board;
		this.me = new User(host, port, name);
		this.log = log;

		//temp
		for(User user: allUsers) {
			if(!isMe(user)) {
				joinUser(user);
			} else {
				me.name = user.name;
			}
		}

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

	public void setView(View view) {
		this.view = view;
	}

	private boolean isMe(User user) {
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			NetworkInterface ni;
			while (nis.hasMoreElements()) {
				ni = nis.nextElement();
				if (!ni.isLoopback()/*not loopback*/ && ni.isUp()/*it works now*/) {
					for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
						//filter for ipv4/ipv6
						if (user.host.equals(ia.getAddress().getHostAddress())) {
							Log.i(TAG, user.host+" to moje prawdziwe ja");
							return true;
						}
					}
				}
			}
		} catch (SocketException e) {
			Log.e(TAG, CONNECTION_ERROR);
			return false;
		}
		return false;
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

	public void joinUser(User user) {
		userSendBoardUpdateExecutors.put(user, newExecutor());
	}

	public void startListeners() {
		for (BoardUpdateListener boardUpdateListener : boardUpdateListeners) {
			boardUpdateListener.start();
		}
	}

	public void sendBoardUpdate(final BoardUpdate boardUpdate) {
		for (final Map.Entry<User, ThreadPoolExecutor> entry: userSendBoardUpdateExecutors.entrySet()) {
			final User user = entry.getKey();
			Log.i(TAG, "submit send "+boardUpdate.getPointsDrawn().size() + " points to "+user+", active threads: "+entry.getValue().getActiveCount()+", enqueued updates: "+ entry.getValue().getTaskCount());
			if(user.connected) {
				entry.getValue().execute(new Runnable() {
					@Override
					public void run() {
						Log.i(TAG, "sending " + boardUpdate.getPointsDrawn().size() + " points to " + user);
						boolean ok = false;
						int retriesLeft = CONNECT_RETRIES_ALLOWED;
						while (!ok && retriesLeft > 0) {
							try (Socket socket = new Socket(user.host, user.port)) {
								socket.setSoTimeout(10000);
								String json = jsonSerializer.toJson(me.name, boardUpdate);

								OutputStream outputStream = socket.getOutputStream();
								writeString(outputStream, json);
								Log.i(TAG, "sent " + boardUpdate.getPointsDrawn().size() + " points to " + user);
								ok = true;
							} catch (JSONException e) {
								Log.e(TAG, "failed to parse json from " + boardUpdate + ": " + e);
								e.printStackTrace();
								ok = true;
							} catch (IOException e) {
								Log.e(TAG, "failed to send board update: " + e);
								e.printStackTrace();
								--retriesLeft;
								Log.w(TAG, retriesLeft+" retries left");
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
							}
						}
						if (!ok) {
							Log.w(TAG, "user " + user + " left the board");
							user.connected = false;
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
		int readBytes = inputStream.read(buffer);
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
		readBytes = readAllAvailable(inputStream, buffer, jsonSize);
		if(readBytes != jsonSize) {
			throw new RuntimeException("wrong json length: got "+readBytes+" bytes but expected "+jsonSize);
		}
		if(log) {
			Log.i(TAG, "read " + readBytes + " bytes of msg");
		}
		return new String(buffer);
	}

	private int readAllAvailable(InputStream inputStream, byte[] finalBuffer, int messageSize) throws IOException {
		int readBytesTotal = 0;
		ByteBuffer byteBuffer = ByteBuffer.allocate(messageSize+100);
		Log.i(TAG, "msg size: "+messageSize);
		while(byteBuffer.position() < messageSize) {
			byte[] buffer = new byte[messageSize];
			int readBytes = inputStream.read(buffer);
			Log.i(TAG, "readBytes "+readBytes);
			byteBuffer.put(buffer, 0, readBytes);
			readBytesTotal += readBytes;
		}
		byteBuffer.position(0);
		byteBuffer.get(finalBuffer, 0, messageSize);
		return readBytesTotal;
	}

	void writeString(OutputStream outputStream, String json) throws IOException {
		byte[] jsonSizeAsBytes = ByteBuffer.allocate(4).putInt(json.getBytes().length).array();
		outputStream.write(jsonSizeAsBytes);
		if(log) {
			Log.i(TAG, "wrote size "+json.getBytes().length+":  " + Arrays.toString(jsonSizeAsBytes));
		}
		outputStream.write(json.getBytes());
		if(log) {
			Log.i(TAG, "wrote " + json.getBytes().length + " bytes of msg");
		}
		outputStream.flush();
	}

}
