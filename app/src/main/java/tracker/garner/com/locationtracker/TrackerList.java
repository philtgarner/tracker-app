package tracker.garner.com.locationtracker;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import tracker.garner.com.locationtracker.lists.StoredTracker;
import tracker.garner.com.locationtracker.lists.TrackerArrayAdapter;
import tracker.garner.com.locationtracker.lists.sql.TrackerDataSource;


public class TrackerList extends TrackerActivity implements View.OnClickListener, AdapterView.OnItemClickListener, MenuItem.OnMenuItemClickListener{

    private FloatingActionButton add = null;
    private ListView list = null;

    private MenuItem settings = null;

    private List<StoredTracker> storedTrackers = null;
    private TrackerArrayAdapter arrayAdapter = null;
    private TrackerDataSource dataSource = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        add = (FloatingActionButton)findViewById(R.id.fab);
        list = (ListView)findViewById(R.id.list);

        add.attachToListView(list);

        add.setOnClickListener(this);

        list.setOnItemClickListener(this);

        //Get the stored trackers
        try {
            dataSource = new TrackerDataSource(this);
            dataSource.open();
            storedTrackers = dataSource.getAllTrackers();

            arrayAdapter = new TrackerArrayAdapter(this, storedTrackers);
            list.setAdapter(arrayAdapter);

        }catch (SQLException e){
            Log.e(TrackerList.class.getName(), e.getMessage());
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Locate MenuItem with ShareActionProvider
        settings = menu.findItem(R.id.settings);

        settings.setOnMenuItemClickListener(this);

        // Return true to display menu
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item == settings){
            //Start the activity to show the service progress
            Intent i = new Intent(this, Settings.class);
            startActivity(i);
        }
        return true;
    }




    @Override
    public void onClick(View v) {
        if(v == add){
            Intent i = new Intent(this, AddTracker.class);
            startActivity(i);


        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        StoredTracker t = storedTrackers.get(position);
        Intent i = new Intent(this, LaunchTracker.class);
        i.putExtra(EXTRA_STORED_TRACKER, t);
        startActivity(i);
    }
}
