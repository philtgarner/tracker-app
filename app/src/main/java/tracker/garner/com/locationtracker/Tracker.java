package tracker.garner.com.locationtracker;

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


public class Tracker extends TrackerActivity implements MenuItem.OnMenuItemClickListener {

    private MenuItem stop = null;

    private GoogleMap map = null;
    private Marker currentLocation = null;



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking2);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

    }

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,  new IntentFilter(BROADCAST_EVENT));
    }

    @Override
    public void onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }


    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            double lat = intent.getDoubleExtra(BROADCAST_LATITUDE, 0);
            double longd = intent.getDoubleExtra(BROADCAST_LONGDITUDE, 0);
            float spd = intent.getFloatExtra(BROADCAST_SPEED, 0);
            double alt = intent.getDoubleExtra(BROADCAST_ALTITUDE, 0);

            LatLng pos = new LatLng(lat,longd);
            if(currentLocation == null){
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(pos);
                markerOptions.title("You");
                currentLocation = map.addMarker(markerOptions);
            }
            else{
                currentLocation.setPosition(pos);
            }

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.tracking, menu);

        // Locate MenuItem with ShareActionProvider
        stop = menu.findItem(R.id.menu_item_stop);

        stop.setOnMenuItemClickListener(this);

        // Return true to display menu
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item == stop){
            stopService(new Intent(this, TrackingService.class));
            Intent i = new Intent(this, TrackerList.class);
            startActivity(i);
        }
        return true;
    }
}
