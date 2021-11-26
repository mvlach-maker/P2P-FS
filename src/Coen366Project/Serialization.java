package Coen366Project;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;

public class Serialization implements Serializable {
    ArrayList<Client> clientList;
    ArrayList<Client> clientListLoggedOn;
    int requestNumber;

    // Check if file exists & if it does can open it

    public Serialization(ArrayList<Client> clientList, ArrayList<Client> clientListLoggedOn, int requestNumber) {
        this.clientList = clientList;
        this.clientListLoggedOn = clientListLoggedOn;
        this.requestNumber = requestNumber;
    }

    ArrayList<Client> getClientList() {
        return  clientList;
    }
    ArrayList<Client> getClientListLoggedOn() {
        return  clientListLoggedOn;
    }
}
