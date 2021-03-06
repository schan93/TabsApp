package com.tabs.location;

import android.location.Location;

/**
 * Created by schan on 9/19/16.
 */
public interface LocationUpdateListener {

    /**
     * Called immediately the service starts if the service can obtain location
     */
    void canReceiveLocationUpdates();

    /**
     * Called immediately the service tries to start if it cannot obtain location - eg the user has disabled wireless and
     */
    void cannotReceiveLocationUpdates();

    /**
     * Called whenever the location has changed (at least non-trivially)
     * @param location
     */
    void updateLocation(Location location);

    /**
     * Called when GoogleLocationServices detects that the device has moved to a new location.
     * @param localityName The name of the locality (somewhere below street but above area).
     */
    void updateLocationName(String localityName, Location location);
}