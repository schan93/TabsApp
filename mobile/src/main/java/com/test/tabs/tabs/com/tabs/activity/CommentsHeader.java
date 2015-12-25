package com.test.tabs.tabs.com.tabs.activity;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by schan on 12/16/15.
 */
public class CommentsHeader {
    private String posterName;
    private String posterDate;
    private String viewStatus;
    private String posterUserId;

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


}
