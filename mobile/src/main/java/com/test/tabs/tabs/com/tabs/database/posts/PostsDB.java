package com.test.tabs.tabs.com.tabs.database.posts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.test.tabs.tabs.com.tabs.database.friends.FriendsDB;

/**
 * Created by schan on 11/16/15.
 */
public class PostsDB extends SQLiteOpenHelper {

    //Database constants
    public static final String TABLE_POSTS = "posts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_POSTER_NAME = "name";
    public static final String COLUMN_POSTER_USER_ID = "user_id";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_TIME_STAMP = "time_stamp";
    private static PostsDB sInstance;

    //Not focused on this yet, but we will have photos also stored in the DB so we don't have to keep going back and forth
    //Not sure if this is good design
    //public static final String COLUMN_PICTURE = "picture";


    private static final String DATABASE_NAME = "posts.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_POSTS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_POSTER_USER_ID
            + " text not null, " + COLUMN_POSTER_NAME + " text not null, " + COLUMN_STATUS +
            " text not null, " + COLUMN_TIME_STAMP + " text not null);";

    //SQLiteOpenHelper methods
    private PostsDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(FriendsDB.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }

    public static synchronized PostsDB getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new PostsDB(context.getApplicationContext());
        }
        return sInstance;
    }
}
