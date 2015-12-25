package com.test.tabs.tabs.com.tabs.database.posts;

import android.content.Intent;
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
import com.test.tabs.tabs.com.tabs.activity.news_feed;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.Post;

/**
 * Created by Chiharu on 10/26/2015.
 */
public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.PostViewHolder>{
    List<Post> posts;
    Context context;

    public static class PostViewHolder extends RecyclerView.ViewHolder{
        CardView cardViewPost;
        TextView name;
        TextView timestamp;
        TextView statusMsg;
        TextView numComments;
        SimpleDraweeView posterPhoto;

        PostViewHolder(View itemView) {
            super(itemView);
            cardViewPost = (CardView) itemView.findViewById(R.id.cv_news_feed);
            name = (TextView) itemView.findViewById(R.id.txt_name);
            timestamp = (TextView) itemView.findViewById(R.id.txt_timestamp);
            statusMsg = (TextView) itemView.findViewById(R.id.txt_statusMsg);
            numComments = (TextView) itemView.findViewById(R.id.num_comments);
            posterPhoto = (SimpleDraweeView) itemView.findViewById(R.id.poster_profile_photo);
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
            PostsDataSource datasource;
            datasource = new PostsDataSource(context);
            datasource.open();
            postViewHolder.name.setText(posts.get(i).getName());
            postViewHolder.timestamp.setText(convertDate(posts.get(i).getTimeStamp()));
            postViewHolder.statusMsg.setText(posts.get(i).getStatus());
            DraweeController controller = news_feed.getImage(posts.get(i).getPosterUserId());
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(10f);
            roundingParams.setCornersRadius(10);
            postViewHolder.posterPhoto.getHierarchy().setRoundingParams(roundingParams);
            postViewHolder.posterPhoto.setController(controller);
            postViewHolder.numComments.setText(datasource.getNumberComments(posts.get(i).getId()).toString() + " Comments");

            final Post post = posts.get(i);

            postViewHolder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Intent intent = new Intent(v.getContext(), Comments.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("id", (post.getId()));
                    intent.putExtras(bundle);
                    v.getContext().startActivity(intent);
                }
            });
        }

        public PostRecyclerViewAdapter(List<Post> posts, Context context){
            this.posts = posts;
            this.context = context;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public String convertDate(String timestamp){
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            String dateText = "";
            Date date = null;
            try {
                date = dateFormat.parse(timestamp);
            } catch(Exception e){
                e.printStackTrace();
            }

            Calendar postDate = Calendar.getInstance();
            postDate.setTime(date); // your date

            Calendar now = Calendar.getInstance();

            Integer dateOffset = 0;

            if (now.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)
                    && now.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR)
                    && now.get(Calendar.DAY_OF_MONTH) == postDate.get(Calendar.DAY_OF_MONTH)
                    && (now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR) > 1)){

                dateOffset = now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR);
                dateText = "h";
            }
            else if(now.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)
                    && now.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR)
                    && now.get(Calendar.DAY_OF_MONTH) == postDate.get(Calendar.DAY_OF_MONTH)
                    && (now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR) == 0)){
                dateOffset = now.get(Calendar.MINUTE) - postDate.get(Calendar.MINUTE);
                dateText = "m";
            }
            else{
                dateOffset = now.get(Calendar.DAY_OF_YEAR) - postDate.get(Calendar.DAY_OF_YEAR);
                dateText = "d";
            }
            String newFormat = dateOffset + dateText;
            return newFormat;
        }
    }

//        private Activity activity;
//    private LayoutInflater inflater;
//    private List<Post> posts;
//
//    //Local Database for storing friends
//    private PostsDataSource datasource;
//
//
//    public PostListAdapter(Activity activity, List<Post> posts) {
//        this.activity = activity;
//        this.posts = posts;
//    }
//
//    @Override
//    public int getCount() {
//        return posts.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return posts.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        if (inflater == null)
//            inflater = (LayoutInflater) activity
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        if (convertView == null)
//            convertView = inflater.inflate(R.layout.news_feed_item, null);
//
////        TextView name = (TextView) convertView.findViewById(R.id.friend_name);
////        ProfilePictureView profilePictureView = (ProfilePictureView) convertView.findViewById(R.id.friend_profile_picture);
////        if(profilePictureView != null) {
////            profilePictureView.setProfileId(friendItems.get(position).getId());
////        }
//
//        TextView name = (TextView) convertView.findViewById(R.id.txt_name);
//        TextView timestamp = (TextView)convertView.findViewById(R.id.txt_timestamp);
//        TextView statusMsg = (TextView)convertView.findViewById(R.id.txt_statusMsg);
//        TextView numComments = (TextView)convertView.findViewById(R.id.num_comments);
//
//        //Set profile picture
//        DraweeController controller = news_feed.getImage(posts.get(position).getPosterUserId());
//        SimpleDraweeView draweeView = (SimpleDraweeView) convertView.findViewById(R.id.poster_profile_photo);
//        draweeView.setController(controller);
//
//
//        //Set the views
//        Post item = posts.get(position);
//        name.setText(item.getName());
//        timestamp.setText(convertDate(item.getTimeStamp()));
//        statusMsg.setText((item.getStatus()));
//
//        datasource = new PostsDataSource(parent.getContext());
//        datasource.open();
//        System.out.println("Item id: " + item.getId());
//        numComments.setText(datasource.getNumberComments(item.getId()).toString() + " Comments");
//        numComments.setTextSize(12);
//
//        return convertView;
//    }

