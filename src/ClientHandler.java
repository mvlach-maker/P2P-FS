import Coen366Project.Client;
import Coen366Project.Server;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class ClientHandler {


    public static String zeroStep(Scanner reader) {
        System.out.println("Enter username: ");
        return reader.next();
    }

    // FUNCTION TO START THE FIRST STEP OF THE CLIENT PROCESS
    public static JSONObject firstStep(DatagramSocket client, String username) throws JSONException {

        JSONObject registerObj = new JSONObject();

        boolean i = true;


        // Register
        try {
            registerObj.put("header", "Register");
            registerObj.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Json Object gets sent to server
        byte[] registerBytes = registerObj.toString().getBytes();

        DatagramPacket p = new DatagramPacket(registerBytes,
                registerBytes.length, Server.serverIp, Server.serverPort);

        // client sends it to the server
        try {
            client.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Json Object gets sent to client

        byte[] buf = new byte[300];
        DatagramPacket packet = new DatagramPacket(buf,
                buf.length);

        // timer; so the client is not trapped waiting for a response indefinitely
        // schedule a timer to timeout after x amount of seconds of no response
        // Timer responseTimer = new Timer();

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

        // Use the Json response to continue the program
        return jsonResponse;
    }

    // CLIENT IS LOGGED IN, THEY ACCESS MORE OPTIONS (PUBLISH, ETC. ,ETC.)
    public static void secondStep(DatagramSocket client, String username) throws JSONException {

        JSONObject secondClientRequest = new JSONObject();

        System.out.print("Choose one of the following options for P2P File Sharing: \n a) Publish File \n b) Remove File" +
                "\n c) Retrieve All Clients \n d) Retrieve Client Info \n e) Search-File \n f) Download File" +
                "\n g) Update Contact Info  \n h) De-Register \n");

        Scanner reader = new Scanner(System.in);
        String input = reader.next();
        // Create second requests and send to the server

        switch(input.toLowerCase()) {
            case "a":

                System.out.println("Enter List of File(s): ");
                reader = new Scanner(System.in);
                String listOfFiles = reader.nextLine();

                secondClientRequest.put("header","Publish");
                secondClientRequest.put("username", username);
                secondClientRequest.put("files", listOfFiles);
                reader.close();
                break;

            case "b":

                System.out.println("Enter List of File(s) to Remove: ");
                reader = new Scanner(System.in);
                listOfFiles = reader.nextLine();

                secondClientRequest.put("header","Remove");
                secondClientRequest.put("username", username);
                secondClientRequest.put("files", listOfFiles);
                reader.close();
                break;

            case "c":
                secondClientRequest.put("header","Retrieve-All");
                secondClientRequest.put("username", username);
                break;

            case "d":

                System.out.println("Enter Peer Username: ");
                reader = new Scanner(System.in);
                String peerUsername = reader.next();

                secondClientRequest.put("header","Retrieve-Info");
                secondClientRequest.put("username", peerUsername);
                reader.close();
                break;
            case "e":
                secondClientRequest.put("header","Search-File");
                secondClientRequest.put("username", username);
                break;
            case "f":
                secondClientRequest.put("header","Download");
                secondClientRequest.put("username", username);
                break;
            case "g":
                secondClientRequest.put("header","Update-Contact");
                secondClientRequest.put("username", username);
                reader = new Scanner(System.in);
                System.out.println("Input updated IP address: ");
                String ipUpdatedString = reader.next();


                secondClientRequest.put("ip", ipUpdatedString);


                System.out.println("Input updated UDP socket number: ");
                int udpUpdated = reader.nextInt();
                secondClientRequest.put("udp", udpUpdated);

                System.out.println("Input updated TCP socket number: ");
                int tcpUpdated = reader.nextInt();
                secondClientRequest.put("tcp", tcpUpdated);
                break;

            case "h":
                secondClientRequest.put("header","De-Register");
                secondClientRequest.put("username", username);
                break;
            default:
                System.out.println("Invalid Input.");
        }

        // Send this request to server
        // Json Object gets sent to server
        byte[] secondRequestBytes = secondClientRequest.toString().getBytes();
        DatagramPacket p = new DatagramPacket(secondRequestBytes,
                secondRequestBytes.length, Server.serverIp, Server.serverPort);

        // client sends it to the server

        try {
            client.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Client receives response from server
        byte[] buffer = new byte[600];
        DatagramPacket packet = new DatagramPacket(buffer,
                buffer.length);
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
        reader.close();
    }


    public static void main(String[]  args) throws InterruptedException, IOException, JSONException {

        // Declare datagramSocket

        DatagramSocket client = null;

        try {
            client = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Declare scanner
        Scanner reader = new Scanner(System.in);
        String username = ""; //todo watch out
        boolean i = true;
        while(i) {
            username = zeroStep(reader);
            // Call firstStep method and give it the socket & scanner
            JSONObject JsonResponseRegistration = ClientHandler.firstStep(client, username);

            String header = (String) JsonResponseRegistration.get("header");
            if (header.equals("Registered")) {
                // You are logged in

                i = false;
            } else if (header.equals("Register-Denied")) {
                String reason = (String) JsonResponseRegistration.get("reason");
                if (reason.equals("You are already registered.")) {
                    // You are logged in
                    i = false;
                }
            } else {
                // You are not logged in, stay in the loop
            }
        }
        ClientHandler.secondStep(client, username);

        reader.close();
        client.close();

    }
}
