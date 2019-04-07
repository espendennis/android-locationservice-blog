//package com.example.locationservice;
//
//import android.Manifest;
//import android.app.Activity;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.Geofence;
//import com.google.android.gms.location.GeofencingClient;
//import com.google.android.gms.location.GeofencingRequest;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Set;
//
///**
// * Created by Dennis on 01.03.2017.
// */
//
//public class LocationService2 implements LocationListener, OnCompleteListener<Void> {
//
//    private LocationManager locationManager;
//    private Context context;
//    private final int MY_PERMISSION_REQUEST_COARSE_LOCATION = 0;
//    private final int MY_PERMISSION_REQUEST_FINE_LOCATION = 1;
//    private Set<LocationServiceUpdateSubscriber> updateSubscribers;
//    private Set<LocationServiceLastKnownLocationSubscriber> lastKnownLocationSubscribers;
//    private GeofencingClient geofencingClient;
//    private FusedLocationProviderClient mFusedLocationClient;
//    private LocationCallback locationCallback;
//
//    public LocationService2(Context context) {
//        this.context = context;
//        updateSubscribers = new HashSet<>();
//        lastKnownLocationSubscribers = new HashSet<>();
//        geofencingClient = LocationServices.getGeofencingClient(context);
//
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
//
//        locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//                    for(LocationServiceUpdateSubscriber subscriber : updateSubscribers){
//                        if(subscriber != null){
//                            subscriber.updateLocation(location);
//                        }
//                    }
//                }
//            };
//        };
//    }
//
//    public void getLastKnownLocation(Activity activity) {
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            mFusedLocationClient.getLastLocation().addOnSuccessListener(activity, new OnSuccessListener<Location>() {
//                @Override
//                public void onSuccess(Location location) {
//                    if(location != null){
//                        for(LocationServiceLastKnownLocationSubscriber subscriber: lastKnownLocationSubscribers){
//                            subscriber.onLastKnownLocationReceived(location);
//                        }
//                    }
//                }
//            });
//        }
//    }
//
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSION_REQUEST_COARSE_LOCATION: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    System.out.println("COARSE_LOCATION_PERMISSION granted");
//                } else {
//
//                }
//                return;
//            }
//            case MY_PERMISSION_REQUEST_FINE_LOCATION: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    System.out.println("FINE_LOCATION_PERMISSION granted");
//                } else {
//                }
//                return;
//            }
//        }
//    }
//
//    public void startTrackingLocation() {
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            LocationRequest locationRequest = new LocationRequest();
//            locationRequest.setInterval(10000);
//            locationRequest.setFastestInterval(5000);
//            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//            mFusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, null);
//        }
//    }
//
//    public void stopTrackingLocation(){
//        mFusedLocationClient.removeLocationUpdates(locationCallback);
//    }
//
//
//    public void checkForPermissions(Activity activity) {
//
//            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(activity,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        MY_PERMISSION_REQUEST_FINE_LOCATION);
//            }
//            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(activity,
//                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                        MY_PERMISSION_REQUEST_COARSE_LOCATION);
//            }
//    }
//
//    public boolean addGeofence(String title, double latitude, double longitude, float radius, boolean isReactivation) {
//        removeGeofence(title);
//        int geofencingRequestTrigger;
//        int geofenceTransition;
//
//        if(!isReactivation){
//            geofencingRequestTrigger = GeofencingRequest.INITIAL_TRIGGER_ENTER;
//            geofenceTransition = Geofence.GEOFENCE_TRANSITION_ENTER;
//        } else {
//            geofencingRequestTrigger = GeofencingRequest.INITIAL_TRIGGER_EXIT;
//            geofenceTransition = Geofence.GEOFENCE_TRANSITION_EXIT;
//        }
//
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//            Geofence geofence = new Geofence.Builder().setRequestId(title)
//                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
//                    .setCircularRegion(latitude,
//                            longitude,
//                            radius)
//                    .setTransitionTypes(geofenceTransition)
//                    .build();
//            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
//            GeofencingRequest request = builder.setInitialTrigger(geofencingRequestTrigger)
//                    .addGeofence(geofence)
//                    .build();
//            geofencingClient.addGeofences(request,getGeofencePendingIntent()).addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                        }
//                    })
//                    .addOnFailureListener( new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                        }
//                    });
//            System.out.println("Geofence " + title + " added");
//            return true;
//        }
//        return false;
//    }
//
//    public boolean removeGeofence(String title) {
//        List<String> geofencesToRemove = new LinkedList<>();
//        geofencesToRemove.add(title);
//
//        geofencingClient.removeGeofences(geofencesToRemove);
//        System.out.println("Geofence " + title + " removed");
//        return true;
//    }
//
//
//    @Override
//    public void onLocationChanged(Location location) {
//        for(LocationServiceUpdateSubscriber subscriber : updateSubscribers){
//            if(subscriber != null){
//                subscriber.updateLocation(location);
//            }
//        }
//    }
//
//    private void initialiseGeofences(List<TrackedLocation> locations) {
//        for (TrackedLocation location : locations) {
//            if (location.getIsTracked()) {
//                addGeofence(
//                        location.getIdForGeofence(),
//                        location.getLatitude(),
//                        location.getLongitude(),
//                        location.getDistanceForFence(),
//                        false);
//            }
//        }
//    }
//
//    private PendingIntent getGeofencePendingIntent() {
//        Intent intent = new Intent(context, GeofenceReceiverIntentService.class);
//        PendingIntent mGeofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.
//                FLAG_UPDATE_CURRENT);
//        return mGeofencePendingIntent;
//    }
//
//    public void addLocationUpdateSubscriber(LocationServiceUpdateSubscriber subscriber){
//        updateSubscribers.add(subscriber);
//    }
//
//    public void removeLocationUpdateSubscriber(LocationServiceUpdateSubscriber subscriber){
//        updateSubscribers.remove(subscriber);
//    }
//
//    public void addLastKnownLocationSubscriber(LocationServiceLastKnownLocationSubscriber subscriber){
//        lastKnownLocationSubscribers.add(subscriber);
//    }
//
//    public void removeLastKnownLocationSubscriber(LocationServiceLastKnownLocationSubscriber subscriber){
//        lastKnownLocationSubscribers.remove(subscriber);
//    }
//
//    @Override
//    public void onComplete(@NonNull Task<Void> task) {
//
//    }
//}
