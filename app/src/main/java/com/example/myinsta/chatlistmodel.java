package com.example.myinsta;

import java.util.List;

public class chatlistmodel {
    private String lastmessage;
    private com.google.firebase.Timestamp lastTimestamp;
    private List<String> participants;
    public chatlistmodel(){
    }
    public void setlastmessage(String lastmessage){
        this.lastmessage=lastmessage;
    }
    public void setLasttimetimestamp(com.google.firebase.Timestamp lasttimetimestamp){
        this.lastTimestamp=lasttimetimestamp;
    }
    public void setParticipants(List<String> participants){
        this.participants=participants;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public com.google.firebase.Timestamp getlastTimestamp() {
        return lastTimestamp;
    }

    public String getLastmessage() {
        return lastmessage;
    }
}
