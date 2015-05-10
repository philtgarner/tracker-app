package tracker.garner.com.locationtracker.async;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import java.util.ArrayList;
import java.util.List;

import tracker.garner.com.locationtracker.TrackerActivity;
import tracker.garner.com.locationtracker.async.wrappers.LocationDetails;

/**
 * Created by Phil on 24/02/2015.
 */
public class Uploader extends AsyncTask<LocationDetails, Integer, Boolean> {


    private static final String RESPONSE = "response";

    @Override
    protected Boolean doInBackground(LocationDetails... params) {
        String urlToSend = params[0].getUrl();
        Location l = params[0].getLocation();
        long time = params[0].getTime();
        String password = params[0].getPassword();

        try {
            //URL to update: /api/v1/update/ul where "ul" is the upload key
            urlToSend += TrackerActivity.URL_UPDATE + URLEncoder.encode(password, "UTF-8");


            //Latitude
            String param = TrackerActivity.URL_UPDATE_LATITUDE_PARAM;
            param += URLEncoder.encode(l.getLatitude() + "", "UTF-8");
            //Longditude
            param += TrackerActivity.URL_ADDITIONAL_PARAM;
            param += TrackerActivity.URL_UPDATE_LONGDITUDE_PARAM;
            param += URLEncoder.encode(l.getLongitude() + "", "UTF-8");
            //Speed
            param += TrackerActivity.URL_ADDITIONAL_PARAM;
            param += TrackerActivity.URL_UPDATE_SPEED_PARAM;
            param += URLEncoder.encode(l.getSpeed() + "", "UTF-8");
            //Altitude
            param += TrackerActivity.URL_ADDITIONAL_PARAM;
            param += TrackerActivity.URL_UPDATE_ALTITUDE_PARAM;
            param += URLEncoder.encode(l.getAltitude() + "", "UTF-8");
            //Date time
            param += TrackerActivity.URL_ADDITIONAL_PARAM;
            param += TrackerActivity.URL_UPDATE_TIME_PARAM;
            param += URLEncoder.encode(time + "", "UTF-8");


            Log.i("Uploader.java", urlToSend);

            //Build the URL and connect to it with the right page number
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

            InputStream is = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder jsonString = new StringBuilder(1024);
            String l1 = "";
            while((l1 = reader.readLine()) != null)
                jsonString.append(l1);

            is.close();
            connection.disconnect();

            Log.d(this.getClass().toString(), jsonString.toString());



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
