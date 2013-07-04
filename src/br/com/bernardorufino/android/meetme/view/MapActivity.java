package br.com.bernardorufino.android.meetme.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import br.com.bernardorufino.android.meetme.R;
import br.com.bernardorufino.android.meetme.helper.Helper;
import br.com.bernardorufino.android.meetme.helper.ViewHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends BaseActivity {
    private GoogleMap map;
    private MapFragment mapFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        // Sets up view components
        FragmentManager manager = getFragmentManager();
        mapFragment = (MapFragment) manager.findFragmentById(R.id.map);

        if (Helper.hasPlayServices(this)) {
            createMap();
            ViewHelper.flash(this, "Tem play =)");
        } else {
            ViewHelper.flash(this, "Nao tem play =(");
        }

    }

    public void createMap() {
        if (map == null) {
            map = mapFragment.getMap();
            if (map == null) {
                ViewHelper.flash(this, R.string.no_map_error_message);
                return;
            }
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            LatLng position = new LatLng(37.4, -122.1);
            MarkerOptions marker = new MarkerOptions()
                    .position(position)
                    .title("Joao");
            map.addMarker(marker);
            CameraPosition camera = new CameraPosition.Builder()
                    .target(position)
                    .zoom(15)
                    .tilt(30)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(camera));
        }
    }
}