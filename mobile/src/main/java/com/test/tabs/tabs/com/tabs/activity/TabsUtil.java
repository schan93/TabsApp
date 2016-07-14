package com.test.tabs.tabs.com.tabs.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.followers.Follower;
import com.test.tabs.tabs.com.tabs.database.followers.FollowerRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;

import java.util.List;

/**
 * Created by schan on 5/26/16.
 */
public class TabsUtil {

    static FireBaseApplication application;

    private static View getTabType(View fragmentView, TabEnum tabType) {
        if(tabType == TabEnum.Friends) {
            return fragmentView.findViewById(R.id.rv_private_feed);
        } else if(tabType == TabEnum.Following) {
            return fragmentView.findViewById(R.id.rv_followers_feed);
        } else if(tabType == TabEnum.Public) {
            return fragmentView.findViewById(R.id.rv_public_feed);
        } else if(tabType == TabEnum.MyTab) {
            return fragmentView.findViewById(R.id.rv_my_tabs_feed);
        }
        return null;
    }

    public static void populateNewsFeedList(View fragmentView, PostRecyclerViewAdapter adapter, TabEnum tabType, Context context) {
        RecyclerView rv = (RecyclerView) getTabType(fragmentView, tabType);
        RecyclerView.ItemAnimator animator = rv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        //Assuming we refresh the activity, we have to make sure that new posts are loaded
        adapter.notifyDataSetChanged();
        LinearLayoutManager llm = new LinearLayoutManager(context);
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);
    }

    public static void populateFollowList(Context context, RecyclerView recyclerView, FollowerRecyclerViewAdapter adapter) {
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        //Assuming we refresh the followers list, we have to make sure that new followers are loaded
        adapter.notifyDataSetChanged();
        LinearLayoutManager llm = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(adapter);
    }

    public static DraweeController getImage(String userId){
        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>(){
            @Override
            public void onFinalImageSet(
                    String id,
                    @Nullable ImageInfo imageInfo,
                    @Nullable Animatable anim) {
                if(imageInfo == null)
                    return;
                QualityInfo qualityInfo = imageInfo.getQualityInfo();
                FLog.d("Final image received ! " + "Size %d x %d", "Quality level %d, good enough : %s, full quality: %s",
                        imageInfo.getWidth(),
                        imageInfo.getHeight(),
                        qualityInfo.getQuality(),
                        qualityInfo.isOfGoodEnoughQuality(),
                        qualityInfo.isOfFullQuality());
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo){
                FLog.d(getClass(), "Intermediate image received");
            }

            @Override
            public void onFailure(String id, Throwable throwable){
                FLog.e(getClass(), throwable, "Error loading %s ", id);
            }
        };
        Uri uri = Uri.parse("http://graph.facebook.com/" + userId + "/picture?type=large");
        System.out.println("Uri: " + uri);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(controllerListener)
                .setUri(uri)
                .build();
        System.out.println("Controller: " + controller);
        return controller;
        //draweeView.setImageURI(uri);
    }

    public static void setupProfileView(final View view, final String callingActivityName, String ... intentVariables) {
        final Bundle bundle = new Bundle();
        if(intentVariables.length > 0) {
            bundle.putString("posterUserId", intentVariables[0]);
            bundle.putString("posterName", intentVariables[1]);
            bundle.putString("postStatus", intentVariables[2]);
            bundle.putString("postTimeStamp", intentVariables[3]);
            bundle.putString("postTitle", intentVariables[4]);
        }
        bundle.putString("parentClass", callingActivityName);

        Button followersButton = (Button) view.findViewById(R.id.followers_button);
        Button followingButton = (Button) view.findViewById(R.id.following_button);
        TextView totalNumPosts = (TextView) view.findViewById(R.id.total_num_posts);
        TextView totalNumComments = (TextView) view.findViewById(R.id.total_num_comments);

        SpannableString spanString = new SpannableString(String.valueOf(application.getMyTabsAdapter().getItemCount()));
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);

        totalNumPosts.setText(Html.fromHtml("<b>" + application.getMyTabsAdapter().getItemCount() + "</b>" + "\nPosts"));
        totalNumComments.setText(Html.fromHtml("<b>" + application.getCommentsRecyclerViewAdapter().getItemCount() + "</b>" + "\nComments"));
        followersButton.setText(Html.fromHtml("<b>" + application.getFollowerRecyclerViewAdapter().getItemCount() + "</b>" + "\nFollowers"));
        followingButton.setText(Html.fromHtml("<b>" + application.getFollowingRecyclerViewAdapter().getItemCount() + "</b>" + "\nFollowing"));

        followersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), FollowersList.class);
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
        });

        followingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), FollowingList.class);
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
        });
    }

}
