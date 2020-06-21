package com.example.honey_im_home;

import java.io.Serializable;


public class LocationInfo implements Serializable
{
   // latitude, langitude and accuracy.

    private double latitude;
    private double langitude;
    private float accuracy;



    public void newLocationInfo (double lat, double lang, float acc)
    {
        latitude = lat;
        langitude = lang;
        accuracy = acc;
    }

     public double getLatitude()
     {
         return latitude;
     }

    public double getLangitude() {
        return langitude;
    }

    public float getAccuracy() {
        return accuracy;
    }
}
