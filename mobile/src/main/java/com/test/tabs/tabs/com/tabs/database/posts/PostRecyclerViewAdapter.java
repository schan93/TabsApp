package com.test.tabs.tabs.com.tabs.database.posts;

import android.content.Intent;
import android.database.DataSetObserver;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.activity.Comments;
import com.test.tabs.tabs.com.tabs.activity.LocationService;
import com.test.tabs.tabs.com.tabs.activity.news_feed;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.Post;

/**
 * Created by Chiharu on 10/26/2015.
 */
public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.PostViewHolder> {
    List<Post> posts;
    Context context;
    String tab;
    boolean isPublic;

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

    public String getTab() { return tab; }

    public void setTab(String tab) { this.tab = tab; }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewPost;
        TextView name;
        TextView timestamp;
        TextView statusMsg;
        TextView numComments;
        TextView privacyStatus;
        SimpleDraweeView posterPhoto;

        PostViewHolder(View itemView) {
            super(itemView);
            System.out.println("PostRecyclerViewAdapter: Constructor");
            cardViewPost = (CardView) itemView.findViewById(R.id.cv_news_feed);
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
        final String userId = posts.get(i).getPosterUserId();
        postViewHolder.name.setText(name);
        postViewHolder.timestamp.setText(convertDate(posts.get(i).getTimeStamp()));
        postViewHolder.statusMsg.setText(posts.get(i).getStatus());
        DraweeController controller = news_feed.getImage(posts.get(i).getPosterUserId());
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        postViewHolder.posterPhoto.getHierarchy().setRoundingParams(roundingParams);
        postViewHolder.posterPhoto.setController(controller);
        postViewHolder.numComments.setText(posts.get(i).getNumComments() + " Comments");
        postViewHolder.numComments.setTextSize(14);
        System.out.println("Context: " + context);
        if (posts.get(i).getPrivacy().equals("Private") && !isPublic) {
            //If it is a private post, set the text to be "Private"
            postViewHolder.privacyStatus.setText("Private");
        } else if (posts.get(i).getPrivacy().equals("Public") && !isPublic) {
            postViewHolder.privacyStatus.setText("Public");
            //Set text to be public
        } else {
            //We know that it is in the public tab so we don't show it.
            postViewHolder.privacyStatus.setText("");
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
                bundle.putString("tab", tab);
                bundle.putString("userId", userId);
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });
    }

    public PostRecyclerViewAdapter(List<Post> posts, Context context, boolean isPublic, String tab) {
        this.posts = posts;
        this.context = context;
        this.isPublic = isPublic;
        this.tab = tab;
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

    public void remove(Post item, Context context) {
//        System.out.println("PostRecyclerViewAdapter: Removing Post: " + item.getId());
//        PostsDataSource postsDataSource = new PostsDataSource(context);
//        postsDataSource.open();
//        if(item.getPrivacy().equals("1")) {
//            posts = postsDataSource.getAllPrivatePosts();
//        }
//        else {
//            LocationService.getLocationManager(context);
//            Location location = LocationService.getLastLocation();
//            double latitude = location.getLatitude();
//            double longitude = location.getLongitude();
//            posts = postsDataSource.getAllPublicPosts(latitude, longitude, 24140.2);
//        }
//        for(int i = 0; i < posts.size(); i++) {
//            System.out.println("PostRecyclerViewAdapter: Post Id: " + posts.get(i).getId());
//            if(posts.get(i).getId().equals(item.getId())) {
//                System.out.println("PostRecyclerViewAdapter: Removing post from list");
//                posts.remove(i);
//                notifyItemRemoved(i);
//                notifyItemRangeChanged(i, getItemCount() - i);
//            }
//        }
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

    public String convertDate(String timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        String dateText = "";
        Date date = null;
        try {
            date = dateFormat.parse(timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar postDate = Calendar.getInstance();
        postDate.setTime(date); // your date

        Calendar now = Calendar.getInstance();

        Integer dateOffset = 0;
        System.out.println("Post Date: " + postDate);
        System.out.println("Now: " + now);
        if (now.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == postDate.get(Calendar.MONTH)
                && now.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR)
                && (now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR) > 1)) {

            dateOffset = now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR);
            dateText = "h";
        } else if (now.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == postDate.get(Calendar.MONTH)
                && now.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR)
                && (now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR) == 0)) {
            dateOffset = now.get(Calendar.MINUTE) - postDate.get(Calendar.MINUTE);
            dateText = "m";
        } else if (Math.abs(now.getTime().getTime() - postDate.getTime().getTime()) <= 24 * 60 * 60 * 1000L) {
            dateOffset = (int) getHoursDifference(now, postDate);
            if(dateOffset == 24){
                dateOffset = 1;
                dateText = "d";
            }
            else {
                dateText = "h";
            }
        } else {
            long hours = getHoursDifference(now, postDate);

            dateOffset = (int)hours / 24;
            dateText = "d";
        }
        String newFormat = dateOffset + dateText;
        return newFormat;
    }


    private long getHoursDifference(Calendar now, Calendar postDate) {
        long secs = (now.getTime().getTime() - postDate.getTime().getTime()) / 1000;
        long hours = secs / 3600;
//        secs = secs % 3600;
//        long mins = secs / 60;
//        secs = secs % 60;
        return hours;
    }
}

