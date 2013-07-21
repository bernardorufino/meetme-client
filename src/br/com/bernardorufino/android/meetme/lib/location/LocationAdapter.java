package br.com.bernardorufino.android.meetme.lib.location;

import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;

public class LocationAdapter implements LocationClient.ConnectionCallbacks,
        LocationClient.OnConnectionFailedListener,
        LocationListener {

    public void onConnected(Bundle bundle) { /* Empty */ }
    public void onDisconnected() { /* Empty */ }
    public void onLocationChanged(Location location) { /* Empty */ }
    public void onConnectionFailed(ConnectionResult connectionResult) { /* Empty */ }

}
