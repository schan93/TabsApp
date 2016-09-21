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
            if(adapter.getTabType() == TabEnum.Public) {
                textView.setText(R.string.noPostsPublic);
            }
            fragmentView.findViewById(R.id.rv_posts_feed).setVisibility(View.GONE);
        } else {
            fragmentView.findViewById(R.id.rv_posts_feed).setVisibility(View.VISIBLE);
            fragmentView.findViewById(R.id.no_posts_layout).setVisibility(View.GONE);
            fragmentView.findViewById(R.id.no_posts_text).setVisibility(View.GONE);
        }
        return rv;
    }

    public static void populateFollowList(Context context, RecyclerView recyclerView, FollowerRecyclerViewAdapter adapter) {
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
    }

    public static void populateCommentsList(Activity activity, String posterName, String postTitle, String postTimeStamp, String posterUserId, String postStatus, View fragmentView, ListView rv, Context context, CommentsRecyclerViewAdapter adapter) {
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

    public static void setupProfileView(final View view, final String callingActivityName, FireBaseApplication fireBaseApplication,  String ... intentVariables) {
        application = fireBaseApplication;
        final Bundle bundle = new Bundle();
        if(intentVariables.length > 0) {
            bundle.putString("posterUserId", intentVariables[0]);
            bundle.putString("posterName", intentVariables[1]);
            if(intentVariables.length > 2) {
                bundle.putString("postStatus", intentVariables[2]);
                bundle.putString("postTimeStamp", intentVariables[3]);
                bundle.putString("postTitle", intentVariables[4]);
            }
        }
        bundle.putString("parentClass", callingActivityName);

        Button followersButton = (Button) view.findViewById(R.id.followers_button);
        Button followingButton = (Button) view.findViewById(R.id.following_button);
        TextView totalNumPosts = (TextView) view.findViewById(R.id.total_num_posts);
        TextView totalNumComments = (TextView) view.findViewById(R.id.total_num_comments);
        
        if(callingActivityName.equals("Profile")) {
            totalNumPosts.setText(Html.fromHtml("<b>" + application.getMyTabsAdapter().getItemCount() + "</b>" + "\nPosts"));
            //TODO: This wont work. this will only show the number of posts that a user has commented on but not necessarily the # comments
            totalNumComments.setText(Html.fromHtml("<b>" + application.getCommentsCount() + "</b>" + "\nComments"));
            followersButton.setText(Html.fromHtml("<b>" + application.getFollowerNum() + "</b>" + "\nFollowers"));
            followingButton.setText(Html.fromHtml("<b>" + application.getFollowingNum() + "</b>" + "\nFollowing"));
        } else {
            //TODO: when we restart application for example on the user page we have to get all the posts first
            totalNumPosts.setText(Html.fromHtml("<b>" + application.getUserPostNum() + "</b>" + "\nPosts"));
            totalNumComments.setText(Html.fromHtml("<b>" + application.getUserCommentNum() + "</b>" + "\nComments"));
            followersButton.setText(Html.fromHtml("<b>" + application.getUserFollowerNum() + "</b>" + "\nFollowers"));
            followingButton.setText(Html.fromHtml("<b>" + application.getUserFollowingNum() + "</b>" + "\nFollowing"));
            // correctly the posts and such of another user.
        }

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
