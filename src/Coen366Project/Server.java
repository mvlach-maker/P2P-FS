package Coen366Project;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.sun.xml.internal.xsom.impl.util.Uri.isValid;


// Always on, waiting for client requests
// Does not initiate communication with clients
// A socket connection means the two machines have information about each other’s network location (IP Address) and TCP port.

public class Server implements Serializable {
	public static final int serverPort = 3000;

	static ArrayList<Client> clientListArray;
	static ArrayList<Client> clientListLoggedOn;
	static ArrayList<String> listOfFilesString;
	static ArrayList<File> listOfFileObjects;
	static DatagramSocket serverSocket;
	static Serialization serialization;
	public static InetAddress serverIp = null;
	static JSONObject response;
	static int requestNumber;

	static {
		try {
			serverIp = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException, IOException, JSONException, ClassNotFoundException {

		// Deserialization
		File sFile = new File("/Users/marina/eclipse-workspace/Coen366Project/src/serialization.txt");

		// If file is empty, new client arrays and request number
		if (sFile.length() == 0) {
			clientListArray = new ArrayList<>();
			clientListLoggedOn = new ArrayList<>();
			requestNumber = 1;
		}
		// Otherwise, we read from the serialization text file

		else {
			serialization = null;
			try {
				// Reading the object from a file
				FileInputStream fis = new FileInputStream(sFile);
				ObjectInputStream in = new ObjectInputStream(fis);

				// Method for deserialization of object
				serialization = (Serialization) in.readObject();

				clientListArray = serialization.clientList;
				clientListLoggedOn = serialization.clientListLoggedOn;
				requestNumber = serialization.requestNumber;

				in.close();
				fis.close();
				System.out.println("Object has been deserialized");
			} catch (IOException ex) {
				System.out.println("IOException is caught");
			} catch (ClassNotFoundException ex) {
				System.out.println("ClassNotFoundException" +
						" is caught");
			}
		}

		Client currentClient;
		listOfFilesString = new ArrayList<>();
		listOfFileObjects = new ArrayList<>();
		int index1;
		int index2;


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
						index1 = getClientIndex(currentClient);
						index2 = getClientIndexLoggedOn(currentClient);

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

								// What if file is already published?
								if (fileIsAlreadyPublished) {
									response.put("header", "Publish-Denied");
									response.put("rq", requestNumber);
									response.put("reason", "File is already published.");
								} else {
									response.put("header", "Published");
									response.put("rq", requestNumber);
									// REPLACE THIS NEW CONTACT IN THE CLIENT ARRAY LIST
									currentClient.listOfFileObjects.add(f);
									clientListArray.set(index1, currentClient);
									clientListLoggedOn.set(index2, currentClient);
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
						index1 = getClientIndex(currentClient);
						index2 = getClientIndexLoggedOn(currentClient);

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
									currentClient.listOfFileObjects.remove(f);
									clientListArray.set(index1, currentClient);
									clientListLoggedOn.set(index2, currentClient);
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
						response.put("header", "Retrieve");
						response.put("rq", requestNumber);
						JSONArray clientListJson = new JSONArray();

						for (int i = 0; i < clientListArray.size(); i++) {

							Client client = clientListArray.get(i);
							clientListJson.put(client.getClientInfo());

						}
						response.put("clients", clientListJson);
						break;

					case "Retrieve-Info":
						String peerUsername = (String) jsonResponse.get("username");

						if (isRegistered(peerUsername)){
							response.put("header", "Retrieve-Info");
							response.put("rq", requestNumber);
							Client client = getClient(peerUsername);
							response.put("client info", client.getClientInfo());
						}
						else {
							// Client does not exist
							response.put("header", "Retrieve-Error");
							response.put("reason", "Name Does Not Exist / Not Registered ");
							response.put("rq", requestNumber);
						}
						break;

					case "Update-Contact":

						username = (String) jsonResponse.get("username");
						String ipUpdated = (String) jsonResponse.get("ip");
						Client clientToUpdate = getClient(username);

						// Possible errors with ip ? How do we make sure it is valid

						InetAddress ipUpdatedInet = InetAddress.getByName(ipUpdated);
						int udpUpdated = (int) jsonResponse.get("udp");
						int tcpUpdated = (int) jsonResponse.get("tcp");

						if (isValid(ipUpdated) && tcpUpdated >= 1024 && udpUpdated >= 1024) {
							index1 = getClientIndex(clientToUpdate);
							index2 = getClientIndexLoggedOn(clientToUpdate);

							clientToUpdate.setClientIp(ipUpdatedInet);
							clientToUpdate.setClientUdp(udpUpdated);
							clientToUpdate.setClientTcp(tcpUpdated);

							clientListArray.set(index1, clientToUpdate);
							clientListLoggedOn.set(index2, clientToUpdate);

							response.put("header", "Update-Confirmed");
							response.put("rq", requestNumber);
							response.put("ip", ipUpdatedInet);
							response.put("udp", udpUpdated);
							response.put("tcp", tcpUpdated);
						}
						else {
							response.put("header", "Update-Denied");
							response.put("rq", requestNumber);
							response.put("reason", "Invalid Input.");
						}

						System.out.println(clientToUpdate.getIp());
						// Replace this client in the array of clients
						break;

					case "Search-File":
						ArrayList<Client> clientsThatHaveFile = new ArrayList<>();
						// Loop through each client and their file object to find a match
						// If found add to the array of clients the have this file
						JSONArray clientsThatHaveFileJson = new JSONArray();
						String nameOfFile = (String) jsonResponse.get("file");
						for (Client client : clientListArray) {

							for (File f : client.listOfFileObjects) {

								if (f.getName().equalsIgnoreCase(nameOfFile)) {
									clientsThatHaveFile.add(client);
								}
								else continue;
							}
						}

						if (clientsThatHaveFile.isEmpty()) {
							response.put("header", "Search-Error");
							response.put("rq", requestNumber);
							response.put("reason", "File does not exist.");
						}
						else {
							for (Client c : clientsThatHaveFile) {
								clientsThatHaveFileJson.put(c.getClientInfo());
							}

							response.put("header", "Search-File");
							response.put("rq", requestNumber);
							response.put("file", nameOfFile);
							response.put("Clients", clientsThatHaveFileJson);
						}
						break;
				}
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			// Print client list array
			System.out.println("Client List Array: ");
			for (Client c: clientListArray) {
				c.printClientInfo();
			}
			System.out.println("Client List Logged On: ");
			for (Client c: clientListLoggedOn) {
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

			serialization = new Serialization(clientListArray, clientListLoggedOn, requestNumber);

			// Serialization
			try {
				// Saving of object in a file
				FileOutputStream file = new FileOutputStream(sFile);
				ObjectOutputStream out = new ObjectOutputStream(file);

				// Method for serialization of object
				out.writeObject(serialization);

				out.close();
				file.close();

				System.out.println("Object has been serialized\n"
						+ "Data before Deserialization.");
			}

			catch (IOException ex) {
				System.out.println("IOException is caught");
			}

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

	static int getClientIndexLoggedOn(Client client) {
		int index = -1;
		for (int i = 0; i < clientListLoggedOn.size(); i++) {
			Client currentClient = clientListLoggedOn.get(i);
			if (client == currentClient) {
				index = i;
			}
		}
		return index;
	}

	static boolean isRegistered(String username) {
		boolean isRegistered = false;

		for (Client c : clientListArray) {
			if (c.getUsername().equalsIgnoreCase(username)) {
				isRegistered = true;
				break;
			}
			else continue;
		}
		return isRegistered;
	}

	static Client getClient (String username) {
		Client client = null;

		for (Client c : clientListArray) {
			if (c.getUsername().equalsIgnoreCase(username)) {
				client = c;
				break;
			}
			else continue;
		}
		return client;
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
			if (clientObject.getUsername().equalsIgnoreCase(client.getUsername()))
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

