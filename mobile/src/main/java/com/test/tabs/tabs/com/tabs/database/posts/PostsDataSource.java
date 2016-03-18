package com.test.tabs.tabs.com.tabs.database.posts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;
import android.provider.ContactsContract;
import android.util.Log;

import com.test.tabs.tabs.com.tabs.database.SQLite.DatabaseHelper;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;

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
            DatabaseHelper.COLUMN_TIME_STAMP, DatabaseHelper.COLUMN_PRIVACY, DatabaseHelper.COLUMN_LATITUDE, DatabaseHelper.COLUMN_LONGITUDE };

    public PostsDataSource(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
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

    public Post createPost(String postId, String posterUserId, String status, String posterName, String privacy, double latitude, double longitude) {
        //Create a ContentValues object so we can put our column name key/value pairs into it.
        ContentValues values = new ContentValues();
        //Insert 0 as column id because it will autoincrement for us (?)
        values.put(DatabaseHelper.KEY_ID, postId);
        values.put(DatabaseHelper.COLUMN_POSTER_NAME, posterName);
        values.put(DatabaseHelper.COLUMN_POSTER_USER_ID, posterUserId);
        values.put(DatabaseHelper.COLUMN_STATUS, status);
        String dateTime = getDateTime();
        values.put(DatabaseHelper.COLUMN_TIME_STAMP, dateTime);
        values.put(DatabaseHelper.COLUMN_PRIVACY, privacy);
        values.put(DatabaseHelper.COLUMN_LATITUDE, latitude);
        values.put(DatabaseHelper.COLUMN_LONGITUDE, longitude);
        //Insert into the database
        database.insert(DatabaseHelper.TABLE_POSTS, null, values);

//        database.rawQuery("INSERT OR IGNORE INTO " + DatabaseHelper.TABLE_POSTS + " (" +
//                        DatabaseHelper.KEY_ID +", " + DatabaseHelper.COLUMN_POSTER_NAME + ", " + DatabaseHelper.COLUMN_POSTER_USER_ID +", " + DatabaseHelper.COLUMN_STATUS +", " + DatabaseHelper.COLUMN_PRIVACY + ", " + DatabaseHelper.COLUMN_LATITUDE + ", " + DatabaseHelper.COLUMN_LONGITUDE + ") VALUES (?, ?, ?, ?, ?, ?, ?)",
//                new String[]{postId, posterName, posterUserId, status, privacy.toString(), Double.toString(latitude), Double.toString(longitude)});

        Post post = new Post(postId, posterName, status, posterUserId, dateTime, privacy, Double.toString(latitude), Double.toString(longitude), 0);
        return post;
    }

    public Post createPostFromFireBase(String postId, String posterUserId, String status, String timeStamp, String posterName, String privacy, double latitude, double longitude) {
        //Create a ContentValues object so we can put our column name key/value pairs into it.
        ContentValues values = new ContentValues();
        //Insert 0 as column id because it will autoincrement for us (?)
        values.put(DatabaseHelper.KEY_ID, postId);
        values.put(DatabaseHelper.COLUMN_POSTER_NAME, posterName);
        values.put(DatabaseHelper.COLUMN_POSTER_USER_ID, posterUserId);
        values.put(DatabaseHelper.COLUMN_STATUS, status);
        values.put(DatabaseHelper.COLUMN_TIME_STAMP, timeStamp);
        values.put(DatabaseHelper.COLUMN_PRIVACY, privacy);
        values.put(DatabaseHelper.COLUMN_LATITUDE, latitude);
        values.put(DatabaseHelper.COLUMN_LONGITUDE, longitude);
        //Insert into the database

//        database.rawQuery("INSERT OR IGNORE INTO " + DatabaseHelper.TABLE_POSTS + " ("+
//                DatabaseHelper.KEY_ID +", " + DatabaseHelper.COLUMN_POSTER_NAME + ", " + DatabaseHelper.COLUMN_POSTER_USER_ID +", " + DatabaseHelper.COLUMN_STATUS + ", "
//                + DatabaseHelper.COLUMN_TIME_STAMP +", " + DatabaseHelper.COLUMN_PRIVACY + ", " + DatabaseHelper.COLUMN_LATITUDE + ", " + DatabaseHelper.COLUMN_LONGITUDE + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
//                new String[]{postId, posterName, posterUserId, status, timeStamp, privacy.toString(), Double.toString(longitude), Double.toString(latitude)});
//        database.insertWithOnConflict(DatabaseHelper.TABLE_POSTS, null,
//                values, SQLiteDatabase.CONFLICT_IGNORE);
        database.insert(DatabaseHelper.TABLE_POSTS, null, values);

        Post post = new Post(postId, posterName, status, posterUserId, timeStamp, privacy, Double.toString(latitude), Double.toString(longitude), 0);
        return post;
    }

    public Post getPost(String id) {
        //This might only get one post. We have the get all posts below however. This method may not be useful now but just leaving here
        //Get the values from the database, querying by poster's user id
        ContentValues row = new ContentValues();
        Cursor cursor = database.query(DatabaseHelper.TABLE_POSTS,
                allColumns, DatabaseHelper.KEY_ID + " = ?", new String[]{id},
                null, null, null);
        if(cursor.moveToFirst()) {
            Post newPost = cursorToPost(cursor);
            cursor.close();
            return newPost;
        }
        else {
            return null;
        }
    }

    private Post cursorToPost(Cursor cursor) {
        //List is: 0 = Id, 1 = name, 2 = userid, 3 = status, 4 =timestamp, 5 = privacy, 6 = latitude, 7 = longitude
        System.out.println("Id: " + cursor.getString(0));
        System.out.println("Name: " + cursor.getString(1));
        System.out.println("Status: " + cursor.getString(3));
        System.out.println("User Id: " + cursor.getString(2));
        System.out.println("Time Stamp: " + cursor.getString(4));
        System.out.println("Privacy: " + cursor.getString(5));
        System.out.println("Latitude: " + cursor.getString(6));

        Post post = new Post(cursor.getString(0), cursor.getString(1), cursor.getString(3),
                cursor.getString(2), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), 0);
        return post;
    }

    /**
     * Deletes post based on user id
     * @param post
     */
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

    /**
     * Gets all posts that have public toggle. We pass in the current location of the user.
     * @return
     */
    public List<Post> getAllPublicPosts(double latitude, double longitude, double radius) {
        PointF center = new PointF((float)latitude, (float)longitude);
        System.out.println("Center: " + center);
        final double mult = 1; // mult = 1.1; is more reliable
        PointF p1 = calculateDerivedPosition(center, mult * radius, 0);
        PointF p2 = calculateDerivedPosition(center, mult * radius, 90);
        PointF p3 = calculateDerivedPosition(center, mult * radius, 180);
        PointF p4 = calculateDerivedPosition(center, mult * radius, 270);

        List<Post> posts = new ArrayList<Post>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_POSTS,
                allColumns, DatabaseHelper.COLUMN_PRIVACY + " = ? AND " + DatabaseHelper.COLUMN_LATITUDE + " > ? AND " +
                DatabaseHelper.COLUMN_LATITUDE + " < ? AND " + DatabaseHelper.COLUMN_LONGITUDE + " < ? AND "
                        + DatabaseHelper.COLUMN_LONGITUDE + " > ?" , new String[]{Integer.toString(0), String.valueOf(p3.x), String.valueOf(p1.x), String.valueOf(p2.y), String.valueOf(p4.y)}, null, null, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Post post = cursorToPost(cursor);
            PointF pointForCheck = new PointF(Float.parseFloat(post.getLatitude()), Float.parseFloat(post.getLongitude()));
            if(pointIsInCircle(pointForCheck, center, radius)) {
                System.out.println("Point was in circle radius: " + radius + " point for check: " + pointForCheck);
                posts.add(post);
            }
            cursor.moveToNext();
        }
        // make sure to close the cursor
        System.out.println("Number of posts from public: " + posts.size());
        cursor.close();
        return posts;
    }

    /**
     * Gets all posts with private toggle
     * @return List of Posts
     */
   public List<Post> getAllPrivatePosts() {
        List<Post> posts = new ArrayList<Post>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_POSTS,
                allColumns, DatabaseHelper.COLUMN_PRIVACY + " = ? ", new String[]{"Private"}, null, null, null);

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

    public List<Post> getPostsByUser(String userId) {
        List<Post> posts = new ArrayList<Post>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_POSTS,
                allColumns, DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{userId},
                null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Post post = cursorToPost(cursor);
            posts.add(post);
            cursor.moveToNext();
        }
        cursor.close();
        return posts;
    }

    public List<Post> getPostsByFriends(List<Friend> friendsUserIds) {
        List<Post> posts = new ArrayList<Post>();
        Cursor cursor = null;
        for(int i = 0; i < friendsUserIds.size(); i++){
            cursor = database.query(DatabaseHelper.TABLE_POSTS,
                    allColumns, DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{friendsUserIds.get(i).getUserId()},
                    null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Post post = cursorToPost(cursor);
                posts.add(post);
                cursor.moveToNext();
            }
        }
        if(cursor != null)
            cursor.close();
        System.out.println("Number of posts from friends: " + posts.size());
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

    public Integer getNumberComments(String postId){
        System.out.println("Getting number of comments from Post Id: " + postId);
        Cursor cursor = database.query(DatabaseHelper.TABLE_COMMENTS,
                new String[]{DatabaseHelper.COLUMN_POST_ID}, DatabaseHelper.COLUMN_POST_ID + " = ?", new String[]{postId},
                null, null, null);
        Integer count = cursor.getCount();
        cursor.close();
        return count;
    }

    public boolean updatePost(Post post){
        database = dbHelper.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        newValues.put(DatabaseHelper.COLUMN_PRIVACY, post.getPrivacy());

        String[] args = new String[]{post.getId()};
        int value = database.update("posts", newValues, "id= ?", args);
        return value > 0;
    }

    /**
     * Calculates the end-point from a given source at a given range (meters)
     * and bearing (degrees). This methods uses simple geometry equations to
     * calculate the end-point.
     *
     * @param point
     *            Point of origin
     * @param range
     *            Range in meters
     * @param bearing
     *            Bearing in degrees
     * @return End-point from the source given the desired range and bearing.
     */
    public static PointF calculateDerivedPosition(PointF point, double range, double bearing) {
        double EarthRadius = 6371000; // m

        double latA = Math.toRadians(point.x);
        double lonA = Math.toRadians(point.y);
        double angularDistance = range / EarthRadius;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
                Math.sin(latA) * Math.cos(angularDistance) +
                        Math.cos(latA) * Math.sin(angularDistance)
                                * Math.cos(trueCourse));

        double dlon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance)
                        * Math.cos(latA),
                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);

        PointF newPoint = new PointF((float) lat, (float) lon);

        return newPoint;

    }

    public static boolean pointIsInCircle(PointF pointForCheck, PointF center,
                                          double radius) {
        if (getDistanceBetweenTwoPoints(pointForCheck, center) <= radius)
            return true;
        else
            return false;
    }

    public static double getDistanceBetweenTwoPoints(PointF p1, PointF p2) {
        double R = 6371000; // m
        double dLat = Math.toRadians(p2.x - p1.x);
        double dLon = Math.toRadians(p2.y - p1.y);
        double lat1 = Math.toRadians(p1.x);
        double lat2 = Math.toRadians(p2.x);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;

        return d;
    }
}
