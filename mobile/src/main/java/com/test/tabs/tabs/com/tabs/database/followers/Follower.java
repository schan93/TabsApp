package com.test.tabs.tabs.com.tabs.database.followers;

/**
 * Created by schan on 5/14/16.
 */
public class Follower {

    Follower(){
    }

    public Follower(String id, String userId, String name, String user, String isFollowing){
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.user = user;
        this.isFollowing = isFollowing;
    }
    ;
    private String id;
    private String userId;
    private String name;
    private String user;
    private String isFollowing;

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

    public String getIsFollowing() { return isFollowing; }

    public void setId(String id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setUserId(String userId) {this.userId = userId; }

    public void setIsFollowing(String isFriend) { this.isFollowing = isFriend; }

    public void setUser(String user) { this.user = user; }
}
