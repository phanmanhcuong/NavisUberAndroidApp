package com.example.admin.navisuber;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Admin on 3/18/2018.
 */

public class RouteWrapper {
    public ArrayList<LatLng> latLngBound;
    public ArrayList<LatLng> arrayListPolyline;
    public Direction.Leg.Distance distance;
    public Direction.Leg.Duration duration;

    public RouteWrapper(ArrayList<LatLng> latLngBound, ArrayList<LatLng> arrayListLatLng, Direction.Leg.Distance distance, Direction.Leg.Duration duration) {
        this.latLngBound = latLngBound;
        this.arrayListPolyline = arrayListLatLng;
        this.distance = distance;
        this.duration = duration;
    }

    public ArrayList<LatLng> getLatLngBound() {
        return latLngBound;
    }

    public ArrayList<LatLng> getArrayListPolyline() {
        return arrayListPolyline;
    }

    public Direction.Leg.Distance getDistance() {
        return distance;
    }

    public Direction.Leg.Duration getDuration() {
        return duration;
    }
}
