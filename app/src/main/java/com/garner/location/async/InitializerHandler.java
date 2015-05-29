package com.garner.location.async;

/**
 * @author Phil Garner
 * An interface that activities wanting to initialize the ststem should implement
 */
public interface InitializerHandler {

    /**
     * Handles the upload key appropriately - stores it for later use.
     * @param output The upload key to handle
     */
    void setUploadKey(String output);
}
