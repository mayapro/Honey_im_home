package com.example.honey_im_home;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class ClassExtendingWorker extends Worker {

    private Context myContext;
    private LocationTracker currentLocation;
    private SharedPreferences sp;
    private String newPhoneNumber;
    private LocationInfo previous;
    private LocationInfo myHomeData;
    private LocationInfo currentLocationData;


    public ClassExtendingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        myContext = context;
        currentLocation = new LocationTracker(myContext);
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

        if (myHomeData == null || newPhoneNumber.equals(""))
        {
            return Result.success();
        }

        currentLocation.startTracking();
        currentLocationData =  currentLocation.getLocationInfo();

        //if "previous" is null (no previous location was stored) or "previous" is close enough to
        // "current" - less then 50 meters - nothing to do.
        //    store "current" in the SP value for last location stored and return RESULT-SUCESS.

        return Result.success();
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
