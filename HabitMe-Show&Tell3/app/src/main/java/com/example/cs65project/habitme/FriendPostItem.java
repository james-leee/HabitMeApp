package com.example.cs65project.habitme;

/**
 * Created by haominzhang on 15/3/4.
 * This is the definition of the post object in FriendFragment.
 */
public class FriendPostItem {
    private String uid;
    private String pid;
    private String user_img;
    private String post_title;
    private String post_content;
    private String post_img;
    private String location;
    private int privacy;

    public FriendPostItem() {
        uid = new String();
        pid = new String();
        post_img = new String();
        post_title = new String();
        post_content = new String();
        post_img = new String();
        location = new String();
        privacy = 0;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getPrivacy() {
        return privacy;
    }

    public void setPrivacy(int privacy) {
        this.privacy = privacy;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUser_img() {
        return user_img;
    }

    public void setUser_img(String user_img) {
        this.user_img = user_img;
    }

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getPost_content() {
        return post_content;
    }

    public void setPost_content(String post_content) {
        this.post_content = post_content;
    }

    public String getPost_img() {
        return post_img;
    }

    public void setPost_img(String post_img) {
        this.post_img = post_img;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String loc) {
        this.location = loc;
    }
}
