package com.tabs.database.followers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.schan.tabs.R;
import com.tabs.activity.UserProfileActivity;
import com.tabs.utils.AndroidUtils;
import com.tabs.activity.FireBaseApplication;
import com.tabs.enums.FollowerEnum;
import com.tabs.utils.TabsUtil;
import com.tabs.database.databaseQuery.DatabaseQuery;
import com.tabs.database.users.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schan on 5/14/16.
 */
public class FollowerRecyclerViewAdapter extends RecyclerView.Adapter<FollowerRecyclerViewAdapter.FollowerViewHolder>{

    private List<User> followers;
    private DatabaseQuery databaseQuery;
    private Context context;
    private FireBaseApplication fireBaseApplication;
    private Map<String, Boolean> changedFollowing;
    private FollowerEnum followerEnum;
    private String posterUserId;
    private String posterName;
    private String postStatus;
    private Long postTimeStamp;
    private String postTitle;
    private String userProfileId;

    public Map<String, Boolean> getChangedFollowing() {
        return this.changedFollowing;
    }

    public void initializeChangedFollowing() {
        this.changedFollowing = new HashMap<>();
    }

    public FollowerEnum getFollowerEnum() {
        return this.followerEnum;
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

    public FollowerRecyclerViewAdapter(List<User> followers, Context context, FollowerEnum followerEnum, FireBaseApplication fireBaseApplication) {
        this.followers = followers;
        this.context = context;
        this.changedFollowing = new HashMap<>();
        this.followerEnum = followerEnum;
        this.fireBaseApplication = fireBaseApplication;
    }

    public void setupFollowersRecyclerView(DatabaseQuery databaseQuery, Context context, String posterUserId, String posterName,
                                           String postStatus, Long postTimeStamp, String postTitle, String userProfileId) {
        this.databaseQuery = databaseQuery;
        this.context = context;
        this.posterUserId = posterUserId;
        this.posterName = posterName;
        this.postStatus = postStatus;
        this.postTimeStamp = postTimeStamp;
        this.postTitle = postTitle;
        this.userProfileId = userProfileId;
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
        //Want to make sure it is only teh first time we initialize the array because if it is setting up, then
        //We need to initialze the button, otherwise we don't need to keep recalling the setup button, only the first time.
        setupFollowButton(followerViewHolder.isFollowingButton, currentItem.getUserId(), currentItem.getName());
        if(fireBaseApplication.getUserId().equals(currentItem.getUserId())) {
            followerViewHolder.isFollowingButton.setVisibility(View.GONE);
        }
        if(currentItem.getUserId().equals(fireBaseApplication.getUserId())) {
            return;
        }
        followerViewHolder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupOnClickListener(v, currentItem);
            }
        });

        followerViewHolder.followerProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupOnClickListener(v, currentItem);
            }
        });
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

    public void add(User follower, FollowerRecyclerViewAdapter adapter){
        if(adapter.containsUserId(follower.getUserId()) == null) {
            followers.add(follower);
            notifyItemInserted(followers.size() - 1);
            notifyItemRangeChanged(followers.size() + 1, followers.size());
        }

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
                    setButtonIsNotFollowing(button, context);
                } else {
                    User newUser = new User();
                    newUser.setUserId(posterUserId);
                    newUser.setName(posterName);
                    newUser.setId(AndroidUtils.generateId());
                    changedFollowing.put(posterUserId, true);
                    databaseQuery.addFollowing(posterUserId);
                    setButtonIsFollowing(button, context);
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

    private void setupOnClickListener(View view, User user) {
        Intent intent = new Intent(view.getContext(), UserProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString("userProfileId", user.getUserId());
        bundle.putString("posterUserId", posterUserId);
        bundle.putString("posterName", user.getName());
        bundle.putString("postStatus", postStatus);
        if(postTimeStamp == null) {
            postTimeStamp = 12345678910L;
            bundle.putLong("postTimeStamp", postTimeStamp);
        }
        bundle.putString("postTitle", postTitle);
        intent.putExtras(bundle);
        view.getContext().startActivity(intent, bundle);
    }


}
