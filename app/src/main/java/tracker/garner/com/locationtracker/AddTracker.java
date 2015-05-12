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

/**
 * @author Phil Garner
 * The activity used to add an entry to the list of trackers
 */
public class AddTracker extends AbstractTrackerActivity implements View.OnClickListener, TextWatcher{

    private Button go = null;
    private EditText url = null;
    private EditText password = null;
    private EditText frequency = null;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        //Get the components of the UI
        go = (Button)findViewById(R.id.add);
        url = (EditText)findViewById(R.id.url);
        password = (EditText)findViewById(R.id.password);
        frequency = (EditText)findViewById(R.id.frequency);

        //Ger the settings so we can use the last used URL and frequency
        SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, 0);
        url.setText(settings.getString(SETTINGS_URL, ""));
        frequency.setText(settings.getString(SETTINGS_FREQUENCY, "10"));

        //Listen to changes to the URL and password - so we can update the sharing URL
        url.addTextChangedListener(this);
        password.addTextChangedListener(this);

        //Listen for clicks on the "add" button
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

    /**
     * Updates the URL that is used when sharing the tracking link
     */
    private void updateShare(){
        //Set up the sharing intent
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        i.putExtra(Intent.EXTRA_TEXT, getShareURL(url.getText().toString(), password.getText().toString()));
        setShareIntent(i);
    }

    /**
     * Updates the intent that will be shared when selected from the menu
     * @param shareIntent The intent to share
     */
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onClick(View v) {
        //If the user clicked "add"
        if(v == go){

            //Fire up the tracker data source to load the information from the SQLite database
            TrackerDataSource dataSource = new TrackerDataSource(this);
            try {
                //Open the database
                dataSource.open();
                //Add the information entered on this screen
                dataSource.createStoredTracker(url.getText().toString(), password.getText().toString(), Integer.parseInt(frequency.getText().toString()));
            }catch(SQLException e){
                //There should never be an exception but log it if there is
                Log.e(AddTracker.class.getName(), e.getMessage(), e);
                //Display a quick error message to the user
                Toast.makeText(this, getString(R.string.error_adding_tracker) + e.getErrorCode(), Toast.LENGTH_SHORT).show();
            }


            //Store the URL and frequency for the next key added
            SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor settingsEditor = settings.edit();
            settingsEditor.putString(SETTINGS_URL, url.getText().toString());
            settingsEditor.putString(SETTINGS_FREQUENCY, frequency.getText().toString());
            settingsEditor.commit();

            //Display the list of trackings, this should now include the newly added entry
            Intent i = new Intent(this, TrackerList.class);
            startActivity(i);

        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //Do nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //Do nothing
    }

    @Override
    public void afterTextChanged(Editable s) {
        //After text is changed update the sharing URL
        updateShare();
    }
}
