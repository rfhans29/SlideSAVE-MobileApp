package com.example.slidesave;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Authority {
    public String name;
    public String type;
    public LatLng location;
    public float distance;
    public String placeId;
    public String phoneNumber;
    public Marker marker;

    public Authority(String name,String type,LatLng location,float distance,String placeId) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.distance = distance;
        this.placeId = placeId;
        this.phoneNumber = "";
    }
    public int getIconResource() {
        switch (type.toLowerCase()) {
            case "police":
                return R.drawable.ic_police;
            case "fire_station":
                return R.drawable.ic_fire_station;
            case "hospital":
                return R.drawable.ic_hospital;
            default:
                return R.drawable.ic_location;
        }
    }
}