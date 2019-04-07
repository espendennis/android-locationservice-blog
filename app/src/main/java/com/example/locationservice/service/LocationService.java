package com.example.locationservice.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
    private Set<LocationUpdateListener> locationUpdateListeners;

    private LocationCallback locationCallback;

    public LocationService(Context context) {
        this.context = context;
        locationClient = LocationServices.getFusedLocationProviderClient(context);
        lastKnownLocationListeners = new HashSet<>();
        locationUpdateListeners = new HashSet<>();


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    for(LocationUpdateListener listener: locationUpdateListeners){
                        listener.onLocationUpdate(location);
                    }
                }
            };
        };
    }

    public void addLocationUpdateListener(LocationUpdateListener listener){
        locationUpdateListeners.add(listener);
    }

    public void removeLocationUpdateListener(LocationUpdateListener listener){
        locationUpdateListeners.remove(listener);
    }

    public void addLastKnownLocationListener(LastKnownLocationListener listener){
        lastKnownLocationListeners.add(listener);
    }

    public void removeLastKnownLocationListener(LastKnownLocationListener listener){
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

    @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
    public void getLocationUpdates() {
        if (checkPermissions()) {
            locationClient.requestLocationUpdates(getLocationRequest(), locationCallback, null);
        }
    }

    public LocationRequest getLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public void stopLocationUpdates(){
        locationClient.removeLocationUpdates(locationCallback);
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
