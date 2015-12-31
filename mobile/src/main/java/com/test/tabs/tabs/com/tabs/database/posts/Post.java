package com.test.tabs.tabs.com.tabs.database.posts;

/**
 * Created by Chiharu on 10/26/2015.
 */
public class Post {
    private long id;
    private String name;
    private String status;
    private String posterUserId;
    private String timeStamp;
    //Privacy = 0 means public, 1 = private meaning only between friends
    private Integer privacy;

    public Post(long id, String name, String status, String posterUserId, String timeStamp, Integer privacy) {
        super();
        this.id = id;
        this.name = name;
        this.status = status;
        this.posterUserId = posterUserId;
        this.timeStamp = timeStamp;
        this.privacy = privacy;
    }

    public Integer getPrivacy() { return privacy; }

    public void setPrivacy(Integer privacy) { this.privacy = privacy; }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPosterUserId() {
        return posterUserId;
    }

    public void setPosterUserId(String posterUserId) {
        this.posterUserId = posterUserId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

}
