package com.test.tabs.tabs.com.tabs.database.posts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.activity.AndroidUtils;
import com.test.tabs.tabs.com.tabs.activity.Comments;
import com.test.tabs.tabs.com.tabs.activity.PrivacyEnum;
import com.test.tabs.tabs.com.tabs.activity.TabEnum;
import com.test.tabs.tabs.com.tabs.activity.TabsUtil;
import com.test.tabs.tabs.com.tabs.activity.news_feed;

/**
 * Created by Chiharu on 10/26/2015.
 */
public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.PostViewHolder> {
    List<Post> posts;
    Context context;
    TabEnum tabType;
    boolean isPublic;
    String userId;

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public TabEnum getTabType() { return tabType; }

    public void setTabType(TabEnum tabType) { this.tabType = tabType; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) {this.userId = userId; }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewPost;
        TextView name;
        TextView timestamp;
        TextView statusMsg;
        TextView numComments;
        TextView privacyStatus;
        TextView postTitle;
        SimpleDraweeView posterPhoto;

        PostViewHolder(View itemView) {
            super(itemView);
            System.out.println("PostRecyclerViewAdapter: Constructor");
            cardViewPost = (CardView) itemView.findViewById(R.id.cv_news_feed);
            postTitle = (TextView) itemView.findViewById(R.id.txt_post_title);
            name = (TextView) itemView.findViewById(R.id.txt_name);
            timestamp = (TextView) itemView.findViewById(R.id.txt_timestamp);
            statusMsg = (TextView) itemView.findViewById(R.id.txt_statusMsg);
            numComments = (TextView) itemView.findViewById(R.id.num_comments);
            posterPhoto = (SimpleDraweeView) itemView.findViewById(R.id.poster_profile_photo);
            privacyStatus = (TextView) itemView.findViewById(R.id.privacy_status);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_feed_item, viewGroup, false);
        PostViewHolder pvh = new PostViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PostViewHolder postViewHolder, int i) {
        final String name = posts.get(i).getName();
        final String userId = getUserId();
        postViewHolder.postTitle.setText(posts.get(i).getTitle());
        postViewHolder.name.setText(name);
        postViewHolder.timestamp.setText(AndroidUtils.convertDate(posts.get(i).getTimeStamp()));
        postViewHolder.statusMsg.setText(posts.get(i).getStatus());
        DraweeController controller = TabsUtil.getImage(posts.get(i).getPosterUserId());
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        postViewHolder.posterPhoto.getHierarchy().setRoundingParams(roundingParams);
        postViewHolder.posterPhoto.setController(controller);
        postViewHolder.numComments.setText(posts.get(i).getNumComments() + " Comments");
        postViewHolder.numComments.setTextSize(14);
        if(getTabType() == TabEnum.Public || getTabType() == TabEnum.Friends) {
            postViewHolder.privacyStatus.setVisibility(View.GONE);
        } else {
            if (posts.get(i).getPrivacy() == PrivacyEnum.Friends && !isPublic) {
                //If it is a friends post, set the text to be "Friends"
                postViewHolder.privacyStatus.setText(PrivacyEnum.Friends.toString());
            } else if (posts.get(i).getPrivacy() == PrivacyEnum.Public && !isPublic) {
                postViewHolder.privacyStatus.setText(PrivacyEnum.Public.toString());
                //Set text to be public
            } else {
                //We know that it is in the public tab so we don't show it.
                postViewHolder.privacyStatus.setText("");
            }
        }
        final Post post = posts.get(i);

        postViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Comments.class);
                Bundle bundle = new Bundle();
                bundle.putString("postId", post.getId());
                bundle.putString("posterUserId", post.getPosterUserId());
                bundle.putString("posterName", post.getName());
                bundle.putString("postTimeStamp", post.getTimeStamp());;
                bundle.putString("postStatus", post.getStatus());
                bundle.putString("tab", tabType.toString());
                bundle.putString("postTitle", post.getTitle());
                bundle.putString("userId", userId);
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });
    }

    public PostRecyclerViewAdapter(List<Post> posts, Context context, boolean isPublic, TabEnum tab) {
        this.posts = posts;
        this.context = context;
        this.isPublic = isPublic;
        this.tabType = tab;
    }


    public void add(Post item){
        for(int i = 0; i < posts.size(); i++) {
            if(item.getId().equals(posts.get(i).getId())) {
                return;
            }
        }
        posts.add(item);
        notifyItemInserted(posts.size() - 1);
        notifyItemRangeChanged(posts.size() - 1, posts.size());
    }

    public static Post containsId(List<Post> list, String id) {
        System.out.println("PostRecyclerViewAdapter: id: " + id);
        for (Post object : list) {
            if (object.getId().equals(id)) {
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

