package tracker.garner.com.locationtracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.SecureRandom;

/**
 * Created by Phil on 23/02/2015.
 */
public abstract class TrackerActivity extends Activity{

    protected static final String EXTRA_URL = "extra_url";
    protected static final String EXTRA_PASSWORD = "extra_pass";
    protected static final String EXTRA_FREQUENCY = "extra_freq";
    protected static final String EXTRA_RESET = "extra_reset";
    protected static final String EXTRA_DEVICE_ID = "extra_device";
    protected static final String EXTRA_UPLOAD = "extra_upl";
    protected static final String EXTRA_STORED_TRACKER = "extra_tracker";

    protected static final String SETTINGS_NAME = "tracker_settings";
    protected static final String SETTINGS_PRIVACY_RADIUS = "tracker_settings_radius";
    protected static final String SETTINGS_PRIVACY_LATITUDE = "tracker_settings_lat";
    protected static final String SETTINGS_PRIVACY_LONGDITUDE = "tracker_settings_long";
    protected static final String SETTINGS_PRIVACY_LOCATION = "tracker_settings_location";
    protected static final String SETTINGS_URL = "tracker_settings_url";
    protected static final String SETTINGS_PASSWORD = "tracker_settings_password";
    protected static final String SETTINGS_FREQUENCY = "tracker_settings_frequency";
    public static final String SETTINGS_UPLOAD = "tracker_settings_upload";
    protected static final String SETTINGS_DEVICE_ID = "tracker_settings_dev_id";
    protected static final int SETTINGS_DEFAULT_PRIVACY_RADIUS = 500;

    public static final String BROADCAST_EVENT = "tracker_broadcast";
    public static final String BROADCAST_LONGDITUDE = "tracker_broadcast_long";
    public static final String BROADCAST_LATITUDE = "tracker_broadcast_lat";
    public static final String BROADCAST_SPEED = "tracker_broadcast_speed";
    public static final String BROADCAST_ALTITUDE = "tracker_broadcast_alt";

    protected static final int NOTIFICATION_ID = 1912;

    public static final String SERVICE_NAME = "GarnerTrackerService";

    protected static final String API_VERSION = "v1";
    private static final String URL_DIR = "/api/" + API_VERSION + "/";
    public static final String URL_INIT = URL_DIR + "init/";
    public static final String URL_UPDATE = URL_DIR + "update/";
    public static final String URL_FIRST_PARAM = "?";
    public static final String URL_ADDITIONAL_PARAM = "&";
    public static final String URL_INIT_DL_PARAM = "down=";
    public static final String URL_INIT_RESET_PARAM = "reset=";
    public static final String URL_INIT_DEVICE_PARAM = "device=";
    public static final String URL_UPDATE_UPLOAD_PARAM = "pass=";
    public static final String URL_UPDATE_LATITUDE_PARAM = "lat=";
    public static final String URL_UPDATE_LONGDITUDE_PARAM = "long=";
    public static final String URL_UPDATE_SPEED_PARAM = "speed=";
    public static final String URL_UPDATE_ALTITUDE_PARAM = "alt=";
    public static final String URL_UPDATE_TIME_PARAM = "dt=";
    public static final String URL_TRACKER = "/tracker";
    public static final String URL_TRACK = "/track";
    public static final String URL_TRACKER_DOWNLOAD = "dl=";



    public static final String getUsableURL(String givenURL){
        //Remove slash from end and add http:// at the beginning
        if(givenURL.endsWith("/")){
            givenURL = givenURL.substring(0,givenURL.length()-1);
        }
        if(!givenURL.startsWith("http://") && !givenURL.startsWith("https://")){
            givenURL = "http://" + givenURL;
        }

        return givenURL;
    }

    public static final String getShareURL(String givenURL, String dl){
        try {
            String usableURL = getUsableURL(givenURL);
            usableURL += URL_TRACK;
            usableURL += "/";
            usableURL += URLEncoder.encode(dl, "UTF-8");
            return usableURL;
        }catch(UnsupportedEncodingException e){
            return null;
        }
    }


    public static final String niceTime(long milliseconds){
        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);

        String output = "";

        if(hours != 0)
            output += hours + " hours";
        if(minutes != 0)
            output += " " + minutes + " minutes";
        if(seconds != 0)
            output += " " + seconds + " seconds";
        return output;
    }

    protected boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static final String generateID(){
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

}
