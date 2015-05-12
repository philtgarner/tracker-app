package tracker.garner.com.locationtracker.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import tracker.garner.com.locationtracker.R;
import tracker.garner.com.locationtracker.AbstractTrackerActivity;

/**
 * @author Phil Garner
 * An array adapter used to display stored trackers
 */
public class TrackerArrayAdapter extends ArrayAdapter<StoredTracker> {

    private Context context = null;
    private List<StoredTracker> values = null;

    /**
     * Creates a new array adapter for the given list
     * @param context The context to use
     * @param values The list to make the adapter from
     */
    public TrackerArrayAdapter(Context context, List<StoredTracker> values){
        super(context, R.layout.tracker_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        //Inflate the layour
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.tracker_list_item, parent, false);
        //Get the various UI elements
        TextView urlView = (TextView)rowView.findViewById(R.id.list_url);
        TextView downloadView = (TextView)rowView.findViewById(R.id.list_dl);
        TextView frequencyView = (TextView)rowView.findViewById(R.id.list_frequency);

        //Set the values for the layout elements
        urlView.setText(values.get(position).getUrl());
        downloadView.setText(values.get(position).getDownload());
        frequencyView.setText(AbstractTrackerActivity.niceTime(values.get(position).getFrequency() * 1000));

        return rowView;

    }


}

