package com.test.tabs.tabs.com.tabs.database.posts;

/**
 * Created by Chiharu on 10/26/2015.
 */
public class Post {
    private String id;
    private String name;
    private String status;
    private String posterUserId;
    private String timeStamp;
    //Privacy = 0 means public, 1 = private meaning only between friends
    private Integer privacy;
    private double longitude;
    private double latitude;

    public Post(){
        // empty default constructor, necessary for Firebase to be able to deserialize blog posts
    }

    public Post(String id, String name, String status, String posterUserId, String timeStamp, Integer privacy, double latitude, double longitude) {
        super();
        this.id = id;
        this.name = name;
        this.status = status;
        this.posterUserId = posterUserId;
        this.timeStamp = timeStamp;
        this.privacy = privacy;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getPrivacy() { return privacy; }

    public void setPrivacy(Integer privacy) { this.privacy = privacy; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void getLongitude(double longitude) {
        this.longitude = longitude;
    }

}
