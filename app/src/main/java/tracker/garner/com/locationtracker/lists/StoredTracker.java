package tracker.garner.com.locationtracker.lists;

import java.io.Serializable;

/**
 * Created by Phil on 13/04/2015.
 */
public class StoredTracker implements Serializable {

    private long id = 0;
    private String url = null;
    private String download = null;
    private long frequency = 0;
    private long lastUpdate = 0;

    public StoredTracker(long id, String url, String download, long frequency, long lastUpdate) {
        this.lastUpdate = lastUpdate;
        this.id = id;
        this.url = url;
        this.download = download;
        this.frequency = frequency;
    }

    public StoredTracker(long id, String url, String download, long frequency) {
        this.id = id;
        this.url = url;
        this.download = download;
        this.frequency = frequency;
        this.lastUpdate = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getDownload() {
        return download;
    }

    public long getFrequency() {
        return frequency;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }
}
