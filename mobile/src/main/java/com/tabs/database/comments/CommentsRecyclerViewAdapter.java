package com.tabs.database.comments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.tabs.R;
import com.tabs.activity.AndroidUtils;
import com.tabs.activity.CommentsHeader;
import com.tabs.activity.FireBaseApplication;
import com.tabs.activity.TabsUtil;
import com.tabs.activity.UserProfile;
import com.tabs.database.Database.DatabaseQuery;

import java.util.List;

/**
 * Created by schan on 11/28/15.
 */
public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private CommentsHeader commentsHeader;
    private List<Comment> comments;
    private FireBaseApplication fireBaseApplication;
    private DatabaseQuery databaseQuery;
    private Context context;

    public CommentsRecyclerViewAdapter(CommentsHeader header, List<Comment> comments) {
        this.commentsHeader = header;
        this.comments = comments;
    }

    public CommentsRecyclerViewAdapter(FireBaseApplication application, Activity activity, CommentsHeader header, List<Comment> comments) {
        this.commentsHeader = header;
        this.comments = comments;
        this.fireBaseApplication = application;
        this.databaseQuery = new DatabaseQuery(activity);
    }

    public List<Comment> getCommentsList(){
        return this.comments;
    }

    public CommentsHeader getCommentsHeader(){
        return this.commentsHeader;
    }

    public static Comment containsId(List<Comment> list, String id) {
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
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comments_header, viewGroup, false);
            CommentsHeaderView vh = new CommentsHeaderView(v);
            context = v.getContext();
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
            final CommentsHeaderView commentsHeaderView = (CommentsHeaderView) commentViewHolder;
            commentsHeaderView.postTitle.setText(commentsHeader.getPostTitle());
            commentsHeaderView.name.setText(commentsHeader.getPosterName());
            commentsHeaderView.status.setText(commentsHeader.getViewStatus());
            commentsHeaderView.timeStamp.setText(AndroidUtils.convertDate(commentsHeader.getPosterDate()));
            //FireBaseApplication.getFriendsRecyclerViewAdapter().containsId(FireBaseApplication.getFriendsRecyclerViewAdapter().getFriends(), commentsHeader.getPosterUserId()) == null
//            if(!commentsHeader.getPosterUserId().equals(FireBaseApplication.getUserId())) {
//                setupFollowButton(commentsHeaderView.followButton, commentsHeader);
//            }
            DraweeController controller = TabsUtil.getImage(commentsHeader.getPosterUserId());
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
            roundingParams.setRoundAsCircle(true);
            commentsHeaderView.photo.getHierarchy().setRoundingParams(roundingParams);
            commentsHeaderView.photo.setController(controller);
            if(!commentsHeader.getPosterUserId().equals(fireBaseApplication.getUserId())) {
                commentsHeaderView.photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, UserProfile.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("posterUserId", commentsHeader.getPosterUserId());
                        bundle.putString("posterName", commentsHeader.getPosterName());
                        bundle.putString("postStatus", commentsHeader.getViewStatus());
                        bundle.putString("postTimeStamp", commentsHeader.getPosterDate());
                        bundle.putString("postTitle", commentsHeader.getPostTitle());
                        intent.putExtras(bundle);
                        if (intent != null) {
                            context.startActivity(intent, bundle);
                        }
                    }
                });
            }
        }
        else if(commentViewHolder instanceof CommentsViewHolder){
            Comment currentItem = getItem(i - 1);
            if(currentItem != null) {
                CommentsViewHolder comment = (CommentsViewHolder) commentViewHolder;
                comment.name.setText(currentItem.getCommenter());
                comment.message.setText(currentItem.getComment());
                DraweeController controller = TabsUtil.getImage(currentItem.getCommenterUserId());
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
                roundingParams.setRoundAsCircle(true);
                comment.photo.getHierarchy().setRoundingParams(roundingParams);
                comment.photo.setController(controller);
                comment.timeStamp.setText(AndroidUtils.convertDate(currentItem.getTimeStamp()));
            }
        }
    }

    private void setButtonColor(Button button, Boolean isFollowing) {
        if(!isFollowing) {
            button.setBackgroundColor(Color.parseColor("#d94130"));
            button.setText("+ Follow");
        } else {
            button.setBackgroundColor(Color.parseColor("#4CAF50"));
            button.setText("Following");
        }
    }

    public void add(Comment item){
        comments.add(item);
        notifyItemInserted(comments.size() - 1);
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
        TextView postTitle;
        SimpleDraweeView photo;
        Button followButton;

        CommentsHeaderView(View itemView){
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.poster_name);
            timeStamp = (TextView)itemView.findViewById(R.id.post_date);
            postTitle = (TextView) itemView.findViewById(R.id.comment_post_title);
            status = (TextView)itemView.findViewById(R.id.view_status);
            photo = (SimpleDraweeView) itemView.findViewById(R.id.poster_picture);
        }
    }
}
