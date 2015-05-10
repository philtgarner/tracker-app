package tracker.garner.com.locationtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class PrivacyPicker extends TrackerActivity implements GoogleMap.OnMapClickListener {

    private MenuItem stop = null;

    private GoogleMap map = null;
    private Marker currentLocation = null;
    private Circle circle = null;

    private CircleOptions co = new CircleOptions();
    private static final int CIRCLE_FILL = Color.argb(100,255,0,0);
    private static final int CIRCLE_STROKE = Color.argb(200,255,0,0);
    private static final float ZOOM_LEVEL = 12;

    private int radius = -1;

    private SharedPreferences settings = null;
    private SharedPreferences.Editor settingsEditor = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_picker);

        //Get the map from the UI
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.privacy_map)).getMap();

        //Listen for clicks on the map
        map.setOnMapClickListener(this);

        //Get the settings (to find out if there is already a privacy location set
        settings = getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
        settingsEditor = settings.edit();

        //Get the radius for the privacy circle
        radius = settings.getInt(SETTINGS_PRIVACY_RADIUS, SETTINGS_DEFAULT_PRIVACY_RADIUS);

        //Set the circle info
        co.radius(radius);
        co.strokeColor(CIRCLE_STROKE);
        co.fillColor(CIRCLE_FILL);

        if(settings.contains(SETTINGS_PRIVACY_LATITUDE) && settings.contains(SETTINGS_PRIVACY_LONGDITUDE)) {
            double privacyLat = Double.parseDouble(settings.getString(SETTINGS_PRIVACY_LATITUDE, "0"));
            double privacyLong = Double.parseDouble(settings.getString(SETTINGS_PRIVACY_LONGDITUDE, "0"));
            LatLng latLng = new LatLng(privacyLat,privacyLong);

            MarkerOptions mo = new MarkerOptions();
            mo.position(latLng);

            co.center(latLng);

            currentLocation = map.addMarker(mo);
            circle = map.addCircle(co);

            CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL);
            map.animateCamera(zoom);


        }

    }

    @Override
    public void onMapClick(LatLng latLng) {

        MarkerOptions mo = new MarkerOptions();
        mo.position(latLng);

        co.center(latLng);

        settingsEditor.putString(SETTINGS_PRIVACY_LATITUDE, latLng.latitude + "");
        settingsEditor.putString(SETTINGS_PRIVACY_LONGDITUDE, latLng.longitude + "");
        settingsEditor.commit();

        if(currentLocation == null){
            currentLocation = map.addMarker(mo);
            circle = map.addCircle(co);
        }
        else{
            currentLocation.setPosition(latLng);
            circle.setCenter(latLng);
        }



    }
}
