package com.example.vikashkumarsharma.eagleeye;

/**
 * Created by Vikash Kumar Sharma on 30-10-2017.
 */

public class LocationDetails {

    private double mLatitudte;
    private double mLongitude;

    public LocationDetails() {
    }

    public LocationDetails(double mLatitudte, double mLongitude) {
        this.mLatitudte = mLatitudte;
        this.mLongitude = mLongitude;
    }

    public double getmLatitudte() {
        return mLatitudte;
    }

    public void setmLatitudte(double mLatitudte) {
        this.mLatitudte = mLatitudte;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }
}
