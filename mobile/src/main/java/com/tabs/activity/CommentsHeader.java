package com.tabs.activity;

/**
 * Created by schan on 12/16/15.
 */
public class CommentsHeader {
    private String posterName;
    private String posterDate;
    private String viewStatus;
    private String posterUserId;
    private Boolean isFollowing;
    private String postTitle;

    public CommentsHeader(){}

    public String getPosterUserId(){
        return posterUserId;
    }
    public void setPosterUserId(String posterUserId){
        this.posterUserId = posterUserId;
    }
    public String getPosterName() {
        return posterName;
    }
    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public String getPosterDate() {
        return posterDate;
    }
    public void setPosterDate(String posterDate) {
        this.posterDate = posterDate;
    }

    public String getViewStatus() {
        return viewStatus;
    }
    public void setViewStatus(String viewStatus) {
        this.viewStatus = viewStatus;
    }

    public Boolean getIsFollowing() { return isFollowing; }
    public void setIsFollowing(Boolean isFollowing) { this.isFollowing = isFollowing; }

    public String getPostTitle() { return postTitle; }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }


}
