package com.tabs.database.posts;

import com.google.firebase.database.Exclude;
import com.tabs.activity.PrivacyEnum;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chiharu on 10/26/2015.
 */
public class Post implements Serializable {
    private static final long serialVersionUID = -2022770406236044428L;
    private String title;
    private String id;
    private String name;
    private String status;
    private String posterUserId;
    private String timeStamp;
    private PrivacyEnum privacy;
    private Integer numComments;
    private Map<String, Object> commenters;

    /**
     * Empty default constructor, necessary for DatabaseReference to be able to deserialize Posts
     */
    public Post(){
    }

    public Post(String id, String title, String name, String status, String posterUserId, String timeStamp, String privacy, Integer numComments) {
        super();
        this.id = id;
        this.title = title;
        this.name = name;
        this.status = status;
        this.posterUserId = posterUserId;
        this.timeStamp = timeStamp;
        setPrivacy(privacy);
        this.numComments = numComments;
    }

    public String getPrivacy() {
        // Convert enum to string
        if (privacy == null) {
            return null;
        } else {
            return privacy.name();
        }
    }

    public void setPrivacy(String privacyString) {
        if (privacyString == null) {
            this.privacy = null;
        } else {
            this.privacy = privacy.valueOf(privacyString);
        }
    }

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

    public void setCommenters(Map<String, Object> commenters) { this.commenters = commenters; }

    public Map<String, Object> getCommenters() { return this.commenters; }
}
