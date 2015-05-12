package tracker.garner.com.locationtracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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

/**
 * @author Phil Garner
 * An activity showing a map to pick the privacy location for the user.
 */
public class PrivacyPicker extends AbstractTrackerActivity implements GoogleMap.OnMapClickListener {

    //Google Maps and things to display on it
    private GoogleMap map = null;
    private Marker currentLocation = null;
    private Circle circle = null;

    //Circle options and default values
    private CircleOptions co = new CircleOptions();
    private static final int CIRCLE_FILL = Color.argb(100,255,0,0);
    private static final int CIRCLE_STROKE = Color.argb(200,255,0,0);
    private static final float ZOOM_LEVEL = 12;

    private int radius = -1;

    //Settings - to load and store the location and radius
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

        //If we have a privacy location already then display it and focus on it
        if(settings.contains(SETTINGS_PRIVACY_LATITUDE) && settings.contains(SETTINGS_PRIVACY_LONGDITUDE)) {
            double privacyLat = Double.parseDouble(settings.getString(SETTINGS_PRIVACY_LATITUDE, "0"));
            double privacyLong = Double.parseDouble(settings.getString(SETTINGS_PRIVACY_LONGDITUDE, "0"));
            LatLng latLng = new LatLng(privacyLat,privacyLong);

            //Build and add the marker
            MarkerOptions mo = new MarkerOptions();
            mo.position(latLng);
            currentLocation = map.addMarker(mo);

            //Build and add the circle around the marker
            co.center(latLng);
            circle = map.addCircle(co);

            //Set the camera position and go there
            CameraUpdate zoom = CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL);
            map.animateCamera(zoom);


        }

    }

    @Override
    public void onMapClick(LatLng latLng) {

        //Change the position of the marker
        MarkerOptions mo = new MarkerOptions();
        mo.position(latLng);

        //Change the position of the circle
        co.center(latLng);

        //Store the position of the click
        settingsEditor.putString(SETTINGS_PRIVACY_LATITUDE, latLng.latitude + "");
        settingsEditor.putString(SETTINGS_PRIVACY_LONGDITUDE, latLng.longitude + "");
        settingsEditor.commit();

        //If no previous location then make the marker and add the circle
        if(currentLocation == null){
            currentLocation = map.addMarker(mo);
            circle = map.addCircle(co);
        }
        //If there is already a marker and circle then just move them
        else{
            currentLocation.setPosition(latLng);
            circle.setCenter(latLng);
        }
    }
}
