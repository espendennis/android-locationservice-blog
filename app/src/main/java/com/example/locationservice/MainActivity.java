package com.example.locationservice;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.locationservice.service.LastKnownLocationListener;
import com.example.locationservice.service.LocationService;
import com.example.locationservice.service.LocationUpdateListener;

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
