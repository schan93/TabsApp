package com.test.tabs.tabs.com.tabs.database.friends;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import com.test.tabs.tabs.com.tabs.activity.Comments;
import com.test.tabs.tabs.com.tabs.activity.CommentsHeader;
import com.test.tabs.tabs.com.tabs.activity.FriendsHeader;
import com.test.tabs.tabs.com.tabs.activity.news_feed;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by schan on 10/31/15.
 */

public class FriendRecyclerViewAdapter extends RecyclerView.Adapter<FriendRecyclerViewAdapter.FriendViewHolder> {

    private List<Friend> friendItems;

    public List<Friend> getFriends() {
        return friendItems;
    }

    public void setFriends(List<Friend> friendItems) {
        this.friendItems = friendItems;
    }

    public static Friend containsId(List<Friend> list, String id) {
        for (Friend object : list) {
            if (object.getUserId().equals(id)) {
                return object;
            }
        }
        return null;
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewFriend;
        TextView name;
        CheckBox checkBox;
        SimpleDraweeView friendProfilePhoto;

        FriendViewHolder(View itemView) {
            super(itemView);
            System.out.println("FriendViewHolder: Constructor");
            cardViewFriend = (CardView) itemView.findViewById(R.id.friend_card_view);
            name = (TextView) itemView.findViewById(R.id.friend_name);
            friendProfilePhoto = (SimpleDraweeView) itemView.findViewById(R.id.friend_profile_picture);
            checkBox = (CheckBox) itemView.findViewById(R.id.friend_checkbox);
        }
    }

    //+1 for header
    @Override
    public int getItemCount() {
        return friendItems.size();
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_item, viewGroup, false);
        FriendViewHolder pvh = new FriendViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(FriendViewHolder friendViewHolder, int i) {
            String name = friendItems.get(i).getName();
            String userId = friendItems.get(i).getUserId();
            String isFriend = friendItems.get(i).getIsFriend();
            friendViewHolder.name.setText(name);
            DraweeController controller = news_feed.getImage(userId);
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
            roundingParams.setRoundAsCircle(true);
            friendViewHolder.friendProfilePhoto.getHierarchy().setRoundingParams(roundingParams);
            friendViewHolder.friendProfilePhoto.setController(controller);
            if(isFriend.equals("true")) {
                friendViewHolder.checkBox.setChecked(true);
            } else {
                friendViewHolder.checkBox.setChecked(false);
            }

            final int itemPosition = i;
            friendViewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean updateSuccessful;
                    if (((CheckBox) v).isChecked()) {
                        friendItems.get(itemPosition).setIsFriend("true");
                    } else {
                        friendItems.get(itemPosition).setIsFriend("false");
                    }
                }
            });
    }

    public FriendRecyclerViewAdapter(List<Friend> friends) {
        this.friendItems = friends;
    }


    public void add(Friend friend){
        for(int i = 0; i < friendItems.size(); i++) {
            if(friend.getId().equals(friendItems.get(i).getId())) {
                return;
            }
        }
        friendItems.add(friend);
        notifyItemInserted(friendItems.size() + 1);
        notifyItemRangeChanged(friendItems.size() + 1, friendItems.size());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}