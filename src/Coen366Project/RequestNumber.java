package Coen366Project;

import java.io.Serializable;

public class RequestNumber implements Serializable {
    int requestNumber;

    RequestNumber(int requestNumber) {
        this.requestNumber = requestNumber;
    }

    int getRequestNumber() {
        return requestNumber;
    }

}