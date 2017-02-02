package pl.shareddrawboard.api;

import android.util.Log;

import org.json.JSONException;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pl.shareddrawboard.NetworkUtils;
import pl.shareddrawboard.domain.BoardUpdate;
import pl.shareddrawboard.domain.Point;

/**
 * Created by Arjan on 15.01.2017.
 */

public class DrawboardClientPool {

	private static final String TAG = DrawboardClientPool.class.getSimpleName();
	private final List<UserEndpoint> activeUsers = new ArrayList<>();

	private final ThreadPoolExecutor joinUserExecutor = newExecutor();

	private boolean log = true;
	private String myName;

	public DrawboardClientPool(String myName) {
		this.myName = myName;
		//temp
		for(UserEndpoint user: UserEndpoint.allUsers) {
			if(!isMe(user)) {
				//joinUser(user);
			} else {
				this.myName = user.getName();
			}
		}
	}

	public void joinUser(final UserEndpoint user) {
		joinUserExecutor.execute(new Runnable() {
			@Override
			public void run() {
				boolean ok = false;
				Log.w(TAG, "connecting to " + user + "...");
				while(!ok) {
					try {
						ok = user.connect();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!ok) {
						Log.w(TAG, "still not connected to " + user + ", retrying in 5 secs");
						try {
							Thread.currentThread().sleep(5000L);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				Log.i(TAG, "connected to user "+user+"'s server");
				activeUsers.add(user);
			}
		});
	}

	/* TODO
	- usuwanie usera po N nieudanych probach nie chce dzialac
	- dodac jakich mechanizm synchronizacji tablicy jak komus zmuli lacze
	 */
	private ThreadPoolExecutor newExecutor() {
		return new ThreadPoolExecutor(8, 8, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
	}

	private boolean isMe(UserEndpoint user) {
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			NetworkInterface ni;
			while (nis.hasMoreElements()) {
				ni = nis.nextElement();
				if (!ni.isLoopback()/*not loopback*/ && ni.isUp()/*it works now*/) {
					for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
						//filter for ipv4/ipv6
						if (user.getAddress().getHost().equals(ia.getAddress().getHostAddress())) {
							Log.i(TAG, user.getAddress().getHost()+" to moje prawdziwe ja");
							return true;
						}
					}
				}
			}
		} catch (SocketException e) {
			Log.e(TAG, NetworkUtils.CONNECTION_ERROR);
			return false;
		}
		return false;
	}

	public void sendBoardUpdate(final BoardUpdate boardUpdate) {
		String message;
		try {
			message = JsonSerializer.toJson(myName, boardUpdate);
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
		Log.i(TAG, "sending "+message.length()+" bytes to "+activeUsers.size()+" users");
		for(UserEndpoint user: activeUsers) {
			user.send(message);
		}
	}

	public void send(Point point, Point previousPoint) {
		String message;
		try {
			message = JsonSerializer.toJson(point.x, point.y, previousPoint.x, previousPoint.y);
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
		Log.i(TAG, "sending "+message.length()+" bytes to "+activeUsers.size()+" users");
		for(UserEndpoint user: activeUsers) {
			user.send(message);
		}
	}
}
