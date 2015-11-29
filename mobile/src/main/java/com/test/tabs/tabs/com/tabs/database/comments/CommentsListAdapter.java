package com.test.tabs.tabs.com.tabs.database.comments;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.activity.news_feed;

import java.util.List;

/**
 * Created by schan on 11/28/15.
 */
public class CommentsListAdapter extends BaseAdapter{

    private Activity activity;
    private LayoutInflater inflater;
    private List<Comment> comments;

    //Local Database for storing friends
    private CommentsDataSource datasource;


    public CommentsListAdapter(Activity activity, List<Comment> comments) {
        this.activity = activity;
        this.comments = comments;
    }

    @Override
    public int getCount() {
        return comments.size();
    }

    @Override
    public Object getItem(int position) {
        return comments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.comment_item, null);

        datasource = new CommentsDataSource(parent.getContext());
//        TextView name = (TextView) convertView.findViewById(R.id.friend_name);
//        ProfilePictureView profilePictureView = (ProfilePictureView) convertView.findViewById(R.id.friend_profile_picture);
//        if(profilePictureView != null) {
//            profilePictureView.setProfileId(friendItems.get(position).getId());
//        }

        TextView commenterName = (TextView) convertView.findViewById(R.id.commenter_name);
        TextView commentTimestamp = (TextView)convertView.findViewById(R.id.comment_timestamp);
        TextView commentMsg = (TextView)convertView.findViewById(R.id.comment_message);

        //Set profile picture
        DraweeController controller = news_feed.getImage(comments.get(position).getCommenterUserId());
        SimpleDraweeView draweeView = (SimpleDraweeView) convertView.findViewById(R.id.commenter_profile_photo);
        draweeView.setController(controller);


        //Set the views
        Comment item = comments.get(position);
        commenterName.setText(item.getCommenter());
        commentTimestamp.setText(item.getTimeStamp());
        commentMsg.setText((item.getComment()));

        //TODO: set the other data fields in Post

        return convertView;
    }
}
