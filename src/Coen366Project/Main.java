package Coen366Project;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Main {
	static int serverPort = 3000;
	public static InetAddress serverIp = null;
	private static Server server;

	static {
		try {
			serverIp = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}