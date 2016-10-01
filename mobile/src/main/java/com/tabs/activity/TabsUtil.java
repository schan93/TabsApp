package com.tabs.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;
import com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.tabs.database.followers.FollowerRecyclerViewAdapter;
import com.tabs.database.posts.PostRecyclerViewAdapter;

import java.util.List;

/**
 * Created by schan on 5/26/16.
 */
public class TabsUtil {

    static FireBaseApplication application;

    public static final String ARGS_INSTANCE = "com.tabs.argInstance";

    public static RecyclerView populateNewsFeedList(View fragmentView, PostRecyclerViewAdapter adapter, Context context, Integer adapterSize) {
        RecyclerView rv = (RecyclerView) fragmentView.findViewById(R.id.rv_posts_feed);
        RecyclerView.ItemAnimator animator = rv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        //Assuming we refresh the activity, we have to make sure that new posts are loaded
        adapter.notifyDataSetChanged();
        LinearLayoutManager llm = new LinearLayoutManager(context);
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);

        if(TabsUtil.checkPostsLength(rv) && adapterSize == 0) {
            fragmentView.findViewById(R.id.no_posts_layout).setVisibility(View.VISIBLE);
            fragmentView.findViewById(R.id.no_posts_text).setVisibility(View.VISIBLE);
            TextView textView = (TextView) fragmentView.findViewById(R.id.no_posts_text);
            fragmentView.findViewById(R.id.rv_posts_feed).setVisibility(View.GONE);
            if(adapter.getAdapterType() == AdapterEnum.Public) {
                textView.setText(R.string.noPostsPublic);
            }
            if(adapter.getAdapterType() == AdapterEnum.Following) {
                textView.setText(R.string.noPostsFollowing);
            }
            if(adapter.getAdapterType() == AdapterEnum.ProfileComments) {
                textView.setText(R.string.noCommentsProfile);
            }
            if(adapter.getAdapterType() == AdapterEnum.ProfilePosts) {
                textView.setText(R.string.noPostsProfile);
            }
            if(adapter.getAdapterType() == AdapterEnum.UserComments) {
                textView.setText(R.string.noCommentsPostedUser);
            }
            if(adapter.getAdapterType() == AdapterEnum.UserPosts) {
                textView.setText(R.string.noPostsPostedUser);
            }
        } else {
            fragmentView.findViewById(R.id.rv_posts_feed).setVisibility(View.VISIBLE);
            fragmentView.findViewById(R.id.no_posts_layout).setVisibility(View.GONE);
            fragmentView.findViewById(R.id.no_posts_text).setVisibility(View.GONE);
        }
        return rv;
    }

    public static void populateFollowList(View fragmentView, Context context, FollowerRecyclerViewAdapter adapter) {
        RecyclerView recyclerView = (RecyclerView) fragmentView.findViewById(R.id.follow_list);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        recyclerView.setNestedScrollingEnabled(false);
        //Assuming we refresh the followers list, we have to make sure that new followers are loaded
        adapter.notifyDataSetChanged();
        LinearLayoutManager llm = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(adapter);
        if(adapter.getItemCount() == 0) {
            fragmentView.findViewById(R.id.no_posts_layout).setVisibility(View.VISIBLE);
            fragmentView.findViewById(R.id.no_posts_text).setVisibility(View.VISIBLE);
            TextView textView = (TextView) fragmentView.findViewById(R.id.no_posts_text);
            fragmentView.findViewById(R.id.follow_list).setVisibility(View.GONE);
            if(adapter.getFollowerEnum() == FollowerEnum.UserFollower) {
                textView.setText(R.string.noFollowersUser);
            }
            if(adapter.getFollowerEnum() == FollowerEnum.UserFollowing) {
                textView.setText(R.string.noFollowingUser);
            }
            if(adapter.getFollowerEnum() == FollowerEnum.ProfileFollower) {
                textView.setText(R.string.noFollowersProfile);
            }
            if(adapter.getFollowerEnum() == FollowerEnum.ProfileFollowing) {
                textView.setText(R.string.noFollowingProfile);
            }
        } else {
            fragmentView.findViewById(R.id.follow_list).setVisibility(View.VISIBLE);
            fragmentView.findViewById(R.id.no_posts_layout).setVisibility(View.GONE);
            fragmentView.findViewById(R.id.no_posts_text).setVisibility(View.GONE);
        }
    }

    public static void populateCommentsList(Activity activity, String posterName, String postTitle, Long postTimeStamp, String posterUserId, String postStatus, View fragmentView, ListView rv, Context context, CommentsRecyclerViewAdapter adapter) {
        rv.setNestedScrollingEnabled(false);
        //Assuming we refresh the followers list, we have to make sure that new followers are loaded
        adapter.notifyDataSetChanged();
        if(TabsUtil.checkCommentsLength(rv)) {
//            fragmentView.findViewById(R.id.no_posts_layout).setVisibility(View.VISIBLE);
//            fragmentView.findViewById(R.id.no_posts_text).setVisibility(View.VISIBLE);
            TextView textView = (TextView) fragmentView.findViewById(R.id.empty_list_item);
            textView.setText(R.string.noComments);
            textView.setVisibility(View.VISIBLE);
//            fragmentView.findViewById(R.id.rv_view_comments).setVisibility(View.VISIBLE);
        } else {
            fragmentView.findViewById(R.id.rv_view_comments).setVisibility(View.VISIBLE);
            rv.setAdapter(adapter);
        }
    }

//    public static DraweeController getMoreCommentersImage(String numberCommenters) {
//        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>(){
//            @Override
//            public void onFinalImageSet(
//                    String id,
//                    @Nullable ImageInfo imageInfo,
//                    @Nullable Animatable anim) {
//                if(imageInfo == null)
//                    return;
//                QualityInfo qualityInfo = imageInfo.getQualityInfo();
//                FLog.d("Final image received ! " + "Size %d x %d", "Quality level %d, good enough : %s, full quality: %s",
//                        imageInfo.getWidth(),
//                        imageInfo.getHeight(),
//                        qualityInfo.getQuality(),
//                        qualityInfo.isOfGoodEnoughQuality(),
//                        qualityInfo.isOfFullQuality());
//            }
//
//            @Override
//            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo){
//                FLog.d(getClass(), "Intermediate image received");
//            }
//
//            @Override
//            public void onFailure(String id, Throwable throwable){
//                FLog.e(getClass(), throwable, "Error loading %s ", id);
//            }
//        };
//        Uri uri = Uri.parse("http://graph.facebook.com/" + userId + "/picture?type=large");
//        System.out.println("Uri: " + uri);
//        DraweeController controller = Fresco.newDraweeControllerBuilder()
//                .setControllerListener(controllerListener)
//                .setUri(uri)
//                .build();
//        System.out.println("Controller: " + controller);
//        return controller;
//        controller.setT
        //draweeView.setImageURI(uri);
//    }


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

    public static void setupProfileView(final View view, final String callingActivityName, FireBaseApplication fireBaseApplication, DatabaseQuery databaseQuery, String ... intentVariables) {
        application = fireBaseApplication;
        final Bundle bundle = new Bundle();
        if(intentVariables.length > 0) {
            bundle.putString("userProfileId", intentVariables[0]);
            bundle.putString("posterName", intentVariables[1]);
            if(intentVariables.length > 2) {
                bundle.putString("postStatus", intentVariables[2]);
                bundle.putString("postTimeStamp", intentVariables[3]);
                bundle.putString("postTitle", intentVariables[4]);
                bundle.putString("posterUserId", intentVariables[5]);
            }
        }
        bundle.putString("parentClass", callingActivityName);

        Button followersButton = (Button) view.findViewById(R.id.followers_button);
        Button followingButton = (Button) view.findViewById(R.id.following_button);

        databaseQuery.getNumUserComments(intentVariables[0], view, callingActivityName);
        databaseQuery.getNumUserPosts(intentVariables[0], view, callingActivityName);
        databaseQuery.getNumUserFollowers(intentVariables[0], view, callingActivityName);
        databaseQuery.getNumUserFollowing(intentVariables[0], view, callingActivityName);

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

    public static boolean checkPostsLength(RecyclerView view) {
        if(view.getAdapter().getItemCount() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkIfAdapterEmpty(PostRecyclerViewAdapter adapter) {
        System.out.println("Adapter Item Count: " + adapter.getItemCount());
        if(adapter.getItemCount() < 1) {
            return false;
        } else {
            return true;
        }
    }

    public static void setupPostsCommentsView(View view, int id) {
        view.findViewById(R.id.no_posts_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.no_posts_text).setVisibility(View.VISIBLE);
        TextView textView = (TextView) view.findViewById(R.id.no_posts_text);
        textView.setText(id);
    }

    public static boolean checkCommentsLength(ListView view) {
        if(view.getAdapter().getCount() == 1) {
            return true;
        } else {
            return false;
        }
    }

}
