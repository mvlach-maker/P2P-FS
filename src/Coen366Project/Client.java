package Coen366Project;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;

import java.io.IOException;
import java.util.Scanner; 
import java.io.FileWriter;
import java.io.File;
import java.net.*;

// Is on when it first needs to send a request 
// Initiates communication by sending a request 
// Needs to know the server's address: IP address and port number

public class Client {

	boolean login;
	String username;
	InetAddress clientIp;
	int clientPort;

	// Constructor
	public Client(boolean login, String username, InetAddress clientIp, int clientPort){
		this.login = login;
		this.username = username;
		this.clientIp = clientIp;
		this.clientPort = clientPort;
	}

	public static void main(String[]  args) throws InterruptedException, IOException, JSONException {

		DatagramSocket client = null;

		try {
			client = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		Scanner reader = new Scanner(System.in);

		JSONObject registerObj = new JSONObject();

		boolean i = true;
		System.out.println("Enter username: ");
		String username = reader.next();

		while (i) {

			System.out.println("Type A to Login, B to Register, C to De-Register or X to Logout:");
			String n = reader.next();

			if (n.equalsIgnoreCase("a")) {
				// Put this command in a JSon Object
				try {
					registerObj.put("header", "Login");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				i = false;
			}

			else if (n.equalsIgnoreCase("b")) {
				// Register
				try {
					registerObj.put("header", "Register");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				i = false;
			}

			else if (n.equalsIgnoreCase("c")) {
				// deRegister
				try {
					registerObj.put("header", "De-Register");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				i = false;
			}

			else if (n.equalsIgnoreCase("x")) {
				registerObj.put("header", "Logout");
				i = false;
			}

			else {
				System.out.println("Please enter a valid key.");
			}

			try {
				registerObj.put("username", username);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		// Json Object gets sent to server
		byte[] registerBytes = registerObj.toString().getBytes();

		DatagramPacket p = new DatagramPacket(registerBytes,
				registerBytes.length, Main.serverIp, Main.serverPort);

		try {
			client.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Json Object gets sent to client

		byte[] buf = new byte[300];
		DatagramPacket packet = new DatagramPacket(buf,
				buf.length);
		try {
			client.receive(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Receive a response from the server
		String serverResponse = new String(packet.getData());
		JSONObject jsonResponse = new JSONObject(serverResponse);
		System.out.println("Server response: " + jsonResponse.toString());

		// Explore more options
		// Publish, remove, retrieve, search file, download, update contact,

		// Get list of options to do various things
		reader.close();
		client.close();

	}
	
}

		



