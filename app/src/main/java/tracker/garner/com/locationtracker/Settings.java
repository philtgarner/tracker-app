package tracker.garner.com.locationtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import tracker.garner.com.locationtracker.lists.StoredTracker;
import tracker.garner.com.locationtracker.lists.TrackerArrayAdapter;
import tracker.garner.com.locationtracker.lists.sql.TrackerDataSource;


public class Settings extends TrackerActivity implements View.OnClickListener{



    private ImageButton map = null;
    private EditText radius = null;
    private TextView home = null;
    private TextView apiVersion = null;
    private SharedPreferences settings = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        map = (ImageButton)findViewById(R.id.settings_location_picker);
        home = (TextView)findViewById(R.id.settings_location);
        apiVersion = (TextView)findViewById(R.id.settings_api_version);
        radius = (EditText)findViewById(R.id.settings_radius);

        apiVersion.setText(API_VERSION);

        settings = getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
        int privacyDistance = settings.getInt(SETTINGS_PRIVACY_RADIUS, SETTINGS_DEFAULT_PRIVACY_RADIUS);
        radius.setText(privacyDistance + "");
        if(settings.contains(SETTINGS_PRIVACY_LATITUDE) && settings.contains(SETTINGS_PRIVACY_LONGDITUDE)) {
            String privacyLat = settings.getString(SETTINGS_PRIVACY_LATITUDE, "0");
            String privacyLong = settings.getString(SETTINGS_PRIVACY_LONGDITUDE, "0");
            String privacyLocation = privacyLat + ", " + privacyLong;
            try {
                Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = gcd.getFromLocation(Double.parseDouble(privacyLat), Double.parseDouble(privacyLong), 1);
                if(addresses.size() > 0){
                    privacyLocation = addresses.get(0).getLocality();
                }
            }catch(Exception e){
                //Do nothing - just cope with the lat/long as the displayed privacy location
            }
            home.setText(privacyLocation);
        }
        else{
            home.setText("None set");
        }

        map.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(SETTINGS_PRIVACY_RADIUS, Integer.parseInt(radius.getText().toString()));
        editor.commit();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        if(v == map){
            Intent i = new Intent(this, PrivacyPicker.class);
            startActivity(i);


        }
    }

}
