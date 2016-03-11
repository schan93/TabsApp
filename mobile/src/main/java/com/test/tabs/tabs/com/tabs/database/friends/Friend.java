package com.test.tabs.tabs.com.tabs.database.friends;

/**
 * Created by schan on 10/28/15.
 */
public class Friend {

    Friend(){
    }

    public Friend(String id, String userId, String name, String user, String isFriend){
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.user = user;
        this.isFriend = isFriend;
    }

    private String id;
    private String userId;
    private String name;
    private String user;
    private String isFriend;

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public String getUser() { return user; }

    public String getIsFriend() { return isFriend; }

    public void setId(String id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setUserId(String userId) {this.userId = userId; }

    public void setIsFriend(String isFriend) { this.isFriend = isFriend; }

    public void setUser(String user) { this.user = user; }
}
