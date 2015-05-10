package tracker.garner.com.locationtracker.async;

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

import tracker.garner.com.locationtracker.TrackerActivity;
import tracker.garner.com.locationtracker.async.wrappers.InitializationDetails;

/**
 * Created by Phil on 24/02/2015.
 */
public class Initializer extends AsyncTask<InitializationDetails, Integer, String> {

    private InitializerHandler callback = null;

    public static final String SUCCESS = "success";
    public static final String UPLOAD_KEY = "key";

    @Override
    protected String doInBackground(InitializationDetails... params) {
        String urlToSend = TrackerActivity.getUsableURL(params[0].getUrl());
        String password = params[0].getPassword();
        int reset = params[0].isReset();
        callback = params[0].getCallback();
        String deviceID = params[0].getDeviceID();



        try {

            //URL to update: /api/v1/init/dl where "dl" is the download key
            urlToSend += TrackerActivity.URL_INIT + URLEncoder.encode(password, "UTF-8");

            //Reset
            String param = TrackerActivity.URL_INIT_RESET_PARAM;
            param += URLEncoder.encode(reset + "", "UTF-8");
            //Device ID
            param += TrackerActivity.URL_ADDITIONAL_PARAM;
            param += TrackerActivity.URL_INIT_DEVICE_PARAM;
            param += URLEncoder.encode(deviceID, "UTF-8");


            Log.i("Initializer.java", urlToSend);

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

            JSONObject output = new JSONObject(jsonString.toString());
            if(output.has(SUCCESS) && output.getBoolean(SUCCESS)){
                return output.getString(UPLOAD_KEY);
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
        callback.setUploadKey(output);
    }


}
