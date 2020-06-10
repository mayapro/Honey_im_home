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
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.instantapps.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

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
    private LocationCallback locationCallback;
    private FusedLocationProviderClient client;

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

        loadHomeLock();
        isItNight();

        set_home_loct.setVisibility(View.INVISIBLE);

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
                loadHomeLock();
                homeImg.setImageResource((R.drawable.open1));   // light up the house
            }
        });

        clear_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
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

//    // is it neccery?
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        client.removeLocationUpdates(locationCallback);
//    }

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

    private void loadHomeLock ()
    {
        sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
        String lat_sp = sp.getString("latitude", null);
        String long_sp = sp.getString("longitude", null);
        if (lat_sp != null && long_sp != null)
        {
            hone_Title.setText("home location");
            String temp = "<" + lat_sp + "," + long_sp + ">";
            home_location.setText(temp);

            clear_home.setVisibility(View.VISIBLE);
        }
        else
        {
            hone_Title.setText("");
            home_location.setText("");
        }
    }


    private void saveHome(String acc, final String lat, final String lon)
    {
        if (Double.parseDouble(acc) < 50 )
        {
            set_home_loct.setVisibility(View.VISIBLE);

            SharedPreferences sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.putString("latitude", lat);
            editor.putString("longitude", lon);
            editor.apply();

        }
        else
        {
            set_home_loct.setVisibility(View.INVISIBLE);
        }

    }


    private void locateFunc ()
    {
//        distanceBetween(double startLatitude, double startLongitude, double endLatitude, double endLongitude, float[] results)
        if (!stop_tracking){
            click_me.setText("stop tracking");
            stop_tracking = true;

            client = new FusedLocationProviderClient(this);
            locationRequest = new LocationRequest();
//            locationCallback = new LocationCallback();

            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setFastestInterval(2000);
            locationRequest.setInterval(4000);

            client.requestLocationUpdates(locationRequest, new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult)
                {
                    super.onLocationResult(locationResult);
                    rotateAnimation();      // rotate the earth img

                    client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            if (location != null && stop_tracking)
                            {
                                String lat = String.valueOf(location.getLatitude());
                                String lon = String.valueOf(location.getLongitude());
//                                Log.d("location accu  ", String.valueOf(location.getAccuracy()));
                                String acc = String.valueOf(location.getAccuracy());
                                location_1.setText(lat);
                                location_2.setText(lon);
                                location_3.setText(acc);

                                saveHome(acc,lat,lon);
                            }
                        }
                    });
                }
            }, getMainLooper());

        }
        else if (stop_tracking)
        {
//            client.removeLocationUpdates(locationCallback);

            click_me.setText("start_tracking_location");
            location_1.setText("stopped tracking");
            location_2.setText(" ");
            location_3.setText(" ");
            stop_tracking = false;
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
        }
    }
}
