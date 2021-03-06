package com.example.honey_im_home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.app.ActivityCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import android.Manifest;

import android.annotation.SuppressLint;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 1;
    public static final int SMS_PERMISSION_CODE = 2;
    public static final String SEND_SMS = "POST_PC.ACTION_SEND_SMS";
    public static final String MSG_SMS = "Honey I'm Sending a Test Message!";

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
    Button set_phone_number;
    Button test_sms;

    String newPhoneNumber;

    boolean stop_tracking;

    private LocationInfo myLocationData;
    private LocationInfo myHomeData;
    private LocationTracker locationTracker;
    private MyBroadcastReciver myReceiver;

    private SharedPreferences sp;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myLocationData = new LocationInfo();
        myHomeData = new LocationInfo();
        locationTracker = new LocationTracker(this);
        startDisplay();
        isItNight();

        if (myLocationData != null)
        {
            loadHomeLock();
        }

        createButtons();
        set_home_loct.setVisibility(View.INVISIBLE);

        if (!newPhoneNumber.equals(""))
        {
            test_sms.setVisibility(View.VISIBLE);
        }
        else
        {
            test_sms.setVisibility(View.INVISIBLE);
        }

        if (savedInstanceState != null)
        {
            loadLocationLock();
            uiUpdate();
            mySetLocation();
        }

        setMyReceiver();
        repeatedWork();
    }


    public void repeatedWork()
    {
        PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(ClassExtendingWorker.class, 15,
                TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueue(work);
    }


    private void startDisplay()
    {
        click_me = findViewById(R.id.click_me);
        clear_home = findViewById(R.id.clear_home);
        set_home_loct = findViewById(R.id.set_location_at_home);
        set_phone_number = findViewById(R.id.set_sms_phone_number);
        test_sms = findViewById(R.id.test_sms);

        earthImg = findViewById(R.id.earthImg);
        homeImg = findViewById(R.id.closeH);
        location_1 = findViewById(R.id.locationLatitude);
        location_2 = findViewById(R.id.locationLongitude);
        location_3 = findViewById(R.id.locationAccuracy);

        hone_Title = findViewById(R.id.home_location);
        home_location = findViewById(R.id.home_lat_and_long);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "location Permission GRANTED", Toast.LENGTH_SHORT).show();
                locationTracker.startTracking();
            } else {
                Toast.makeText(this, "location Permission DENIED", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == SMS_PERMISSION_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "SNS Permission GRANTED", Toast.LENGTH_SHORT).show();
                updatePhoneNumSP();
            }
            else
            {
                Toast.makeText(this, "i need your number so i could operate", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationTracker!= null)
        {
            locationTracker.stopTracking();
        }
        this.unregisterReceiver(myReceiver);
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
                uiUpdate();
            }
        });

        set_home_loct.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                saveHome();
                loadHomeLock();
                homeImg.setImageResource((R.drawable.open1));   // light up the house
            }
        });

        clear_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearhome();
            }
        });

        set_phone_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission
                        (MainActivity.this, Manifest.permission.SEND_SMS) !=
                        PermissionChecker.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
                }
                insertPhone();
            }
        });

        test_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMS();
            }
        });
    }



    public void insertPhone ()
    {
        final EditText taskEditText = new EditText(this);
        if (!newPhoneNumber.equals(""))
        {
            taskEditText.setText(newPhoneNumber);
        }
        taskEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("phone number")
                .setMessage("please insert your phone number now")
                .setView(taskEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newPhoneNumber = String.valueOf(taskEditText.getText());
                        //insert to sp:
                        updatePhoneNumSP();
                        if (!newPhoneNumber.equals(""))
                        {
                            test_sms.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            test_sms.setVisibility(View.INVISIBLE);
                        }
                    }
                })
                .create();
        dialog.show();
    }


    public void sendSMS()
    {
        Intent intent = new Intent();
        intent.putExtra(LocalSendSmsBroadcastReceiver.PHONE, newPhoneNumber);
        intent.putExtra(LocalSendSmsBroadcastReceiver.CONTENT, MSG_SMS);
        intent.setAction(SEND_SMS);
        sendBroadcast(intent);
    }

    public void uiUpdate()
    {
        if (!locationTracker.trackingCheck())
        {
            Log.d("what?????0", "why");
            locationTracker.startTracking();
            click_me.setText("stop tracking");
            location_1.setText(" ");
        }
        else
        {
            Log.d("what?????0", "stopped!!!!!");
            locationTracker.stopTracking();

            click_me.setText("start_tracking_location");
            set_home_loct.setVisibility(View.INVISIBLE);
            location_1.setText("stopped tracking");
            location_2.setText(" ");
            location_3.setText(" ");
        }
    }


    public void setMyReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationTracker.STARTED);
        filter.addAction(LocationTracker.STOPPED);
        filter.addAction(LocationTracker.UPDATED);

        myReceiver = new MyBroadcastReciver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (Objects.requireNonNull(intent.getAction())) {
                    case LocationTracker.STARTED:
                        break;
                    case LocationTracker.STOPPED:
                        break;
                    case LocationTracker.UPDATED:
                        rotateAnimation();
                        myLocationData = (LocationInfo) intent.getSerializableExtra(LocationTracker.LOCATION_INFO);
                        regularLocation();
                        assert myLocationData != null;
                        mySetLocation();
                        if (myLocationData.getAccuracy() < 50 )
                            {
                                set_home_loct.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                set_home_loct.setVisibility(View.INVISIBLE);
                            }
                        break;
                }
            }
        };
        this.registerReceiver(myReceiver, filter);
    }



    public void mySetLocation()
    {
        String lat_sp = String.valueOf(myLocationData.getLatitude());
        String long_sp = String.valueOf(myLocationData.getLongitude());
        String accuracy = String.valueOf(myLocationData.getAccuracy());

        location_1.setText(lat_sp);
        location_2.setText(long_sp);
        location_3.setText(accuracy);
    }

    private void saveHome()
    {
        myHomeData = myLocationData;
        sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();

        Gson gson = new Gson();
        String json = gson.toJson(myHomeData);
        editor.putString("home location data", json);
        editor.apply();
    }


    private void clearhome()
    {
        sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();

        hone_Title.setText("");
        home_location.setText("");

        clear_home.setVisibility(View.INVISIBLE);
        homeImg.setImageResource((R.drawable.close1));
    }


    private void regularLocation()
    {
        sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();

        Gson gson = new Gson();
        String json = gson.toJson(myLocationData);
        editor.putString("location data", json);
        editor.apply();
    }



    private void loadHomeLock ()
    {
        sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        // getting the phone number from sp
        newPhoneNumber = sp.getString("phone number", "");

        Gson gson = new Gson();
        String json = sp.getString("home location data", "");
        Type type = new TypeToken<LocationInfo>() {}.getType();
        LocationInfo tempLoc = (LocationInfo) gson.fromJson(json, type);

        if (tempLoc != null)
        {
            myHomeData = tempLoc;
            Log.d("why", String.valueOf(myLocationData));

            String lat_sp = String.valueOf(myHomeData.getLatitude());
            String long_sp = String.valueOf(myHomeData.getLongitude());
            String accuracy = String.valueOf(myHomeData.getAccuracy());

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


    private void loadLocationLock ()
    {
        sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        Gson gson = new Gson();
        String json = sp.getString("location data", "");
        Type type = new TypeToken<LocationInfo>() {}.getType();
        LocationInfo tempLoc = (LocationInfo) gson.fromJson(json, type);


        if (tempLoc != null)
        {
            myLocationData = tempLoc;
            mySetLocation();
        }
    }


    private void updatePhoneNumSP()
    {
        sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("phone number", newPhoneNumber);
        editor.apply();
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

            set_phone_number.setTextColor(Color.WHITE);
            test_sms.setTextColor(Color.WHITE);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }
}
