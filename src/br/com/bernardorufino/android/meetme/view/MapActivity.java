package br.com.bernardorufino.android.meetme.view;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
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
import com.google.android.gms.maps.model.*;

import java.io.IOException;
import java.util.*;

import static br.com.bernardorufino.android.meetme.Definitions.*;
import static com.google.android.gms.maps.GoogleMap.*;

public class MapActivity extends BaseActivity {
    private static final long UPDATE_MAP_INTERVAL = 500;
    // Needs a safe delay from the completion of camera animation to next animation
    private static final long CAMERA_ANIMATION = (long) (0.95 * UPDATE_MAP_INTERVAL - 25);
    private static final long UPDATE_LOCATION_INTERVAL = UPDATE_MAP_INTERVAL * 2;
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
    private TextView userMessage;
    private ProgressBar loadingView;
    private ToggleButton toggleAutoZoom;
    private boolean autoZoom = true;
    // int user id => marker
    private Map<Integer, Marker> userMarkersMap = new HashMap<>();

    private enum MessageOrigin { LocationUpdates, MyLocation, Other }

    private class MapUpdater extends TimedTask {
        private Exception exception;

        public void work() {
            try {
                group.update();
                exception = null;
            } catch (IOException e) {
                exception = e;
            }
        }

        private void handleException(Exception exception) {
            if (Helper.isInternetException(exception))
                loadingMessage("Esperando conexão", MessageOrigin.LocationUpdates);
            else userMessage("Erro na recuperação das posições", MessageOrigin.LocationUpdates);
            Helper.logException(exception);
        }

        public void onComplete() {
            // Check wheter the request was successful, if it was then hide user
            // messages, if not show corresponding messages
            if (exception != null) {
                toggleAutoZoom.setEnabled(false);
                handleException(exception);
                return;
            } else {
                toggleAutoZoom.setEnabled(true);
                hideMessage(MessageOrigin.LocationUpdates);
            }
            // If it's in the middle of a request, wait in order not to pile up
            if (GroupsAPI.isRequestOpen()) return;
            updateMarkers();
            if (autoZoom) {
                LatLng center = (position != null) ? position : map.getCameraPosition().target;
                CameraUpdate update = MapHelper.displayMarkers(map, center, userMarkersMap.values());
                map.animateCamera(update, (int) CAMERA_ANIMATION, null);
            }
        }
    }

    private void updateMarkers() {
        // Set to keep track of the ones to be removed
        Set<Integer> usersWithMarker = new HashSet<>(userMarkersMap.keySet());
        // Adds new markers and updates existing ones
        for (User groupUser : group.getUsers()) {
            int id = groupUser.getID();
            Marker marker = userMarkersMap.get(id);
            if (marker == null) { // Add new marker
                MarkerOptions options = new MarkerOptions()
                    .position(groupUser.getPosition())
                    .title(groupUser.getName());
                if (groupUser.equals(user)) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                }
                marker = map.addMarker(options);
                userMarkersMap.put(id, marker);
            } else { // Update position of existing one
                usersWithMarker.remove(id);
                marker.setPosition(groupUser.getPosition());
            }
        }
        // Delete remaining markers on screen
        for (int id : usersWithMarker) {
            userMarkersMap.get(id).remove();
            userMarkersMap.remove(id);
        }
    }

    private class LocationHandler extends LocationAdapter {

        public void onLocationChanged(final Location location) {
            hideMessage(MessageOrigin.MyLocation);
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        // Enable navigate up button
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Check wheter has play services
        if (!Helper.hasPlayServices(this)) {
            ViewHelper.flash(this, "Nao tem play =(");
            return;
        }

        // Sets up view components
        FragmentManager manager = getFragmentManager();
        mapFragment = (MapFragment) manager.findFragmentById(R.id.map);
        groupPasswordText = (TextView) findViewById(R.id.groupPasswordText);
        userMessage = (TextView) findViewById(R.id.userMessage);
        loadingView = (ProgressBar) findViewById(R.id.loadingView);
        toggleAutoZoom = (ToggleButton) findViewById(R.id.toggleAutoZoom);

        // Sets inital state, if you want to change, change autoZoom field initial value above
        toggleAutoZoom.setChecked(autoZoom);

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
        super.onStart();
        loadingMessage("Carregando localizações", MessageOrigin.LocationUpdates);
        mapUpdater.fire(UPDATE_MAP_INTERVAL);
        loadingMessage("Aguardando minha posição", MessageOrigin.MyLocation);
        locationClient.connect();
    }

    protected void onStop() {
        mapUpdater.cancel();
        locationClient.disconnect();
        super.onStop();
    }

    private OnMarkerClickListener markerClickHandler = new OnMarkerClickListener() {

        public boolean onMarkerClick(Marker marker) {
            toggleAutoZoom.setChecked(false);
            toggleAutoZoomClick(toggleAutoZoom);
            return false;
        }

    };

    public void createMap() {
        loadingMessage("Carregando mapa");
        if (map == null) {
            map = mapFragment.getMap();
            if (map == null) {
                ViewHelper.flash(this, R.string.no_map_error_message);
                return;
            }
            map.setMapType(MAP_TYPE_HYBRID);
            CameraPosition camera = new CameraPosition.Builder()
                    .target(INITIAL_POSITION)
                    .zoom(15)
                    .tilt(30)
                    .build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(camera));
            map.setOnMarkerClickListener(markerClickHandler);
        }
        hideMessage();
    }

    public void toggleAutoZoomClick(View view) {
        // Explicitly using boolean field / listener for scalability purposes
        autoZoom = toggleAutoZoom.isChecked();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                new AlertDialog.Builder(this)
                    .setMessage(R.string.exit_map_confirm_message)
                    .setPositiveButton("Sair", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            NavUtils.navigateUpFromSameTask(MapActivity.this);
                        }
                    })
                    .setNegativeButton("Permanecer", null)
                    .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //TODO: Refactor! Extract this messaging system, maybe in BaseActivity
    //TODO: Implement priority and immediate update
    // User messages

    private class UserMessage {
        String text;
        boolean loading;

        private UserMessage(String text, boolean loading) {
            this.text = text;
            this.loading = loading;
        }
    }

    private Map<MessageOrigin, UserMessage> messages = new LinkedHashMap<>();

    // Handling user messages in a queue, when there is a message being displayed, 
    // it will wait until it disappears to show the new one, much like the following timeline
    //
    // Display command:  A B - - - - - C D - - - - - E - - - - F - - -
    //    Hide command:  - - - A - B - - - - D - C - - - - E - - - - F
    //       User sees:  A A A B B - - C C C C C - - E E E - - F F F -
    //
    // Note that if a message hides before having the chance to be displayed, it will never be displayed
    // New messages of the same origin erases the old one and are put as a fresh message

    // Messages to the user!

    private void updateMessage() {
        if (messages.size() > 0) {
            UserMessage message = messages.values().iterator().next();
            userMessage.setText(message.text);
            userMessage.setVisibility(View.VISIBLE);
            loadingView.setVisibility(message.loading ? View.VISIBLE : View.GONE);
        } else {
            userMessage.setText("");
            // Using INVISIBLE instead of GONE to preserve height of container
            userMessage.setVisibility(View.INVISIBLE);
            loadingView.setVisibility(View.GONE);
        }
    }

    private void insertMessage(MessageOrigin origin, UserMessage message) {
        messages.remove(origin);
        messages.put(origin, message);
        if (messages.size() == 1) updateMessage();
    }

    private void userMessage(String message, MessageOrigin origin) {
        insertMessage(origin, new UserMessage(message, false));
    }

    private void userMessage(String message) {
        userMessage(message, MessageOrigin.Other);
    }

    private void loadingMessage(String message, MessageOrigin origin) {
        insertMessage(origin, new UserMessage(message, true));
    }

    private void loadingMessage(String message) {
        loadingMessage(message, MessageOrigin.Other);
    }

    private void hideMessage(MessageOrigin origin) {
        messages.remove(origin);
        updateMessage();
    }

    private void hideMessage() {
        hideMessage(MessageOrigin.Other);
    }

}