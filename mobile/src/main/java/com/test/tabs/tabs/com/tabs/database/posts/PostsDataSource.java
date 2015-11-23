package com.test.tabs.tabs.com.tabs.database.posts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schan on 11/16/15.
 */
public class PostsDataSource {


    // Database fields
    private SQLiteDatabase database;
    private PostsDB dbHelper;
    private String[] allColumns = { PostsDB.COLUMN_ID,
            PostsDB.COLUMN_POSTER_NAME, PostsDB.COLUMN_POSTER_USER_ID, PostsDB.COLUMN_STATUS, PostsDB.COLUMN_TIME_STAMP };

    public PostsDataSource(Context context) {
        dbHelper = PostsDB.getInstance(context);
//        database = dbHelper.getWritableDatabase();

    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createPost(String posterUserId, String status, String posterName, String timeStamp) {
        //Create a ContentValues object so we can put our column name key/value pairs into it.
        ContentValues values = new ContentValues();
        //Insert 0 as column id because it will autoincrement for us (?)
        values.put(PostsDB.COLUMN_POSTER_NAME, posterName);
        values.put(PostsDB.COLUMN_POSTER_USER_ID, posterUserId);
        values.put(PostsDB.COLUMN_STATUS, status);
        values.put(PostsDB.COLUMN_TIME_STAMP, timeStamp);
        //Insert into the database
        System.out.println("Values in create: " + values);
        database.insert(PostsDB.TABLE_POSTS, null,
                values);
        return;
    }

    public Post getPost(String id) {
        //This might only get one post. We have the get all posts below however. This method may not be useful now but just leaving here
        //Incase we may need it later.
        //Get the values from the database, querying by poster's user id
        Cursor cursor = database.query(PostsDB.TABLE_POSTS,
                allColumns, PostsDB.COLUMN_POSTER_USER_ID + " = ?", new String[]{id},
                null, null, null);
        cursor.moveToFirst();
        Post newPost = cursorToPost(cursor);
        cursor.close();
        return newPost;
    }

    private Post cursorToPost(Cursor cursor) {
        Post post = new Post();
        post.setId(cursor.getLong(0));
        post.setName(cursor.getString(1));
        post.setPosterUserId(cursor.getString(2));
        post.setStatus(cursor.getString(3));
        post.setTimeStamp(cursor.getString(4));
        return post;
    }

    public void deletePost(Post post) {
        String id = post.getPosterUserId();
        System.out.println("Comment deleted with id: " + id);
        database.delete(PostsDB.TABLE_POSTS, PostsDB.COLUMN_POSTER_USER_ID
                + " = " + id, null);
    }

    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<Post>();

        Cursor cursor = database.query(PostsDB.TABLE_POSTS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Post post = cursorToPost(cursor);
            posts.add(post);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return posts;
    }

    public boolean isTablePopulated(){
        String count = "SELECT count(*) FROM posts";
        Cursor cursor = database.rawQuery(count, null);
        cursor.moveToFirst();
        int rowCount = cursor.getInt(0);
        if(rowCount > 0){

            return true;
        }
        else{
            return false;
        }
    }
}
