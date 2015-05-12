package tracker.garner.com.locationtracker.async;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import tracker.garner.com.locationtracker.AbstractTrackerActivity;
import tracker.garner.com.locationtracker.async.wrappers.LocationDetails;

/**
 * @author Phil Garner
 * The asynchronous task for uploading the current position
 */
public class Uploader extends AsyncTask<LocationDetails, Integer, Boolean> {


    @Override
    protected Boolean doInBackground(LocationDetails... params) {
        //Get all the info to send
        String urlToSend = params[0].getUrl();
        Location l = params[0].getLocation();
        long time = params[0].getTime();
        String password = params[0].getPassword();

        try {
            //URL to update: /api/v1/update/ul where "ul" is the upload key
            urlToSend += AbstractTrackerActivity.URL_UPDATE + URLEncoder.encode(password, "UTF-8");


            //Latitude
            String param = AbstractTrackerActivity.URL_UPDATE_LATITUDE_PARAM;
            param += URLEncoder.encode(l.getLatitude() + "", "UTF-8");
            //Longditude
            param += AbstractTrackerActivity.URL_ADDITIONAL_PARAM;
            param += AbstractTrackerActivity.URL_UPDATE_LONGDITUDE_PARAM;
            param += URLEncoder.encode(l.getLongitude() + "", "UTF-8");
            //Speed
            param += AbstractTrackerActivity.URL_ADDITIONAL_PARAM;
            param += AbstractTrackerActivity.URL_UPDATE_SPEED_PARAM;
            param += URLEncoder.encode(l.getSpeed() + "", "UTF-8");
            //Altitude
            param += AbstractTrackerActivity.URL_ADDITIONAL_PARAM;
            param += AbstractTrackerActivity.URL_UPDATE_ALTITUDE_PARAM;
            param += URLEncoder.encode(l.getAltitude() + "", "UTF-8");
            //Date time
            param += AbstractTrackerActivity.URL_ADDITIONAL_PARAM;
            param += AbstractTrackerActivity.URL_UPDATE_TIME_PARAM;
            param += URLEncoder.encode(time + "", "UTF-8");


            //Connect to the URL
            URL url = new URL(urlToSend);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(false);
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(param);
            writer.flush();
            writer.close();
            os.close();

            connection.connect();

            //Get the response
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonString = new StringBuilder(1024);
            String l1 = "";
            while((l1 = reader.readLine()) != null)
                jsonString.append(l1);

            //Close the connection and input streams
            is.close();
            connection.disconnect();

            //Log.d(this.getClass().toString(), jsonString.toString());

            //Parse the response
            JSONObject output = new JSONObject(jsonString.toString());
            //If we have success return true
            if(output.has(AbstractTrackerActivity.JSON_RESPONSE_UPDATE_RESPONSE)){
                return output.getBoolean(AbstractTrackerActivity.JSON_RESPONSE_UPDATE_RESPONSE);
            }
            return false;

        }
        catch(Exception e){
            Log.e("Tracker.java", "Error", e);
            return false;
        }
    }


}
