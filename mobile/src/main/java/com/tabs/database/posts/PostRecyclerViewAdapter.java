package com.tabs.database.posts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.schan.tabs.R;
import com.tabs.activity.AndroidUtils;
import com.tabs.activity.Comments;
import com.tabs.activity.FireBaseApplication;
import com.tabs.activity.PrivacyEnum;
import com.tabs.activity.AdapterEnum;
import com.tabs.activity.TabsUtil;

/**
 * Created by Chiharu on 10/26/2015.
 */
public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.PostViewHolder> {
    List<Post> posts;
    Context context;
    AdapterEnum tabType;
    String userId;
    FireBaseApplication application;

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public AdapterEnum getAdapterType() { return tabType; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) {this.userId = userId; }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewPost;
        TextView name;
        TextView timestamp;
        TextView statusMsg;
        TextView numComments;
        TextView privacyStatus;
        TextView circle;
        TextView postTitle;
        TextView numCommenters;
        SimpleDraweeView posterPhoto;
        SimpleDraweeView firstCommenter;
        ImageView secondCommenter;
        SimpleDraweeView thirdCommenter;
        ImageView fourthCommenter;
        List<SimpleDraweeView> commenters = new ArrayList<SimpleDraweeView>();

        PostViewHolder(View itemView) {
            super(itemView);
            cardViewPost = (CardView) itemView.findViewById(R.id.cv_news_feed);
            postTitle = (TextView) itemView.findViewById(R.id.txt_post_title);
            name = (TextView) itemView.findViewById(R.id.txt_name);
            timestamp = (TextView) itemView.findViewById(R.id.txt_timestamp);
            statusMsg = (TextView) itemView.findViewById(R.id.txt_statusMsg);
            numComments = (TextView) itemView.findViewById(R.id.num_comments);
//            numCommenters = (TextView) itemView.findViewById(R.id.num_commenters);
            posterPhoto = (SimpleDraweeView) itemView.findViewById(R.id.poster_profile_photo);
            privacyStatus = (TextView) itemView.findViewById(R.id.privacy_status);
            circle = (TextView) itemView.findViewById(R.id.circle);
//            firstCommenter = (SimpleDraweeView) itemView.findViewById(R.id.commenter_photo_one);
//            secondCommenter = (ImageView) itemView.findViewById(R.id.commenter_photo_two);
//            thirdCommenter = (SimpleDraweeView) itemView.findViewById(R.id.commenter_photo_three);
//            fourthCommenter = (ImageView) itemView.findViewById(R.id.commenter_photo_four);
//            commenters.add(firstCommenter);
//            commenters.add(secondCommenter);
//            commenters.add(thirdCommenter);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), Comments.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("postId", posts.get(getAdapterPosition()).getId());
                    bundle.putString("userProfileId", posts.get(getAdapterPosition()).getPosterUserId());
                    bundle.putString("posterUserId", posts.get(getAdapterPosition()).getPosterUserId());
                    bundle.putString("posterName", posts.get(getAdapterPosition()).getName());
                    bundle.putLong("postTimeStamp", posts.get(getAdapterPosition()).getTimeStamp());

                    bundle.putString("postStatus", posts.get(getAdapterPosition()).getStatus());
                    bundle.putString("postTitle", posts.get(getAdapterPosition()).getTitle());
                    if(getUserId() == null) {
                        bundle.putString("userId", application.getUserId());
                    } else {
                        bundle.putString("userId", getUserId());
                    }
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
        PostViewHolder pvh = new PostViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final PostViewHolder postViewHolder, int i) {
        final Post post = posts.get(i);
        if (post != null) {
            final String name = post.getName();
            //post.getNumComments() +
//            postViewHolder.numCommenters.setText(" have commented!");
            postViewHolder.numComments.setText(post.getNumComments() + " Comments");
            postViewHolder.postTitle.setText(post.getTitle());
            postViewHolder.name.setText(name);
            postViewHolder.timestamp.setText(AndroidUtils.convertDate(post.getTimeStamp()));
            postViewHolder.statusMsg.setText(post.getStatus());
            DraweeController controller = TabsUtil.getImage(post.getPosterUserId());
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
//            roundingParams.setBorder(ContextCompat.getColor(context, R.color.white), 7f);
            roundingParams.setRoundAsCircle(true);
            postViewHolder.posterPhoto.getHierarchy().setRoundingParams(roundingParams);
            postViewHolder.posterPhoto.setController(controller);
//            if(post.getCommenters() != null && post.getCommenters().size() > 0) {
//                int k = 0;
//                for(String commenterId: post.getCommenters().keySet()) {
//                    roundingParams = RoundingParams.fromCornersRadius(5f);
//                    roundingParams.setRoundAsCircle(true);
//                    roundingParams.setBorder(ContextCompat.getColor(context, R.color.material_color_grey_200), 7f);
//                    postViewHolder.commenters.get(k).getHierarchy().setRoundingParams(roundingParams);
//                    if(k == 1) {
////                        int restOfCommentersNumber = post.getCommenters().keySet().size() - 3;
//                        int restOfCommentersNumber = post.getCommenters().keySet().size();
//                        Bitmap bmp = Bitmap.createBitmap(25,25,Bitmap.Config.ARGB_8888); //change the values whatever you like
//                        ImageView imageView = postViewHolder.secondCommenter;
//                        imageView.setImageBitmap(drawText("+" + restOfCommentersNumber ,getCircledBmp(context,bmp), 14));
//                    }
//                    if(k < 1) {
//                        controller = TabsUtil.getImage(commenterId);
//                        postViewHolder.commenters.get(k).setController(controller);
//                    }
//                    k++;
//                }
//            }

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

    public Bitmap getCircledBmp(Context mContext,Bitmap bmp){

        Canvas canvas = new Canvas(bmp);
        int color = ContextCompat.getColor(mContext, R.color.material_color_grey_400);

        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        RectF rectFloat = new RectF(rect);

        paint.setAntiAlias(true);
        paint.setColor(color);


        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawOval(rectFloat, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(bmp, rect, rect, paint);

        return bmp;

    }

    public Bitmap drawText(String text, Bitmap bmp, int textSize) {

        //text dimensions

        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        StaticLayout mTextLayout = new StaticLayout(text, textPaint,
                bmp.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        Canvas canvas = new Canvas(bmp);

        //draw text
        canvas.save();
        canvas.translate((canvas.getWidth() / 2), (canvas.getHeight() / 2) -
                ((mTextLayout.getHeight() / 2)));
        mTextLayout.draw(canvas);
        canvas.restore();

        return bmp;
    }

    public PostRecyclerViewAdapter(List<Post> posts, Context context, AdapterEnum tab) {
        this.posts = posts;
        this.context = context;
        this.tabType = tab;
    }


    public void add(Post item, PostRecyclerViewAdapter adapter){
        if(adapter.containsId(item.getId()) == null) {
            posts.add(0, item);
            notifyItemInserted(posts.size() - 1);
            notifyItemRangeChanged(posts.size() - 1, posts.size());
        }
    }

    public void remove(Post item){
        for(int i = 0; i < posts.size(); i++) {
            if(posts.get(i).getPosterUserId().equals(item.getPosterUserId())) {
                posts.remove(i);
            }
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

