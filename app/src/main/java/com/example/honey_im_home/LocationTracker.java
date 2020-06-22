package com.example.honey_im_home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;


import androidx.core.app.ActivityCompat;


public class LocationTracker implements LocationListener {
    static final String STARTED = "com.example.im_home.started";
    static final String STOPPED = "com.example.im_home.stopped";
    static final String UPDATED = "com.example.im_home.updated";
    static final String LOCATION_INFO = "info";
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 00 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0; // 0 minute

    protected LocationListener locationListener;
    private LocationManager locationManager;
    private Context myContex;

    private LocationInfo myLocationData;
    boolean stop_tracking;

//    Animation rotateAnimation;

    LocationTracker(Context context) {
        myContex = context;
        myLocationData = new LocationInfo();
        stop_tracking = false;
    }


    public void startTracking() {

        if (ActivityCompat.checkSelfPermission(myContex,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(myContex, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.d ("this is a problem", "you didnt get premission");
            return;
        }
        locationManager = (LocationManager) myContex.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this, Looper.getMainLooper());

        startBroadcast(STARTED, null);
        stop_tracking = true;

    }


    public void stopTracking()
    {
        locationManager.removeUpdates(this);
        startBroadcast(STOPPED, null);
        stop_tracking = false;
    }

    public void startBroadcast (String state, LocationInfo info)
    {
        Intent intent = new Intent();
        intent.setAction(state);
        intent.putExtra(LOCATION_INFO, info);
        myContex.sendBroadcast(intent);
    }

    @Override
    public void onLocationChanged(Location location) {

        // i should check?
        if ((myLocationData.getLatitude() != location.getLatitude()) &&
                (myLocationData.getLongitude() != location.getLongitude()) ||
                (myLocationData.getAccuracy() != location.getAccuracy()))
        {
            myLocationData.newLocationInfo(location.getLatitude(), location.getLongitude(),
                    location.getAccuracy());

//            Log.d("what?????0", String.valueOf(location.getAccuracy()));
            startBroadcast(UPDATED, myLocationData);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public boolean trackingCheck()
    {
        return stop_tracking;
    }

    public LocationInfo getLocationInfo()
    {
        return myLocationData;
    }


}
