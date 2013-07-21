package br.com.bernardorufino.android.meetme.activities.map;

import android.widget.ToggleButton;
import br.com.bernardorufino.android.meetme.R;
import br.com.bernardorufino.android.meetme.activities.map.messaging.MapMessageQueue;
import br.com.bernardorufino.android.meetme.helper.Helper;
import br.com.bernardorufino.android.meetme.helper.MapHelper;
import br.com.bernardorufino.android.meetme.helper.ViewHelper;
import br.com.bernardorufino.android.meetme.lib.concurrency.TimedTask;
import br.com.bernardorufino.android.meetme.model.Group;
import br.com.bernardorufino.android.meetme.model.GroupsAPI;
import br.com.bernardorufino.android.meetme.model.User;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static br.com.bernardorufino.android.meetme.Definitions.INITIAL_POSITION;
import static br.com.bernardorufino.android.meetme.activities.map.MapActivity.*;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;

class MapUpdater {

    /* package private */ static final long UPDATE_MAP_INTERVAL = 200;
    // Needs a safe delay from the completion of camera animation to next animation
    private static final long CAMERA_ANIMATION = (long) (0.95 * UPDATE_MAP_INTERVAL - 25);

    private final ToggleButton mToggleAutoZoom;
    private final Group mGroup;
    private final AtomicReference<LatLng> mAtomicPosition;
    private final MapFragment mMapFragment;
    private final User mUser;
    // int user id => marker
    private Map<Integer, Marker> mUserMarkersMap = new HashMap<>();
    private final MapMessageQueue mMessageQueue;
    private Exception exception;
    private GoogleMap mMap;

    private TimedTask mTimedTask = new TimedTask() {

        @Override
        public void work() {
            // If it's in the middle of a request, wait in order not to pile up
            if (GroupsAPI.isRequestOpen()) return;
            try {
                mGroup.update();
                exception = null;
            } catch (IOException e) {
                exception = e;
            }
        }

        private void handleException(Exception exception) {
            if (Helper.isInternetException(exception)) {
                mMessageQueue.addLoadingMessage("location_updates", "Esperando conexão");
            } else {
                mMessageQueue.addMessage("location_updates", "Erro na recuperação das posições");
            }
            Helper.logException(exception);
        }

        @Override
        public void onComplete() {
            if (exception == null) {
                // Request was successful, hiding user messages
                mToggleAutoZoom.setEnabled(true);
                mMessageQueue.hideMessage("location_updates");
            } else {
                // Request wasn't successful
                mToggleAutoZoom.setEnabled(false);
                handleException(exception);
                return;
            }
            updateMarkers();
            if (mToggleAutoZoom.isChecked()) {
                LatLng position = mAtomicPosition.get();
                LatLng center = (position != null) ? position : mMap.getCameraPosition().target;
                CameraUpdate update = MapHelper.displayMarkers(mMap, center, mUserMarkersMap.values());
                mMap.animateCamera(update, (int) CAMERA_ANIMATION, null);
            }
        }

    };

    /* package private */ MapUpdater(MapFragment mapFragment, User user, Group group,
                                     AtomicReference<LatLng> atomicPosition,
                                     ToggleButton toggleAutoZoom,
                                     MapMessageQueue messageQueue) {
        mMapFragment = mapFragment;
        mUser = user;
        mGroup = group;
        mAtomicPosition = atomicPosition;
        mToggleAutoZoom = toggleAutoZoom;
        mMessageQueue = messageQueue;
        createMap();
    }

    /* package private */ void fire() {
        mMessageQueue.addLoadingMessage("location_updates", "Carregando localizações");
        mTimedTask.fire(UPDATE_MAP_INTERVAL);
    }

    /* package private */ void cancel() {
        mTimedTask.cancel();
    }

    private GoogleMap.OnMarkerClickListener markerClickHandler = new GoogleMap.OnMarkerClickListener() {

        public boolean onMarkerClick(Marker marker) {
            // Uncomment to disable auto zoom when clicking the markers
            // mToggleAutoZoom.setChecked(false);
            // toggleAutoZoomClick(mToggleAutoZoom);
            return false;
        }

    };

    public void createMap() {
        mMessageQueue.addLoadingMessage("Carregando mapa");
        if (mMap == null) {
            // TODO: Threat null case
            mMap = mMapFragment.getMap();
            mMap.setMapType(MAP_TYPE_HYBRID);
            CameraPosition camera = new CameraPosition.Builder()
                    .target(INITIAL_POSITION)
                    .zoom(15)
                    .tilt(30)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera));
            mMap.setOnMarkerClickListener(markerClickHandler);
        }
        mMessageQueue.hideMessage();
    }

    private void updateMarkers() {
        // Set to keep track of the ones to be removed
        Set<Integer> usersWithMarker = new HashSet<>(mUserMarkersMap.keySet());
        // Adds new markers and updates existing ones
        for (User groupUser : mGroup.getUsers()) {
            int id = groupUser.getID();
            Marker marker = mUserMarkersMap.get(id);
            if (marker == null) { // Add new marker
                MarkerOptions options = new MarkerOptions()
                        .position(groupUser.getPosition())
                        .title(groupUser.getName());
                if (groupUser.equals(mUser)) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                }
                marker = mMap.addMarker(options);
                mUserMarkersMap.put(id, marker);
            } else { // Update position of existing one
                usersWithMarker.remove(id);
                marker.setPosition(groupUser.getPosition());
            }
        }
        // Delete remaining markers on screen
        for (int id : usersWithMarker) {
            mUserMarkersMap.get(id).remove();
            mUserMarkersMap.remove(id);
        }
    }

}
