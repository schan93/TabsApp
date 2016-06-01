package com.test.tabs.tabs.com.tabs.database.followers;

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
import com.test.tabs.tabs.com.tabs.activity.TabsUtil;
import com.test.tabs.tabs.com.tabs.activity.news_feed;

import java.util.List;

/**
 * Created by schan on 5/14/16.
 */
public class FollowerRecyclerViewAdapter extends RecyclerView.Adapter<FollowerRecyclerViewAdapter.FollowerViewHolder>{

    private List<Follower> followerItems;

    public List<Follower> getFollowers() {
        return followerItems;
    }

    public void setFollowers(List<Follower> followerItems) {
        this.followerItems = followerItems;
    }

    public static Follower containsId(List<Follower> list, String id) {
        for (Follower object : list) {
            if (object.getUserId().equals(id)) {
                return object;
            }
        }
        return null;
    }

    public static class FollowerViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewfollower;
        TextView name;
        CheckBox checkBox;
        SimpleDraweeView followerProfilePhoto;

        FollowerViewHolder(View itemView) {
            super(itemView);
            System.out.println("followerViewHolder: Constructor");
            cardViewfollower = (CardView) itemView.findViewById(R.id.follower_card_view);
            name = (TextView) itemView.findViewById(R.id.follower_name);
            followerProfilePhoto = (SimpleDraweeView) itemView.findViewById(R.id.follower_profile_picture);
            checkBox = (CheckBox) itemView.findViewById(R.id.follower_checkbox);
        }
    }

    //+1 for header
    @Override
    public int getItemCount() {
        return followerItems.size();
    }

    @Override
    public FollowerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.follower_item, viewGroup, false);
        FollowerViewHolder pvh = new FollowerViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(FollowerViewHolder followerViewHolder, int i) {
        String name = followerItems.get(i).getName();
        String userId = followerItems.get(i).getUserId();
        String isfollower = followerItems.get(i).getIsFollowing();
        followerViewHolder.name.setText(name);
        DraweeController controller = TabsUtil.getImage(userId);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        followerViewHolder.followerProfilePhoto.getHierarchy().setRoundingParams(roundingParams);
        followerViewHolder.followerProfilePhoto.setController(controller);
        if(isfollower.equals("true")) {
            followerViewHolder.checkBox.setChecked(true);
        } else {
            followerViewHolder.checkBox.setChecked(false);
        }

        final int itemPosition = i;
        followerViewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean updateSuccessful;
                if (((CheckBox) v).isChecked()) {
                    followerItems.get(itemPosition).setIsFollowing("true");
                } else {
                    followerItems.get(itemPosition).setIsFollowing("false");
                }
            }
        });
    }

    public FollowerRecyclerViewAdapter(List<Follower> followers) {
        this.followerItems = followers;
    }


    public void add(Follower follower){
        for(int i = 0; i < followerItems.size(); i++) {
            if(follower.getId().equals(followerItems.get(i).getId())) {
                return;
            }
        }
        followerItems.add(follower);
        notifyItemInserted(followerItems.size() + 1);
        notifyItemRangeChanged(followerItems.size() + 1, followerItems.size());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
