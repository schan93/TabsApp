package com.tabs.database.users;

/**
 * Created by schan on 2/9/16.
 */
public class User {
    private String id;
    private String userId;
    private String name;

    /**
     * Empty default constructor, necessary for Firebase to be able to deserialize Users
     */
    public User(){
    }

    public User(String id, String userId, String name) {
        super();
        this.id = id;
        this.userId = userId;
        this.name = name;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

}