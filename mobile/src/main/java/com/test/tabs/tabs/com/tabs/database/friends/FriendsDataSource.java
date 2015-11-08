package com.test.tabs.tabs.com.tabs.database.friends;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schan on 10/28/15.
 */

//This class is specifically because we use something a DAO to access the Database
    //As opposed to traditionally going to the DB, we can wrap these DB objects with Java objects
    //Although it is less efficient, it  makes it easier to go to and from the DB
public class FriendsDataSource {

    // Database fields
    private SQLiteDatabase database;
    private FriendsDB dbHelper;
    private String[] allColumns = { FriendsDB.COLUMN_ID,
            FriendsDB.COLUMN_NAME, FriendsDB.COLUMN_EMAIL };

    public FriendsDataSource(Context context) {
        dbHelper = new FriendsDB(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createFriend(String name, String id) {
        //Create a ContentValues object so we can put our column name key/value pairs into it.
        ContentValues values = new ContentValues();
        values.put(FriendsDB.COLUMN_NAME, name);
        values.put(FriendsDB.COLUMN_ID, id);
        values.put(FriendsDB.COLUMN_EMAIL, "email");
        //Insert into the database
        long insertId = database.insert(FriendsDB.TABLE_FRIENDS, null,
                values);
        return;
    }

    public Friend getFriend(String id) {
        //Get the values from the database, querying by email
        Cursor cursor = database.query(FriendsDB.TABLE_FRIENDS,
                allColumns, FriendsDB.COLUMN_ID + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();
        Friend newFriend = cursorToFriend(cursor);
        cursor.close();
        return newFriend;
    }

    private Friend cursorToFriend(Cursor cursor) {
        Friend friend = new Friend();
        friend.setId(cursor.getLong(0));
        friend.setName(cursor.getString(1));
        friend.setEmail(cursor.getString(2));
        return friend;
    }

    public void deleteFriend(Friend friend) {
        long id = friend.getId();
        System.out.println("Comment deleted with id: " + id);
        database.delete(FriendsDB.TABLE_FRIENDS, FriendsDB.COLUMN_ID
                + " = " + id, null);
    }

    public List<Friend> getAllFriends() {
        List<Friend> friends = new ArrayList<Friend>();

        Cursor cursor = database.query(FriendsDB.TABLE_FRIENDS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Friend friend = cursorToFriend(cursor);
            friends.add(friend);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return friends;
    }

}
