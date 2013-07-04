package br.com.bernardorufino.android.meetme.helper;

import android.graphics.Point;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.*;

import java.util.Collection;

public class MapHelper {
    public static final int VISIBLE_AREA_OFFSET = 80; // pixels

    // Prevents instantiation
    private MapHelper() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }

    public static CameraUpdate displayMarkers(GoogleMap map, LatLng center, Collection<? extends Marker> markers) {
        Projection projection = map.getProjection();
        VisibleRegion region = projection.getVisibleRegion();
        Point origin = projection.toScreenLocation(center);
        int screenLeft = projection.toScreenLocation(region.farLeft).x - origin.x;
        int screenRight = projection.toScreenLocation(region.nearRight).x - origin.x;
        int screenTop = projection.toScreenLocation(region.farLeft).y - origin.y;
        int screenBottom = projection.toScreenLocation(region.nearRight).y - origin.y;
        int left = 0, right = 0, bottom = 0, top = 0;
        for (Marker marker : markers) {
            Point p = projection.toScreenLocation(marker.getPosition());
            left = Math.min(p.x - origin.x, left);
            right = Math.max(p.x - origin.x, right);
            top = Math.min(p.y - origin.y, top);
            bottom = Math.max(p.y - origin.y, bottom);
        }
        left -= VISIBLE_AREA_OFFSET;
        right += VISIBLE_AREA_OFFSET;
        top -= VISIBLE_AREA_OFFSET;
        bottom += VISIBLE_AREA_OFFSET;
        // scale = target / current
        float scale = Math.abs((float) screenLeft / left);
        scale = Math.min(Math.abs((float) screenRight / right), scale);
        scale = Math.min(Math.abs((float) screenTop / top), scale);
        scale = Math.min(Math.abs((float) screenBottom / bottom), scale);
        // targetWorldWidth = currentWorldWidth * target / current
        // targetWorldWidth = currentWorldWidth * scale
        // scale = targetWorldWidth / currentWorldWidth
        // targetWorldWidth = currentWorldWidth * 2 ^ zoomBy
        // zoomBy = log2(targetWorldWidth / currentWorldWidth)
        // zoomBy = log2(scale) = ln(scale) / ln(2)
        float zoomBy = (float) (Math.log(scale) / Math.log(2));
        if (markers.size() <= 1) zoomBy = 0;
        CameraPosition camera = map.getCameraPosition();
        return CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .zoom(camera.zoom + zoomBy)
                .target(center)
                .tilt(camera.tilt)
                .bearing(camera.bearing)
                .build()
        );
    }

}
