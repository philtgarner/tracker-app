package com.garner.location;

import android.app.Activity;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.SecureRandom;

/**
 * @author Phil Garner
 * An abstract class that all tracking activities extend from. Contains commonly used methods and constants
 */
public abstract class AbstractTrackerActivity extends Activity{

    //Constants for passing info between activities
    protected static final String EXTRA_URL = "extra_url";
    protected static final String EXTRA_PASSWORD = "extra_pass";
    protected static final String EXTRA_FREQUENCY = "extra_freq";
    protected static final String EXTRA_RESET = "extra_reset";
    protected static final String EXTRA_DEVICE_ID = "extra_device";
    protected static final String EXTRA_UPLOAD = "extra_upl";
    protected static final String EXTRA_STORED_TRACKER = "extra_tracker";

    //Constants for the stored settings
    public static final String SETTINGS_UPLOAD = "tracker_settings_upload";
    protected static final String SETTINGS_NAME = "tracker_settings";
    protected static final String SETTINGS_PRIVACY_RADIUS = "tracker_settings_radius";
    protected static final String SETTINGS_PRIVACY_LATITUDE = "tracker_settings_lat";
    protected static final String SETTINGS_PRIVACY_LONGDITUDE = "tracker_settings_long";
    protected static final String SETTINGS_PRIVACY_LOCATION = "tracker_settings_location";
    protected static final String SETTINGS_URL = "tracker_settings_url";
    protected static final String SETTINGS_PASSWORD = "tracker_settings_password";
    protected static final String SETTINGS_FREQUENCY = "tracker_settings_frequency";
    protected static final String SETTINGS_DEVICE_ID = "tracker_settings_dev_id";
    protected static final String SETTINGS_TOAST_MODE = "tracker_settings_toast";
    protected static final boolean SETTINGS_DEFAULT_TOAST = false;
    protected static final int SETTINGS_DEFAULT_PRIVACY_RADIUS = 500;

    //Constants for sending/receiving broadcast messages from the service to the tracking UI
    public static final String BROADCAST_EVENT = "tracker_broadcast";
    public static final String BROADCAST_LONGDITUDE = "tracker_broadcast_long";
    public static final String BROADCAST_LATITUDE = "tracker_broadcast_lat";
    public static final String BROADCAST_SPEED = "tracker_broadcast_speed";
    public static final String BROADCAST_ALTITUDE = "tracker_broadcast_alt";

    //ID for the notification - used to update the notification where necessary.
    protected static final int NOTIFICATION_ID = 1912;

    //The version of the API used, also used in the URL construction
    protected static final String API_VERSION = "v1";

    //Constants used to make up the URL to send the location details to
    private static final String URL_DIR = "/api/" + API_VERSION + "/";
    public static final String URL_INIT = URL_DIR + "init/";
    public static final String URL_UPDATE = URL_DIR + "update/";
    public static final String URL_FIRST_PARAM = "?";
    public static final String URL_ADDITIONAL_PARAM = "&";
    public static final String URL_INIT_RESET_PARAM = "reset=";
    public static final String URL_INIT_DEVICE_PARAM = "device=";
    public static final String URL_UPDATE_LATITUDE_PARAM = "lat=";
    public static final String URL_UPDATE_LONGDITUDE_PARAM = "long=";
    public static final String URL_UPDATE_SPEED_PARAM = "speed=";
    public static final String URL_UPDATE_ALTITUDE_PARAM = "alt=";
    public static final String URL_UPDATE_TIME_PARAM = "dt=";
    public static final String URL_TRACK = "/track";

    //JSON response keys
    public static final String JSON_RESPONSE_INIT_SUCCESS = "success";
    public static final String JSON_RESPONSE_INIT_UPLOAD_KEY = "key";
    public static final String JSON_RESPONSE_UPDATE_RESPONSE = "response";
    public static final String JSON_RESPONSE_UPDATE_TIME = "date_time";

    public static final int UPLOAD_RESPONSE_SUCCESS = 1;
    public static final int UPLOAD_RESPONSE_FAILURE = -1;
    public static final int UPLOAD_RESPONSE_EXCEPTION = -2;




    /**
     * Ensures the given URL is in a usable form. Adds http:// at the beginning if needed and removes trailing slash
     * @param givenURL The URL to neaten up
     * @return The URL which is safe to use
     */
    public static final String getUsableURL(String givenURL){
        if(givenURL.endsWith("/")){
            givenURL = givenURL.substring(0,givenURL.length()-1);
        }
        if(!givenURL.startsWith("http://") && !givenURL.startsWith("https://")){
            givenURL = "http://" + givenURL;
        }
        return givenURL;
    }

    /**
     * Gets the URL to be used when called from the sharing menu. This is the URL observers will use to view the tracking details
     * @param givenURL The base URL of the REST interface
     * @param dl The download key
     * @return URL safe for sharing
     */
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


    /**
     * Formats the time in milliseconds into hours, minutes and seconds
     * @param milliseconds The time in milliseconds
     * @return Nicely formatted time
     */
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

    /**
     * Generates a random alphanumeric String to act as a unique ID for this device
     * @return A random String
     */
    public static final String generateID(){
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

}
