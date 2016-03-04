package com.test.tabs.tabs.com.tabs.database.friends;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.test.tabs.tabs.com.tabs.database.SQLite.DatabaseHelper;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");

    public FriendsDataSource(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Friend createFriend(String uniqueId, String name, String userId, String user, String isFriend) {
        //Create a ContentValues object so we can put our column name key/value pairs into it.
        //User = the user id of the person logged in
        //User_id = the user id of the friend
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_ID, uniqueId);
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_USER, user);
        values.put(DatabaseHelper.COLUMN_IS_FRIEND, isFriend);
        //Insert into the database
        //database.insertWithOnConflict(DatabaseHelper.TABLE_FRIENDS, null, values, SQLiteDatabase.CONFLICT_IGNORE);

//            database.rawQuery("INSERT OR IGNORE INTO " + DatabaseHelper.TABLE_FRIENDS + " ("+
//                        DatabaseHelper.KEY_ID +", " + DatabaseHelper.COLUMN_USER_ID + ", " + DatabaseHelper.COLUMN_NAME +", " + DatabaseHelper.COLUMN_USER + ", "
//                        + DatabaseHelper.COLUMN_IS_FRIEND + ") VALUES (?, ?, ?, ?, ?)",
//                        new String[]{uniqueId, userId, name, user, Integer.toString(isFriend)});
        Friend friend = new Friend(uniqueId, userId, name, user, isFriend);
        database.insert(DatabaseHelper.TABLE_FRIENDS, null,
                values);
        return friend;
    }

    public Friend getFriend(String userId, String user) {
        //Get the values from the database, querying by email
        Cursor cursor = database.query(DatabaseHelper.TABLE_FRIENDS,
                allColumns, DatabaseHelper.COLUMN_USER_ID + " = ? and " + DatabaseHelper.COLUMN_USER + " = ?",
                new String[]{userId, user}, null, null, null);
        if(cursor.moveToFirst()) {
            Friend newFriend = cursorToFriend(cursor);
            cursor.close();
            return newFriend;
        }
        else {
            return null;
        }
    }

    private Friend cursorToFriend(Cursor cursor) {
        System.out.println("Cursor 0: " + cursor.getString(0));
        System.out.println("Cursor 1: " + cursor.getString(1));
        System.out.println("Cursor 2: " + cursor.getString(2));

        Friend friend = new Friend(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        return friend;
    }

    public void deleteFriend(Friend friend) {
        String id = friend.getUserId();
        System.out.println("Comment deleted with user id: " + id);
        database.delete(DatabaseHelper.TABLE_FRIENDS, DatabaseHelper.COLUMN_USER_ID
                + " = " + id, null);
    }

    /**
     * Method to get all freinds that you have added
     * @param userId
     * @return
     */
    public List<Friend> getAllAddedFriends(String userId){
        List<Friend> friends = new ArrayList<Friend>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_FRIENDS,
                allColumns, DatabaseHelper.COLUMN_USER + " = ? and " + DatabaseHelper.COLUMN_IS_FRIEND + " = 1", new String[]{userId}, null, null, null);

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

    /**
     * General method to get all freinds from Facebook that have the app installed
     * @param userId
     * @return
     */
    public List<Friend> getAllFriends(String userId) {
        System.out.println("Friends: User Id: " + userId);
        List<Friend> friends = new ArrayList<Friend>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_FRIENDS,
                allColumns, DatabaseHelper.COLUMN_USER + " = ?", new String[]{userId}, null, null, null);
        cursor.moveToFirst();
        System.out.println("Cursor: " + cursor);
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
        System.out.println("Row Count: " + rowCount);
        if(rowCount > 0){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean updateFriend(String userId, String user, String isFriend){
        if(database == null) {
            open();
        }
        ContentValues newValues = new ContentValues();
        newValues.put(DatabaseHelper.COLUMN_IS_FRIEND, isFriend);

        String[] args = new String[]{userId, user};

        System.out.println("FriendsDataSource: Before");
        //Debugging purposes
        Cursor cursor = database.query(DatabaseHelper.TABLE_FRIENDS,
                allColumns, DatabaseHelper.COLUMN_USER + " = ? and " + DatabaseHelper.COLUMN_USER_ID + " = ?" , args, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Friend friend = cursorToFriend(cursor);
            System.out.println("FriendsDataSource: Name: " + friend.getName() + " User of person logged in: " +
                    friend.getUser() + " user id of the friend: " + friend.getUserId());
            System.out.println("FriendsDataSource: Friend is friend: " + friend.getIsFriend());
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();


        int value = database.update(DatabaseHelper.TABLE_FRIENDS, newValues, DatabaseHelper.COLUMN_USER + " = ? AND " + DatabaseHelper.COLUMN_USER_ID + " = ?", args);

        System.out.println("FriendsDataSource: After");
        //Debugging purposes
        Cursor cursor2 = database.query(DatabaseHelper.TABLE_FRIENDS,
                allColumns, DatabaseHelper.COLUMN_USER + " = ? and " + DatabaseHelper.COLUMN_USER_ID + " = ?" , new String[]{userId, user}, null, null, null);
        cursor2.moveToFirst();
        while (!cursor2.isAfterLast()) {
            Friend friend = cursorToFriend(cursor2);
            System.out.println("FriendsDataSource: Name2: " + friend.getName() + " User of person logged in2: " +
                    friend.getUser() + " user id of the friend2: " + friend.getUserId());
            System.out.println("FriendsDataSource: Friend is friend2: " + friend.getIsFriend());
            cursor2.moveToNext();
        }


        System.out.println("FriendsDataSource: " + "Number of rows affecteD: " + value + " isFreind: " + isFriend);
        System.out.println("FriendsDataSource: " + "Friend " + userId + " is a friend of " + user + " and his is friend");
        return value > 0;
    }

}
