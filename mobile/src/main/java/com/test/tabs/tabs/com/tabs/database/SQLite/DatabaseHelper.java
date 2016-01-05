package com.test.tabs.tabs.com.tabs.database.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by schan on 11/28/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    //Static instance of helper so that we don't have database leaks!
    private static DatabaseHelper sInstance;


    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "databaseManager.db";

    // Table Names
    public static final String TABLE_COMMENTS = "comments";
    public static final String TABLE_POSTS = "posts";
    public static final String TABLE_FRIENDS = "friends";

    // Common column names
    public static final String KEY_ID = "id";
    //private static final String KEY_CREATED_AT = "created_at";

    // Common column to just posts and comments
    public static final String COLUMN_TIME_STAMP = "time_stamp";

    // FRIENDS Table - column nmaes
    //User = the user id of the person logged in
    //User_id = the user id of the friend
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_USER = "user";
    public static final String COLUMN_IS_FRIEND = "isFriend";

    // POSTS Table - column names
    public static final String COLUMN_POSTER_NAME = "name";
    public static final String COLUMN_POSTER_USER_ID = "user_id";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_PRIVACY = "privacy";

    // COMMENTS Table - column names
    public static final String COLUMN_POST_ID = "post_id";
    public static final String COLUMN_COMMENTER = "commenter";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_COMMENTER_USER_ID = "commenter_user_id";

    // Table Create Statements
    private static final String CREATE_TABLE_FRIENDS = "create table "
            + TABLE_FRIENDS + "(" + KEY_ID
            + " integer primary key autoincrement, " +  COLUMN_USER_ID  + " text not null unique, " + COLUMN_NAME
            + " text not null, " + COLUMN_USER + " text not null, " + COLUMN_IS_FRIEND + " integer not null);";

    // Tag table create statement
    private static final String CREATE_TABLE_POSTS = "create table "
            + TABLE_POSTS + "(" + KEY_ID
            + " integer primary key autoincrement, " + COLUMN_POSTER_USER_ID
            + " text not null, " + COLUMN_POSTER_NAME + " text not null, " + COLUMN_STATUS +
            " text not null, " + COLUMN_TIME_STAMP + " datetime not null, " + COLUMN_PRIVACY + " integer not null);";

    // Comments table create statement
    private static final String CREATE_TABLE_COMMENTS = "create table "
            + TABLE_COMMENTS + "(" + KEY_ID + " integer primary key autoincrement, "
            + COLUMN_POST_ID + " integer not null, " + COLUMN_COMMENTER + " text not null, "
            + COLUMN_COMMENT + " text not null, " + COLUMN_COMMENTER_USER_ID + " text not null, " + COLUMN_TIME_STAMP + " datetime not null)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        System.out.println("Creating required tables.");
        db.execSQL(CREATE_TABLE_FRIENDS);
        db.execSQL(CREATE_TABLE_POSTS);
        db.execSQL(CREATE_TABLE_COMMENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);

        // create new tables
        onCreate(db);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        System.out.println("Getting DB Context");
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }
}
