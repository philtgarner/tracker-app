package com.garner.location.lists;

import java.io.Serializable;

/**
 * @author Phil Garner
 * A class to encapsulate the details of tracking information stored in the SQLite database
 */
public class StoredTracker implements Serializable {

    private long id = 0;
    private String url = null;
    private String download = null;
    private long frequency = 0;
    private long lastUpdate = 0;

    /**
     * Constructor for a new stored tracker - never call this directly, it will be called when inserting into the database
     * @param id The ID for the new stored tracker
     * @param url The URL to send the information to
     * @param download The download key
     * @param frequency The frequency to update with
     * @param lastUpdate The time (in milliseconds) when this tracker was last used
     */
    public StoredTracker(long id, String url, String download, long frequency, long lastUpdate) {
        this.lastUpdate = lastUpdate;
        this.id = id;
        this.url = url;
        this.download = download;
        this.frequency = frequency;
    }

    /**
     * Gets the ID of this tracker
     * @return The ID
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the URL to send this tracker info to
     * @return The URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the download key for this tracker
     * @return The download key
     */
    public String getDownload() {
        return download;
    }

    /**
     * Gets the frequency with which this tracker should be updated
     * @return The update frequency
     */
    public long getFrequency() {
        return frequency;
    }

    /**
     * Gets the last update time for this tracker
     * @return The last update time
     */
    public long getLastUpdate() {
        return lastUpdate;
    }
}
