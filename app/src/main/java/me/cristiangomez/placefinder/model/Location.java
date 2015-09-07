package me.cristiangomez.placefinder.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cristianjgomez on 9/4/15.
 */
public class Location {
    @SerializedName("lat")
    private double mLat;
    @SerializedName("lng")
    private double mLng;

    public double getLat() {
        return mLat;
    }

    public void setLat(double lat) {
        this.mLat = lat;
    }

    public double getLng() {
        return mLng;
    }

    public void setLng(double lng) {
        this.mLng = lng;
    }

    @Override
    public String toString() {
        return "Geometry{" +
                "lat=" + mLat +
                ", lng=" + mLng +
                '}';
    }
}
