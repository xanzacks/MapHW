package edu.sjsu.android.maphw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class LocationsDB extends SQLiteOpenHelper {
    private static final String BASENAME = "myDatabase";
    private static final String TABLENAME = "Location";
    private static final String PRIMEKEY = "primary_key";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String ZOOMLEVEL = "zoomLevel";
    static final String CREATE_TABLE = "CREATE TABLE " + TABLENAME +
            "(primary_key INTEGER PRIMARY KEY AUTOINCREMENT, " + " latitude REAL NOT NULL, " + " longitude REAL NOT NULL, " + "zoomLevel REAL NOT NULL);";

    public LocationsDB(@Nullable Context context) {
        super(context, BASENAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLENAME);
        onCreate(db);
    }

    boolean addEmployee(double latitude, double longitude, double zoomLevel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LATITUDE, latitude);
        contentValues.put(LONGITUDE, longitude);
        contentValues.put(ZOOMLEVEL, zoomLevel);
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLENAME, null, contentValues) != -1;
    }

    Cursor getAllEmployees() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLENAME, null);
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLENAME);
        db.close();
    }
}
