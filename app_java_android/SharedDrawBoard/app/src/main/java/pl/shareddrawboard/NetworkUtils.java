package pl.shareddrawboard;

import android.util.Log;

import java.net.ConnectException;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Arjan on 16.01.2017.
 */
public class NetworkUtils {

	private static final String TAG = NetworkUtils.class.getSimpleName();

	public static final String CONNECTION_ERROR = "did not find ip, probably not connected to the internet";

	public static final String SERVER_URL = "http://srcloudboardserver.azurewebsites.net/api/Boards";
	public static final int DEFAULT_PORT = 28000;


	public static String getIP() throws ConnectException {
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
			Log.e(TAG, "can't get ip", e);
			throw new ConnectException(CONNECTION_ERROR);
		}
		throw new ConnectException(CONNECTION_ERROR);
	}
}
