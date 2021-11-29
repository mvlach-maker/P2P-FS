package Coen366Project;
import netscape.javascript.JSObject;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.Buffer;


public class ClientAsServerTCP implements Runnable {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientAsServerTCP(ServerSocket serverSocket) throws Exception {
        this.serverSocket = serverSocket;
    }

    private void listen() throws Exception {

        // Accept client connection
        boolean running = true;
        String data = null;

        while (running) {

            System.out.println(serverSocket.getLocalPort());
            Socket clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String message = in.readLine();
            System.out.println("From another client: " + message);

            JSONObject jsonRequest = new JSONObject(message);
            String header = (String) jsonRequest.get("header");

            if (header.equalsIgnoreCase("Download")) {

                String fileName = (String) jsonRequest.get("file");
                File fileToDownload = new File ( "/Users/marina/eclipse-workspace/Coen366Project/src/Coen366Project/" + fileName);

                if (fileToDownload.isFile() && fileToDownload.exists()) {
                    // Download file
                    int chunkNumber = 1;
                    BufferedReader reader = new BufferedReader(new FileReader(fileToDownload));
                    char[] buffer = new char[200];

                    int length = (reader.read(buffer, 0, 200));
                    while (length > 0) {

                        JSONObject jsonResponse = new JSONObject();
                        String chunkOfFile = String.valueOf(buffer);
                        if (length < 200) {
                            jsonResponse.put("header", "File-End");
                        } else jsonResponse.put("header", "File");

                        jsonResponse.put("file", fileName);
                        jsonResponse.put("chunk", chunkNumber);
                        jsonResponse.put("text", chunkOfFile);

                        out.write(jsonResponse.toString());
                        out.flush();
                        chunkNumber++;
                    }
                }
                else {
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("header", "Download-Error");
                    jsonResponse.put("reason", "File Does Not Exist");
                }
            }

            in.close();
            out.close();
        }

        clientSocket.close();
        serverSocket.close();
    }

    public InetAddress getSocketAddress() {
        return this.serverSocket.getInetAddress();
    }

    public int getPort() {
        return this.serverSocket.getLocalPort();
    }

    public void run() {
        try {
            listen();
        } catch (Exception e) {
            System.out.println("exception e ");
            e.printStackTrace();
        }
    }
}
