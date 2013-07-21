package br.com.bernardorufino.android.meetme.activities.map;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import br.com.bernardorufino.android.meetme.R;
import br.com.bernardorufino.android.meetme.activities.map.messaging.MapMessageQueue;
import br.com.bernardorufino.android.meetme.helper.Helper;
import br.com.bernardorufino.android.meetme.helper.ViewHelper;
import br.com.bernardorufino.android.meetme.model.Group;
import br.com.bernardorufino.android.meetme.model.User;
import br.com.bernardorufino.android.meetme.activities.BaseActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;

import java.io.IOException;

import static br.com.bernardorufino.android.meetme.activities.map.LocationUpdater.*;

// TODO: MapUpdater
public class MapActivity extends BaseActivity {

    private MapFragment mMapFragment;
    private Group mGroup;
    private User mUser;
    private MapUpdater mMapUpdater;
    private TextView mGroupPasswordText;
    private ToggleButton mToggleAutoZoom;
    private LocationUpdater mLocationUpdater;
    private MapMessageQueue mMessageQueue;

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        // Sets up layout
        setContentView(R.layout.map);
        // Enable navigate up button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // View components
        FragmentManager manager = getFragmentManager();
        mMapFragment = (MapFragment) manager.findFragmentById(R.id.map);
        mGroupPasswordText = (TextView) findViewById(R.id.groupPasswordText);
        mToggleAutoZoom = (ToggleButton) findViewById(R.id.toggleAutoZoom);
        mToggleAutoZoom.setChecked(true);
    }

    private void initializeMessager() {
        TextView userMessage = (TextView) findViewById(R.id.userMessage);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.loadingView);
        mMessageQueue = new MapMessageQueue(userMessage, progressBar);
    }

    private void checkPlayServices() {
        if (Helper.hasPlayServices(this)) return;
        ViewHelper.flash(this, "Instale Google Play Services");
        NavUtils.navigateUpFromSameTask(this);
    }

    private void loadModels() {
        Intent intent = getIntent();
        mGroup = (Group) intent.getSerializableExtra(ViewHelper.withNamespace("group"));
        mUser = (User) intent.getSerializableExtra(ViewHelper.withNamespace("user"));
        mGroupPasswordText.setText(mGroup.getPassword());
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPlayServices();
        initializeMessager();
        loadModels();
        // Will handle user location updates, it's started on onStart()
        mLocationUpdater = new LocationUpdater(this, mUser, mMessageQueue);
        // Will handle group locations updates
        mMapUpdater = new MapUpdater(mMapFragment, mUser, mGroup, mLocationUpdater.atomicPosition,
                                     mToggleAutoZoom, mMessageQueue);
    }

    protected void onStart() {
        super.onStart();
        mMapUpdater.fire();
        mLocationUpdater.connect();
    }

    protected void onStop() {
        mLocationUpdater.disconnect();
        mMapUpdater.cancel();
        super.onStop();
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

}