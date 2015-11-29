package com.test.tabs.tabs.com.tabs.database.comments;

/**
 * Created by schan on 11/27/15.
 */

//Just for ONE individual comment
public class Comment {
    private long id; //This is the ID of the coment
    private long postId; //ID of the post
    private String commenter; //Can be same as poster
    private String comment; //Message that is written
    private String commenterUserId; //Commtenter id from facebook
    private String timeStamp; //When he wrote it



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long postId() {
        return postId;
    }

    public void setPostId(long postId) {
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

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

}