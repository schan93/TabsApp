package com.test.tabs.tabs.com.tabs.database.posts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.test.tabs.tabs.com.tabs.database.SQLite.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by schan on 11/16/15.
 */
public class PostsDataSource {


    // Database fields
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private String[] allColumns = { DatabaseHelper.KEY_ID, DatabaseHelper.COLUMN_POSTER_NAME,
            DatabaseHelper.COLUMN_POSTER_USER_ID, DatabaseHelper.COLUMN_STATUS,
            DatabaseHelper.COLUMN_TIME_STAMP };

    public PostsDataSource(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
//        database = dbHelper.getWritableDatabase();

    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "MM/dd/yyyy hh:mm:ss a", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void createPost(String posterUserId, String status, String posterName) {
        //Create a ContentValues object so we can put our column name key/value pairs into it.
        ContentValues values = new ContentValues();
        //Insert 0 as column id because it will autoincrement for us (?)
        values.put(DatabaseHelper.COLUMN_POSTER_NAME, posterName);
        values.put(DatabaseHelper.COLUMN_POSTER_USER_ID, posterUserId);
        values.put(DatabaseHelper.COLUMN_STATUS, status);
        values.put(DatabaseHelper.COLUMN_TIME_STAMP, getDateTime());
        //Insert into the database
        database.insert(DatabaseHelper.TABLE_POSTS, null,
                values);
        return;
    }

    public Post getPost(long id) {
        //This might only get one post. We have the get all posts below however. This method may not be useful now but just leaving here
        //Get the values from the database, querying by poster's user id
        ContentValues row = new ContentValues();
        Cursor cursor = database.query(DatabaseHelper.TABLE_POSTS,
                allColumns, DatabaseHelper.KEY_ID + " = ?", new String[]{Long.toString(id)},
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
        database.delete(DatabaseHelper.TABLE_POSTS, DatabaseHelper.COLUMN_POSTER_USER_ID
                + " = ?", new String[]{id});
    }

    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<Post>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_POSTS,
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

    public Integer getNumberComments(long postId){
        Cursor cursor = database.query(DatabaseHelper.TABLE_COMMENTS,
                new String[]{DatabaseHelper.COLUMN_POST_ID}, DatabaseHelper.COLUMN_POST_ID + " = ?", new String[]{Long.toString(postId)},
                null, null, null);
        Integer count = cursor.getCount();
        //cursor.moveToFirst();
        //Integer count = cursor.getCount();
        cursor.close();
        return count;
    }
}
