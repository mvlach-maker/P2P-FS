package Coen366Project;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;

public class Serialization implements Serializable {
    ArrayList<Client> clientList;
    ArrayList<Client> clientListLoggedOn;

    public Serialization(ArrayList<Client> clientList, ArrayList<Client> clientListLoggedOn) {
        this.clientList = clientList;
        this.clientListLoggedOn = clientListLoggedOn;
    }

    ArrayList<Client> getClientList() {
        return  clientList;
    }
    ArrayList<Client> getClientListLoggedOn() {
        return  clientListLoggedOn;
    }
}
