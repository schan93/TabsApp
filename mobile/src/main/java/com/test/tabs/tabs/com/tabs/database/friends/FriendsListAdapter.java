package com.test.tabs.tabs.com.tabs.database.friends;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.interfaces.SimpleDraweeControllerBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.login.widget.ProfilePictureView;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.activity.news_feed;

import java.util.List;

/**
 * Created by schan on 10/31/15.
 */
public class FriendsListAdapter extends BaseAdapter{

    private Activity activity;
    private LayoutInflater inflater;
    private List<Friend> friendItems;

    public List<Friend> getFriends() {
        return friendItems;
    }

    public void setFriends(List<Friend> friendItems) {
        this.friendItems = friendItems;
    }

    public static boolean containsId(List<Friend> list, String id) {
        for (Friend object : list) {
            if (object.getUserId().equals(id)) {
                return true;
            }
        }
        return false;
    }
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
        System.out.println("Getting view");
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.friend_item, null);

        datasource = new FriendsDataSource(parent.getContext());
        TextView name = (TextView) convertView.findViewById(R.id.friend_name);

        //Set profile picture
        DraweeController controller = news_feed.getImage(friendItems.get(position).getUserId());
        SimpleDraweeView draweeView = (SimpleDraweeView) convertView.findViewById(R.id.friend_profile_picture);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        draweeView.getHierarchy().setRoundingParams(roundingParams);
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.friend_checkbox);
        System.out.println("FriendsListAdapter: " + friendItems.get(position).getName() + " Is friend: " + friendItems.get(position).getIsFriend());
        if(friendItems.get(position).getIsFriend().equals("1")){
            System.out.println("FriendsListAdapter: is true.");
            checkBox.setChecked(true);
        }
        else{
            System.out.println("FriendsListAdapter: is false.");
            checkBox.setChecked(false);

        }
        draweeView.setController(controller);

        final int itemPosition = position;
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean updateSuccessful;
                if(((CheckBox) v).isChecked()){
                    //Display their posts to the newsfeed and update db
                    System.out.println("Friend User Id: " + friendItems.get(itemPosition).getUserId());
                    System.out.println("Friend user: " + friendItems.get(itemPosition).getUser());
                    updateSuccessful = datasource.updateFriend(friendItems.get(itemPosition).getUserId(), friendItems.get(itemPosition).getUser(), "1");
                    if(updateSuccessful){
                        friendItems.get(itemPosition).setIsFriend("1");
                    }
                    System.out.println("FriendsListAdapter: Friend was updated." + friendItems.get(itemPosition).getName() + " Is friend: " + friendItems.get(itemPosition).getIsFriend());
                }
                else {
                    System.out.println("FriendsListAdapter: Wrong one");
                    //Update this friend in DB
                    updateSuccessful = datasource.updateFriend(friendItems.get(itemPosition).getUserId(), friendItems.get(itemPosition).getUser(), "0");
                    if(updateSuccessful){
                        friendItems.get(itemPosition).setIsFriend("0");
                    }
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
