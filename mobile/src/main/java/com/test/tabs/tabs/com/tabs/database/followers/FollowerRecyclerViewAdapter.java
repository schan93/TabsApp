package com.test.tabs.tabs.com.tabs.database.followers;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.activity.AndroidUtils;
import com.test.tabs.tabs.com.tabs.activity.FireBaseApplication;
import com.test.tabs.tabs.com.tabs.activity.TabsUtil;
import com.test.tabs.tabs.com.tabs.database.Database.DatabaseQuery;
import com.test.tabs.tabs.com.tabs.database.users.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schan on 5/14/16.
 */
public class FollowerRecyclerViewAdapter extends RecyclerView.Adapter<FollowerRecyclerViewAdapter.FollowerViewHolder>{

    private List<User> followers;
    private FireBaseApplication fireBaseApplication;
    private DatabaseQuery databaseQuery;
    Context context;
    private Map<String, Boolean> changedFollowing;

    public Map<String, Boolean> getChangedFollowing() {
        return this.changedFollowing;
    }

    public void setFollowers(List<User> followers) {
        this.followers = followers;
    }

    public User containsUserId(String id) {
        for (User object : followers) {
            if (object != null && object.getUserId() != null && object.getUserId().equals(id)) {
                return object;
            }
        }
        return null;
    }

    public FollowerRecyclerViewAdapter(List<User> followers) {
        this.followers = followers;
    }

    public FollowerRecyclerViewAdapter(List<User> followers, Context context) {
        this.followers = followers;
        this.context = context;
    }

    public void setupFollowersRecyclerView(DatabaseQuery databaseQuery, Context context) {
        this.databaseQuery = databaseQuery;
        this.context = context;
        this.changedFollowing = new HashMap<String, Boolean>();
    }

    public List<User> getFollowers(){
        return this.followers;
    }

    @Override
    public int getItemCount() {
        return followers.size();
    }

    public static class FollowerViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewfollower;
        TextView name;
        Button isFollowingButton;
        SimpleDraweeView followerProfilePhoto;

        FollowerViewHolder(View itemView) {
            super(itemView);
            cardViewfollower = (CardView) itemView.findViewById(R.id.follower_card_view);
            name = (TextView) itemView.findViewById(R.id.follower_name);
            followerProfilePhoto = (SimpleDraweeView) itemView.findViewById(R.id.follower_profile_picture);
            isFollowingButton = (Button) itemView.findViewById(R.id.follower_button);
        }
    }

    @Override
    public void onBindViewHolder(FollowerViewHolder followerViewHolder, int i) {
        final User currentItem = getItem(i);
        followerViewHolder.name.setText(currentItem.getName());
        DraweeController controller = TabsUtil.getImage(currentItem.getUserId());
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        followerViewHolder.followerProfilePhoto.getHierarchy().setRoundingParams(roundingParams);
        followerViewHolder.followerProfilePhoto.setController(controller);
        List<User> followers = fireBaseApplication.getFollowingRecyclerViewAdapter().getFollowers();
        setupFollowButton(followerViewHolder.isFollowingButton, currentItem.getUserId(), currentItem.getName());
//        if(fireBaseApplication.getFollowingRecyclerViewAdapter().containsUserId(followers, currentItem.getUserId()) != null) {
//            followerViewHolder.isFollowingButton.setText("Following");
//            followerViewHolder.isFollowingButton.setBackgroundResource(R.drawable.following_button_bg);
//            followerViewHolder.isFollowingButton.setTextColor(ContextCompat.getColor(followerViewHolder.itemView.getContext(), R.color.white));
//        } else {
//            followerViewHolder.isFollowingButton.setText("+ Follow");
//            followerViewHolder.isFollowingButton.setBackgroundResource(R.drawable.follow_button_bg);
//            followerViewHolder.isFollowingButton.setTextColor(ContextCompat.getColor(followerViewHolder.itemView.getContext(), R.color.colorPrimary));
//
//        }
//        if(fireBaseApplication.getFollo().containsId(fireBaseApplication.getFollowerRecyclerViewAdapter().getFollowers(), currentItem.getId()) != null){
//            followerViewHolder.isFollowingCheckBox.setChecked(true);
//        } else {
//            followerViewHolder.isFollowingCheckBox.setChecked(false);
//        }

//        followerViewHolder.isFollowingCheckBox.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (((CheckBox) v).isChecked()) {
//                    //Do something if you check it
//                } else {
//                    //Do something if you uncheck it
//                }
//            }
//        });
    }

    @Override
    public FollowerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.follower_item, viewGroup, false);
        FollowerViewHolder pvh = new FollowerViewHolder(v);
        return pvh;
    }

    private User getItem(int position)
    {
        return followers.get(position);
    }

    public void add(User follower){
        followers.add(follower);
        notifyItemInserted(followers.size() - 1);
//        notifyItemRangeChanged(followers.size() + 1, followers.size());
    }

    public void remove(Follower item) {
        int position = followers.indexOf(item);
        followers.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void setupFollowButton(final Button button, final String posterUserId, final String posterName) {
        if(fireBaseApplication.getFollowingRecyclerViewAdapter().containsUserId(posterUserId) != null && (changedFollowing.get(posterUserId) == null || (changedFollowing.get(posterUserId) != null && changedFollowing.get(posterUserId)))) {
            setButtonIsFollowing(button, context);
        } else {
            setButtonIsNotFollowing(button, context);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = fireBaseApplication.getFollowingRecyclerViewAdapter().containsUserId(posterUserId);
                if(user != null && (changedFollowing.get(posterUserId) == null || (changedFollowing.get(posterUserId) != null && changedFollowing.get(posterUserId)))) {
                    changedFollowing.put(posterUserId, false);
                    databaseQuery.removeFollowing(posterUserId);
                } else {
                    User newUser = new User();
                    newUser.setUserId(posterUserId);
                    newUser.setName(posterName);
                    newUser.setId(AndroidUtils.generateId());
                    changedFollowing.put(posterUserId, true);
                    databaseQuery.addFollowing(posterUserId);
                }
            }
        });
    }

    private void setButtonIsFollowing(Button button, Context context) {
        button.setBackgroundResource(R.drawable.following_button_bg);
        button.setTextColor(ContextCompat.getColor(context, R.color.white));
        button.setText("Following");
    }

    private void setButtonIsNotFollowing(Button button, Context context) {
        button.setBackgroundResource(R.drawable.follow_button_bg);
        button.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        button.setText("+ Follow");
    }



}
