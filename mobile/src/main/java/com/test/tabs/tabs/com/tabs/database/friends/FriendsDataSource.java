package com.test.tabs.tabs.com.tabs.database.friends;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.test.tabs.tabs.com.tabs.database.SQLite.DatabaseHelper;

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
    private DatabaseHelper dbHelper;
    private String[] allColumns = { DatabaseHelper.KEY_ID,
            DatabaseHelper.COLUMN_USER_ID, DatabaseHelper.COLUMN_NAME,
            DatabaseHelper.COLUMN_USER, DatabaseHelper.COLUMN_IS_FRIEND };

    public FriendsDataSource(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createFriend(String name, String id, String user) {
        //Create a ContentValues object so we can put our column name key/value pairs into it.
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, id);
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_USER, user);
        values.put(DatabaseHelper.COLUMN_IS_FRIEND, 0);
        //Insert into the database
        database.insertWithOnConflict(DatabaseHelper.TABLE_FRIENDS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
//        database.insert(DatabaseHelper.TABLE_FRIENDS, null,
//                values);
        return;
    }

    public Friend getFriend(String id, String user) {
        //Get the values from the database, querying by email
        Cursor cursor = database.query(DatabaseHelper.TABLE_FRIENDS,
                allColumns, DatabaseHelper.COLUMN_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_USER + " = ?",
                new String[]{id, user}, null, null, null);
        cursor.moveToFirst();
        Friend newFriend = cursorToFriend(cursor);
        cursor.close();
        return newFriend;
    }

    private Friend cursorToFriend(Cursor cursor) {
        Friend friend = new Friend();
        friend.setUserId(cursor.getString(1));
        friend.setName(cursor.getString(2));
        friend.setUser(cursor.getString(3));
        friend.setIsFriend(cursor.getInt(4));
        return friend;
    }

    public void deleteFriend(Friend friend) {
        String id = friend.getUserId();
        System.out.println("Comment deleted with user id: " + id);
        database.delete(DatabaseHelper.TABLE_FRIENDS, DatabaseHelper.COLUMN_USER_ID
                + " = " + id, null);
    }

    public List<Friend> getAllFriends(String userId) {
        List<Friend> friends = new ArrayList<Friend>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_FRIENDS,
                allColumns, DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{userId}, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Friend friend = cursorToFriend(cursor);
            System.out.println("Friend is friend: " + friend.getIsFriend());
            friends.add(friend);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return friends;
    }


    public boolean isTablePopulated(){
        String count = "SELECT count(*) FROM friends";
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

    public void updateFriend(String userId, String user, Integer val){
        database = dbHelper.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        newValues.put(DatabaseHelper.COLUMN_IS_FRIEND, val);

        String[] args = new String[]{userId, user};
        System.out.println("Database: " + database);
        database.update("friends", newValues, "user_id=? AND user=?", args);
    }

}
