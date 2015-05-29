package com.garner.location.async;

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

import com.garner.location.AbstractTrackerActivity;
import com.garner.location.async.wrappers.LocationDetails;
import com.garner.location.async.wrappers.UploadResponse;

/**
 * @author Phil Garner
 * The asynchronous task for uploading the current position
 */
public class Uploader extends AsyncTask<LocationDetails, Integer, UploadResponse> {

    private UploaderHandler uploadHandler = null;


    @Override
    protected UploadResponse doInBackground(LocationDetails... params) {
        //Get all the info to send
        String urlToSend = params[0].getUrl();
        Location l = params[0].getLocation();
        long time = params[0].getTime();
        String password = params[0].getPassword();
        uploadHandler = params[0].getUploadHandler();

        //The response code after attempting to upload the location
        int responseCode = 0;

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
            if(output.has(AbstractTrackerActivity.JSON_RESPONSE_UPDATE_RESPONSE) && output.getBoolean(AbstractTrackerActivity.JSON_RESPONSE_UPDATE_RESPONSE)){
                responseCode = AbstractTrackerActivity.UPLOAD_RESPONSE_SUCCESS;
            }
            //If the JSON response does not include success then we've failed
            else {
                responseCode = AbstractTrackerActivity.UPLOAD_RESPONSE_FAILURE;
            }

        }
        catch(Exception e){
            Log.e("Tracker.java", "Error", e);
            responseCode = AbstractTrackerActivity.UPLOAD_RESPONSE_EXCEPTION;
        }

        UploadResponse response = new UploadResponse(l, responseCode);
        return response;
    }

    @Override
    protected void onPostExecute(UploadResponse response) {
        super.onPostExecute(response);
        //Let the upload handler deal with the response from the
        uploadHandler.handleUpload(response);
    }
}
