package com.example.locationservice.service;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashSet;
import java.util.Set;

public class LocationService {

    private FusedLocationProviderClient locationClient;
    private Context context;
    private final int MY_PERMISSION_REQUEST_COARSE_LOCATION = 0;
    private final int MY_PERMISSION_REQUEST_FINE_LOCATION = 1;

    private Set<LastKnownLocationListener> lastKnownLocationListeners;

    public LocationService(Context context) {
        this.context = context;
        locationClient = LocationServices.getFusedLocationProviderClient(context);
        lastKnownLocationListeners = new HashSet<>();
    }

    public void addLastKnownLocationListener(LastKnownLocationListener listener){
        lastKnownLocationListeners.add(listener);
    }

    public void removeLastKnownLocaionListener(LastKnownLocationListener listener){
        lastKnownLocationListeners.remove(listener);
    }

    public void askForPermissions(Activity activity) {

        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    public void getLastKnownLocation(Activity activity) {
        if (checkPermissions()) {
            locationClient.getLastLocation().addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    for(LastKnownLocationListener listener: lastKnownLocationListeners){
                        listener.onLastKnownLocationReceived(location);
                    }
                }
            });
        }
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
