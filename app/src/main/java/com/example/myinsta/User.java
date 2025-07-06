package com.example.myinsta;

public class User {
    private String uid;
    private String username;
    private String email;
    private String profileImageUrl;
    private String bio;
    private String fcmToken;

    public User() {
    }

    public User(String uid, String username, String email, String profileImageUrl, String bio) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
    }

    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getBio() { return bio; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public void setUid(String uid) { this.uid = uid; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setBio(String bio) { this.bio = bio; }
}
