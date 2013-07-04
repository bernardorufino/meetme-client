package br.com.bernardorufino.android.meetme.view;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.TextView;
import br.com.bernardorufino.android.meetme.R;
import br.com.bernardorufino.android.meetme.helper.Helper;
import br.com.bernardorufino.android.meetme.helper.MapHelper;
import br.com.bernardorufino.android.meetme.helper.ViewHelper;
import br.com.bernardorufino.android.meetme.lib.LocationAdapter;
import br.com.bernardorufino.android.meetme.lib.TimedTask;
import br.com.bernardorufino.android.meetme.model.Group;
import br.com.bernardorufino.android.meetme.model.GroupsAPI;
import br.com.bernardorufino.android.meetme.model.User;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static br.com.bernardorufino.android.meetme.Definitions.*;

public class MapActivity extends BaseActivity {
    private static final long UPDATE_MAP_INTERVAL = 200;
    //Needs a safe delay from the completion of camera animation to next animation
    private static final long CAMERA_ANIMATION = UPDATE_MAP_INTERVAL - 25;
    private static final long UPDATE_LOCATION_INTERVAL = 1000;
    private static final long MIN_UPDATE_LOCATION_INTERVAL = 500;

    private GoogleMap map;
    private MapFragment mapFragment;
    private Group group;
    private User user;
    private MapUpdater mapUpdater;
    private TextView groupPasswordText;
    private LocationHandler locationHandler;
    private LocationClient locationClient;
    private LocationRequest locationRequest;
    private LatLng position;

    private class MapUpdater extends TimedTask {

        public void work() {
            try { group.update(); }
            catch (IOException e) { ViewHelper.flash(MapActivity.this, "deu merda no update"); }
        }

        public void onComplete() {
            // If it's in the middle of a request, wait in order not to pile up
            if (GroupsAPI.isRequestOpen()) return;
            map.clear();
            Collection<Marker> markers = new ArrayList<>();
            Point p = map.getProjection().toScreenLocation(map.getCameraPosition().target);
            for (User user : group.getUsers()) {
                p = map.getProjection().toScreenLocation(user.getPosition());
                Marker marker = map.addMarker(new MarkerOptions()
                    .position(user.getPosition())
                    .title(user.getName()));
                markers.add(marker);
            }
            LatLng center = (position != null) ? position : map.getCameraPosition().target;
            CameraUpdate update = MapHelper.displayMarkers(map, center, markers);
            map.animateCamera(update, (int) CAMERA_ANIMATION, null);
        }
    }

    private class LocationHandler extends LocationAdapter {

        public void onLocationChanged(final Location location) {
            position = new LatLng(location.getLatitude(), location.getLongitude());
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... params) {
                    try { user.updatePosition(position); }
                    catch (IOException e) { /* Empty */ }
                    return null;
                }
            }.execute();
        }

        public void onConnected(Bundle bundle) {
            locationClient.requestLocationUpdates(locationRequest, locationHandler);
        }

        public void onDisconnected() {
            locationClient.removeLocationUpdates(locationHandler);
        }

    }

    public void onCreate(Bundle savedInstanceState) {
        Helper.log("onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        // Enable navigate up button
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (!Helper.hasPlayServices(this)) {
            ViewHelper.flash(this, "Nao tem play =(");
            return;
        }

        // Sets up view components
        FragmentManager manager = getFragmentManager();
        mapFragment = (MapFragment) manager.findFragmentById(R.id.map);
        groupPasswordText = (TextView) findViewById(R.id.groupPasswordText);

        // Sets up group and user
        Intent intent = getIntent();
        group = (Group) intent.getSerializableExtra(ViewHelper.withNamespace("group"));
        user = (User) intent.getSerializableExtra(ViewHelper.withNamespace("user"));
        groupPasswordText.setText(group.getPassword());

        // Creates map
        createMap();

        // Sets up location client
        locationHandler = new LocationHandler();
        locationClient = new LocationClient(this, locationHandler, locationHandler);
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_LOCATION_INTERVAL)
                .setFastestInterval(MIN_UPDATE_LOCATION_INTERVAL);


        // Sets up map updater
        mapUpdater = new MapUpdater();
    }

    protected void onStart() {
        Helper.log("onStart()");
        super.onStart();
        locationClient.connect();
        mapUpdater.fire(UPDATE_MAP_INTERVAL);
    }

    protected void onStop() {
        Helper.log("onStop()");
        mapUpdater.cancel();
        locationClient.disconnect();
        super.onStop();
    }

    protected void onDestroy() {
        Helper.log("onDestroy()");
        super.onDestroy();
    }

    public void createMap() {
        if (map == null) {
            map = mapFragment.getMap();
            if (map == null) {
                ViewHelper.flash(this, R.string.no_map_error_message);
                return;
            }
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            CameraPosition camera = new CameraPosition.Builder()
                    .target(INITIAL_POSITION)
                    .zoom(15)
                    .tilt(30)
                    .build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(camera));
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}