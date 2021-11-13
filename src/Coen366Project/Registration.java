package Coen366Project;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.File;
import java.net.InetAddress;

public class Registration {

	public void login(String username, InetAddress ip) {
		// Go through the client list and match username and ip
		// if there is no match - tell the user that there is no existing client and restart the Client thread

	}

	public void Register(DatagramSocket serverSocket, File clientListJson) {

		// Get username and check if it is already in use (.json text file)

		// if it isn't, insert new user - ACCEPTED

		// if it is, don't do anything - DENIED "YOU ARE ALREADY REGISTERED" & restart client thread

		// if username is already in use but ip doesnt match - DENIED "TRY DIFFERENT USERNAME"
	}
	public void deRegister(DatagramSocket serverSocket, File clientListJson) {

		// Get username and check if it is already in use (.json text file)

		// if it isn't, insert new user - ACCEPTED

		// if it is, don't do anything - DENIED "YOU ARE ALREADY REGISTERED" - USER GETS AUTOMATICALLY LOGGED IN

		// if username is already in use but ip doesnt match - DENIED "TRY DIFFERENT USERNAME"
	}
}
