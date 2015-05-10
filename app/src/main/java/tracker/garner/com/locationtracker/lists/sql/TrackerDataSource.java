package tracker.garner.com.locationtracker.lists.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import tracker.garner.com.locationtracker.Tracker;
import tracker.garner.com.locationtracker.lists.StoredTracker;

/**
 * Created by Phil on 13/04/2015.
 */
public class TrackerDataSource {

    private SQLiteDatabase db = null;
    private TrackerSQLiteHelper dbHelper = null;
    private String [] allColumns = {TrackerSQLiteHelper.COLUMN_ID, TrackerSQLiteHelper.COLUMN_URL, TrackerSQLiteHelper.COLUMN_DOWNLOAD, TrackerSQLiteHelper.COLUMN_FREQUENCY, TrackerSQLiteHelper.COLUMN_LAST_USED};

    public TrackerDataSource(Context context){
        dbHelper = new TrackerSQLiteHelper(context);
    }

    public void open() throws SQLException{
        db = dbHelper.getWritableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

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

    public void updateTime(StoredTracker t){
        ContentValues values = new ContentValues();
        values.put(TrackerSQLiteHelper.COLUMN_LAST_USED, System.currentTimeMillis());
        db.update(TrackerSQLiteHelper.TABLE,values, TrackerSQLiteHelper.COLUMN_ID + " = " + t.getId(), null);
    }

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

    public void deleteTracker(StoredTracker t){
        long id = t.getId();
        db.delete(TrackerSQLiteHelper.TABLE, TrackerSQLiteHelper.COLUMN_ID + " = " + id, null);
    }

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
