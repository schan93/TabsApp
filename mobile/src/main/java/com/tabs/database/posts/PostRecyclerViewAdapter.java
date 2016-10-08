package com.tabs.database.posts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Locale;

import android.view.LayoutInflater;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.schan.tabs.R;
import com.tabs.utils.AndroidUtils;
import com.tabs.activity.CommentsActivity;
import com.tabs.activity.FireBaseApplication;
import com.tabs.enums.PrivacyEnum;
import com.tabs.enums.AdapterEnum;
import com.tabs.utils.TabsUtil;

/**
 * Created by Chiharu on 10/26/2015.
 */
public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.PostViewHolder> {
    private List<Post> posts;
    private AdapterEnum tabType;
    private String adapterOwnerId;
    private FireBaseApplication application;

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public AdapterEnum getAdapterType() { return tabType; }

    public String getAdapterOwnerId() { return adapterOwnerId; }

    public void setAdapterOwnerId(String adapterOwnerId) {this.adapterOwnerId = adapterOwnerId; }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewPost;
        TextView name;
        TextView timestamp;
        TextView statusMsg;
        TextView numComments;
        TextView privacyStatus;
        TextView circle;
        TextView postTitle;
        SimpleDraweeView posterPhoto;

        PostViewHolder(View itemView) {
            super(itemView);
            cardViewPost = (CardView) itemView.findViewById(R.id.cv_news_feed);
            postTitle = (TextView) itemView.findViewById(R.id.txt_post_title);
            name = (TextView) itemView.findViewById(R.id.txt_name);
            timestamp = (TextView) itemView.findViewById(R.id.txt_timestamp);
            statusMsg = (TextView) itemView.findViewById(R.id.txt_statusMsg);
            numComments = (TextView) itemView.findViewById(R.id.num_comments);
            posterPhoto = (SimpleDraweeView) itemView.findViewById(R.id.poster_profile_photo);
            privacyStatus = (TextView) itemView.findViewById(R.id.privacy_status);
            circle = (TextView) itemView.findViewById(R.id.circle);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), CommentsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("postId", posts.get(getAdapterPosition()).getId());
                    bundle.putString("userProfileId", posts.get(getAdapterPosition()).getPosterUserId());
                    bundle.putString("posterUserId", posts.get(getAdapterPosition()).getPosterUserId());
                    bundle.putString("posterName", posts.get(getAdapterPosition()).getName());
                    bundle.putLong("postTimeStamp", posts.get(getAdapterPosition()).getTimeStamp());

                    bundle.putString("postStatus", posts.get(getAdapterPosition()).getStatus());
                    bundle.putString("postTitle", posts.get(getAdapterPosition()).getTitle());
                    bundle.putString("userId", application.getUserId());
                    bundle.putString("name", application.getName());
                    intent.putExtras(bundle);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_feed_item, viewGroup, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final PostViewHolder postViewHolder, int i) {
        final Post post = posts.get(i);
        if (post != null) {
            final String name = post.getName();
            postViewHolder.numComments.setText(String.format(Locale.ENGLISH, "%d Comments", post.getNumComments()));
            postViewHolder.postTitle.setText(post.getTitle());
            postViewHolder.name.setText(name);
            postViewHolder.timestamp.setText(AndroidUtils.convertDate(post.getTimeStamp()));
            postViewHolder.statusMsg.setText(post.getStatus());
            DraweeController controller = TabsUtil.getImage(post.getPosterUserId());
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
            roundingParams.setRoundAsCircle(true);
            postViewHolder.posterPhoto.getHierarchy().setRoundingParams(roundingParams);
            postViewHolder.posterPhoto.setController(controller);

            if (getAdapterType() == AdapterEnum.Public || getAdapterType() == AdapterEnum.Following) {
                postViewHolder.privacyStatus.setVisibility(View.GONE);
                postViewHolder.circle.setVisibility(View.GONE);
            } else {
                if (PrivacyEnum.valueOf(post.getPrivacy()) == PrivacyEnum.Public) {
                    postViewHolder.privacyStatus.setText(PrivacyEnum.Public.toString());
                } else if (PrivacyEnum.valueOf(post.getPrivacy()) == PrivacyEnum.Following) {
                    postViewHolder.privacyStatus.setText(PrivacyEnum.Following.toString());
                }
                postViewHolder.circle.setVisibility(View.VISIBLE);
            }


        }
    }

    public PostRecyclerViewAdapter(List<Post> posts, AdapterEnum tab, FireBaseApplication application) {
        this.posts = posts;
        this.tabType = tab;
        this.application = application;
    }


    public void add(Post item, PostRecyclerViewAdapter adapter){
        if(item != null && adapter.containsId(item.getId()) == null) {
            posts.add(0, item);
            notifyItemInserted(0);
            notifyItemRangeChanged(posts.size() - 1, posts.size());
        }
    }

    public Post containsId(String id) {
        for (Post object : posts) {
            if (object != null && object.getId().equals(id)) {
                return object;
            }
        }
        return null;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}

