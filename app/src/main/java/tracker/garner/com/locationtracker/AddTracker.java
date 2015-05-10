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
import android.widget.Toast;

import java.sql.SQLException;

import tracker.garner.com.locationtracker.async.InitializerHandler;
import tracker.garner.com.locationtracker.lists.sql.TrackerDataSource;


public class AddTracker extends TrackerActivity implements View.OnClickListener, InitializerHandler, TextWatcher{

    private Button go = null;
    private EditText url = null;
    private EditText password = null;
    private EditText frequency = null;
    private CheckBox reset = null;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        go = (Button)findViewById(R.id.add);
        url = (EditText)findViewById(R.id.url);
        password = (EditText)findViewById(R.id.password);
        frequency = (EditText)findViewById(R.id.frequency);
        reset = (CheckBox)findViewById(R.id.reset);

        SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, 0);
        url.setText(settings.getString(SETTINGS_URL, ""));
        frequency.setText(settings.getString(SETTINGS_FREQUENCY, "10"));

        url.addTextChangedListener(this);
        password.addTextChangedListener(this);

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


        updateShare();


        // Return true to display menu
        return true;
    }

    private void updateShare(){
        //Set up the sharing intent
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Share tracking URL");
        i.putExtra(Intent.EXTRA_TEXT, getShareURL(url.getText().toString(), password.getText().toString()));
        setShareIntent(i);
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

            TrackerDataSource dataSource = new TrackerDataSource(this);
            try {
                dataSource.open();
                dataSource.createStoredTracker(url.getText().toString(), password.getText().toString(), Integer.parseInt(frequency.getText().toString()));
            }catch(SQLException e){
                Log.e(AddTracker.class.getName(), e.getMessage(), e);
            }


            //Store the URL and frequency for the next key added
            SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor settingsEditor = settings.edit();
            settingsEditor.putString(SETTINGS_URL, url.getText().toString());
            settingsEditor.putString(SETTINGS_FREQUENCY, frequency.getText().toString());
            settingsEditor.commit();

            //Go back to the list
            Intent i = new Intent(this, TrackerList.class);
            startActivity(i);

            /*

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

            //Generate up key
            Initializer init = new Initializer();
            InitializationDetails initializationDetails = new InitializationDetails(url.getText().toString(), password.getText().toString(), reset.isChecked(), deviceID, this);
            init.execute(initializationDetails);

            */
        }
    }

    @Override
    public void setUploadKey(String output) {
        if(output != null){

            String urlString = getUsableURL(url.getText().toString());

            Intent i = new Intent(this, Tracker.class);
            i.putExtra(EXTRA_URL, urlString);
            i.putExtra(EXTRA_PASSWORD, password.getText().toString());
            i.putExtra(EXTRA_FREQUENCY, Long.parseLong(frequency.getText().toString()));
            i.putExtra(EXTRA_UPLOAD, output);

            SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor settingsEditor = settings.edit();
            settingsEditor.putString(SETTINGS_URL, url.getText().toString());
            settingsEditor.putString(SETTINGS_PASSWORD, password.getText().toString());
            settingsEditor.putString(SETTINGS_FREQUENCY, frequency.getText().toString());
            settingsEditor.putString(SETTINGS_UPLOAD, output);

            settingsEditor.commit();

            startActivity(i);

        }
        else{
            Toast.makeText(this, "Something went wrong with the initialization", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        updateShare();
    }
}
