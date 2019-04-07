### Using Android Location in a real world app

Most tutorials show you how to use Location in Android and pack all of the code just into the Activity. If your app has
more than activity you really should put this code into a service which handles it.

####Setup

Lets start with creating a new empty Project with an empty activity

2 Screenshots

We run it once to be sure it works

Screenshot

We will need add permission to access Location to our manifest

gist
manifest
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.locationservice">

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

We will start with getting the last known location. For getting the users location we will use the FusedLocationProviderClient.
We will need to add the dependency

    implementation 'com.google.android.gms:play-services-location:16.0.0'

to our build.gradle and sync it

Then we instantiate it in our LocationService. For doing this we need to pass a Context. We could also pass an Activity
 but we may not have an Activity available every time we want to use this service.

gist LocationService

```
package com.example.locationservice.service;
   
   import android.content.Context;
   
   import com.google.android.gms.location.FusedLocationProviderClient;
   import com.google.android.gms.location.LocationServices;
   
   public class LocationService {
   
       private FusedLocationProviderClient locationClient;
       private Context context
   
       public LocationService(Context context) {
           this.context = context;
           locationClient = LocationServices.getFusedLocationProviderClient(context);
       }
       
   }
```

Before we can acces LocationData we have to ask the user for permission to do so at runtime. Because we decided to pass 
a context into the constructor we now have to pass the activity to this method.

gist LocationService
```
    private final int MY_PERMISSION_REQUEST_COARSE_LOCATION = 0;
    private final int MY_PERMISSION_REQUEST_FINE_LOCATION = 1;

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
```

We pack this neatly into a method we can call whenever we want. According to google docs it is best practice to ask the 
user for the permission whenever you first need it. I on the other hand like to ask the user when he starts the app for 
the first time. I use Intros in my apps and it kinda feels natural to ask the user after the intro.

gist MainActivity
```
package com.example.locationservice;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.locationservice.service.LocationService;

public class MainActivity extends AppCompatActivity {

    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationService = new LocationService(this);
        locationService.askForPermissions(this);
    }
}
```

If we now start our app again we are asked if we want to allow our app to access the device's location.
Screenshot

Each time before before we access location we now have to check if the user has actually given the permission. He could 
have declined after all. To do so we can add a method to our LocationService:

gist LocationService
```
    private boolean checkPermissions() {
           return ActivityCompat.checkSelfPermission(context,
                   Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                   && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
       }
```

Now we are finally ready to get the last known location

gist LocationService
```   
    public void getLastKnownLocation(Activity activity) {
        if (checkPermissions()) {
            locationClient.getLastLocation().addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    System.out.println(String.format("Received last known location: latitude: %d, longitude: %d",
                            location.getLatitude(),
                            location.getLongitude()));
                }
            });
        }
    }
```

Android Studio will complain about the call to locationClient.getLastLocation() because of a missing 
permission check. We do check the permissions in checkPermissions(). Unfortunately Android Studio does 
not recognise that. You can either not extract the code to a method and just leave it in the if() or just 
suppress the warning.

We call our new method in the onCreate Method of the MainAcivity and start the app again. 

gist MainActivity
```
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationService = new LocationService(this);
        locationService.askForPermissions(this);
        locationService.getLastKnownLocation(this);
    }
```

After starting the app we can see the location in the logs:

Screenshot

Now how do we pass the result back to the activity? There are several ways to this. We will do this via registering 
listeners.

We add an interface:
gist LastKnownLocationListener
```
package com.example.locationservice.service;

import android.location.Location;

public interface LastKnownLocationListener {
    void onLastKnownLocationReceived(Location location);
}
```
We adjust!!! our LocationService:

gist LocationService

```
    private Set<LastKnownLocationListener> lastKnownLocationListeners;
    
    public LocationService(Context context) {
        this.context = context;
        locationClient = LocationServices.getFusedLocationProviderClient(context);
        lastKnownLocationListeners = new HashSet<>();
    }
     
    public void addLastKnownLocationListener(LastKnownLocationListener listener){
        lastKnownLocationListeners.add(listener);
    }
    
    public void removeLastKnownLocationListener(LastKnownLocationListener listener){
        lastKnownLocationListeners.remove(listener);
    }
```

We implement the interface in our MainActivity and subscribe/ unsubscribe in the onPostResume and onPause methods

gist LocationService
```
package com.example.locationservice;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.locationservice.service.LastKnownLocationListener;
import com.example.locationservice.service.LocationService;

public class MainActivity extends AppCompatActivity implements LastKnownLocationListener {

    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationService = new LocationService(this);
        locationService.askForPermissions(this);
        locationService.getLastKnownLocation(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        locationService.addLastKnownLocationListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationService.removeLastKnownLocationListener(this);
    }

    @Override
    public void onLastKnownLocationReceived(Location location) {
        System.out.println(String.format("Received last knwon location: latitude: %d, longitude: %d",
                location.getLatitude(),
                location.getLongitude()));
    }
}

```

And finally we call our listeners when we get the last known location

gist LocationService
```
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
```

After starting the app, we can see the location in the logs like before

To make it a little bit nicer we can display it in the app.

gist activity_main.xml
```
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/label"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="64dp"
        android:textSize="24sp"
        android:text="Last Known Location"
        app:layout_constraintBottom_toTopOf="@id/lastKnownLocation"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
    <TextView
        android:id="@+id/lastKnownLocation"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="18sp"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@id/label" />

</android.support.constraint.ConstraintLayout>
```

gist MainActivity
```
    private TextView lastKnownLocationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastKnownLocationView = findViewById(R.id.lastKnownLocation);

        locationService = new LocationService(this);
        locationService.askForPermissions(this);
        locationService.getLastKnownLocation(this);
    }
    
    
    @Override
    public void onLastKnownLocationReceived(Location location) {
        lastKnownLocationView.setText(String.format("Latitude: %f, Longitude: %f",
                location.getLatitude(),
                location.getLongitude()));
    }
```

Now we have an initial location of the device. If we want to update the location as it changes we can subscribe to 
location updates. First we have to create a LocationRequest:

```
    public LocationRequest getLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
```

With setInterval we specify in which interval in milliseconds the app would prefer to get updates. It is not guaranteed 
that we will receive updates in this specific interval though. The system merges different LocationRequest from different 
apps and decides the interval it thinks fits best and updates all apps with this interval. It can be faster, slower 
than we specified or even no updates if the device cant determine a new Location. Because of this have use 
setFastestInterval to tell the system which is the fastest interval of updates that our app can handle. If for example 
the system decides to update every second because another app needs it and our takes 2 seconds for some calculations 
and rendering the results, the updates would crash our app. If we set the fastest intervall to 5 seconds the system 
will just skip updating the app to match the 5 seconds interval.
For the priority there are 4 choices:
- PRIORITY_NO_POWER: The app won't trigger any updates but will receive updates triggered by other apps
- PRIORITY_LOW_POWER: This is used for city level precision. The accuracy is about 10km.
- PRIORITY_HIGH_ACCURACY: The best the device can do, but it consumes a lot of battery
- PRIORITY_BALANCED_POWER_ACCURACY: This priority won't use GPS. Its about 100m accurate and is a good balance between 
accuracy and power consumption.

You should choose the least accurate Level you can get away with to save power.

We need one more thing. When calling the locationClient's requestLocationUpdates Method we have to pass a 
LocationCallBack. We will instantiate it in the LocationServices constructor and save it to a local variable. We need 
to pass the exact same object to the LocationClients removeLocationUpdates Method to stop getting location updates.

```
    private LocationCallback locationCallback;

    public LocationService(Context context) {
        this.context = context;
        locationClient = LocationServices.getFusedLocationProviderClient(context);
        lastKnownLocationListeners = new HashSet<>();


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    System.out.println(String.format("Received location update: latitude: %f, longitude: %f, time: %d",
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getTime()));
                }
            };
        };
    }
```

Now we finally have everything we need to request location updates:

```
    public void getLocationUpdates() {
        if (checkPermissions()) {
            locationClient.requestLocationUpdates(getLocationRequest(), locationCallback, null);
        }
    }
```

When we are done getting location updates we can stop by calling:

```
    public void stopLocationUpdates(){
        locationClient.removeLocationUpdates(locationCallback);
    }
```

Now we only have to call these methods from the MainActivity:

```
    @Override
    protected void onPostResume() {
        super.onPostResume();
        locationService.addLastKnownLocationListener(this);
        locationService.getLastKnownLocation(this);
        locationService.getLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationService.removeLastKnownLocationListener(this);
        locationService.stopLocationUpdates();
    }
```

When we start the app we can see the updates in the logs:

Screenshot

In my case its exactly every 10 seconds because there are no other apps running in my emulator.

To finish the first part of this tutorial we will show the updated location to the user. Once again we create an 
interface:

```
package com.example.locationservice.service;

import android.location.Location;

public interface LocationUpdateListener {
    void onLocationUpdate(Location location);
}
```
We add the listeners to the LocationService and call them in the locationCallback:

```
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
```

We adjust the MainActivity and are ready to go

```
public class MainActivity extends AppCompatActivity implements LastKnownLocationListener, LocationUpdateListener {

    private LocationService locationService;
    private TextView lastKnownLocationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastKnownLocationView = findViewById(R.id.lastKnownLocation);

        locationService = new LocationService(this);
        locationService.askForPermissions(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        locationService.addLastKnownLocationListener(this);
        locationService.getLastKnownLocation(this);
        locationService.addLocationUpdateListener(this);
        locationService.getLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationService.removeLastKnownLocationListener(this);
        locationService.stopLocationUpdates();
        locationService.removeLocationUpdateListener(this);
    }

    @Override
    public void onLastKnownLocationReceived(Location location) {
        setLocationToUi(location);
    }

    private void setLocationToUi(Location location) {
        lastKnownLocationView.setText(String.format("Latitude: %f, Longitude: %f",
                location.getLatitude(),
                location.getLongitude()));
    }

    @Override
    public void onLocationUpdate(Location location) {
        setLocationToUi(location);
    }
}
```

Note that you can only see changes when the device moves and you get another output on the ui
That concludes part 1. In part 2 we will learn how check the devices settings and how to add geofences

https://developer.android.com/training/location/change-location-settings.html#java