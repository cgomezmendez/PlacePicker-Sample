package me.cristiangomez.placefinder.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cristianjgomez on 9/4/15.
 */
public class Geometry {
    @SerializedName("location")
    private Location mLocation;

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    @Override
    public String toString() {
        return "Geometry{" +
                "location=" + mLocation +
                '}';
    }
}
