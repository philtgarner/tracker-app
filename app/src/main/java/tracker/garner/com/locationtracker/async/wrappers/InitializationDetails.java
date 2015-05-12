package tracker.garner.com.locationtracker.async.wrappers;

import tracker.garner.com.locationtracker.async.InitializerHandler;

/**
 * @author Phil Garner
 * A wrapper for the details required for initialization
 */
public class InitializationDetails {

    private String url = null;
    private String password = null;
    private boolean reset = false;
    private String deviceID = null;
    private InitializerHandler callback = null;

    /**
     * Constructor for new initialization parameters wrapper
     * @param url The base URL of the REST API (excluding /api/vX/)
     * @param password The download key/password used to view the tracking points
     * @param reset True if to delete the existing points for this entry, false otherwise
     * @param deviceID The unique device ID for this device
     * @param callback The callback which handles the initialization success/failure
     */
    public InitializationDetails(String url, String password, boolean reset, String deviceID, InitializerHandler callback) {
        this.url = url;
        this.password = password;
        this.reset = reset;
        this.callback = callback;
        this.deviceID = deviceID;
    }

    /**
     * Gats the URL
     * @return The URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the download key/password
     * @return The download key
     */
    public String getPassword() {
        return password;
    }

    /**
     * Translates the reset value to an int
     * @return 1 if reset is true, 0 otherwise
     */
    public int isReset() {
        if(reset)
            return 1;
        return 0;
    }

    /**
     * Gets the callback that handles the success/failure of the initialization
     * @return The initialization handler
     */
    public InitializerHandler getCallback() {
        return callback;
    }

    /**
     * Gets the device ID
     * @return The device ID
     */
    public String getDeviceID(){
        return deviceID;
    }
}
