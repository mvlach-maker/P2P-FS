package Coen366Project;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

public class ClientHandler {

    private static int myTcpPort;

    public static void main (String[]args) throws Exception {
        // Declare datagramSocket for UDP
        DatagramSocket client = null;

        try {
            client = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Retry timer
        client.setSoTimeout(2000);

        // Declare scanner to read username
        Scanner reader = new Scanner(System.in);
        String username = ""; //todo watch out
        boolean i = true;

        while (i) {

            username = zeroStep(reader);

            // Call firstStep method and give it the socket & scanner
            JSONObject JsonResponseRegistration;
            JsonResponseRegistration = ClientHandler.firstStep(client, username);

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
                // Username is already in use with a different ip
                // Stuck in a loop until a good username is chosen
                continue;
            }
        }

        // Start listening here on a thread
        ServerSocket serverSocket = new ServerSocket(myTcpPort);
        ClientAsServerTCP clientAsServerTCP = new ClientAsServerTCP(serverSocket);
        Thread t = new Thread(clientAsServerTCP);
        t.start();

        boolean repeat = true;
        Scanner readerSecondStep = new Scanner(System.in);


        while (repeat) {
            ClientHandler.secondStep(client, username, readerSecondStep);
            Scanner scannerMain = new Scanner(System.in);
            String answer;
            answer = fifthStep(scannerMain);
            if (answer.equalsIgnoreCase("N")) {
                System.out.println("Exiting.....");
                repeat = false;
            } else {
                repeat = true;
            }
            //scannerMain.close();
        }

        // Client is now logged in and can start sharing files
        readerSecondStep.close();
        reader.close();
        client.close();

    }

    public static String zeroStep(Scanner reader) {
        System.out.println("Enter username: ");
        return reader.next();
    }

    // FUNCTION TO START THE FIRST STEP OF THE CLIENT PROCESS
    public static JSONObject firstStep(DatagramSocket client, String username) throws JSONException, IOException {

        JSONObject registerObj = new JSONObject();

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

        int numberOfRetries = 0;

        try {
            client.receive(packet);
        } catch (SocketTimeoutException q) {
            numberOfRetries++;
            client.send(p);
            System.out.println("Packet sent again, retry #: " + numberOfRetries);
            try {
                client.receive(packet);
            } catch (SocketTimeoutException r) {
                numberOfRetries++;
                client.send(p);
                System.out.println("Packet sent again, retry #: " + numberOfRetries);
                try {
                    client.receive(packet);
                } catch (SocketTimeoutException s) {
                    numberOfRetries++;
                    client.send(p);
                    System.out.println("Packet sent again, retry #: " + numberOfRetries);
                    try {
                        client.receive(packet);
                    } catch (SocketTimeoutException t) {
                        System.out.println("Max number of retries: " + numberOfRetries);
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Receive response from the server
        myTcpPort = 0;
        String serverResponse = new String(packet.getData());
        JSONObject jsonResponse = new JSONObject(serverResponse);
        String registrationHeader = (String) jsonResponse.get("header");

        if (registrationHeader.equals("Registered")) {
            myTcpPort = (int) jsonResponse.get("tcp");
        } else if (registrationHeader.equals("Register-Denied")) {
            String reason = (String) jsonResponse.get("reason");
            if (reason.equals("You are already registered.")) {
                myTcpPort = (int) jsonResponse.get("tcp");
            }

            System.out.println("Server response: " + jsonResponse.toString());

        }
        // Use the Json response to continue the program
        return jsonResponse;
    }


    // CLIENT IS LOGGED IN, THEY ACCESS MORE OPTIONS (PUBLISH, ETC. ,ETC.)
    public static void secondStep(DatagramSocket client, String username, Scanner readerSecondStep) throws JSONException, IOException {

            JSONObject secondClientRequest = new JSONObject();
            String nameOfFile;
            Scanner reader = new Scanner(System.in);

                System.out.print("Choose one of the following options for P2P File Sharing: \n a) Publish File(s) \n b) Remove File(s)" +
                        "\n c) Retrieve All Clients \n d) Retrieve Client Info \n e) Search-File \n f) Download File" +
                        "\n g) Update Contact Info  \n h) De-Register \n ");

                String input = reader.next();
                // Create second requests and send to the server

                switch (input.toLowerCase()) {
                    // Publish Files
                    case "a":

                        System.out.println("Enter List of File(s) to Publish: ");
                        reader = new Scanner(System.in);
                        String listOfFiles = reader.nextLine();

                        secondClientRequest.put("header", "Publish");
                        secondClientRequest.put("username", username);
                        secondClientRequest.put("files", listOfFiles);
                        //reader.close();
                        thirdStep(client, secondClientRequest);
                        break;

                        // Remove File
                    case "b":

                        System.out.println("Enter List of File(s) to Remove: ");
                        reader = new Scanner(System.in);
                        listOfFiles = reader.nextLine();

                        secondClientRequest.put("header", "Remove");
                        secondClientRequest.put("username", username);
                        secondClientRequest.put("files", listOfFiles);
                        //reader.close();
                        thirdStep(client, secondClientRequest);
                        break;

                        // Retrieve all clients
                    case "c":
                        secondClientRequest.put("header", "Retrieve-All");
                        secondClientRequest.put("username", username);
                        //reader.close();
                        thirdStep(client, secondClientRequest);
                        break;

                    // Retrieve info
                        case "d":

                        System.out.println("Enter Peer Username: ");
                        reader = new Scanner(System.in);
                        String peerUsername = reader.next();

                        secondClientRequest.put("header", "Retrieve-Info");
                        secondClientRequest.put("username", peerUsername);
                        //reader.close();
                        thirdStep(client, secondClientRequest);
                        break;

                    // Search file
                        case "e":
                        secondClientRequest.put("header", "Search-File");
                        secondClientRequest.put("username", username);
                        System.out.println("Input name of the file you would like to search: ");
                        nameOfFile = reader.next();
                        secondClientRequest.put("file", nameOfFile);
                        //reader.close();
                        thirdStep(client, secondClientRequest);
                        break;

                    // Download
                        case "f":
                        Scanner tcpScanner = new Scanner(System.in);
                        System.out.println("Enter file name to download: ");
                        String fileNamePeer = tcpScanner.next();
                        System.out.println("Enter Ip address of peer: ");
                        String ipAddressPeer = tcpScanner.next();
                        System.out.println("Enter TCP port of peer: ");
                        int peerPort = tcpScanner.nextInt();


                        secondClientRequest.put("header", "Download");
                        secondClientRequest.put("file", fileNamePeer);
                        String message = secondClientRequest.toString();
                        // Start connection
                        Socket clientSocket = new Socket(ipAddressPeer, peerPort);
                        clientSocket.setSoTimeout(2000);

                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        out.println(message);

                        char[] resp = new char[200];
                        int length = in.read(resp, 0, 200);

                        if (length > 0)
                        {
                            System.out.println(String.valueOf(resp));
                        }
                        in.close();
                        out.close();
                        clientSocket.close();

                        break;

                    // Update Contact
                        case "g":
                        secondClientRequest.put("header", "Update-Contact");
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
                        //reader.close();
                        thirdStep(client, secondClientRequest);
                        break;

                    // De-Register
                        case "h":
                        secondClientRequest.put("header", "De-Register");
                        secondClientRequest.put("username", username);
                        //readerSecondStep.close();
                        thirdStep(client, secondClientRequest);
                        System.exit(-1);
                        break;

                    default:
                        System.out.println("Invalid Input.");
                        //reader.close();
                        thirdStep(client, secondClientRequest);
                }
    }

    // Send the client request to server
    public static void thirdStep(DatagramSocket client, JSONObject secondClientRequest) throws JSONException, IOException {

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

        int retries = 0;

        // Receive response from server with retries after 1s timeout

        try {
            client.receive(packet);
        } catch (SocketTimeoutException q) {
            retries++;
            client.send(p);
            System.out.println("Packet sent again, retry #: " + retries);

            try {
                client.receive(packet);
            } catch (SocketTimeoutException r) {
                retries++;
                client.send(p);
                System.out.println("Packet sent again, retry #: " + retries);
                try {
                    client.receive(packet);
                } catch (SocketTimeoutException s) {
                    retries++;
                    client.send(p);
                    System.out.println("Packet sent again, retry #: " + retries);
                    try {
                        client.receive(packet);
                    } catch (SocketTimeoutException t) {
                        System.out.println("Max number of retries: " + retries);
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Receive response from server
        String serverResponse = new String(packet.getData());
        JSONObject jsonResponse = new JSONObject(serverResponse);
        System.out.println("Server response: " + jsonResponse.toString());

        // client.close();
    }
}
