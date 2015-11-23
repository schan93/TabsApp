package com.test.tabs.tabs.com.tabs.database.friends;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by schan on 10/28/15.
 */
public class FriendsDB extends SQLiteOpenHelper{

    //Database constants
    //One thing to note is that user = the user who is friends with this individual, user_id is the user_id of that actual person (that friend).
    public static final String TABLE_FRIENDS = "friends";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_USER = "user";
    public static final String COLUMN_IS_FRIEND = "isFriend";
    //Helper
    private static FriendsDB sInstance;

    //Not focused on this yet, but we will have photos also stored in the DB so we don't have to keep going back and forth
    //Not sure if this is good design
    //public static final String COLUMN_PICTURE = "picture";


    private static final String DATABASE_NAME = "friends.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_FRIENDS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " +  COLUMN_USER_ID  + " text not null unique, " + COLUMN_NAME
            + " text not null, " + COLUMN_USER + " text not null, " + COLUMN_IS_FRIEND + " integer not null);";

    //SQLiteOpenHelper methods
    private FriendsDB(Context context) {
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        onCreate(db);
    }

    public static synchronized FriendsDB getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new FriendsDB(context.getApplicationContext());
        }
        return sInstance;
    }
}
