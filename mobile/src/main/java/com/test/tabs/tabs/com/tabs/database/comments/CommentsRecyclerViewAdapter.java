package com.test.tabs.tabs.com.tabs.database.comments;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.activity.AndroidUtils;
import com.test.tabs.tabs.com.tabs.activity.Comments;
import com.test.tabs.tabs.com.tabs.activity.CommentsHeader;
import com.test.tabs.tabs.com.tabs.activity.news_feed;
import com.test.tabs.tabs.com.tabs.database.posts.Post;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by schan on 11/28/15.
 */
public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private CommentsHeader commentsHeader;
    private List<Comment> comments;
    private static View view;

    public CommentsRecyclerViewAdapter(CommentsHeader header, List<Comment> comments) {
        this.commentsHeader = header;
        this.comments = comments;
    }

    public List<Comment> getCommentsList(){
        return this.comments;
    }

    public CommentsHeader getCommentsHeader(){
        return this.commentsHeader;
    }

    public static Comment containsId(List<Comment> list, String id) {
        System.out.println("CommentsAdapter: id: " + id);
        for (Comment object : list) {
            if (object.getId().equals(id)) {
                return object;
            }
        }
        return null;
    }

    //+1 for header
    @Override
    public int getItemCount() {
        return comments.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if(i == TYPE_HEADER)
        {
            System.out.println("Inflating header");
            System.out.println("View Group Context: " + viewGroup);
            System.out.println("Context: " + viewGroup.getContext());
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comments_header, viewGroup, false);
            CommentsHeaderView vh = new CommentsHeaderView(v);
            return vh;
        }
        else if(i == TYPE_ITEM) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_item, viewGroup, false);
            CommentsViewHolder pvh = new CommentsViewHolder(v);
            return pvh;
        }
        throw new RuntimeException("there is no type that matches the type " + i + " + make sure your using types correctly");
    }

    private Comment getItem(int position)
    {
        return comments.get(position);
    }

    @Override
     public void onBindViewHolder(RecyclerView.ViewHolder commentViewHolder, int i) {
        if(commentViewHolder instanceof CommentsHeaderView){
            CommentsHeaderView commentsHeaderView = (CommentsHeaderView) commentViewHolder;
            commentsHeaderView.name.setText(commentsHeader.getPosterName());
            commentsHeaderView.status.setText(commentsHeader.getViewStatus());
            commentsHeaderView.timeStamp.setText(AndroidUtils.convertDate(commentsHeader.getPosterDate()));
            DraweeController controller = news_feed.getImage(commentsHeader.getPosterUserId());
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
            roundingParams.setRoundAsCircle(true);
            commentsHeaderView.photo.getHierarchy().setRoundingParams(roundingParams);
            commentsHeaderView.photo.setController(controller);
        }
        else if(commentViewHolder instanceof CommentsViewHolder){
            Comment currentItem = getItem(i - 1);
            CommentsViewHolder comment = (CommentsViewHolder) commentViewHolder;
            comment.name.setText(currentItem.getCommenter());
            comment.message.setText(currentItem.getComment());
            DraweeController controller = news_feed.getImage(currentItem.getCommenterUserId());
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
            roundingParams.setRoundAsCircle(true);
            comment.photo.getHierarchy().setRoundingParams(roundingParams);
            comment.photo.setController(controller);
            comment.timeStamp.setText(AndroidUtils.convertDate(currentItem.getTimeStamp()));
        }
    }

    public void add(Comment item, int position){
        comments.add(item);
        //Also need to update the post that you are updating
    }

    public void remove(Comment item) {
        int position = comments.indexOf(item);
        comments.remove(position);
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

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView name;
        TextView message;
        TextView timeStamp;
        SimpleDraweeView photo;

        CommentsViewHolder(View itemView){
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.comments_card_view);
            name = (TextView)itemView.findViewById(R.id.commenter_name);
            message = (TextView)itemView.findViewById(R.id.comment_message);
            timeStamp = (TextView)itemView.findViewById(R.id.comment_timestamp);
            photo = (SimpleDraweeView) itemView.findViewById(R.id.commenter_profile_photo);
        }
    }

    public static class CommentsHeaderView extends RecyclerView.ViewHolder {
        TextView name;
        TextView status;
        TextView timeStamp;
        SimpleDraweeView photo;

        CommentsHeaderView(View itemView){
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.poster_name);
            timeStamp = (TextView)itemView.findViewById(R.id.post_date);
            status = (TextView)itemView.findViewById(R.id.view_status);
            photo = (SimpleDraweeView) itemView.findViewById(R.id.poster_picture);
        }
    }
}
