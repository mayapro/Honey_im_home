package com.example.honey_im_home;


import android.Manifest;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class LocalSendSmsBroadcastReceiver extends BroadcastReceiver {
    static final String PHONE = "PHONE";
    static final String CONTENT = "CONTENT";

    private String myPhone;
    private String msgContent;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED)
        {
            Log.d("error", " you don't have sms permission");
            return;
        }

        myPhone = intent.getStringExtra("PHONE");
        msgContent = intent.getStringExtra("CONTENT");

        if (myPhone == null || msgContent == null || myPhone.equals("") || msgContent.equals(""))
        {
            Log.d("error", " you don't have phone number available or message content");
            return;
        }

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(myPhone, null, msgContent, null, null);

        notificationSend(context);

    }


    public void notificationSend(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//        notificationManager.cancel(1);

        Notification notification = new NotificationCompat.Builder(context, AppSmsNotification.CHANNEL_ID)
                .setContentTitle("Sending SMS")
                .setContentText(String.format("Sending SMS to %s: %s", myPhone, msgContent))
                .setContentInfo("Sending SMS")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .build();
        notificationManager.notify(1, notification);
    }
}

