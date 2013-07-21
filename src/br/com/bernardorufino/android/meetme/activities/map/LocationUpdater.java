package br.com.bernardorufino.android.meetme.activities.map;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import br.com.bernardorufino.android.meetme.activities.map.messaging.MapMessageQueue;
import br.com.bernardorufino.android.meetme.lib.location.LocationAdapter;
import br.com.bernardorufino.android.meetme.model.User;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static br.com.bernardorufino.android.meetme.activities.map.MapUpdater.*;

public class LocationUpdater {

    private static final long UPDATE_LOCATION_INTERVAL = UPDATE_MAP_INTERVAL * 2;
    private static final long MIN_UPDATE_LOCATION_INTERVAL = 500;

    private final User mUser;
    private final LocationRequest mLocationRequest;
    private final MapMessageQueue mMessageQueue;
    private final LocationClient mLocationClient;
    // TODO: Fix security flaw with public field, can set the value
    /* package private */ final AtomicReference<LatLng> atomicPosition;

    private final LocationAdapter mLocationHandler = new LocationAdapter() {

        public void onLocationChanged(Location location) {
            mMessageQueue.hideMessage("my_location");
            atomicPosition.set(
                    new LatLng(location.getLatitude(), location.getLongitude())
            );
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... params) {
                    try { mUser.updatePosition(atomicPosition.get()); }
                    catch (IOException e) { /* Empty */ }
                    return null;
                }
            }.execute();
        }

        public void onConnected(Bundle bundle) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }

        public void onDisconnected() {
            mLocationClient.removeLocationUpdates(this);
        }

    };

    public LocationUpdater(Context context, User user, MapMessageQueue messageQueue) {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_LOCATION_INTERVAL)
                .setFastestInterval(MIN_UPDATE_LOCATION_INTERVAL);
        mLocationClient = new LocationClient(context, mLocationHandler, mLocationHandler);
        mMessageQueue = messageQueue;
        mUser = user;
        atomicPosition = new AtomicReference<>(null);
    }

    public void connect() {
        mMessageQueue.addLoadingMessage("my_location", "Aguardando minha posição");
        mLocationClient.connect();
    }

    public void disconnect() {
        mLocationClient.disconnect();
    }

}
