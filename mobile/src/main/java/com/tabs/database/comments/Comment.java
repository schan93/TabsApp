package com.tabs.database.comments;

import java.io.Serializable;

/**
 * Created by schan on 11/27/15.
 */

//Just for ONE individual comment
public class Comment implements Serializable{
    private static final long serialVersionUID = -5194275075664836313L;
    private String id; //This is the ID of the coment
    private String postId; //ID of the post
    private String commenter; //Can be same as poster
    private String comment; //Message that is written
    private String commenterUserId; //Commtenter id from facebook
    private Long timeStamp; //When he wrote it

    Comment(){
    }

    public Comment(String uniqueId, String postId, String commenter, String comment, String commenterUserId, Long timeStamp){
        this.id = uniqueId;
        this.postId = postId;
        this.commenter = commenter;
        this.comment = comment;
        this.commenterUserId = commenterUserId;
        this.timeStamp = timeStamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCommenter() {
        return this.commenter;
    }

    public void setCommenter(String commenter){ this.commenter = commenter; }

    public String getComment() { return this.comment; }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommenterUserId() {
        return commenterUserId;
    }

    public void setCommenterUserId(String commenterUserId) { this.commenterUserId = commenterUserId; }

    public void setPosterUserId(String commenterUserId) {
        this.commenterUserId = commenterUserId;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}