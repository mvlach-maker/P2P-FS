package Coen366Project;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

import java.net.*;
import java.util.*;


// Always on, waiting for client requests
// Does not initiate communication with clients
// A socket connection means the two machines have information about each other’s network location (IP Address) and TCP port.

public class Server implements Serializable {
	public static final int serverPort = 3000;

	static ArrayList<Client> clientListArray;
	static ArrayList<Client> clientListLoggedOn;
	static ArrayList<String> listOfFiles;
	static ArrayList<File> listOfFileObjects;
	static DatagramSocket serverSocket;
	public static InetAddress serverIp = null;
	static JSONObject response;

	static {
		try {
			serverIp = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException, IOException, JSONException, ClassNotFoundException {
		Client currentClient;
		clientListArray = new ArrayList<>();
		clientListLoggedOn = new ArrayList<>();
		listOfFiles = new ArrayList<>();
		listOfFileObjects = new ArrayList<>();
		int index;

		int requestNumber = 1;
		serverSocket = new DatagramSocket(serverPort);

		while (true) {
			response = new JSONObject();
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);

			try {
				serverSocket.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Client information
			int clientTcpPort = packet.getPort();
			InetAddress clientIp = packet.getAddress();

			// Get request  in String
			String request = new String(packet.getData());
			System.out.println("Request from client: " + request);

			try {

				JSONObject jsonResponse = new JSONObject(request);
				String header = (String) jsonResponse.get("header");
				String username;

				JSONObject jsonRequest = new JSONObject();

				// Update json file with all the requests
				switch (header) {
					case "Register":
						// Form complete request and insert into Json File
						username = (String) jsonResponse.get("username");

						jsonRequest.put("header", "Register");
						jsonRequest.put("rq", requestNumber);
						jsonRequest.put("username", username);
						jsonRequest.put("ip", clientIp);
						jsonRequest.put("udp", serverPort);
						jsonRequest.put("tcp", clientTcpPort);

						currentClient = new Client(username, clientIp, serverPort, clientTcpPort);
						Server.register(currentClient, requestNumber);
						break;

					case "De-Register":
						// Get username
						username = (String) jsonResponse.get("username");
						currentClient = getCurrentClient(username, clientIp);

						jsonRequest.put("header", "De-Register");
						jsonRequest.put("rq", requestNumber);
						jsonRequest.put("username", username);

						Server.deRegister(currentClient, requestNumber);
						break;

					case "Publish":
						username = (String) jsonResponse.get("username");
						String listOfFiles = (String) jsonResponse.get("files");
						ArrayList<String> files = parseListOfFiles(listOfFiles);
						currentClient = getCurrentClient(username, clientIp);
						index = getClientIndex(currentClient);

						for (String file : files) {

							File f = new File(file);

							boolean fileIsAlreadyPublished = false;
							if (f.isFile()) {

								for (File fileObject : currentClient.listOfFileObjects) {
									// System.out.println("The name of the file in client: " + fileObject.getName());
									// System.out.println("Name of file to be inserted" + f.getName());

									if (f.equals(fileObject)) {
										fileIsAlreadyPublished = true;
										break;
									} else continue;
								}
								//System.out.println(fileIsAlreadyPublished);

								// WHAT IF THE FILE ALREADY EXISTS
								if (fileIsAlreadyPublished) {
									response.put("header", "Publish-Denied");
									response.put("rq", requestNumber);
									response.put("reason", "File is already published.");
								} else {
									response.put("header", "Published");
									response.put("rq", requestNumber);
									// REPLACE THIS NEW CONTACT IN THE CLIENT ARRAY LIST
									currentClient.listOfFileObjects.add(f);
									clientListArray.set(index, currentClient);
								}

							}
							else {
								response.put("header", "Publish-Denied");
								response.put("rq", requestNumber);
								response.put("reason", "File does not exist.");
								break;
							}
						}
						break;

					case "Remove":
						username = (String) jsonResponse.get("username");
						listOfFiles = (String) jsonResponse.get("files");
						files = parseListOfFiles(listOfFiles);
						currentClient = getCurrentClient(username, clientIp);
						index = getClientIndex(currentClient);

						boolean fileIsThere = false;

						for (String file : files) {
							File f = new File(file);

							if (f.isFile()) {

								for (File fileObject : currentClient.listOfFileObjects) {
									if (f.equals(fileObject)) {
										fileIsThere = true;
										break;
									}
									else continue;
								}

								if (fileIsThere) {
									response.put("header", "Removed");
									response.put("rq", requestNumber);
									clientListArray.set(index, currentClient);
									currentClient.listOfFileObjects.remove(f);
								}
								else {
									// The file does not exist
									response.put("header", "Remove-Denied");
									response.put("rq", requestNumber);
									response.put("reason", "File is not published.");
								}
							}
							else {
								// The file does not exist
								response.put("header", "Remove-Denied");
								response.put("rq", requestNumber);
								response.put("reason", "File does not exist.");
								break;
							}
						}
						break;
					case "Retrieve-All":
						String listOfUsers;
						StringBuilder sb = new StringBuilder();
						response.put("header", "Retrieve");
						response.put("rq", requestNumber);
						JSONArray clientListJson = new JSONArray();

						for (int i = 0; i < clientListArray.size(); i++) {

							Client client = clientListArray.get(i);
							clientListJson.put(client.getCLientInfo());
							//response.put("clients", clientListJson);
						}
						response.put("clients", clientListJson);
						System.out.println(clientListJson.toString());
						break;
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			// Print client list array

			for (Client c: clientListArray) {
				c.printClientInfo();
			}

			// Send response to client

			System.out.println("Response sent to client: " + response.toString());

			byte[] responseBytes = response.toString().getBytes();

			DatagramPacket p = new DatagramPacket(responseBytes,
					responseBytes.length, clientIp, clientTcpPort);

			try {
				serverSocket.send(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
			requestNumber++; // how will the system retain the request number
		}
	}
	static int getClientIndex(Client client) {
		int index = -1;
		for (int i = 0; i < clientListArray.size(); i++) {
			Client currentClient = clientListArray.get(i);
			if (client == currentClient) {
				index = i;
			}
		}
		return index;
	}

	static Client getCurrentClient(String username, InetAddress ip) {

		Client currentClient = null;

		for (int i = 0; i < clientListArray.size(); i++) {

			currentClient = clientListArray.get(i);

			String username1 = currentClient.getUsername();
			InetAddress ip1 = currentClient.getIp();

			if (username1.equalsIgnoreCase(username) && ip1.equals(ip)) {
				// This is the same client
				break;
			}
			else continue;
		}
		return currentClient;
	}

	static ArrayList<String> parseListOfFiles(String listOfFiles) {
		ArrayList<String> files = new ArrayList<>(Arrays.asList(listOfFiles.split(",")));
		return files;
	}

	public static boolean isLoggedOn(Client client, ArrayList<Client> clientListLoggedOn) {
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

	public static void register(Client client, int requestNumber) throws IOException, JSONException {

		boolean registrationAccepted = true;

		for (int i = 0; i < clientListArray.size()  ; i++) {

			Client clientInArray = clientListArray.get(i);

			String username1 = clientInArray.getUsername();

			// System.out.println("Username 1: " + username1);
			InetAddress ip1 = clientInArray.getIp();

			String username2 = client.getUsername();
			// System.out.println("Username 2: " + username2);
			InetAddress ip2 = client.getIp();

			if (username1.equalsIgnoreCase(username2) && ip1.equals(ip2)) {
				// You are already registered
				response.put("header", "Register-Denied");
				response.put("rq", requestNumber);
				response.put("reason", "You are already registered.");
				System.out.println(response.toString());
				registrationAccepted = false;

				// Write to clientListLoggedOn

				if (!isLoggedOn(client, clientListLoggedOn))  {
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
	}
	public static void deRegister(Client currentClient, int requestNumber) throws JSONException {
		// Check if user is on file
		JSONObject response = new JSONObject();

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
	}
}

