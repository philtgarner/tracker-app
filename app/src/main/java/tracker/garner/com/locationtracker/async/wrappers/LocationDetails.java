package tracker.garner.com.locationtracker.async.wrappers;

import android.location.Location;

import java.io.Serializable;

import tracker.garner.com.locationtracker.async.UploaderHandler;

/**
 * @author Phil Garner
 * A wrapper for the details required to upload a location
 */
public class LocationDetails implements Serializable {

    private Location location = null;
    private long time = 0;
    private String url = null;
    private String password = null;
    private UploaderHandler upload = null;

    /**
     * The constructor to wrap the location and upload details in a single object
     * @param location The location to upload
     * @param time The timestamp for the location
     * @param url The URL to send to
     * @param password The upload key to send the location with
     * @param upload The callback handler to deal with the success/failure of an upload
     */
    public LocationDetails(Location location, long time, String url, String password, UploaderHandler upload) {
        this.location = location;
        this.time = time;
        this.url = url;
        this.password = password;
        this.upload = upload;
    }

    /**
     * Gets the location details to upload
     * @return The location details
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets the timestamp to upload with
     * @return The timestamp
     */
    public long getTime() {
        return time;
    }

    /**
     * Gets the URL to send the information to
     * @return The URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the upload key to send the information with
     * @return The upload key
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the upload handler for dealing with the success/failure of an upload
     * @return The upload handler that deals with the upload response
     */
    public UploaderHandler getUploadHandler(){
        return upload;
    }
}
