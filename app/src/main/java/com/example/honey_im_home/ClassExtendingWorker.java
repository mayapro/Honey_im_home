package com.example.honey_im_home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;


public class ClassExtendingWorker extends Worker {
    public static final String SMS_HOME = "Honey I'm Home!";

    private Context myContext;
    private LocationTracker currentLocation;
    private Location Location;

    private SharedPreferences sp;
    private String newPhoneNumber;
    private LocationInfo previous;
    private LocationInfo myHomeData;
    private LocationInfo currentLocationData;
    private float[] result;


    public ClassExtendingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        myContext = context;
        currentLocation = new LocationTracker(context);
        result = new float[1];
    }

    @NonNull
    @Override
    public Result doWork() {
        if ((ContextCompat.checkSelfPermission
                (myContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PermissionChecker.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission
                (myContext, Manifest.permission.SEND_SMS) != PermissionChecker.PERMISSION_GRANTED))
        {
            return Result.success();
        }
        loadHomeLock();

        if (newPhoneNumber.equals("") || myHomeData == null)
        {
            Log.d("is it working?", "in here");
            return Result.success();
        }

        currentLocation.startTracking();
        currentLocationData =  currentLocation.getLocationInfo();

        while (currentLocationData.getAccuracy() > 50 || currentLocationData.getAccuracy() == 0) {
            currentLocationData = currentLocation.getLocationInfo();
        }
        currentLocation.stopTracking();

        if (previous != null)
        {
            double lat1 = previous.getLatitude();
            double long1 = previous.getLongitude();
            double lat2 = currentLocationData.getLatitude();
            double long2 = currentLocationData.getLongitude();

            Location.distanceBetween(lat1, long1, lat2, long2, result);
        }
        
        if (previous == null || result[0] < 50)
        {
            saveLocation();
            return Result.success();
        }

        double lat1 = myHomeData.getLatitude();
        double long1 = myHomeData.getLongitude();
        double lat2 = currentLocationData.getLatitude();
        double long2 = currentLocationData.getLongitude();

        Location.distanceBetween(lat1, long1, lat2, long2, result);

        if (result[0] < 50)
        {
            sendSMS();
        }
        saveLocation();
        return Result.success();
    }


    public void sendSMS()
    {
        Intent intent = new Intent();
        intent.putExtra(LocalSendSmsBroadcastReceiver.PHONE, newPhoneNumber);
        intent.putExtra(LocalSendSmsBroadcastReceiver.CONTENT, SMS_HOME);
        intent.setAction(MainActivity.SEND_SMS);
        myContext.sendBroadcast(intent);
    }


    private void saveLocation(){
        previous = currentLocationData;
        sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("phone number", newPhoneNumber);

        Gson gson = new Gson();
        String json = gson.toJson(myHomeData);
        editor.putString("home location data", json);
        if(myHomeData.getLongitude() == 0 && myHomeData.getAccuracy() == 0 &&
                myHomeData.getLatitude() == 0 )
        {
            myHomeData = null;
        }

        Gson gson2 = new Gson();
        String json2 = gson2.toJson(currentLocationData);
        editor.putString("previous location data", json2);
        if(currentLocationData.getLongitude() == 0 && currentLocationData.getAccuracy() == 0 &&
                currentLocationData.getLatitude() == 0 )
        {
            currentLocationData = null;
        }
        editor.apply();
    }


    private void loadHomeLock () {
        sp = PreferenceManager.getDefaultSharedPreferences(myContext);

        // getting the phone number from sp
        newPhoneNumber = sp.getString("phone number", "");

        Gson gson = new Gson();
        String json = sp.getString("home location data", "");
        Type type = new TypeToken<LocationInfo>() {}.getType();
        myHomeData = (LocationInfo) gson.fromJson(json, type);

        gson = new Gson();
        String json2 = sp.getString("previous location data", "");
        previous = (LocationInfo) gson.fromJson(json2, type);
    }
}
