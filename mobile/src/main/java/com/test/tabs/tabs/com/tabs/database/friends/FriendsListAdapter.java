package com.test.tabs.tabs.com.tabs.database.friends;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.facebook.login.widget.ProfilePictureView;
import com.test.tabs.tabs.R;

import java.util.List;

/**
 * Created by schan on 10/31/15.
 */
public class FriendsListAdapter extends BaseAdapter{

    private Activity activity;
    private LayoutInflater inflater;
    private List<Friend> friendItems;
    //ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    //Local Database for storing friends
    private FriendsDataSource datasource;

    public FriendsListAdapter(Activity activity, List<Friend> friendItems) {
        this.activity = activity;
        this.friendItems = friendItems;
    }

    @Override
    public int getCount() {
        return friendItems.size();
    }

    @Override
    public Object getItem(int position) {
        return friendItems.get(position);
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
            convertView = inflater.inflate(R.layout.friend_item, null);

        datasource = new FriendsDataSource(parent.getContext());
        datasource.open();
        TextView name = (TextView) convertView.findViewById(R.id.friend_name);
        ProfilePictureView profilePictureView = (ProfilePictureView) convertView.findViewById(R.id.friend_profile_picture);
        if(profilePictureView != null) {
            profilePictureView.setProfileId(friendItems.get(position).getUserId());
        }

        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.friend_checkbox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CheckBox) v).isChecked()){
                    //Display their posts to the newsfeed
                    System.out.println("Checked.");
                }
                else {
                    //Remove posts from newsfeed. Probably need some sort of loader icon thing.
                }
            }
        });
        //Set the views
        Friend item = friendItems.get(position);

        name.setText(item.getName());

        //TODO: set the other data fields in Post

        return convertView;
    }

}
