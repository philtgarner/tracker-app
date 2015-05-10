package tracker.garner.com.locationtracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.SQLException;

import tracker.garner.com.locationtracker.async.Initializer;
import tracker.garner.com.locationtracker.async.InitializerHandler;
import tracker.garner.com.locationtracker.async.wrappers.InitializationDetails;
import tracker.garner.com.locationtracker.lists.StoredTracker;
import tracker.garner.com.locationtracker.lists.sql.TrackerDataSource;


public class LaunchTracker extends TrackerActivity implements View.OnClickListener{

    private Button go = null;
    private TextView url = null;
    private TextView download = null;
    private TextView frequency = null;
    private CheckBox reset = null;

    private StoredTracker tracker = null;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        go = (Button) findViewById(R.id.launch);
        url = (TextView) findViewById(R.id.launch_url);
        download = (TextView) findViewById(R.id.launch_password);
        frequency = (TextView) findViewById(R.id.launch_frequency);
        reset = (CheckBox) findViewById(R.id.reset);


        //Get the stuff from the previous page
        Intent intent = getIntent();
        tracker = (StoredTracker) intent.getSerializableExtra(EXTRA_STORED_TRACKER);


        url.setText(tracker.getUrl());
        download.setText(tracker.getDownload());
        frequency.setText(TrackerActivity.niceTime(tracker.getFrequency()*1000));

        go.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.share, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Share tracking URL");
        i.putExtra(Intent.EXTRA_TEXT, getShareURL(tracker.getUrl(), tracker.getDownload()));
        setShareIntent(i);

        // Return true to display menu
        return true;
    }



    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onClick(View v) {
        if(v == go){
            //Get the device ID
            SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
            String deviceID = null;
            //If the device doesn't have an ID yet, make one.
            if(!settings.contains(SETTINGS_DEVICE_ID)){
                //Generate and save the ID
                SharedPreferences.Editor settingsEditor = settings.edit();
                deviceID = generateID();
                settingsEditor.putString(SETTINGS_DEVICE_ID, deviceID);
                settingsEditor.commit();
            }
            else{
                //If there is already an ID, get it
                deviceID = settings.getString(SETTINGS_DEVICE_ID, null);
            }

            //Update the list so the last used gets put to the top
            TrackerDataSource dataSource = new TrackerDataSource(this);
            try {
                dataSource.open();
                dataSource.updateTime(tracker);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            String urlString = getUsableURL(tracker.getUrl());


            //Start the service
            Intent serviceIntent = new Intent(this, TrackingService.class);
            serviceIntent.putExtra(EXTRA_URL, urlString);
            serviceIntent.putExtra(EXTRA_PASSWORD, tracker.getDownload());
            serviceIntent.putExtra(EXTRA_FREQUENCY, Long.parseLong(tracker.getFrequency() + ""));
            serviceIntent.putExtra(EXTRA_RESET, reset.isChecked());
            serviceIntent.putExtra(EXTRA_DEVICE_ID, deviceID);
            startService(serviceIntent);

            //Start the activity to show the service progress
            Intent i = new Intent(this, Tracker.class);
            i.putExtra(EXTRA_URL, urlString);
            i.putExtra(EXTRA_PASSWORD, tracker.getDownload());
            startActivity(i);



        }
    }


}
