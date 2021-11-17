package edu.sjsu.android.maphw;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;


import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.content.CursorLoader;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import edu.sjsu.android.maphw.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {

    LocationsDB db;
    private GoogleMap map;
    private final LatLng LOCATION_UNIV = new LatLng(37.335371, -121.881050);
    private final LatLng LOCATION_CS = new LatLng(37.333714, -121.881860);
    private String[] mColumnProjection = new String[]{"latitude", "longitude", "zoomLevel"};
    int lcount = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        db = new LocationsDB(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        //map.addMarker(new MarkerOptions().position(LOCATION_CS).title("Find Me Here"));
        Cursor c = db.getAllEmployees();
        int count = 1;
        LoaderManager.getInstance(this).initLoader(0, null, this);
        //getLoaderManager().initLoader(0, null, this);
//        if (c.moveToFirst()) {
//            do{
//                LatLng locatmark = new LatLng(c.getDouble(1), c.getDouble(2));
//                map.addMarker(new MarkerOptions().position(locatmark).title("Mark" + count));
//                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(locatmark, c.getFloat(3));
//                map.animateCamera(update);
//                count++;
//            }while (c.moveToNext());
//            Toast.makeText(getBaseContext(), "Markers added to the map", Toast.LENGTH_LONG).show();
//
//        }
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            //reference: https://stackoverflow.com/questions/45207709/how-to-add-marker-on-google-maps-android
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                int num = lcount + 1;
                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title("Mark" + num);

                // Animating to the touched position
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                map.addMarker(markerOptions);
                ContentValues values = new ContentValues();

                values.put("latitude", latLng.latitude);
                values.put("longitude", latLng.longitude);
                float zoom = map.getCameraPosition().zoom;
                values.put("zoomLevel", zoom);

                //Uri uri = getContentResolver().insert(LocationProvider.CONTENT_URI, values);
                LocationInsertTask insertTask = new LocationInsertTask();
                insertTask.doInBackground(values);
                Toast.makeText(getBaseContext(), "Markers added to the map", Toast.LENGTH_LONG).show();
            }
        });
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
//                getContentResolver().delete(LocationProvider.CONTENT_URI, null, null);
//                db.deleteAll();
                LocationDeleteTask deleteTask = new LocationDeleteTask();
                deleteTask.doInBackground();
                map.clear();
                Toast.makeText(getBaseContext(), "All markers removed", Toast.LENGTH_LONG).show();

            }
        });
    }

    public void onClick_CS(View v){
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_CS, 16);
        map.animateCamera(update);
    }

    public void onClick_Univ(View v){
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 14);
        map.animateCamera(update);
    }

    public void onClick_City(View v){
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 10);
        map.animateCamera(update);
    }

    private class LocationInsertTask extends AsyncTask<ContentValues, Void, Void> {

        @Override
        protected Void doInBackground(ContentValues... contentValues) {
            getContentResolver().insert(LocationProvider.CONTENT_URI, contentValues[0]);
            return null;
        }
    }

    private class LocationDeleteTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            getContentResolver().delete(LocationProvider.CONTENT_URI, null, null);
            db.deleteAll();
            return null;
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Loader<Cursor> c = null;
        if(id == 0){
            c = new androidx.loader.content.CursorLoader(this, LocationProvider.CONTENT_URI,
                    mColumnProjection, null, null, null);
        }
        return c;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int locationCount = 0;
        double lat = 0;
        double lng = 0;
        float zoom = 0;
        if(data != null) {
            locationCount = data.getCount();
            data.moveToFirst();
        }else{
            locationCount = 0;
        }
        for(int i = 0; i < locationCount; i++){
            lat = data.getDouble(0);
            lng = data.getDouble(1);
            zoom = data.getFloat(2);
            LatLng locatmark = new LatLng(lat, lng);
            map.addMarker(new MarkerOptions().position(locatmark).title("Mark" + lcount));
            lcount++;
            data.moveToNext();
        }
        if(locationCount > 0){
            LatLng locatmark = new LatLng(lat, lng);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(locatmark, zoom);
            map.animateCamera(update);
        }
        lcount = locationCount;
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}