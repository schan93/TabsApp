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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.schan.tabs.R;
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
public class CommentsRecyclerViewAdapter extends ArrayAdapter<Comment> {

    private final Context context;
    private List<Comment> comments;

    public CommentsRecyclerViewAdapter(Context context, List<Comment> comments) {
        super(context, R.layout.comment_item, comments);
        this.context = context;
        this.comments = comments;
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
    public View getView(int position, View convertView, ViewGroup parent) {
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
        return itemView;
    }

    public void add(Comment item, CommentsRecyclerViewAdapter adapter){
        if(adapter.containsId(item.getId()) == null) {
            comments.add(item);
            adapter.notifyDataSetChanged();
//            adapter.not
//            adapter.notifyItemInserted(comments.size() - 1);
//            notifyItemRangeChanged(comments.size() - 1, comments.size());
        }
        //Also need to update the post that you are updating
    }

    public void remove(Comment item) {
        for(int i = 0; i < comments.size(); i++) {
            if(comments.get(i).getCommenterUserId().equals(item.getCommenterUserId())) {
                comments.remove(i);
            }
        }
    }
}
