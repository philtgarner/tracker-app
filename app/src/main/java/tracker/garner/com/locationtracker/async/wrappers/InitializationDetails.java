package tracker.garner.com.locationtracker.async.wrappers;

import tracker.garner.com.locationtracker.async.InitializerHandler;

/**
 * Created by Phil on 05/03/2015.
 */
public class InitializationDetails {

    private String url = null;
    private String password = null;
    private boolean reset = false;
    private String deviceID = null;
    private InitializerHandler callback = null;

    public InitializationDetails(String url, String password, boolean reset, String deviceID, InitializerHandler callback) {
        this.url = url;
        this.password = password;
        this.reset = reset;
        this.callback = callback;
        this.deviceID = deviceID;
    }

    public String getUrl() {
        return url;
    }

    public String getPassword() {
        return password;
    }

    public int isReset() {
        if(reset)
            return 1;
        return 0;
    }

    public InitializerHandler getCallback() {
        return callback;
    }

    public String getDeviceID(){
        return deviceID;
    }
}
