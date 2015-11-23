package com.test.tabs.tabs.com.tabs.database.friends;

/**
 * Created by schan on 10/28/15.
 */
public class Friend {
    private long id;
    private String user_id;
    private String name;
    private String user;
    private Integer isFriend;

    public long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getUserId() {
        return user_id;
    }

    public String getUser() { return user; }

    public Integer getIsFriend() { return isFriend; }

    public void setId(long id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setUserId(String user_id) {this.user_id = user_id; }

    public void setIsFriend(Integer isFriend) { this.isFriend = isFriend; }

    public void setUser(String user) { this.user = user; }
}
