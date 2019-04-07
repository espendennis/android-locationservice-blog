package com.example.locationservice;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.locationservice.service.LastKnownLocationListener;
import com.example.locationservice.service.LocationService;

public class MainActivity extends AppCompatActivity implements LastKnownLocationListener {

    private LocationService locationService;
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
    protected void onPostResume() {
        super.onPostResume();
        locationService.addLastKnownLocationListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationService.removeLastKnownLocaionListener(this);
    }

    @Override
    public void onLastKnownLocationReceived(Location location) {
        lastKnownLocationView.setText(String.format("Latitude: %f, Longitude: %f",
                location.getLatitude(),
                location.getLongitude()));
    }
}
