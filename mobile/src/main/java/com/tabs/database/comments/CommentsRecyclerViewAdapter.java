package com.tabs.database.comments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.schan.tabs.R;
import com.tabs.activity.UserProfileActivity;
import com.tabs.utils.AndroidUtils;
import com.tabs.activity.FireBaseApplication;
import com.tabs.utils.TabsUtil;

import java.util.List;

/**
 * Created by schan on 11/28/15.
 */
public class CommentsRecyclerViewAdapter extends ArrayAdapter<Comment> {

    private final Context context;
    private List<Comment> comments;
    private String postStatus;
    private Long postTimeStamp;
    private String posterUserId;
    private String postTitle;
    private FireBaseApplication application;

    public CommentsRecyclerViewAdapter(Context context, List<Comment> comments) {
        super(context, R.layout.comment_item, comments);
        this.context = context;
        this.comments = comments;
    }

    public CommentsRecyclerViewAdapter(Context context, List<Comment> comments, String postStatus, Long postTimeStamp, String posterUserId, String postTitle) {
        super(context, R.layout.comment_item, comments);
        this.context = context;
        this.comments = comments;
        this.postStatus = postStatus;
        this.postTimeStamp = postTimeStamp;
        this.posterUserId = posterUserId;
        this.postTitle = postTitle;
    }

    public List<Comment> getCommentsList(){
        return this.comments;
    }

    public Comment containsId(String id) {
        for (Comment object : comments) {
            if (object.getId().equals(id)) {
                return object;
            }
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        application = ((FireBaseApplication) context.getApplicationContext());
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.comment_item, parent, false);
        TextView name = (TextView)itemView.findViewById(R.id.commenter_name);
        TextView message = (TextView)itemView.findViewById(R.id.comment_message);
        TextView timeStamp = (TextView)itemView.findViewById(R.id.comment_timestamp);
        SimpleDraweeView photo = (SimpleDraweeView) itemView.findViewById(R.id.commenter_profile_photo);
        name.setText(comments.get(position).getCommenter());
        message.setText(comments.get(position).getComment());
        timeStamp.setText(AndroidUtils.convertDate(comments.get(position).getTimeStamp()));
        DraweeController controller = TabsUtil.getImage(comments.get(position).getCommenterUserId());
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        photo.getHierarchy().setRoundingParams(roundingParams);
        photo.setController(controller);
        final Comment comment = comments.get(position);
        if(comment.getCommenterUserId().equals(application.getUserId())) {
            return itemView;
        }
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupOnClickListener(v, comment);
            }
        });

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupOnClickListener(v, comment);
            }
        });
        return itemView;
    }

    private void setupOnClickListener(View view, Comment comment) {
        Intent intent = new Intent(view.getContext(), UserProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString("postId", comment.getPostId());
        bundle.putString("userProfileId", comment.getCommenterUserId());
        bundle.putString("posterUserId", posterUserId);
        bundle.putString("posterName", comment.getCommenter());
        bundle.putString("postStatus", postStatus);
        bundle.putLong("postTimeStamp", postTimeStamp);
        bundle.putString("postTitle", postTitle);
        intent.putExtras(bundle);
        view.getContext().startActivity(intent, bundle);
    }

    public void add(Comment item, CommentsRecyclerViewAdapter adapter){
        if(adapter.containsId(item.getId()) == null) {
            comments.add(item);
            adapter.notifyDataSetChanged();
        }
    }

    public void remove(Comment item) {
        for(int i = 0; i < comments.size(); i++) {
            if(comments.get(i).getCommenterUserId().equals(item.getCommenterUserId())) {
                comments.remove(i);
            }
        }
    }
}
