package com.example.cs65project.habitme;

/**
 * Created by yuanjiang on 3/7/15.
 * This class is the Comment object. It will be shown in the friends fragment
 * It is the comment for a post from the user or its friends
 */
public class Comment {

    private String pid;
    private String user;
    private String comment;

    public Comment() {
        this.pid = new String();
        this.user = new String();
        this.comment = new String();
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
