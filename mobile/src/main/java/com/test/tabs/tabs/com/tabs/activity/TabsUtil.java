package com.test.tabs.tabs.com.tabs.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;

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
import com.test.tabs.tabs.com.tabs.database.companion.CompanionNamesAdapter;
import com.test.tabs.tabs.com.tabs.database.followers.Follower;
import com.test.tabs.tabs.com.tabs.database.followers.FollowerRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.tonicartos.superslim.LayoutManager;

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

    private static View getCompanionType(CompanionEnum companionType, Activity activity) {
        if(companionType  == CompanionEnum.Friend) {
            return activity.findViewById(R.id.friends_list);
        } else if(companionType == CompanionEnum.Follower) {
            return activity.findViewById(R.id.followers_list);
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

    public static void populateCompanionList(CompanionEnum companionType, Activity activity) {
        RecyclerView rv = (RecyclerView) getCompanionType(companionType, activity);
        RecyclerView.ItemAnimator animator = rv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        LinearLayoutManager llm = new LinearLayoutManager(activity.getApplicationContext());
        rv.setLayoutManager(llm);
        application = (FireBaseApplication) activity.getApplication();
        List<Follower> followers = application.getFollowerRecyclerViewAdapter().getFollowers();
        application.setFollowerRecyclerViewAdapter(new FollowerRecyclerViewAdapter(application, activity, new FollowersListHeader("Followers"), followers));
        rv.setAdapter(application.getFollowerRecyclerViewAdapter());
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

}
