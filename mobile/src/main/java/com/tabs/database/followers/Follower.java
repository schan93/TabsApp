package com.tabs.database.followers;

import java.io.Serializable;

/**
 * Created by schan on 5/14/16.
 */
public class Follower implements Serializable{

    private static final long serialVersionUID = -54221803423294898L;

    Follower(){
    }

    public Follower(String id, String userId, String name, String user, Boolean isAlsoFollowing){
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.user = user;
        this.isAlsoFollowing = isAlsoFollowing;
    }
    ;
    private String id;
    private String userId;
    private String name;
    private String user;
    private Boolean isAlsoFollowing;


    public String getName(){
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public String getId() {
        return id;
    }

    public String getUser() { return user; }

    public Boolean getIsAlsoFollowing() { return isAlsoFollowing; }

    public void setId(String id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setUserId(String userId) {this.userId = userId; }

    public void setIsAlsoFollowing(Boolean isAlsoFollowing) { this.isAlsoFollowing = isAlsoFollowing; }

    public void setUser(String user) { this.user = user; }
}
