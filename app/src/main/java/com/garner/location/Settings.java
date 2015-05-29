package com.garner.location;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * @author Phil Garner
 * An activity used to pick adjust the settings for the app
 */
public class Settings extends AbstractTrackerActivity implements View.OnClickListener{

    private ImageButton map = null;
    private EditText radius = null;
    private TextView home = null;
    private TextView apiVersion = null;
    private CheckedTextView toastMode = null;
    private SharedPreferences settings = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //Get the UI elements
        map = (ImageButton)findViewById(R.id.settings_location_picker);
        home = (TextView)findViewById(R.id.settings_location);
        apiVersion = (TextView)findViewById(R.id.settings_api_version);
        radius = (EditText)findViewById(R.id.settings_radius);
        toastMode = (CheckedTextView)findViewById(R.id.settings_toast_mode);

        //Display the API version according to the constant set in the super class
        apiVersion.setText(API_VERSION);

        //Get the settings
        settings = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
        //Get the radius and display it
        int privacyDistance = settings.getInt(SETTINGS_PRIVACY_RADIUS, SETTINGS_DEFAULT_PRIVACY_RADIUS);
        radius.setText(privacyDistance + "");
        //Get the toast mode and display it
        toastMode.setChecked(settings.getBoolean(SETTINGS_TOAST_MODE, SETTINGS_DEFAULT_TOAST));

        //If we have a stored privacy position then use it
        if(settings.contains(SETTINGS_PRIVACY_LATITUDE) && settings.contains(SETTINGS_PRIVACY_LONGDITUDE)) {
            //Get the stored latitude and longditude
            String privacyLat = settings.getString(SETTINGS_PRIVACY_LATITUDE, "0");
            String privacyLong = settings.getString(SETTINGS_PRIVACY_LONGDITUDE, "0");
            //Build a string representing the position
            String privacyLocation = privacyLat + ", " + privacyLong;
            //Try and get a friendly name for the position
            try {
                //Build geocoder
                Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                //Get a list (of 1) addresses for the given location
                List<Address> addresses = gcd.getFromLocation(Double.parseDouble(privacyLat), Double.parseDouble(privacyLong), 1);
                //If we found an address then get the locality as a friendly name
                if(addresses != null &&addresses.size() > 0){
                    privacyLocation = addresses.get(0).getLocality();
                }
            }catch(Exception e){
                //Do nothing - just cope with the lat/long as the displayed privacy location
            }
            //Show the privacy location
            home.setText(privacyLocation);
        }
        //If no privacy location set then display message informing the user of this
        else{
            home.setText(getString(R.string.settings_no_privacy));
        }

        //Listen for clicks on the map button and checkbox
        map.setOnClickListener(this);
        toastMode.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        //When we leave the activity store the location for future use
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(SETTINGS_PRIVACY_RADIUS, Integer.parseInt(radius.getText().toString()));
        editor.putBoolean(SETTINGS_TOAST_MODE, toastMode.isChecked());
        editor.commit();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        //If map button clicked then launch the privacy location picker
        if(v == map){
            Intent i = new Intent(this, PrivacyPicker.class);
            startActivity(i);
        }
        else if(v == toastMode){
            toastMode.toggle();
        }
    }

}
