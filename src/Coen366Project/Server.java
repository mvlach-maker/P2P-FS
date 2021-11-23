package Coen366Project;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.net.SocketException;

// import networking.udpBaseClient;
// Program running on end host and  and providing service to many clients
// Always on, waiting for client requests
// Does not initiate communication with clients 
// Needs to have a known address
// 1 Server listening at a specific port
// A socket connection means the two machines have information about each other’s network location (IP Address) and TCP port.

public class Server implements Serializable {

	private static ArrayList<Client> clientListArray;
	private static ArrayList<Client> clientListLoggedOn;
	
	private static Client currentClient;

	Server(ArrayList<Client> clientListArray, ArrayList<Client> clientListLoggedOn) {

		this.clientListArray = clientListArray;
		this.clientListLoggedOn = clientListLoggedOn;

	}

	public static void main(String[] args) throws InterruptedException, IOException, JSONException, ClassNotFoundException {

		clientListArray = new ArrayList<Client>();
		clientListLoggedOn = new ArrayList<Client>();

		int requestNumber = 1;

		DatagramSocket serverSocket = new DatagramSocket(Coen366Project.Main.serverPort);

		while (true) {
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
			// Request number will be automatically generated
			try {

				JSONObject jsonResponse = new JSONObject(request);
				String header = (String) jsonResponse.get("header");
				String username;
				username = (String) jsonResponse.get("username");

				JSONObject jsonRequest = new JSONObject();

				// Update json file with all the requests
				Registration registerClient;
				Client client;

				switch (header) {
					case "Register":
						// Form complete request and insert into Json File


						jsonRequest.put("header", "Register");
						jsonRequest.put("rq", requestNumber);
						jsonRequest.put("username", username);
						jsonRequest.put("ip", clientIp);
						jsonRequest.put("udp", Main.serverPort);
						jsonRequest.put("tcp", clientTcpPort);

						// Create registration object
						client = new Client(username, clientIp, Main.serverPort, clientTcpPort);

						registerClient = new Registration(serverSocket, clientListArray, clientListLoggedOn);

						registerClient.register(client, requestNumber);

						System.out.println("Client name: ");
						System.out.println(client.getUsername());

						clientListArray.add(client);

						System.out.println("Client List After Registration: ");
						for (int i = 0; i <clientListArray.size(); i++) {
							Client client1 = clientListArray.get(i);
							client1.printClientInfo();
						}

						break;

					case "De-Register":
						// Get username
						username = (String) jsonResponse.get("username");

						currentClient = getCurrentClient(username, clientIp, clientListArray);

						jsonRequest.put("header", "De-Register");
						jsonRequest.put("rq", requestNumber);
						jsonRequest.put("username", username);

						registerClient = new Registration(serverSocket, clientListArray, clientListLoggedOn);
						registerClient.deRegister(currentClient, clientListArray, clientListLoggedOn, requestNumber);
						break;

					case "Publish":
						break;
				}

			}
			catch (JSONException e) {
				e.printStackTrace();
			}
			requestNumber++; // how will the system retain the request number

		}
	}
	static Client getCurrentClient(String username, InetAddress ip, ArrayList<Client> clientListArray) {

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

	static void parseListOfFiles(String listOfFiles) {

	}
}

