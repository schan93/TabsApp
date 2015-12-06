package com.test.tabs.tabs.com.tabs.database.posts;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.activity.news_feed;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.Post;

/**
 * Created by Chiharu on 10/26/2015.
 */
public class PostListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Post> posts;

    //Local Database for storing friends
    private PostsDataSource datasource;


    public PostListAdapter(Activity activity, List<Post> posts) {
        this.activity = activity;
        this.posts = posts;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Object getItem(int position) {
        return posts.get(position);
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
            convertView = inflater.inflate(R.layout.news_feed_item, null);

//        TextView name = (TextView) convertView.findViewById(R.id.friend_name);
//        ProfilePictureView profilePictureView = (ProfilePictureView) convertView.findViewById(R.id.friend_profile_picture);
//        if(profilePictureView != null) {
//            profilePictureView.setProfileId(friendItems.get(position).getId());
//        }

        TextView name = (TextView) convertView.findViewById(R.id.txt_name);
        TextView timestamp = (TextView)convertView.findViewById(R.id.txt_timestamp);
        TextView statusMsg = (TextView)convertView.findViewById(R.id.txt_statusMsg);
        TextView numComments = (TextView)convertView.findViewById(R.id.num_comments);

        //Set profile picture
        DraweeController controller = news_feed.getImage(posts.get(position).getPosterUserId());
        SimpleDraweeView draweeView = (SimpleDraweeView) convertView.findViewById(R.id.poster_profile_photo);
        draweeView.setController(controller);


        //Set the views
        Post item = posts.get(position);
        name.setText(item.getName());
        timestamp.setText(convertDate(item.getTimeStamp()));
        statusMsg.setText((item.getStatus()));

        datasource = new PostsDataSource(parent.getContext());
        datasource.open();
        System.out.println("Item id: " + item.getId());
        numComments.setText(datasource.getNumberComments(item.getId()).toString() + " Comments");
        numComments.setTextSize(12);

        return convertView;
    }

    public String convertDate(String timestamp){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        String dateText = "";
        Date date = null;
        try {
            date = dateFormat.parse(timestamp);
        } catch(Exception e){
            e.printStackTrace();
        }

        Calendar postDate = Calendar.getInstance();
        postDate.setTime(date); // your date

        Calendar now = Calendar.getInstance();

        Integer dateOffset = 0;

        if (now.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR)
                && now.get(Calendar.DAY_OF_MONTH) == postDate.get(Calendar.DAY_OF_MONTH)
                && (now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR) > 1)){

            dateOffset = now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR);
            dateText = "h";
        }
        else if(now.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR)
                && now.get(Calendar.DAY_OF_MONTH) == postDate.get(Calendar.DAY_OF_MONTH)
                && (now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR) == 0)){
            dateOffset = now.get(Calendar.MINUTE) - postDate.get(Calendar.MINUTE);
            dateText = "m";
        }
        else{
            dateOffset = now.get(Calendar.DAY_OF_YEAR) - postDate.get(Calendar.DAY_OF_YEAR);
            dateText = "d";
        }
        String newFormat = dateOffset + dateText;
        return newFormat;
    }
}
