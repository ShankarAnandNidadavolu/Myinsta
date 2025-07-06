package com.example.myinsta;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.Timestamp;

public class Messagemodel {
    private String sender;
    private String message;
    private Timestamp timestamp;

    public Messagemodel() {}

    public Messagemodel(String sender, String message,Timestamp timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
