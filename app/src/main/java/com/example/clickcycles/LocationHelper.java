package com.example.clickcycles;

public class LocationHelper {
    private double mLatitude;
    private double mLongitude;

    public LocationHelper(double latitude, double longitude){
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public double getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }
}
