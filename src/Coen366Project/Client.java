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
 
	// Client information 
	String clientName;
	int tcpSocketNumber; 
	InetAddress ipClient;

	public static void main(String[]  args) throws InterruptedException, IOException, JSONException {
		
		// Set request method

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

			if (n.equals("a") || n.equals("A")) {
				// Put this command in a JSon Object
				try {
					registerObj.put("header", "Login");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				i = false;
			}

			else if (n.equals("b") || n.equals("B")) {
				// Register
				try {
					registerObj.put("header", "Register");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				i = false;
			}

			else if (n.equals("c") || n.equals("C")) {
				// deRegister
				try {
					registerObj.put("header", "De-Register");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				i = false;
			}

			else if (n.equals("x") || n.equals("X")) {
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
				registerBytes.length, Coen366Project.Main.serverIp, Coen366Project.Main.serverPort);

		try {
			client.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Get list of options to do various things 

		reader.close();
		client.close();
	}
	
}

		



