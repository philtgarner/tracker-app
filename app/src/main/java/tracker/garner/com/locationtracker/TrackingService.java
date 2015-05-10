package tracker.garner.com.locationtracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Vector;

import tracker.garner.com.locationtracker.async.Initializer;
import tracker.garner.com.locationtracker.async.InitializerHandler;
import tracker.garner.com.locationtracker.async.Uploader;
import tracker.garner.com.locationtracker.async.wrappers.InitializationDetails;
import tracker.garner.com.locationtracker.async.wrappers.LocationDetails;

/**
 * Created by Phil on 03/03/2015.
 */
public class TrackingService extends Service implements LocationListener, InitializerHandler{

    private Notification.Builder notificationBuilder = null;
    private NotificationManager notificationManager = null;

    private String url = null;
    private String password = null;
    private String up = null;
    private long frequency = 0;
    private long lastUpdate = 0;
    private boolean reset = false;
    private String deviceID = null;

    private boolean initialized = false;
    private boolean initializing = false;
    private Vector<Location> looseEnds = new Vector<>();

    private LocationManager locationManager = null;
    private ConnectivityManager connectivityManager = null;

    private int privacyRadius = 0;
    private Location privacyLocation = null;

    @Override
    public void onCreate(){
        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        url = intent.getStringExtra(TrackerActivity.EXTRA_URL);
        password = intent.getStringExtra(TrackerActivity.EXTRA_PASSWORD);
        deviceID = intent.getStringExtra(TrackerActivity.EXTRA_DEVICE_ID);
        frequency = intent.getLongExtra(TrackerActivity.EXTRA_FREQUENCY, 10)*1000;
        reset = intent.getBooleanExtra(TrackerActivity.EXTRA_RESET, false);



        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);  //Provider, min time (millis), min distance, listener

        notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentTitle("Tracking")
                .setContentText("Starting to track")
                .setOngoing(true);

        Intent resultIntent = new Intent(this, Tracker.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        notificationBuilder.setContentIntent(resultPendingIntent);


        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(TrackerActivity.NOTIFICATION_ID, notificationBuilder.build());

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //Get the settings - to find the privacy location
        SharedPreferences settings = getSharedPreferences(TrackerActivity.SETTINGS_NAME, Context.MODE_PRIVATE);
        //Get the radius for the privacy circle
        privacyRadius = settings.getInt(TrackerActivity.SETTINGS_PRIVACY_RADIUS, TrackerActivity.SETTINGS_DEFAULT_PRIVACY_RADIUS);

        //If the user has set a privacy location then use it.
        if(settings.contains(TrackerActivity.SETTINGS_PRIVACY_LATITUDE) && settings.contains(TrackerActivity.SETTINGS_PRIVACY_LONGDITUDE)) {
            double privacyLat = Double.parseDouble(settings.getString(TrackerActivity.SETTINGS_PRIVACY_LATITUDE, "0"));
            double privacyLong = Double.parseDouble(settings.getString(TrackerActivity.SETTINGS_PRIVACY_LONGDITUDE, "0"));
            privacyLocation = new Location(TrackerActivity.SETTINGS_PRIVACY_LOCATION);
            privacyLocation.setLatitude(privacyLat);
            privacyLocation.setLongitude(privacyLong);
        }


        return START_REDELIVER_INTENT;
    }

    private void updateLocation(Location l){

        //First check if we are due an update
        long time = System.currentTimeMillis();
        l.setTime(time);
        long timeSinceUpdate = time - lastUpdate;
        if(timeSinceUpdate >= frequency) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            //If connected then we can either initialize or upload our location
            if (netInfo != null && netInfo.isConnected()) {
                //If initialized upload the location
                if (initialized) {
                    //Upload location
                    uploadLocation(l);
                    tieUpLooseEnds();
                }
                //If not initialized then initialize and add location to loose ends
                else {
                    //Initialize
                    init();
                    //Add to loose ends
                    looseEnds.add(l);
                }
            }
            //If not connected then add the location to our list of loose ends
            else {
                looseEnds.add(l);
            }
        }
        //If no update is due
        else{
            //No update - update time ago
            String updateTime = TrackerActivity.niceTime(timeSinceUpdate);
            //lastUpdateTime.setText(updateTime);
            notificationBuilder.setContentText("Last update: " + updateTime + " ago");
            notificationManager.notify(TrackerActivity.NOTIFICATION_ID, notificationBuilder.build());


        }
    }

    private void init(){
        if(!initializing) {
            Initializer init = new Initializer();
            InitializationDetails initializationDetails = new InitializationDetails(url, password, reset, deviceID, this);
            init.execute(initializationDetails);
            initializing = true;
        }
    }

    private boolean uploadLocation(Location l){

        long time = l.getTime();
        double longd = l.getLongitude();
        double lat = l.getLatitude();
        float spd = l.getSpeed();       //Meters per second
        double alt = l.getAltitude();   //Meters

        //Update last update time
        lastUpdate = time;

        //If there is no privacy set or the user is currently outside the privacy circle upload the position
        if(privacyLocation == null || l.distanceTo(privacyLocation) > privacyRadius ) {

            //Send to server
            LocationDetails ld = new LocationDetails(l, time, url, up);
            Uploader uploader = new Uploader();
            uploader.execute(ld);

            //Update the UI with broadcast message:
            Intent intent = new Intent(TrackerActivity.BROADCAST_EVENT);
            intent.putExtra(TrackerActivity.BROADCAST_LATITUDE, lat);
            intent.putExtra(TrackerActivity.BROADCAST_LONGDITUDE, longd);
            intent.putExtra(TrackerActivity.BROADCAST_SPEED, spd);
            intent.putExtra(TrackerActivity.BROADCAST_ALTITUDE, alt);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


            return true;
        }
        //If we're in the privacy circle then don't upload
        else{
            Toast.makeText(getApplicationContext(), "In privacy zone", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean tieUpLooseEnds(){
        int length = looseEnds.size();
        if(length > 0){

            //Loop through all the loose ends and upload them. Start at the oldest and work to the latest
            for(int i=0; i<length; i++){
                Location l = looseEnds.elementAt(length-i-1);
                uploadLocation(l);
            }
            //Set the time of the last update to the most recent location record
            lastUpdate = looseEnds.firstElement().getTime();
            //Empty all the loose ends
            looseEnds.removeAllElements();

            Toast.makeText(this, "Tied up loose ends (" + length + ")", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(TrackerActivity.NOTIFICATION_ID);
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void setUploadKey(String output) {
        if(output != null){
            up = output;
            initialized = true;
            Toast.makeText(this, "Initialization complete", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Something went wrong with the initialization", Toast.LENGTH_SHORT).show();
        }
        initializing = false;
    }
}
