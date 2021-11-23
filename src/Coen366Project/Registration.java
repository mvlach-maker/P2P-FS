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
import java.util.ArrayList;

public class Registration {

	private static FileWriter fileWriter;
	JSONObject response;
	Client currentClient;

	DatagramSocket serverSocket;
	private static ArrayList<Client> clientListArray;
	private static ArrayList<Client> clientListLoggedOn;
	int requestNumber;

	// Constructor
	public Registration(DatagramSocket serverSocket, ArrayList<Client> clientListArray, ArrayList<Client> clientListLoggedOn){
		this.serverSocket = serverSocket;
		Registration.clientListArray = clientListArray;
		Registration.clientListLoggedOn = clientListLoggedOn;
		response = new JSONObject();
	}

	public ArrayList<Client> getClientListLoggedOn() {
		return clientListLoggedOn;
	}
	public ArrayList<Client> getClientListArray() {
		return clientListArray;
	}

	public boolean isLoggedOn(Client client) {
		boolean isLoggedOn = false;
		for (int i=0; i < clientListLoggedOn.size(); i++) {

			Client clientObject = clientListLoggedOn.get(i);
			if (clientObject.getUsername() == client.getUsername())
			{
				isLoggedOn = true;
				break;
			}
			else continue;
		}
		return isLoggedOn;
	}

	public void register(Client client, int requestNumber) throws IOException, JSONException {

		boolean registrationAccepted = true;

		System.out.println("Client List Before Registration: ");
		for (int i = 0; i <clientListArray.size(); i++) {
			Client client1 = clientListArray.get(i);
			client1.printClientInfo();
		}

		for (int i = 0;  i < clientListArray.size()  ; i++) {

				Client clientInArray = clientListArray.get(i);

				String username1 = clientInArray.getUsername();

				System.out.println("Username 1: " + username1);
				InetAddress ip1 = clientInArray.getIp();

				String username2 = client.getUsername();
				System.out.println("Username 2: " + username2);
				InetAddress ip2 = client.getIp();

				if (username1.equalsIgnoreCase(username2) && ip1.equals(ip2)) {
					// You are already registered
					response.put("header", "Register-Denied");
					response.put("rq", requestNumber);
					response.put("reason", "You are already registered.");
					System.out.println(response.toString());
					registrationAccepted = false;

					// Write to clientListLoggedOn
					if (!isLoggedOn(client))  {
						clientListLoggedOn.add(client);
					}
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
			clientListArray.add(client);
			clientListLoggedOn.add(client);
		}

		System.out.println("Response sent to client: " + response.toString());

		// Send response to client
		InetAddress clientIp = client.getIp();
		int clientPort = (int) client.getClientTcp();

		byte[] responseBytes = response.toString().getBytes();

		DatagramPacket p = new DatagramPacket(responseBytes,
				responseBytes.length, clientIp, clientPort);

		try {
			serverSocket.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateContact(Client currentClient, ArrayList<Client> clientListArray, ArrayList<Client> clientListLoggedOn, int requestNumber) throws JSONException {

		for (int i = 0; i < clientListArray.size(); i++) {

			Client clientObject = clientListArray.get(i);

			String username1 = currentClient.getUsername();
			String username2 = clientObject.getUsername();

			if (username1.equalsIgnoreCase(username2)) {
				clientListArray.remove(i);
				clientListArray.add(currentClient);
				break;
			}
			else continue;
		}

		for (int i = 0; i < clientListLoggedOn.size(); i++) {

			Client clientObject = clientListLoggedOn.get(i);

			String username1 = currentClient.getUsername();
			String username2 = clientObject.getUsername();

			if (username1.equalsIgnoreCase(username2)) {
				clientListLoggedOn.remove(i);
				clientListLoggedOn.add(currentClient);
				break;
			}
			else continue;
		}
	}

	public void deRegister(Client currentClient, ArrayList<Client> clientListArray, ArrayList<Client> clientListLoggedOn, int requestNumber) throws JSONException {
		this.currentClient = currentClient;
		this.clientListArray = clientListArray;
		this.clientListLoggedOn = clientListLoggedOn;
		this.requestNumber = requestNumber;

		// Check if user is on file

		for (int i = 0; i < clientListArray.size(); i++) {

			Client clientObject = clientListArray.get(i);

			String username1 = clientObject.getUsername();
			String username2 = currentClient.getUsername();

			if (username1.equalsIgnoreCase(username2)) {

				// You are already registered, delete user
				clientListArray.remove(i);
				// No need for a response
				response.put("header", "De-Registered");
				response.put("rq", requestNumber);
				response.put("username", username2);
				break;
			}
		}
		for (int i = 0; i < clientListLoggedOn.size(); i++) {

			Client clientObject = clientListLoggedOn.get(i);

			String username1 = clientObject.getUsername();
			String username2 = currentClient.getUsername();

			if (username1.equalsIgnoreCase(username2)) {
				// Delete user
				clientListLoggedOn.remove(i);
				break;
			}
		}

		System.out.println("Response sent to client: " + response.toString());

		// Send response to client
			InetAddress clientIp = currentClient.getIp();
			int clientPort = currentClient.getClientTcp();

			byte[] responseBytes = response.toString().getBytes();

			DatagramPacket p = new DatagramPacket(responseBytes,
					responseBytes.length, clientIp, clientPort);

			try {
				serverSocket.send(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

