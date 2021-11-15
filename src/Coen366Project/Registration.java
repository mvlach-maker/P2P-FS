package Coen366Project;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.File;
import java.net.InetAddress;

public class Registration {

	private static FileWriter fileWriter;
	JSONObject response;

	DatagramSocket serverSocket;
	private byte[] buffer;
	DatagramPacket packet;
	int requestNumber;

	// Constructor
	public Registration(DatagramSocket serverSocket){
		this.serverSocket = serverSocket;
		response = new JSONObject();
		buffer = new byte[256];
		packet = new DatagramPacket(buffer, buffer.length, Main.serverIp, Main.serverPort);
		response = new JSONObject();

	}
	public void login(JSONObject client) {
		// Go through the client list and match username and ip
		// if there is no match - tell the user that there is no existing client and restart the Client thread
		// Create message to send back to client

	}

	public JSONArray register(JSONObject client, JSONArray clientListJson) throws IOException, JSONException {

		requestNumber = (int) client.get("rq");

		boolean registrationAccepted = true;

			for (int i = 0; i < clientListJson.length(); i++) {

			JSONObject clientObject = clientListJson.getJSONObject(i);
			System.out.println(clientObject.toString());
			String username1 = (String) clientObject.get("username");
			InetAddress ip1 = (InetAddress) clientObject.get("ip");

			String username2 = (String) client.get("username");
			InetAddress ip2 = (InetAddress) client.get("ip");

			if (username1.equalsIgnoreCase(username2) && !ip1.equals(ip2)) {

				// You are already registered

				response.put("header", "Register-Denied");
				response.put("rq", requestNumber);
				response.put("Reason", "You are already registered.");
				System.out.println("You are already registered.");
				registrationAccepted = false;
				break;

			} else if (username1 == username2 && ip1 != ip2) {

				// username is already in use

				response.put("header", "Register-Denied");
				response.put("rq", requestNumber);
				response.put("Reason", "Username is already in use.");
				System.out.println("Username is already in use.");
				registrationAccepted = false;
				break;
			}

			else continue;
		}

		if (registrationAccepted) {
			// We register the client
			response.put("header", "Registered");
			response.put("rq", requestNumber);
			clientListJson.put(client);
			System.out.println("Registration accepted.");
		}

		return clientListJson;
	}
	public void deRegister(JSONObject client, JSONArray clientListJson) {

		// Get username and check if it is already in use (.json text file)

		// if it isn't, insert new user - ACCEPTED

		// if it is, don't do anything - DENIED "YOU ARE ALREADY REGISTERED" - USER GETS AUTOMATICALLY LOGGED IN

		// if username is already in use but ip doesnt match - DENIED "TRY DIFFERENT USERNAME"
	}
}
