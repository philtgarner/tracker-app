package com.garner.location;

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

import java.util.Vector;

import com.garner.location.async.Initializer;
import com.garner.location.async.InitializerHandler;
import com.garner.location.async.Uploader;
import com.garner.location.async.wrappers.InitializationDetails;
import com.garner.location.async.wrappers.LocationDetails;
import com.garner.location.async.UploaderHandler;
import com.garner.location.async.wrappers.UploadResponse;

/**
 * @author Phil Garner
 * The service that periodically sends the information to the server
 */
public class TrackingService extends Service implements LocationListener, InitializerHandler, UploaderHandler{

    //Things to build the notification
    private Notification.Builder notificationBuilder = null;
    private NotificationManager notificationManager = null;

    //Info for the server
    private String url = null;
    private String download = null;
    private String upload = null;
    private long frequency = 0;
    private boolean reset = false;
    private String deviceID = null;
    private boolean toast = AbstractTrackerActivity.SETTINGS_DEFAULT_TOAST;

    //Time of last update (or attempted update)
    private long lastUpdate = 0;

    //Booleans used to check initialized state
    private boolean initialized = false;
    private boolean initializing = false;

    //List of locations that could not be uploaded (probably due to lack of signal)
    private Vector<Location> looseEnds = new Vector<>();
    //List of the locations we are currently in the process of uploading
    private Vector<Location> inProgress = new Vector<>();

    //Managers for listening for connectivity and location changes
    private LocationManager locationManager = null;
    private ConnectivityManager connectivityManager = null;

    //Privacy details
    private int privacyRadius = 0;
    private Location privacyLocation = null;

    @Override
    public void onCreate(){
        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Get all the information from the launcher
        url = intent.getStringExtra(AbstractTrackerActivity.EXTRA_URL);
        download = intent.getStringExtra(AbstractTrackerActivity.EXTRA_PASSWORD);
        deviceID = intent.getStringExtra(AbstractTrackerActivity.EXTRA_DEVICE_ID);
        frequency = intent.getLongExtra(AbstractTrackerActivity.EXTRA_FREQUENCY, 10)*1000;
        reset = intent.getBooleanExtra(AbstractTrackerActivity.EXTRA_RESET, false);

        //Start listening for location changes
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);  //Provider, min time (millis), min distance, listener

        //Build the notification
        notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_initial_text))
                .setOngoing(true);

        //Build the intent that is launched when the notification is clicked
        Intent resultIntent = new Intent(this, TrackerViewer.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        //Associate the intent with the notification
        notificationBuilder.setContentIntent(resultPendingIntent);

        //Start the notification
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(AbstractTrackerActivity.NOTIFICATION_ID, notificationBuilder.build());

        //Get the connectivity manager to monitor the changes in connectivity
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //Get the settings - to find the privacy location
        SharedPreferences settings = getSharedPreferences(AbstractTrackerActivity.SETTINGS_NAME, Context.MODE_PRIVATE);
        //Get the radius for the privacy circle
        privacyRadius = settings.getInt(AbstractTrackerActivity.SETTINGS_PRIVACY_RADIUS, AbstractTrackerActivity.SETTINGS_DEFAULT_PRIVACY_RADIUS);

        //Get the users preference on toast messages.
        toast = settings.getBoolean(AbstractTrackerActivity.SETTINGS_TOAST_MODE, AbstractTrackerActivity.SETTINGS_DEFAULT_TOAST);

        //If the user has set a privacy location then use it.
        if(settings.contains(AbstractTrackerActivity.SETTINGS_PRIVACY_LATITUDE) && settings.contains(AbstractTrackerActivity.SETTINGS_PRIVACY_LONGDITUDE)) {
            double privacyLat = Double.parseDouble(settings.getString(AbstractTrackerActivity.SETTINGS_PRIVACY_LATITUDE, "0"));
            double privacyLong = Double.parseDouble(settings.getString(AbstractTrackerActivity.SETTINGS_PRIVACY_LONGDITUDE, "0"));
            privacyLocation = new Location(AbstractTrackerActivity.SETTINGS_PRIVACY_LOCATION);
            privacyLocation.setLatitude(privacyLat);
            privacyLocation.setLongitude(privacyLong);
        }


        return START_REDELIVER_INTENT;
    }

    /**
     * Checks if an upload is required and acts appropriately. This is called every time a change in location is detected.
     * @param l The location to upload
     */
    private void updateLocation(Location l){

        //Check if we are due an update (the difference between now and the last update must be more than the frequency of updates)
        long time = System.currentTimeMillis();
        l.setTime(time);        //Use the time from the device TODO Do we need this step?
        long timeSinceUpdate = time - lastUpdate;


        //If an update is required
        if(timeSinceUpdate >= frequency) {

            //Update last update time
            lastUpdate = time;

            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            //If connected to the internet then we can either initialize or upload our location
            if (netInfo != null && netInfo.isConnected()) {
                //If initialized upload the location
                if (initialized) {
                    //Upload location the current location and tie up any loose ends
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
            //No update - update time ago on the notification
            String updateTime = AbstractTrackerActivity.niceTime(timeSinceUpdate);
            notificationBuilder.setContentText(String.format(getString(R.string.notification_time_ago), updateTime));
            notificationManager.notify(AbstractTrackerActivity.NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    /**
     * Starts the initialization process if it hasn't already been started
     */
    private void init(){
        if(!initializing) {
            Initializer init = new Initializer();
            InitializationDetails initializationDetails = new InitializationDetails(url, download, reset, deviceID, this);
            init.execute(initializationDetails);
            initializing = true;
        }
    }


    /**
     * Uploads the location of the user (if they are not in their privacy zone).
     * @param l The location to upload
     * @return True if request to upload was made, false otherwise
     */
    private boolean uploadLocation(Location l){

        long time = l.getTime();
        double longd = l.getLongitude();
        double lat = l.getLatitude();
        float spd = l.getSpeed();       //Meters per second
        double alt = l.getAltitude();   //Meters

        //If there is no privacy set or the user is currently outside the privacy circle upload the position
        if(privacyLocation == null || l.distanceTo(privacyLocation) > privacyRadius ) {

            //If we aren't already uploading the given location then continue with the upload
            if(!inProgress.contains(l)) {
                //Store the location as in progress, this will be removed after a successful upload
                inProgress.add(l);

                //Send to server
                LocationDetails ld = new LocationDetails(l, time, url, upload, this);
                Uploader uploader = new Uploader();
                uploader.execute(ld);

                //Update the UI with broadcast message:
                Intent intent = new Intent(AbstractTrackerActivity.BROADCAST_EVENT);
                intent.putExtra(AbstractTrackerActivity.BROADCAST_LATITUDE, lat);
                intent.putExtra(AbstractTrackerActivity.BROADCAST_LONGDITUDE, longd);
                intent.putExtra(AbstractTrackerActivity.BROADCAST_SPEED, spd);
                intent.putExtra(AbstractTrackerActivity.BROADCAST_ALTITUDE, alt);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                return true;
            }
            //We're already in the process of uploading this one, do nothing
            else{
                return false;
            }
        }
        //If we're in the privacy circle then don't upload
        else{
            Toast.makeText(getApplicationContext(), getString(R.string.toast_privacy_zone), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Uploads all the loose ends (Locations that couldn't be uploaded due to a lack of connection). These are done one by one.
     * @return True (once all upload requests are sent).
     */
    private boolean tieUpLooseEnds(){
        int length = looseEnds.size();
        if(length > 0){

            //Loop through all the loose ends and upload them. Start at the oldest and work to the latest
            for(int i=0; i<length; i++){
                Location l = looseEnds.elementAt(length-i-1);
                uploadLocation(l);
            }
            //Empty all the loose ends
            looseEnds.removeAllElements();

            if(toast)
                Toast.makeText(this, String.format(getString(R.string.toast_loose_ends), length), Toast.LENGTH_SHORT).show();
        }

        return true;
    }


    @Override
    public void onDestroy() {
        //When the service stops cancel the notification
        notificationManager.cancel(AbstractTrackerActivity.NOTIFICATION_ID);
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        //Update the location whenever a change is detected.
        updateLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Do nothing
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Do nothing
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Do nothing
    }

    @Override
    public void setUploadKey(String output) {
        //When an initialization request has been received check that an upload key was found.
        if(output != null){
            //Store the upload key
            upload = output;
            //Set the initialized variable to true (so we don't try it again)
            initialized = true;
            //Display message to the user.
            if(toast)
                Toast.makeText(this, getString(R.string.initialization_complete), Toast.LENGTH_SHORT).show();
        }
        //If initialization didn't work, display a message, will try again later.
        else{
            if(toast)
                Toast.makeText(this, getString(R.string.initialization_failed), Toast.LENGTH_SHORT).show();
        }
        //Regardless of the result we have now finished attempting to initialize
        initializing = false;
    }

    @Override
    public void handleUpload(UploadResponse response) {
        //Whatever the reponse we're not in the process of uploading it anymore so remove it from the in progress list.
        inProgress.remove(response.getLocation());
        //If the upload was a failure then add the location to the list of loose ends
        if(!response.success())
            looseEnds.add(response.getLocation());

        Log.i(this.getClass().toString(), String.format("Loose ends: %d In progress: %d", looseEnds.size(), inProgress.size()));
    }
}
