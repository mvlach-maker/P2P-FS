package Coen366Project;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException; 

import java.net.DatagramPacket; 
import java.net.DatagramSocket; 
import java.net.InetAddress; 
import java.net.SocketException;

// import networking.udpBaseClient;
// Program runnning on end host and  and providing service to many clients
// Always on, waiting for client requests
// Does not initiate communication with clients 
// Needs to have a known address
// 1 Server listening at a specific port
// A socket connection means the two machines have information about each other’s network location (IP Address) and TCP port.
public class Server {

	// What if the server unexpectedly shuts down?
	// How will we save our info?
	static File requestLogFile;
	static File clientListFile;
	static JSONArray clientListArray;

	public static void main(String[] args) throws InterruptedException, IOException {

		requestLogFile = new File("requestLog.json");
		clientListFile = new File("clientList.json");
		clientListArray = new JSONArray();

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

				JSONObject jsonRequest = new JSONObject();

				// Update json file with all the requests
				String username = null;
				Registration register;

				switch (header) {

					case "Login":

						username = (String) jsonResponse.get("username");

						jsonRequest.put("header", "Login");
						jsonRequest.put("rq", requestNumber);
						jsonRequest.put("username", username);
						jsonRequest.put("ip", clientIp);
						jsonRequest.put("udp", Main.serverPort);
						jsonRequest.put("tcp", clientTcpPort);
						jsonRequest.put("login", true);

						register = new Registration(serverSocket);
						register.login(jsonRequest, clientListArray, requestNumber);

						// Once the user is logged in they can share files

						break;

					case "Register":

						// Form complete request and insert into Json File
						username = (String) jsonResponse.get("username");

						jsonRequest.put("header", "Register");
						jsonRequest.put("rq", requestNumber);
						jsonRequest.put("username", username);
						jsonRequest.put("ip", clientIp);
						jsonRequest.put("udp", Main.serverPort);
						jsonRequest.put("tcp", clientTcpPort);
						jsonRequest.put("login", true);

						// Create registration object
						register = new Registration(serverSocket);
						clientListArray = register.register(jsonRequest, clientListArray, requestNumber);

						// Automatically logged in, they can share files
						break;

					case "De-Register":

						username = (String) jsonResponse.get("username");

						jsonRequest.put("header", "De-Register");
						jsonRequest.put("rq", requestNumber);
						jsonRequest.put("username", username);
						jsonRequest.put("ip", clientIp);
						jsonRequest.put("udp", Main.serverPort);
						jsonRequest.put("tcp", clientTcpPort);
						jsonRequest.put("login", false);

						register = new Registration(serverSocket);
						register.deRegister(jsonRequest, clientListArray, requestNumber);
						break;

					case "Logout":

						username = (String) jsonResponse.get("username");
						register = new Registration(serverSocket);
						jsonRequest.put("header", "Logout");
						jsonRequest.put("rq", requestNumber);
						jsonRequest.put("username", username);
						jsonRequest.put("ip", clientIp);
						jsonRequest.put("tcp", clientTcpPort);
						jsonRequest.put("login", false);
						register.logout(jsonRequest, clientListArray, requestNumber);
						break;

					case "Publish":
						break;
					case "Remove":
						break;
					case "Retrieve-All":
						break;
					case "Retrieve-Info":
						break;
					case "Search-File":
						break;
					case "Update-Contact":
						break;
					default:

				}
				requestNumber++; // how will the system retain the request number
			} catch (JSONException e) {
				e.printStackTrace();
			}
			System.out.println("Client List: " + clientListArray.toString());
		}
	}

}





