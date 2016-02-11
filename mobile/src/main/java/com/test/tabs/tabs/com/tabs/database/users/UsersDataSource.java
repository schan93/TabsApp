package com.test.tabs.tabs.com.tabs.database.users;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.test.tabs.tabs.com.tabs.database.SQLite.DatabaseHelper;
import com.test.tabs.tabs.com.tabs.database.posts.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schan on 2/9/16.
 */
public class UsersDataSource {

    // Database fields
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private String[] allColumns = { DatabaseHelper.KEY_ID, DatabaseHelper.COLUMN_USER_ID,
            DatabaseHelper.COLUMN_NAME };

    public UsersDataSource(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * This method is to create the user and store his or her information in the database.
     * @param id
     * @return
     */
    public User createUser(String id, String userId, String name) {
        //Create a ContentValues object so we can put our column name key/value pairs into it.
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_ID, id);
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_NAME, name);

        //Insert into the database
//        database.rawQuery("INSERT OR IGNORE INTO " + DatabaseHelper.TABLE_USERS + " (" +
//                        DatabaseHelper.KEY_ID +", " + DatabaseHelper.COLUMN_USER_ID + ", " + DatabaseHelper.COLUMN_NAME + ") VALUES (?, ?, ?)",
//                new String[]{id, userId, name});

        database.insert(DatabaseHelper.TABLE_USERS, null,
                values);
        //Return the created user so we can use it for future use.
        User user = new User(id, userId, name);
        return user;
    }

    /**
     * This method is to get the user information based on the id of the user.
     * @param userId
     * @return
     */
    public User getUser(String userId) {
        ContentValues row = new ContentValues();
        Cursor cursor = database.query(DatabaseHelper.TABLE_USERS,
                allColumns, DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{userId},
                null, null, null);
        if(cursor.moveToFirst()) {
            User user = cursorToUser(cursor);
            cursor.close();
            return user;
        } else {
            return null;
        }

    }

    /**
     * This method is to get the user informaiton from the database and serialize it into a User object.
     * 0 = id
     * 1 = userId
     * 2 = name
     * @param cursor
     * @return
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User(cursor.getString(0), cursor.getString(1), cursor.getString(2));
        return user;
    }

    /**
     * Deletes user based on user id
     * @param user
     */
    public void deleteUser(User user) {
        String userId = user.getUserId();
        database.delete(DatabaseHelper.TABLE_USERS, DatabaseHelper.COLUMN_USER_ID
                + " = ?", new String[]{userId});
    }

    /**
     * Gets all users based on user id
     * @return
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<User>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_USERS,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            User user = cursorToUser(cursor);
            users.add(user);
            cursor.moveToNext();
        }
        cursor.close();
        return users;
    }
}
