package com.test.tabs.tabs.com.tabs.database.friends;

/**
 * Created by schan on 10/28/15.
 */
public class Friend {
    private long id;
    private String user_id;
    private String name;

    public long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getUserId() {
        return user_id;
    }

    public void setId(long id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setUserId(String user_id) {this.user_id = user_id; }
}
