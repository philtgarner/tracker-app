package tracker.garner.com.locationtracker.async;

import tracker.garner.com.locationtracker.async.wrappers.UploadResponse;

/**
 * @author Phil Garner
 * An interface to enable activities to handle the success/failure of a location upload
 */
public interface UploaderHandler {

    /**
     * Handles the upload of a location
     * @param response The upload response to handle
     */
    void handleUpload(UploadResponse response);

}
