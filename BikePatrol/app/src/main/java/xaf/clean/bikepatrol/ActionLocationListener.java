package xaf.clean.bikepatrol;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;

class ActionLocationListener implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Location location;

    ActionLocationListener() {

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // do nothing
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(getClass().getName(), String.valueOf(cause));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e(getClass().getName(), String.valueOf(result.getErrorMessage()));
    }

    public Location getLocation() {
        return location;
    }

}
