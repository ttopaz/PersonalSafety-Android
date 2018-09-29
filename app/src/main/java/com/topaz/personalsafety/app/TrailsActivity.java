package com.topaz.personalsafety.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;


public class TrailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trails);

        Bundle bundle = getIntent().getExtras();
        Gson gson = new GsonBuilder().serializeNulls().create();
        String trailsJson = bundle.getString("trails");
        ArrayList<LocationTrail> data = gson.fromJson(trailsJson, new TypeToken<ArrayList<LocationTrail>>(){}.getType());

        TrailsAdapter adapter = new TrailsAdapter(this, data);

        ListView listView = (ListView) findViewById(R.id.trailsList);

        listView.setAdapter(adapter);

        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("trailIndex", position);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.trails, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class TrailsAdapter extends ArrayAdapter<LocationTrail>
    {
        private final Context context;
        private final ArrayList<LocationTrail> itemsArrayList;

        public TrailsAdapter(Context context, ArrayList<LocationTrail> itemsArrayList) {

            super(context, R.layout.activity_trails_row, itemsArrayList);

            this.context = context;
            this.itemsArrayList = itemsArrayList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LocationTrail trail = itemsArrayList.get(position);
            // 1. Create inflater
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 2. Get rowView from inflater
            View rowView = inflater.inflate(R.layout.activity_trails_row, parent, false);

            // 3. Get the two text view from the rowView
            TextView numView = (TextView) rowView.findViewById(R.id.trailNumber);
            TextView dateView = (TextView) rowView.findViewById(R.id.trailDate);
            TextView speedView = (TextView) rowView.findViewById(R.id.trailSpeed);
            TextView avgspeedView = (TextView) rowView.findViewById(R.id.trailAvgSpeed);
            TextView distView = (TextView) rowView.findViewById(R.id.trailDistance);

            numView.setText(String.format("#%d", position + 1));

            String todayString = android.text.format.DateFormat.format("MM/dd/yyyy", new Date()).toString();
            String dateString = android.text.format.DateFormat.format("MM/dd/yyyy", trail.Created).toString();
            if (dateString.compareTo(todayString) == 0)
                dateString = "Today";
            dateString = dateString.concat(android.text.format.DateFormat.format(" hh:mm:ss a", trail.Created).toString());

            dateView.setText(dateString);
            speedView.setText(String.format("%.02f mph", trail.getMPH()));
            avgspeedView.setText(String.format("%.02f mph", 3.6 * 0.621371 * trail.AvgSpeedTotal/ (position + 1)));
            distView.setText(String.format("%.02f mi", trail.TotalDistance/ (position + 1)));

            return rowView;
        }
    }
}
