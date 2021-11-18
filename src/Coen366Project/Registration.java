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
		response = new JSONObject();
	}

	public boolean isRegisteredLoggedIn(JSONObject client, JSONArray clientListArray) throws JSONException {

		boolean isRegistered = false;

		for (int i = 0; i < clientListArray.length(); i++) {

			JSONObject clientObject = null;
			try {
				clientObject = clientListArray.getJSONObject(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			String username1 = (String) clientObject.get("username");
			InetAddress ip1 = (InetAddress) clientObject.get("ip");

			String username2 = (String) client.get("username");
			InetAddress ip2 = (InetAddress) client.get("ip");

			if (username1.equalsIgnoreCase(username2) && ip1.equals(ip2)) {
				// This client is already registered
				isRegistered = true;
				break;
			}
		}
		return isRegistered;
	}

	public boolean login(JSONObject client, JSONArray clientListJson, int requestNumber) throws JSONException {

		boolean login = false;

		for (int i = 0; i < clientListJson.length(); i++) {
			JSONObject clientObject = clientListJson.getJSONObject(i);
			// System.out.println(clientObject.toString());
			String username1 = (String) clientObject.get("username");
			InetAddress ip1 = (InetAddress) clientObject.get("ip");

			String username2 = (String) client.get("username");
			InetAddress ip2 = (InetAddress) client.get("ip");

			if (username1.equalsIgnoreCase(username2) && ip1.equals(ip2)) {
				clientListJson.getJSONObject(i).put("login", 1);
				response.put("header", "logged-in");
				response.put("rq", requestNumber);
				login = true;
				break;
			}
			else continue;
		}

		// Send response to client
		InetAddress clientIp = (InetAddress) client.get("ip");
		int clientPort = (int) client.get("tcp");

		byte[] responseBytes = response.toString().getBytes();

		DatagramPacket p = new DatagramPacket(responseBytes,
				responseBytes.length, clientIp, clientPort);

		try {
			serverSocket.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return login;
	}

	public void logout(JSONObject client, JSONArray clientListJson, int requestNumber) throws JSONException {
		// Set login to 1
		// If client isn't on the list, ignore

		for (int i = 0; i < clientListJson.length(); i++) {

			JSONObject clientObject = clientListJson.getJSONObject(i);
			// System.out.println(clientObject.toString());
			String username1 = (String) clientObject.get("username");
			InetAddress ip1 = (InetAddress) clientObject.get("ip");

			String username2 = (String) client.get("username");
			InetAddress ip2 = (InetAddress) client.get("ip");

			if (username1.equalsIgnoreCase(username2) && ip1.equals(ip2)) {
				clientListJson.getJSONObject(i).put("login", false);
				response.put("header", "Logged-out");
				response.put("rq", requestNumber);
			break; }
			else continue;
		}
		System.out.println("Response sent to client: " + response.toString());
		// Send response to client
		InetAddress clientIp = (InetAddress) client.get("ip");
		int clientPort = (int) client.get("tcp");

		byte[] responseBytes = response.toString().getBytes();

		DatagramPacket p = new DatagramPacket(responseBytes,
				responseBytes.length, clientIp, clientPort);

		try {
			serverSocket.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JSONArray register(JSONObject client, JSONArray clientListJson, int requestNumber) throws IOException, JSONException {

		boolean registrationAccepted = true;

			for (int i = 0; i < clientListJson.length(); i++) {

				JSONObject clientObject = clientListJson.getJSONObject(i);
				// System.out.println(clientObject.toString());
				String username1 = (String) clientObject.get("username");
				InetAddress ip1 = (InetAddress) clientObject.get("ip");

				String username2 = (String) client.get("username");
				InetAddress ip2 = (InetAddress) client.get("ip");

				if (username1.equalsIgnoreCase(username2) && ip1.equals(ip2)) {

					// You are already registered

					response.put("header", "Register-Denied");
					response.put("rq", requestNumber);
					response.put("reason", "You are already registered.");
					System.out.println(response.toString());
					registrationAccepted = false;
					break;

				} else if (username1.equalsIgnoreCase(username2) && !ip1.equals(ip2)) {

					// username is already in use

					response.put("header", "Register-Denied");
					response.put("rq", requestNumber);
					response.put("reason", "Username is already in use.");
					System.out.println("Username is already in use.");
					registrationAccepted = false;
					break;
				} else continue;
			}

		if (registrationAccepted) {
			// We register the client
			response.put("header", "Registered");
			response.put("rq", requestNumber);
			clientListJson.put(client);
			// System.out.println(response.toString());
		}
		System.out.println("Response sent to client: " + response.toString());

		// Send response to client
		InetAddress clientIp = (InetAddress) client.get("ip");
		int clientPort = (int) client.get("tcp");

		byte[] responseBytes = response.toString().getBytes();

		DatagramPacket p = new DatagramPacket(responseBytes,
				responseBytes.length, clientIp, clientPort);

		try {
			serverSocket.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return clientListJson;
	}

	public void deRegister(JSONObject client, JSONArray clientListJson, int requestNumber) throws JSONException {

		// Check if user is on file

		for (int i = 0; i < clientListJson.length(); i++) {

			JSONObject clientObject = null;
			try {
				clientObject = clientListJson.getJSONObject(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			System.out.println(clientObject.toString());

			String username1 = (String) clientObject.get("username");
			InetAddress ip1 = (InetAddress) clientObject.get("ip");

			String username2 = (String) client.get("username");
			InetAddress ip2 = (InetAddress) client.get("ip");

			if (username1.equalsIgnoreCase(username2) && ip1.equals(ip2)) {
				// You are already registered, delete user
				clientListJson.remove(i);
				// No need for a response
				response.put("header", "De-Registered");
				response.put("rq", requestNumber);
				break;
			}
		}

		System.out.println("Response sent to client: " + response.toString());
			// Send response to client
			InetAddress clientIp = (InetAddress) client.get("ip");
			int clientPort = (int) client.get("tcp");

			byte[] responseBytes = response.toString().getBytes();

			DatagramPacket p = new DatagramPacket(responseBytes,
					responseBytes.length, clientIp, clientPort);

			try {
				serverSocket.send(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// If the name is not registered, the message is just ignored by the server
		}
	}

