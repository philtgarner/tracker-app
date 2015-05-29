package com.garner.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * @author Phil Garner
 * An activity used to display the tracking information broadcast by the service
 */
public class TrackerViewer extends AbstractTrackerActivity implements MenuItem.OnMenuItemClickListener {

    private MenuItem stop = null;

    private GoogleMap map = null;
    private Marker currentLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        //Get the map
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

    }

    @Override
    public void onResume(){
        super.onResume();
        //Start the broadcast receiver again
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,  new IntentFilter(BROADCAST_EVENT));
    }

    @Override
    public void onPause(){
        //Stop the broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }


    //Handle received broadcasts
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            double lat = intent.getDoubleExtra(BROADCAST_LATITUDE, 0);
            double longd = intent.getDoubleExtra(BROADCAST_LONGDITUDE, 0);

            //Build a position from the sent latitude and longditude
            LatLng pos = new LatLng(lat,longd);
            //If we haven't alreade got a marker on screen then make one
            if(currentLocation == null){
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(pos);
                markerOptions.title(getString(R.string.tracker_marker_title));
                currentLocation = map.addMarker(markerOptions);
            }
            //If there is already a marker there then move it
            else{
                currentLocation.setPosition(pos);
            }

            //Move the map to be centred on the new position
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.tracking, menu);

        // Locate MenuItem for the stop button
        stop = menu.findItem(R.id.menu_item_stop);

        //Listen for clicks on the stop button
        stop.setOnMenuItemClickListener(this);

        // Return true to display menu
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        //If the stop button is clicked then stop the service and launch the list activity so the user can start again if necessary.
        if(item == stop){
            stopService(new Intent(this, TrackingService.class));
            Intent i = new Intent(this, TrackerList.class);
            startActivity(i);
        }
        return true;
    }
}
