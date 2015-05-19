package com.chin.marco.uofuncheapsf.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.chin.marco.uofuncheapsf.pojo.Event;

import java.util.List;

/**
 * Created by Marco on 3/10/2015.
 */
public class SQLiteDB{
    private static final String DATABASE_NAME = "funcheapsfDB";
    private static final int DATABASE_VERSION = 1;

    private static final String KEY_ROW_ID = "_id"; //primary key
    private static final String KEY_EVENT_ID = "eventID";
    private static final String KEY_TITLE = "title";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_TIME = "time";
    private static final String KEY_PRICE = "price";
    private static final String KEY_THUMBNAIL_URL = "thumbnail_url";
    private static final String KEY_URL_CLICK = "url_click";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_CAVEAT = "caveat";
    private static final String KEY_CONTEST_STATE= "contest_state";
    private static final String KEY_TIME_TYPE = "time_type";

    private static final String DATE_PREFIX = "Date_";

    private DBHelper mDBHelper;
    private static SQLiteDB sInstance;

    private SQLiteDB(Context context){
        mDBHelper = new DBHelper(context);
    }

    public static SQLiteDB getInstance(Context context){
        if(sInstance == null)
            sInstance = new SQLiteDB(context);

        return sInstance;
    }

    public boolean isTableExist(String dateEndPoint){
        Cursor cursor = null;
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String tableName = DATE_PREFIX + dateEndPoint;

        //TODO begin transaction setSuccessful and endTransaction for this one too?
        cursor = db.rawQuery(
                "SELECT name FROM sqlite_master " +
                        "WHERE type = 'table' " +
                        "AND name = " + "'" + tableName + "'", null); //gets name of every table in db

        int cursorCount = cursor.getCount();
        cursor.close();
        //returns true if table already exists else false
        return cursorCount > 0;
    }

    public void createTable(String dateEndPoint, List<Event> eventList){
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String tableName = DATE_PREFIX + dateEndPoint;

        db.beginTransaction();
        try{
            //create a table based on the date
            db.execSQL("CREATE TABLE " + tableName + " (" +
                    KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_EVENT_ID + " INTEGER, " +
                    KEY_TITLE + " VARCHAR(255), " +
                    KEY_LOCATION + " VARCHAR(255), " +
                    KEY_TIME + " VARCHAR(255), " +
                    KEY_PRICE + " VARCHAR(255), " +
                    KEY_THUMBNAIL_URL + " VARCHAR(255), " +
                    KEY_URL_CLICK + " VARCHAR(255), " +
                    KEY_DESCRIPTION + " VARCHAR(255), " +
                    KEY_CAVEAT + " VARCHAR(255), " +
                    KEY_CONTEST_STATE + " VARCHAR(255), " +
                    KEY_TIME_TYPE + " INTEGER);"
            );

            //insert events for that date in the table
            for(Event event: eventList){
                cv.put(KEY_EVENT_ID, event.getEventID());
                cv.put(KEY_TITLE, event.getTitle());
                cv.put(KEY_LOCATION, event.getLocation());
                cv.put(KEY_TIME, event.getTime());
                cv.put(KEY_PRICE, event.getPrice());
                cv.put(KEY_THUMBNAIL_URL, event.getThumbnailURL());
                cv.put(KEY_URL_CLICK, event.getChild().getURLClick());
                cv.put(KEY_DESCRIPTION, event.getChild().getDescription());
                cv.put(KEY_CAVEAT, event.getChild().getCaveat());
                cv.put(KEY_CONTEST_STATE, event.getChild().getContestState());
                cv.put(KEY_TIME_TYPE, event.getTimeType());

                db.insert(tableName, null, cv);
            }

            Log.d("funTableCreation", "table " + tableName + " created"); //TODO delete this
            db.setTransactionSuccessful();

        }catch(SQLException e){
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteAllTables(){
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Cursor cursor = null;
        String tablesDeleted = "";

        db.beginTransaction();
        try{
            cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master " +
                    "WHERE type = 'table' " +
                    "AND name NOT LIKE 'sqlite_%' " +
                    "AND name NOT LIKE 'android_metadata'", null); //gets name of every table in db

            while(cursor.moveToNext()){
                db.execSQL("DROP TABLE IF EXISTS " + cursor.getString(cursor.getColumnIndex("name")));
                tablesDeleted += cursor.getString(cursor.getColumnIndex("name"));
            }

            Log.d("funTableDeletion", tablesDeleted); //TODO delete this
            db.setTransactionSuccessful();

        }catch(SQLException e){
            e.printStackTrace();
        } finally {
            if(cursor != null)
                cursor.close();

            db.endTransaction();
        }
    }

    public void loadEventListFromTable(String dateEndPoint, DataProvider dataProvider){
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        String tableName = DATE_PREFIX + dateEndPoint;
        String[] columns = new String[]{KEY_EVENT_ID, KEY_TITLE, KEY_LOCATION, KEY_TIME, KEY_PRICE, KEY_THUMBNAIL_URL,
                KEY_URL_CLICK, KEY_DESCRIPTION, KEY_CAVEAT, KEY_CONTEST_STATE, KEY_TIME_TYPE};
        Cursor cursor = null;

        db.beginTransaction();
        try {
            dataProvider.clear();
            cursor = db.query(tableName, columns, null, null, null, null, null);

            while (cursor.moveToNext()) {
                int eventID = cursor.getInt(cursor.getColumnIndex(KEY_EVENT_ID));
                String title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
                String location = cursor.getString(cursor.getColumnIndex(KEY_LOCATION));
                String time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
                String price = cursor.getString(cursor.getColumnIndex(KEY_PRICE));
                String thumbnailURL = cursor.getString(cursor.getColumnIndex(KEY_THUMBNAIL_URL));
                String urlClick = cursor.getString(cursor.getColumnIndex(KEY_URL_CLICK));
                String description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION));
                String caveat = cursor.getString(cursor.getColumnIndex(KEY_CAVEAT));
                String contestState = cursor.getString(cursor.getColumnIndex(KEY_CONTEST_STATE));
                int timeType = cursor.getInt(cursor.getColumnIndex(KEY_TIME_TYPE));

                Event event = new Event(eventID, title, location, time, price, thumbnailURL, urlClick, description, caveat, contestState, timeType);
                dataProvider.add(event);
            }

            db.setTransactionSuccessful();
        }catch(SQLException e){
            e.printStackTrace();
        } finally {
            if(cursor != null)
                cursor.close();

            db.endTransaction();
        }
    }

    public void close(){
        mDBHelper.close();
    }

    //I don't think inner class is needed. Reason Vivz did it is because he put his constants in here to prevent outsider classes from accessing it
    // but the private keyword takes care of that...
    private class DBHelper extends SQLiteOpenHelper{

        public DBHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        //begin and endTransaction() not need in onCreate() and onUpgrade() because it is included by default
        @Override
        public void onCreate(SQLiteDatabase db) {
            //Does not have a table until user opens the app
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //onUpgrade not necessary because all tables are dropped in onStop();
        }
    }
}