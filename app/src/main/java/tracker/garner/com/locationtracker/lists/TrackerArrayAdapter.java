package tracker.garner.com.locationtracker.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import tracker.garner.com.locationtracker.R;
import tracker.garner.com.locationtracker.TrackerActivity;

/**
 * Created by Phil on 14/04/2015.
 */
public class TrackerArrayAdapter extends ArrayAdapter<StoredTracker> {
    private Context context = null;
    private List<StoredTracker> values = null;


    public TrackerArrayAdapter(Context context, List<StoredTracker> values){
        super(context, R.layout.tracker_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.tracker_list_item, parent, false);
        TextView urlView = (TextView)rowView.findViewById(R.id.list_url);
        TextView downloadView = (TextView)rowView.findViewById(R.id.list_dl);
        TextView frequencyView = (TextView)rowView.findViewById(R.id.list_frequency);

        urlView.setText(values.get(position).getUrl());
        downloadView.setText(values.get(position).getDownload());
        frequencyView.setText(TrackerActivity.niceTime(values.get(position).getFrequency()*1000));

        return rowView;

    }


}

