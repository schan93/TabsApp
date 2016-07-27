package com.tabs.database.posts;

import com.tabs.activity.PrivacyEnum;

/**
 * Created by Chiharu on 10/26/2015.
 */
public class Post{
    private String title;
    private String id;
    private String name;
    private String status;
    private String posterUserId;
    private String timeStamp;
    private PrivacyEnum privacy;
    private Integer numComments;

    /**
     * Empty default constructor, necessary for Firebase to be able to deserialize Posts
     */
    public Post(){
    }

    public Post(String id, String title, String name, String status, String posterUserId, String timeStamp, PrivacyEnum privacy, Integer numComments) {
        super();
        this.id = id;
        this.title = title;
        this.name = name;
        this.status = status;
        this.posterUserId = posterUserId;
        this.timeStamp = timeStamp;
        this.privacy = privacy;
        this.numComments = numComments;
    }

    public PrivacyEnum getPrivacy() { return privacy; }

    public void setPrivacy(PrivacyEnum privacy) { this.privacy = privacy; }

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

    public Integer getNumComments() { return numComments; }

    public void setNumComments(Integer numComments) { this.numComments = numComments; }

    public void setTitle(String title) { this.title = title; }

    public String getTitle() { return title; };
}
