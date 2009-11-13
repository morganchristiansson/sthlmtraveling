package com.markupartist.sthlmtraveling;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.markupartist.sthlmtraveling.planner.Planner;
import com.markupartist.sthlmtraveling.provider.HistoryDbAdapter;
import com.markupartist.sthlmtraveling.util.Tracker;

public abstract class FromToActivity extends Activity {
    private static final String TAG = "FromTo";
    protected AutoCompleteTextView mText;
    protected ListView mRecent;
    private HistoryDbAdapter mHistoryDbAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mText = (AutoCompleteTextView)findViewById(R.id.text);
        mText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                finishOK();
            }
        });

        mHistoryDbAdapter = new HistoryDbAdapter(this).open();
        final Cursor cursor = mHistoryDbAdapter.fetchAllPoints();
        startManagingCursor(cursor);
        Log.d(TAG, "start/endPoints: " + cursor.getCount());
        mRecent = (ListView)findViewById(R.id.recent);
        mRecent.setAdapter(new SimpleCursorAdapter(this, R.layout.fromto_list_row,
            cursor, new String[]{HistoryDbAdapter.KEY_NAME}, new int[]{android.R.id.text1}));
        mRecent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int index = cursor.getColumnIndex(HistoryDbAdapter.KEY_NAME);
                mText.setText(cursor.getString(index));
                finishOK();
            }
        });

        Planner planner = Planner.getInstance();
        AutoCompleteStopAdapter stopAdapter = new AutoCompleteStopAdapter(this, 
                R.layout.fromto_dropdown_row, planner);
        mText.setAdapter(stopAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHistoryDbAdapter.close();
    }

	protected abstract void finishOK();
    public static class FromActivity extends FromToActivity {
        private Button mMyLocationButton;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            setContentView(R.layout.from);
            super.onCreate(savedInstanceState);
            
            mMyLocationButton = (Button)findViewById(R.id.from_my_location);
            mMyLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mText.setText(getAddressFromCurrentPosition(FromActivity.this));
                    finishOK();
                }
            });
            Tracker.trackPageView("From");
        }
    
        @Override
        protected void finishOK() {
            Intent i = new Intent();
            i.putExtra("com.markupartist.sthlmtraveling.startPoint", mText.getText());
            setResult(RESULT_OK, i);
            super.finish();
        }
    }
    public static class ToActivity extends FromToActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            setContentView(R.layout.to);
            super.onCreate(savedInstanceState);
            Tracker.trackPageView("To");
        }
        @Override
        public void finishOK() {
            Intent i = new Intent();
            i.putExtra("com.markupartist.sthlmtraveling.endPoint", mText.getText());
            setResult(RESULT_OK, i);
            super.finish();
        }
    }

    /**
     * Get address from the current position.
     * TODO: Extract to a class
     * @param fromActivity 
     * @return the address in the format "location name, street" or null if failed
     * to determine address 
     */
    private static String getAddressFromCurrentPosition(FromActivity activity) {
        final LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (loc == null && gpsLoc != null) {
            loc = gpsLoc;
        } else if (gpsLoc != null && gpsLoc.getTime() > loc.getTime()) {
            // If we the gps location is more recent than the network 
            // location use it.
            loc = gpsLoc;
        }

        if (loc == null) {
            return null;
        }

        Double lat = loc.getLatitude();
        Double lng = loc.getLongitude();
        Geocoder geocoder = new Geocoder(activity.getApplicationContext(), Locale.getDefault());
        String addressString = null;
        try {
            Log.d(TAG, "Getting address from position " + lat + "," + lng);
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 5);
            if (!addresses.isEmpty()) {
                for (Address address : addresses) {
                    //Log.d(TAG, address.toString());
                    if (addressString == null) {
                        addressString = address.getThoroughfare();
                        if (address.getFeatureName().contains("-")) {
                            // getFeatureName returns all house numbers on the 
                            // position like 14-16 get the first number and append 
                            // that to the address.
                            addressString += " " + address.getFeatureName().split("-")[0];
                        } else if (address.getFeatureName().length() < 4) {
                            // Sometime the feature name also is the same as the 
                            // postal code, this is a bit ugly but we just assume that
                            // we do not have any house numbers that is bigger longer 
                            // than four, if so append it to the address.
                            addressString += " " + address.getFeatureName();
                        }
                    }

                    String locality = address.getLocality();
                    if (locality != null) {
                        addressString = locality + ", " + addressString;
                        break; // Get out of the loop
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return addressString;
    }
}
