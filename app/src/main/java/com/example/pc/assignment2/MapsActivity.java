package com.example.pc.assignment2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMyLocationClickListener {
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private final String WEB_SERVICE = "https://baf7ef0f.ngrok.io";

    private String username;
    private String password;
    private LocationManager locationManager;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        this.username = getIntent().getStringExtra("username");;
        this.password = getIntent().getStringExtra("password");;


        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *
     * sources:
     * https://developers.google.com/maps/documentation/android-api/location#runtime-permission
     * https://javapapers.com/android/android-show-current-location-on-map-using-google-maps-api/
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Permissions check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_FINE_LOCATION);
            return;
        }

        //mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        //mMap.setOnMyLocationClickListener(this);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);

        // Use last known location
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }

        // Request for location every 20 seconds
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mMap != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            long timestamp = System.currentTimeMillis();

            // location && date format
            Date date = new Date(timestamp);
            DateFormat format = new SimpleDateFormat("hh:mm a");
            format.setTimeZone(TimeZone.getDefault());
            String formatted = format.format(date);

            // marker info
            String title = username;
            String snippet =  "(" + String.format(Locale.US,"%.03f", latitude) + "," + String.format(Locale.US,"%.03f", longitude) + ")" + " " + formatted;

            // Add user marker
            LatLng latLng = new LatLng(latitude, longitude);
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .strokeColor(Color.BLUE)
                    .radius(1000));

            // Update to server && grab friends in 1k radius
            new updateLoc((float) latitude, (float)longitude, timestamp).execute();
            new getFriends().execute();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }


    private class updateLoc extends AsyncTask<Void, Void, Boolean>
    {
        String url = WEB_SERVICE + "/usermgmt/user/";
        float lat;
        float lgt;
        long timestamp;

        updateLoc(float lat, float lgt, long timestamp) {
            this.lat = lat;
            this.lgt = lgt;
            this.timestamp = timestamp;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here

            String query = "latitude=" + lat + "&longitude=" + lgt + "&username=" + username + "&timestamp=" + timestamp;
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, query);
            Request request = new Request.Builder()
                    .url(url)
                    .patch(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.body() == null) return false;
                String strResponse = response.body().string();
                final int code = response.code();

                Log.d("webtag", strResponse);

                if (code != 200) {
                    return false;
                }

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result == true) {
                Toast.makeText(getApplicationContext(), "Updated location.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to update location.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class getFriends extends AsyncTask<Void, Void, Boolean>
    {
        String url = WEB_SERVICE + "/usermgmt/radius/";
        JSONObject friends;

        @Override
        protected Boolean doInBackground(Void... params) {
            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here

            String query = "?username=" + username;
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url + query)
                    .get()
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.body() == null) return false;
                String strResponse = response.body().string();
                final int code = response.code();

                //Log.d("webtag", strResponse);

                if (code != 200) {
                    return false;
                } else {
                    try {
                        friends = new JSONObject(strResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result == true && friends != null) {
                //Toast.makeText(getApplicationContext(), "Got friends.", Toast.LENGTH_SHORT).show();
                Log.d("webtag", "Got friends.");

                if (friends.length() == 0) {
                    return;
                }

                // Friend markers
                try {
                    for(Iterator<String> friend = friends.keys(); friend.hasNext();) {
                        String username = friend.next();
                        JSONObject data = (JSONObject) friends.get(username);

                        // get values
                        double lat = (double) data.get("latitude");
                        double lgt = (double) data.get("longitude");
                        long timestamp = (long) data.get("timestamp");


                        // location && date format
                        Date date = new Date(timestamp);
                        DateFormat format = new SimpleDateFormat("hh:mm: a");
                        format.setTimeZone(TimeZone.getDefault());
                        String formatted = format.format(date);

                        // Marker info
                        String title = username;
                        String snippet =  "(" + String.format(Locale.US,"%.03f", lat) + "," + String.format(Locale.US,"%.03f", lgt) + ")" + " " + formatted;

                        // add marker
                        LatLng latLng = new LatLng(lat, lgt);
                        mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(title)
                                        .snippet(snippet));
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                        // Logging...
                        String msg = "username=" + username + "  lat=" + Double.toString(lat) + "  lgt=" + Double.toString(lgt) + "  timestamp=" + Long.toString(timestamp);
                        Log.d("webtag", msg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                } else {
                Toast.makeText(getApplicationContext(), "Failed to get friends.", Toast.LENGTH_SHORT).show();
            }

        }
    }



}
