package com.example.honey_im_home;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.IntentFilter;
import android.os.Build;

public class AppSmsNotification extends Application {
    public static final String CHANNEL_ID = "myChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.SEND_SMS);
        registerReceiver(new LocalSendSmsBroadcastReceiver(), filter);
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "myChannel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
