package Coen366Project;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import java.io.Serializable;
import java.util.Scanner;
import java.net.*;
import java.util.*;

// Data Structure class

public class Client implements Serializable {

	private static final long serialVersionUID = 1234568L;

	String username;

	InetAddress clientIp;
	int udp;
	int tcp;
	ArrayList<File> listOfFileObjects;
	ArrayList<String> listOfFiles;

	// Constructor
	public Client(String username, InetAddress clientIp, int udp, int tcp){
		this.username = username;
		this.clientIp = clientIp;
		this.udp = udp;
		this.tcp = tcp;
		listOfFileObjects = new ArrayList<>();
	}


	JSONObject getClientInfo() throws JSONException {
		JSONObject client = new JSONObject(); 
		client.put("username", username);
		client.put("ip", clientIp);
		client.put("tcp", tcp);
		client.put("udp", udp);

		listOfFiles = new ArrayList<>();

		for (File f : listOfFileObjects) {

			String filename = f.getName();
			listOfFiles.add(filename);

		}
		client.put("listOfFiles", listOfFiles);
		return client; 
	}

	String getUsername() {
		return username;
	}

	InetAddress getIp() {
		return clientIp;
	}

	int getClientUdp() {
		return udp;
	}

	int getClientTcp() {
		return tcp;
	}

	String setUsername() {
		return username;
	}

	void setClientIp(InetAddress clientIp) {
		this.clientIp = clientIp;
	}

	void setClientUdp(int udp) {
		this.udp = udp;
	}

	void setClientTcp(int tcp) {
		this.tcp = tcp;
	}

	void printClientInfo() {
		System.out.print("Username: " + username + "\n Ip: " + clientIp + "\n Udp: " + udp +
				"\n Tcp: " + tcp + "\n Files: \n");

		for (File f: listOfFileObjects) {
			System.out.println(f.getName());
		}
	}
}






		



