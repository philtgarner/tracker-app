package com.garner.location.lists.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.garner.location.lists.StoredTracker;

/**
 * @author Phil Garner
 * A class used to extract stored tracking information from an SQLite database stored on the device
 */
public class TrackerDataSource {

    private SQLiteDatabase db = null;
    private TrackerSQLiteHelper dbHelper = null;
    private String [] allColumns = {TrackerSQLiteHelper.COLUMN_ID, TrackerSQLiteHelper.COLUMN_URL, TrackerSQLiteHelper.COLUMN_DOWNLOAD, TrackerSQLiteHelper.COLUMN_FREQUENCY, TrackerSQLiteHelper.COLUMN_LAST_USED};

    public TrackerDataSource(Context context){
        dbHelper = new TrackerSQLiteHelper(context);
    }

    /**
     * Opens the database, must be called before any other methods
     * @throws SQLException When the database fails to open
     */
    public void open() throws SQLException{
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Closes the database
     */
    public void close(){
        dbHelper.close();
    }

    /**
     * Takes the details of a tracker and stores them in the database
     * @param url The URL to send the tracking information to
     * @param download The download key
     * @param frequency The frequency to update the location
     * @return An object representing the stored tracker, null if unsuccessful
     */
    public StoredTracker createStoredTracker(String url, String download, int frequency){
        ContentValues values = new ContentValues();
        values.put(TrackerSQLiteHelper.COLUMN_URL, url);
        values.put(TrackerSQLiteHelper.COLUMN_DOWNLOAD, download);
        values.put(TrackerSQLiteHelper.COLUMN_FREQUENCY, frequency);
        values.put(TrackerSQLiteHelper.COLUMN_LAST_USED, System.currentTimeMillis());

        long insertID = db.insert(TrackerSQLiteHelper.TABLE, null, values);
        Cursor cursor = db.query(TrackerSQLiteHelper.TABLE, allColumns, TrackerSQLiteHelper.COLUMN_ID + " = " + insertID, null, null, null, null);
        cursor.moveToFirst();

        StoredTracker t = cursorToTracker(cursor);
        cursor.close();
        return t;
    }

    /**
     * Updates the time of a given stored tracker. The "last used" time is set to the current time.
     * @param t The tracker to update the "last used" time
     */
    public void updateTime(StoredTracker t){
        ContentValues values = new ContentValues();
        values.put(TrackerSQLiteHelper.COLUMN_LAST_USED, System.currentTimeMillis());
        db.update(TrackerSQLiteHelper.TABLE,values, TrackerSQLiteHelper.COLUMN_ID + " = " + t.getId(), null);
    }

    /**
     * Gets a list of all stored trackers with the most recently used first in the list
     * @return All stored trackers
     */
    public List<StoredTracker> getAllTrackers(){
        List<StoredTracker> output = new ArrayList<StoredTracker>();

        Cursor c = db.query(TrackerSQLiteHelper.TABLE, allColumns, null, null, null, null, TrackerSQLiteHelper.COLUMN_LAST_USED + " DESC");
        c.moveToFirst();

        while(!c.isAfterLast()){
            StoredTracker t = cursorToTracker(c);
            output.add(t);
            c.moveToNext();
        }
        c.close();

        return output;
    }

    /**
     * Deletes the given tracker from the SQLite database
     * @param t The stored tracker to delete
     */
    public void deleteTracker(StoredTracker t){
        long id = t.getId();
        db.delete(TrackerSQLiteHelper.TABLE, TrackerSQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    /**
     * Takes a database cursor and creates a stored tracker object from it
     * @param c The cursor to read
     * @return An object representing the stored tracker
     */
    private StoredTracker cursorToTracker(Cursor c){
        long id = c.getLong(0);
        String url = c.getString(1);
        String dl = c.getString(2);
        long freq = c.getLong(3);
        long lastUsed = c.getLong(4);

        StoredTracker t = new StoredTracker(id, url, dl, freq, lastUsed);
        return t;
    }
}
