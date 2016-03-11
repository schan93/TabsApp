package com.test.tabs.tabs.com.tabs.database.posts;

import com.firebase.client.Firebase;

/**
 * Created by Chiharu on 10/26/2015.
 */
public class Post {
    private String id;
    private String name;
    private String status;
    private String posterUserId;
    private String timeStamp;
    private String privacy;
    private String longitude;
    private String latitude;
    private Integer numComments;

    /**
     * Empty default constructor, necessary for Firebase to be able to deserialize Posts
     */
    public Post(){
    }

    public Post(String id, String name, String status, String posterUserId, String timeStamp, String privacy, String latitude, String longitude, Integer numComments) {
        super();
        this.id = id;
        this.name = name;
        this.status = status;
        this.posterUserId = posterUserId;
        this.timeStamp = timeStamp;
        this.privacy = privacy;
        this.latitude = latitude;
        this.longitude = longitude;
        this.numComments = numComments;
    }

    public String getPrivacy() { return privacy; }

    public void setPrivacy(String privacy) { this.privacy = privacy; }

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

    public String getLatitude() { return latitude; }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void getLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Integer getNumComments() { return numComments; }

    public void setNumComments(Integer numComments) { this.numComments = numComments; }

}
