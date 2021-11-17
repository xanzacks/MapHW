package edu.sjsu.android.maphw;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

public class LocationProvider extends ContentProvider {
    private LocationsDB dbHelper;


    private static final String TABLENAME = "Location";
    static final String PROVIDER_NAME = "edu.sjsu.android.maphw.myDatabase";
    static final String URL = "content://" + PROVIDER_NAME + "/Location";
    static final Uri CONTENT_URI = Uri.parse(URL);


    private static HashMap<String, String> STUDENTS_PROJECTION_MAP;

    static final int LOCATIONS = 1;
    static final int LOCATION_ID = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "Location", LOCATIONS);
        uriMatcher.addURI(PROVIDER_NAME, "Location/#", LOCATION_ID);
    }
    private SQLiteDatabase db;



    @Override
    public boolean onCreate() {
        Context context = getContext();
        LocationsDB dbHelper = new LocationsDB(context);

        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLENAME);
        switch (uriMatcher.match(uri)){
            case LOCATIONS:
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                break;
            case LOCATION_ID:
                qb.appendWhere("primary_key" + "=" + uri.getPathSegments());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if(sortOrder == null || sortOrder == ""){
            sortOrder = "primary_key";
        }
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)){
            case LOCATIONS:
                return "vnd.android.cursor.dir/vnd.example.Locations";
            case LOCATION_ID:
                return "vnd.android.cursor.item/vnd.example.Locations";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Context context = getContext();
        LocationsDB dbHelper = new LocationsDB(context);
        long rowID = db.insert(TABLENAME, "", values);
        if(rowID > 0){
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        int count = 0;

        switch(uriMatcher.match(uri)){
            case LOCATIONS:
                count = db.delete(TABLENAME, selection, selectionArgs);
                break;
            case LOCATION_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( TABLENAME, "primary_key" + " = " + id +
                        (!TextUtils.isEmpty(selection) ? "And (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case LOCATIONS:
                count = db.update(TABLENAME, values, selection, selectionArgs);
                break;
            case LOCATION_ID:
                count = db.update(TABLENAME, values, "primary_key" + " = " + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw  new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
