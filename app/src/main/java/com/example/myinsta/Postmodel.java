package com.example.myinsta;

import java.security.PublicKey;
import com.google.firebase.Timestamp;

public class Postmodel {
    private String userUid;
    private String posturl;
    private String post_desc;
    private Timestamp timestamp;
    private String postid;
    public Postmodel(){
    }
    public Postmodel(String userUid,String posturl,String post_desc,Timestamp timestamp){
        this.userUid=userUid;
        this.posturl=posturl;
        this.post_desc=post_desc;
        this.timestamp=timestamp;
    }

    public String getUserUid(){
        return userUid;
    }
    public String getPosturl(){
        return posturl;
    }
    public String getPost_desc(){
        return post_desc;
    }
    public Timestamp getTimestamp(){
        return timestamp;
    }
    public String getPostid(){
        return postid;
    }


    public void setPostid(String postid){
        this.postid=postid;
    }
    public void setUserUid(String userUid){
        this.userUid=userUid;
    }
    public void setPosturl(String posturl){
        this.posturl=posturl;
    }
    public void setPost_desc(String post_desc){
        this.post_desc=post_desc;
    }
    public void setTimestamp(com.google.firebase.Timestamp timestamp){
        this.timestamp=timestamp;
    }
}
