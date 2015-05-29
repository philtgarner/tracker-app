package com.garner.location.lists.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Phil Garner
 * The class that creats and maintains the database structure
 */
public class TrackerSQLiteHelper extends SQLiteOpenHelper {

    //The table name
    public static final String TABLE = "trackings";

    //The column names
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_URL = "_url";
    public static final String COLUMN_DOWNLOAD = "_dl";
    public static final String COLUMN_FREQUENCY = "_freq";
    public static final String COLUMN_LAST_USED = "_last_used";

    //Database version
    private static final int DATABASE_VERSION = 1;

    //Database name (for the SQLite file)
    private static final String DATABASE_NAME = "trackings.db";

    //The create statement
    private static final String DATABASE_CREATE = "CREATE table " + TABLE + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_URL + " TEXT NOT NULL, "
            + COLUMN_DOWNLOAD + " TEXT NOT NULL, "
            + COLUMN_FREQUENCY + " INTEGER, "
            + COLUMN_LAST_USED + " INTEGER);";

    /**
     * Constructor with the given context
     * @param c The context
     */
    public TrackerSQLiteHelper(Context c){
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TrackerSQLiteHelper.class.getName(), "Upgrading Database...");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
}
