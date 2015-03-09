package com.example.cs65project.habitme;

/**
 * user class for each user and store the profile pictures
 * User name and User id.
 */
public class User {
    private String uid;
    private String uname;
    private String uimg;

    public String getUimg() {
        return uimg;
    }

    public void setUimg(String uimg) {
        this.uimg = uimg;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
