package com.test.tabs.tabs.com.tabs.database.comments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.test.tabs.tabs.com.tabs.database.SQLite.DatabaseHelper;
import com.test.tabs.tabs.com.tabs.database.posts.Post;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by schan on 11/28/15.
 */
public class CommentsDataSource {
    // Database fields
    private long id; //This is the ID of the coment
    private long postId; //ID of the post
    private String commenter; //Can be same as poster
    private String comment; //Message that is written
    private String commenterUserId; //Commtenter id from facebook
    private String timeStamp; //When he wrote it

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private String[] allColumns = { DatabaseHelper.KEY_ID, DatabaseHelper.COLUMN_POST_ID,
            DatabaseHelper.COLUMN_COMMENTER, DatabaseHelper.COLUMN_COMMENT,
            DatabaseHelper.COLUMN_COMMENTER_USER_ID, DatabaseHelper.COLUMN_TIME_STAMP};


    public CommentsDataSource(Context context) {
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

    public Comment createComment(String id, String postId, String commenter, String comment, String commenterUserId) {
        //Create a ContentValues object so we can put our column name key/value pairs into it.
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_ID, id);
        values.put(DatabaseHelper.COLUMN_POST_ID, postId);
        values.put(DatabaseHelper.COLUMN_COMMENTER, commenter);
        values.put(DatabaseHelper.COLUMN_COMMENT, comment);
        values.put(DatabaseHelper.COLUMN_COMMENTER_USER_ID, commenterUserId);
        String dateTime = getDateTime();
        values.put(DatabaseHelper.COLUMN_TIME_STAMP, dateTime);
        database.rawQuery("INSERT OR IGNORE INTO " + DatabaseHelper.TABLE_COMMENTS + " (" +
                        DatabaseHelper.KEY_ID + ", " + DatabaseHelper.COLUMN_POST_ID + ", " + DatabaseHelper.COLUMN_COMMENTER + ", " + DatabaseHelper.COLUMN_COMMENT + ", "
                        + DatabaseHelper.COLUMN_COMMENTER_USER_ID + ", " + DatabaseHelper.COLUMN_TIME_STAMP + ") VALUES (?, ?, ?, ?, ?, ?)",
                new String[]{id, postId, commenter, comment, commenterUserId, dateTime});
        Comment newComment = new Comment(id, postId, commenter, comment, commenterUserId, dateTime);
        return newComment;
    }

    public Comment createCommentFromFirebase(String id, String postId, String commenter, String comment, String commenterUserId, String timeStamp) {
        //Create a ContentValues object so we can put our column name key/value pairs into it.
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_ID, id);
        values.put(DatabaseHelper.COLUMN_POST_ID, postId);
        values.put(DatabaseHelper.COLUMN_COMMENTER, commenter);
        values.put(DatabaseHelper.COLUMN_COMMENT, comment);
        values.put(DatabaseHelper.COLUMN_COMMENTER_USER_ID, commenterUserId);
        values.put(DatabaseHelper.COLUMN_TIME_STAMP, timeStamp);
        database.rawQuery("INSERT OR IGNORE INTO " + DatabaseHelper.TABLE_COMMENTS + " (" +
                        DatabaseHelper.KEY_ID + ", " + DatabaseHelper.COLUMN_POST_ID + ", " + DatabaseHelper.COLUMN_COMMENTER + ", " + DatabaseHelper.COLUMN_COMMENT + ", "
                        + DatabaseHelper.COLUMN_COMMENTER_USER_ID + ", " + DatabaseHelper.COLUMN_TIME_STAMP + ") VALUES (?, ?, ?, ?, ?, ?)",
                new String[]{id, postId, commenter, comment, commenterUserId, timeStamp});
        Comment newComment = new Comment(id, postId, commenter, comment, commenterUserId, timeStamp);
        return newComment;
    }


    public Comment getComment(String id) {
        //This might only get one post. We have the get all posts below however. This method may not be useful now but just leaving here
        //Incase we may need it later.
        //Get the values from the database, querying by poster's user id
        Cursor cursor = database.query(DatabaseHelper.TABLE_COMMENTS,
                allColumns, DatabaseHelper.KEY_ID + " = ?", new String[]{id},
                null, null, null);
        if(cursor.moveToFirst()){
            Comment newComment = cursorToPost(cursor);
            cursor.close();
            return newComment;
        }
        else {
            return null;
        }
    }

    private Comment cursorToPost(Cursor cursor) {
        Comment comment = new Comment();
        comment.setId(cursor.getString(0));
        comment.setPostId(cursor.getString(1));
        comment.setCommenter(cursor.getString(2));
        comment.setComment(cursor.getString(3));
        comment.setCommenterUserId(cursor.getString(4));
        comment.setTimeStamp(cursor.getString(5));
        return comment;
    }

    public void deleteComment(Comment post) {
        String id = post.getId();
        System.out.println("Comment deleted with id: " + id);
        database.delete(DatabaseHelper.TABLE_POSTS, DatabaseHelper.KEY_ID
                + " = " + id, null);
    }

    public List<Comment> getCommentsForPost(String postId) {
        List<Comment> comments = new ArrayList<Comment>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_COMMENTS,
                allColumns, DatabaseHelper.COLUMN_POST_ID + " = ?", new String[]{postId}, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Comment comment = cursorToPost(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        System.out.println("Comments: " + comments);
        return comments;
    }

    public boolean isTablePopulated(){
        String count = "SELECT count(*) FROM comments";
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
