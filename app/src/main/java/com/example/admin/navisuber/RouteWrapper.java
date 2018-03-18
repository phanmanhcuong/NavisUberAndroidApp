package com.example.admin.navisuber;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Admin on 3/18/2018.
 */

public class RouteWrapper {
    public ArrayList<LatLng> arrayListLatLng;
    public Direction.Leg.Distance distance;
    public Direction.Leg.Duration duration;

    public RouteWrapper(ArrayList<LatLng> arrayListLatLng, Direction.Leg.Distance distance, Direction.Leg.Duration duration) {
        this.arrayListLatLng = arrayListLatLng;
        this.distance = distance;
        this.duration = duration;
    }

    public ArrayList<LatLng> getArrayListLatLng() {
        return arrayListLatLng;
    }

    public Direction.Leg.Distance getDistance() {
        return distance;
    }

    public Direction.Leg.Duration getDuration() {
        return duration;
    }
}
