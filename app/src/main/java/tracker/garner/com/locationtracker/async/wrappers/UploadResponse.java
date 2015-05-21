package tracker.garner.com.locationtracker.async.wrappers;

import android.location.Location;

/**
 * @author Phil Garner
 * A class to encapsulate the response of an upload
 */
public class UploadResponse {
    private Location location = null;
    private int success = 0;

    /**
     * Builds an upload response object
     * @param location The location that was attempted to upload
     * @param success The response code
     */
    public UploadResponse(Location location, int success) {
        this.location = location;
        this.success = success;
    }

    /**
     * Gets the location that was attempted to upload
     * @return The location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets the response code from the uplaoad attempt
     * @return The response code
     */
    public int getSuccess() {
        return success;
    }

    /**
     * Returns a boolean interpretation of the success
     * @return True if the upload was a success, false otherwise
     */
    public boolean success(){
        return success > 0;
    }
}
