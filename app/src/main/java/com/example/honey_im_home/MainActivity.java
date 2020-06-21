package com.example.honey_im_home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.app.ActivityCompat;


import android.Manifest;

import android.annotation.SuppressLint;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 1;
    Animation rotateAnimation;
    ImageView earthImg;
    ImageView homeImg;
    TextView location_1;
    TextView location_2;
    TextView location_3;
    TextView hone_Title;
    TextView home_location;

    Button click_me;
    Button clear_home;
    Button set_home_loct;

    boolean stop_tracking;
    private LocationRequest locationRequest;
    private Location myLocation;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient client;

    private LocationInfo myLocationData;


    private SharedPreferences sp;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        click_me = findViewById(R.id.click_me);
        clear_home = findViewById(R.id.clear_home);
        set_home_loct = findViewById(R.id.set_location_at_home);

        earthImg = findViewById(R.id.earthImg);
        homeImg = findViewById(R.id.closeH);
        location_1 = findViewById(R.id.locationLatitude);
        location_2 = findViewById(R.id.locationLongitude);
        location_3 = findViewById(R.id.locationAccuracy);

        hone_Title = findViewById(R.id.home_location);
        home_location = findViewById(R.id.home_lat_and_long);

        stop_tracking = false;

        myLocationData = new LocationInfo();

        if (myLocationData != null)
        {
            loadHomeLock();
        }

        Log.d("why", "lalalalaaaaaaaaa");

        isItNight();

        set_home_loct.setVisibility(View.INVISIBLE);

        createButtons();

        if (savedInstanceState != null)
        {
            stop_tracking = savedInstanceState.getBoolean("stop_tracking");
            myLocation = (Location) savedInstanceState.getParcelable("myLocation");

            stop_tracking = !stop_tracking;

            locateFunc ();

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                locateFunc(); // if we can locate - we start
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }




    private void createButtons ()
    {
        click_me.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (ContextCompat.checkSelfPermission
                        (MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PermissionChecker.PERMISSION_GRANTED)
                {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            STORAGE_PERMISSION_CODE);
                }
                locateFunc();  // if we can locate - we start
            }
        });

        set_home_loct.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                saveHome(myLocation);
                loadHomeLock();
                homeImg.setImageResource((R.drawable.open1));   // light up the house
            }
        });

        clear_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.apply();

                hone_Title.setText("");
                home_location.setText("");

                clear_home.setVisibility(View.INVISIBLE);

                homeImg.setImageResource((R.drawable.close1));   // light up the house
            }
        });
    }

    private void startClient()
    {
        client = new FusedLocationProviderClient(this);
        locationRequest = new LocationRequest();

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(2000);
        locationRequest.setInterval(4000);

        client.requestLocationUpdates(locationRequest, new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                super.onLocationResult(locationResult);
                rotateAnimation();      // rotate the earth img

                locationCallback = this;

                client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null)
                        {
                            myLocation = location;

                            myLocationData.newLocationInfo(myLocation.getLatitude(),   // todo
                                    myLocation.getLongitude(),myLocation.getAccuracy());

                            mySetLocation(myLocation);
                            if (Double.parseDouble(String.valueOf(location.getAccuracy())) < 50 )
                            {
                                set_home_loct.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                set_home_loct.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });

            }
        }, getMainLooper());
    }


    private void locateFunc ()
    {
        if (!stop_tracking){
            startClient();

            click_me.setText("stop tracking");
            stop_tracking = true;

        }
        else if (stop_tracking)
        {
            if (locationCallback != null)
            {
                client.removeLocationUpdates(locationCallback);
            }
            click_me.setText("start_tracking_location");
            set_home_loct.setVisibility(View.INVISIBLE);
            location_1.setText("stopped tracking");
            location_2.setText(" ");
            location_3.setText(" ");
            stop_tracking = false;
        }

    }


    private void mySetLocation(Location location)
    {
        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());
        String acc = String.valueOf(location.getAccuracy());
        location_1.setText(lat);
        location_2.setText(lon);
        location_3.setText(acc);
    }

    private void saveHome(Location location)
    {
        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());
        String acc = String.valueOf(location.getAccuracy());
        location_1.setText(lat);
        location_2.setText(lon);
        location_3.setText(acc);

        sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();

        Gson gson = new Gson();
        String json = gson.toJson(myLocationData);
        editor.putString("location data", json);
        editor.apply();

    }


    private void loadHomeLock ()
    {
        sp = getSharedPreferences("shared preferences", MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sp.getString("location data", "");
        Type type = new TypeToken<LocationInfo>() {}.getType();
        LocationInfo tempLoc = (LocationInfo) gson.fromJson(json, type);


        if (tempLoc != null)
        {
            myLocationData = tempLoc;
            Log.d("why", String.valueOf(myLocationData));

            String lat_sp = String.valueOf(myLocationData.getLatitude());
            String long_sp = String.valueOf(myLocationData.getLangitude());
            String accuracy = String.valueOf(myLocationData.getAccuracy());


            hone_Title.setText("home location");
            String temp = "<" + lat_sp + "," + long_sp + ">";
            home_location.setText(temp);

            homeImg.setImageResource((R.drawable.open1));   // light up the house
            clear_home.setVisibility(View.VISIBLE);
        }
        else
        {
            hone_Title.setText("");
            home_location.setText("");
            clear_home.setVisibility(View.INVISIBLE);
        }
    }


    public void rotateAnimation ()
    {
        rotateAnimation= AnimationUtils.loadAnimation(this,R.anim.rotate);
        earthImg.startAnimation(rotateAnimation);
    }

    public void isItNight()
    {
        Calendar cc = Calendar.getInstance();
        int mHour = cc.get(Calendar.HOUR_OF_DAY);

        if (mHour > 18 || mHour < 5)  // if its night time the background img should be night
        {
            View my_back = findViewById(R.id.background);
            my_back.setBackgroundResource(R.drawable.night1);

            click_me.setTextColor(Color.WHITE);
            clear_home.setTextColor(Color.WHITE);
            set_home_loct.setTextColor(Color.WHITE);

            location_1.setTextColor(Color.WHITE);
            location_2.setTextColor(Color.WHITE);
            location_3.setTextColor(Color.WHITE);

            hone_Title.setTextColor(Color.WHITE);
            home_location.setTextColor(Color.WHITE);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("myLocation", myLocation);
        outState.putBoolean("stop_tracking", stop_tracking);

    }
}
