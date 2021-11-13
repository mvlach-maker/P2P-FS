package Coen366Project;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException; 

import java.net.DatagramPacket; 
import java.net.DatagramSocket; 
import java.net.InetAddress; 
import java.net.SocketException;
import java.util.ArrayList; 
// import networking.udpBaseClient; 

// Program runnning on end host and  and poviding service to many clients 
// Always on, waiting for client requests
// Does not initiate communication with clients 
// Needs to have a known address 

// 1 Server listening at a specific port 

// A socket connection means the two machines have information about each other’s network location (IP Address) and TCP port.

public class Server {
	
	int serverPortNumber;
	File clientDatabase;
	
	public Server() {
	// You need a file with client information  
	clientDatabase = new File("clientDatabase.json");
	}

	public static void main(String[]  args) throws InterruptedException, IOException {

		DatagramSocket server = null;
		int requestNumber = 1;

		try {
			server = new DatagramSocket(Coen366Project.Main.serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// Read data from client
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				server.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Client information
			int clientTcpPort = packet.getPort();
			InetAddress clientIp = packet.getAddress();

			// Get request  in String
			String request = new String(packet.getData());
			System.out.println(request);

			// Request number will be automatically generated
			try {
				JSONObject jsonResponse = new JSONObject(request);
				String header = (String) jsonResponse.get("header");

				JSONObject jsonRequest = new JSONObject();

				// Update json file with all the requests

				switch (header) {

					case "Login":

						String username = (String) jsonResponse.get("username");

						jsonRequest.put("header", "Login");
						jsonRequest.put("rq", requestNumber);
						jsonRequest.put("username", username);
						jsonRequest.put("ip", clientIp);
						jsonRequest.put("udp", Main.serverPort);
						jsonRequest.put("tcp", clientTcpPort);
						jsonRequest.put("login", true);

					case "Register":
						// Form complete request and insert into Json File
						// username = (String) jsonResponse.get("username");

						jsonRequest.put("header", "Register");
						jsonRequest.put("rq", requestNumber);
						// jsonRequest.put("username", username);
						jsonRequest.put("ip", clientIp);
						jsonRequest.put("udp", Main.serverPort);
						jsonRequest.put("tcp", clientTcpPort);
						jsonRequest.put("login", true);

						// SEND THIS OBJECT TO REGISTRATION

					case "De-Register":
						// username = (String) jsonResponse.get("username");
						jsonRequest.put("header", "De-Register");
						jsonRequest.put("rq", requestNumber);
						// jsonRequest.put("username", username);
						jsonRequest.put("login", false);

					case "Publish":
					case "Remove":
					case "Retrieve-All":
					case "Retrieve-Info":
					case "Search-File":
					case "Update-Contact":
						default:

				}

				requestNumber++;
				System.out.println(jsonRequest.toString());

				// Write in request file

			} catch (JSONException e) {
				e.printStackTrace();
			}




			// Decipher what the client said

			// Get value by key and do a switch case



			server.close();

			}
		}



