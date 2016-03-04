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
import com.test.tabs.tabs.com.tabs.activity.Comments;
import com.test.tabs.tabs.com.tabs.activity.CommentsHeader;
import com.test.tabs.tabs.com.tabs.activity.news_feed;

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
            commentsHeaderView.timeStamp.setText(convertDate(commentsHeader.getPosterDate()));
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
            comment.timeStamp.setText(convertDate(currentItem.getTimeStamp()));
        }
    }

    public void add(Comment item, int position){
        comments.add(item);
        notifyItemInserted(position);
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
        return hours;
    }

}
