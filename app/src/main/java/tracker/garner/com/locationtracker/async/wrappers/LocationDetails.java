package tracker.garner.com.locationtracker.async.wrappers;

import android.location.Location;

import java.io.Serializable;

/**
 * Created by Phil on 24/02/2015.
 */
public class LocationDetails implements Serializable {

    private Location location = null;
    private long time = 0;
    private String url = null;
    private String password = null;

    public LocationDetails(Location location, long time, String url, String password) {
        this.location = location;
        this.time = time;
        this.url = url;
        this.password = password;
    }

    public Location getLocation() {
        return location;
    }

    public long getTime() {
        return time;
    }

    public String getUrl() {
        return url;
    }

    public String getPassword() {
        return password;
    }
}
