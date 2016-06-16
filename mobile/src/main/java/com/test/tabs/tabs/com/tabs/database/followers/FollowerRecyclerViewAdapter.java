package com.test.tabs.tabs.com.tabs.database.followers;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.activity.FireBaseApplication;
import com.test.tabs.tabs.com.tabs.activity.TabsUtil;
import com.test.tabs.tabs.com.tabs.database.Database.DatabaseQuery;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;

import java.util.List;

/**
 * Created by schan on 5/14/16.
 */
public class FollowerRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private FollowersListHeader followersListHeader;

    private List<Follower> followers;
    private FireBaseApplication fireBaseApplication;
    private DatabaseQuery databaseQuery;

    public void setFollowers(List<Follower> followers) {
        this.followers = followers;
    }

    public static Follower containsId(List<Follower> list, String id) {
        for (Follower object : list) {
            if (object.getUserId().equals(id)) {
                return object;
            }
        }
        return null;
    }

    public FollowerRecyclerViewAdapter(FollowersListHeader header, List<Follower> followers) {
        this.followersListHeader = header;
        this.followers = followers;
    }

    public FollowerRecyclerViewAdapter(FireBaseApplication application, Activity activity, FollowersListHeader header, List<Follower> followers) {
        this.followersListHeader = header;
        this.followers = followers;
        this.fireBaseApplication = application;
        this.databaseQuery = new DatabaseQuery(activity);
    }

    public List<Follower> getFollowers(){
        return this.followers;
    }

    public FollowersListHeader getFollowersHeader(){
        return this.followersListHeader;
    }

    //+1 for header
    @Override
    public int getItemCount() {
        return followers.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if(i == TYPE_HEADER)
        {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.followers_list_header, viewGroup, false);
            FollowersHeaderView vh = new FollowersHeaderView(v);
            return vh;
        }
        else if(i == TYPE_ITEM) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.follower_item, viewGroup, false);
            FollowerViewHolder pvh = new FollowerViewHolder(v);
            return pvh;
        }
        throw new RuntimeException("there is no type that matches the type " + i + " + make sure your using types correctly");
    }

    private Follower getItem(int position)
    {
        return followers.get(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder followerViewHolder, int i) {
        if(followerViewHolder instanceof FollowersHeaderView){
            FollowersHeaderView followersHeaderView = (FollowersHeaderView) followerViewHolder;
            followersHeaderView.sectionTitle.setText(followersListHeader.getSectionTitle());
        }
        else if(followerViewHolder instanceof FollowerViewHolder){
            final Follower currentItem = getItem(i - 1);
            final FollowerViewHolder follower = (FollowerViewHolder) followerViewHolder;
            follower.name.setText(currentItem.getName());
            DraweeController controller = TabsUtil.getImage(currentItem.getUserId());
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
            roundingParams.setRoundAsCircle(true);
            follower.followerProfilePhoto.getHierarchy().setRoundingParams(roundingParams);
            follower.followerProfilePhoto.setController(controller);
            if(currentItem.getIsFollowing().equals("true")) {
                follower.isFollowingCheckBox.setChecked(true);
            } else {
                follower.isFollowingCheckBox.setChecked(false);
            }

            final int itemPosition = i;
            follower.isFollowingCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        currentItem.setIsFollowing("true");
                    } else {
                        currentItem.setIsFollowing("false");
                    }
                }
            });
        }
    }

    public void add(Follower follower){
        for(int i = 0; i < followers.size(); i++) {
            if(follower.getId().equals(followers.get(i).getId())) {
                return;
            }
        }
        followers.add(follower);
        notifyItemInserted(followers.size() + 1);
        notifyItemRangeChanged(followers.size() + 1, followers.size());
    }

    public void remove(Follower item) {
        int position = followers.indexOf(item);
        followers.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position)
    {
        return position == 0;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class FollowerViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewfollower;
        TextView name;
        CheckBox isFollowingCheckBox;
        SimpleDraweeView followerProfilePhoto;

        FollowerViewHolder(View itemView) {
            super(itemView);
            System.out.println("followerViewHolder: Constructor");
            cardViewfollower = (CardView) itemView.findViewById(R.id.follower_card_view);
            name = (TextView) itemView.findViewById(R.id.follower_name);
            followerProfilePhoto = (SimpleDraweeView) itemView.findViewById(R.id.follower_profile_picture);
            isFollowingCheckBox = (CheckBox) itemView.findViewById(R.id.follower_checkbox);
        }
    }

    public static class FollowersHeaderView extends RecyclerView.ViewHolder {
        TextView sectionTitle;

        FollowersHeaderView(View itemView){
            super(itemView);
            sectionTitle = (TextView)itemView.findViewById(R.id.followers_section_title);
        }
    }

}
