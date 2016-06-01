package com.test.tabs.tabs.com.tabs.database.friends;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.firebase.client.Firebase;
import com.test.tabs.tabs.com.tabs.database.SQLite.DatabaseHelper;

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
        while (!cursor.isAfterLast()) {
            Friend friend = cursorToFriend(cursor);
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

    public boolean updateFriend(String userId, String user, String isFriend){
        if(database == null) {
            open();
        }
        ContentValues newValues = new ContentValues();
        newValues.put(DatabaseHelper.COLUMN_IS_FRIEND, isFriend);
        String[] args = new String[]{userId, user};
        int value = database.update(DatabaseHelper.TABLE_FRIENDS, newValues, DatabaseHelper.COLUMN_USER + " = ? AND " + DatabaseHelper.COLUMN_USER_ID + " = ?", args);
        return value > 0;
    }

    private boolean updateName(String userId, String user, String attribute) {
        if(database == null) {
            open();
        }
        ContentValues newValues = new ContentValues();
        newValues.put(DatabaseHelper.COLUMN_NAME, attribute);
        String[] args = new String[]{userId, user};
        int value = database.update(DatabaseHelper.TABLE_FRIENDS, newValues, DatabaseHelper.COLUMN_USER + " = ? AND " + DatabaseHelper.COLUMN_USER_ID + " = ?", args);
        return value > 0;
    }

    private boolean updateUserId(String userId, String user, String attribute) {
        if(database == null) {
            open();
        }
        ContentValues newValues = new ContentValues();
        newValues.put(DatabaseHelper.COLUMN_USER_ID, attribute);
        String[] args = new String[]{userId, user};
        int value = database.update(DatabaseHelper.TABLE_FRIENDS, newValues, DatabaseHelper.COLUMN_USER + " = ? AND " + DatabaseHelper.COLUMN_USER_ID + " = ?", args);
        return value > 0;
    }

    private boolean updateUser(String userId, String user, String attribute) {
        if(database == null) {
            open();
        }
        ContentValues newValues = new ContentValues();
        newValues.put(DatabaseHelper.COLUMN_USER, attribute);
        String[] args = new String[]{userId, user};
        int value = database.update(DatabaseHelper.TABLE_FRIENDS, newValues, DatabaseHelper.COLUMN_USER + " = ? AND " + DatabaseHelper.COLUMN_USER_ID + " = ?", args);
        return value > 0;
    }

    public void checkFriendChanged(Friend firebaseFriend, Friend localDatabaseFriend) {
        String id = firebaseFriend.getId();
        String name = firebaseFriend.getName();
        String userId = firebaseFriend.getUserId();
        String user = firebaseFriend.getUser();
        String isFriend = firebaseFriend.getIsFriend();
        if(!firebaseFriend.getName().equals(localDatabaseFriend.getName())) {
            updateName(localDatabaseFriend.getUser(), localDatabaseFriend.getUserId(), firebaseFriend.getName());
        }
        if(!firebaseFriend.getUserId().equals(localDatabaseFriend.getUserId())) {
            updateName(localDatabaseFriend.getUser(), localDatabaseFriend.getUserId(), firebaseFriend.getName());
        }
    }


}
