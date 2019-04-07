package com.example.locationservice.service;

import android.location.Location;

public interface LastKnownLocationListener {
    void onLastKnownLocationReceived(Location location);
}
