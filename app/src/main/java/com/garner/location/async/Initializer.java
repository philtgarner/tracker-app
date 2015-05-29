package com.garner.location.async;

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
import com.garner.location.async.wrappers.InitializationDetails;

/**
 * @author Phil Garner
 * The asynchrinous task that sends a request to initialise the system
 */
public class Initializer extends AsyncTask<InitializationDetails, Integer, String> {

    private InitializerHandler callback = null;

    @Override
    protected String doInBackground(InitializationDetails... params) {
        //Get all the information regarding the upload location and callbacks etc.
        String urlToSend = AbstractTrackerActivity.getUsableURL(params[0].getUrl());
        String password = params[0].getPassword();
        int reset = params[0].isReset();
        callback = params[0].getCallback();
        String deviceID = params[0].getDeviceID();

        try {

            //URL to update: /api/v1/init/dl where "dl" is the download key
            urlToSend += AbstractTrackerActivity.URL_INIT + URLEncoder.encode(password, "UTF-8");

            //Reset
            String param = AbstractTrackerActivity.URL_INIT_RESET_PARAM;
            param += URLEncoder.encode(reset + "", "UTF-8");
            //Device ID
            param += AbstractTrackerActivity.URL_ADDITIONAL_PARAM;
            param += AbstractTrackerActivity.URL_INIT_DEVICE_PARAM;
            param += URLEncoder.encode(deviceID, "UTF-8");


            //Connect to the URL
            URL url = new URL(urlToSend);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(false);
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);

            //Send the params
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

            //Close all connections and input streams
            is.close();
            connection.disconnect();

            //Parse the response
            JSONObject output = new JSONObject(jsonString.toString());
            //If we have a valid response return it, otherwise return null
            if(output.has(AbstractTrackerActivity.JSON_RESPONSE_INIT_SUCCESS) && output.getBoolean(AbstractTrackerActivity.JSON_RESPONSE_INIT_SUCCESS)){
                return output.getString(AbstractTrackerActivity.JSON_RESPONSE_INIT_UPLOAD_KEY);
            }
            else{
                return null;
            }
        }
        catch(Exception e){
            Log.e("Tracker.java", "Error", e);
            return null;
        }
    }

    @Override
    public void onPostExecute(String output){
        //After getting upload key send it to the callback to handle
        callback.setUploadKey(output);
    }


}
