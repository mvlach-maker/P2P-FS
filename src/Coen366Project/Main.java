package Coen366Project;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Main {
	static int serverPort = 3000;
	public static InetAddress serverIp = null;

	static {
		try {
			serverIp = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
