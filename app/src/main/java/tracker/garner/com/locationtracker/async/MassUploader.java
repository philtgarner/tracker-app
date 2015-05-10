package tracker.garner.com.locationtracker.async;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import tracker.garner.com.locationtracker.TrackerActivity;
import tracker.garner.com.locationtracker.async.wrappers.LocationDetails;

/**
 * Created by Phil on 24/02/2015.
 */
//TODO Make this build the appropriate POST request
public class MassUploader extends AsyncTask<LocationDetails, Integer, Boolean> {


    private static final String RESPONSE = "response";

    @Override
    protected Boolean doInBackground(LocationDetails... params) {
        String urlToSend = params[0].getUrl();
        Location l = params[0].getLocation();
        long time = params[0].getTime();
        String password = params[0].getPassword();


        try {

            urlToSend += TrackerActivity.URL_UPDATE;
            //Download key
            urlToSend += TrackerActivity.URL_FIRST_PARAM;
            urlToSend += TrackerActivity.URL_UPDATE_UPLOAD_PARAM;
            urlToSend += URLEncoder.encode(password, "UTF-8");
            //Latitude
            urlToSend += TrackerActivity.URL_ADDITIONAL_PARAM;
            urlToSend += TrackerActivity.URL_UPDATE_LATITUDE_PARAM;
            urlToSend += URLEncoder.encode(l.getLatitude() + "", "UTF-8");
            //Longditude
            urlToSend += TrackerActivity.URL_ADDITIONAL_PARAM;
            urlToSend += TrackerActivity.URL_UPDATE_LONGDITUDE_PARAM;
            urlToSend += URLEncoder.encode(l.getLongitude() + "", "UTF-8");
            //Speed
            urlToSend += TrackerActivity.URL_ADDITIONAL_PARAM;
            urlToSend += TrackerActivity.URL_UPDATE_SPEED_PARAM;
            urlToSend += URLEncoder.encode(l.getSpeed() + "", "UTF-8");
            //Altitude
            urlToSend += TrackerActivity.URL_ADDITIONAL_PARAM;
            urlToSend += TrackerActivity.URL_UPDATE_ALTITUDE_PARAM;
            urlToSend += URLEncoder.encode(l.getAltitude() + "", "UTF-8");
            //Date time
            urlToSend += TrackerActivity.URL_ADDITIONAL_PARAM;
            urlToSend += TrackerActivity.URL_UPDATE_TIME_PARAM;
            urlToSend += URLEncoder.encode(time + "", "UTF-8");

            Log.i("Uploader.java", urlToSend);

            //Build the URL and connect to it with the right page number
            URL url = new URL(urlToSend);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(false);
            connection.connect();
            InputStream is = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder jsonString = new StringBuilder(1024);
            String l1 = "";
            while((l1 = reader.readLine()) != null)
                jsonString.append(l1);

            is.close();
            connection.disconnect();

            JSONObject output = new JSONObject(jsonString.toString());
            if(output.has(RESPONSE)){
                return output.getBoolean(RESPONSE);
            }
            return false;

        }
        catch(Exception e){
            Log.e("Tracker.java", "Error", e);
            return false;
        }
    }


}
