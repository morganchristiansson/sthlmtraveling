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
import android.widget.Toast;

import com.markupartist.sthlmtraveling.provider.HistoryDbAdapter;

public abstract class FromToActivity extends Activity {
    private static final String TAG = "FromTo";
    protected AutoCompleteTextView mText;
    protected ListView mRecent;
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

        final Cursor cursor = PlannerActivity.mHistoryDbAdapter.fetchAllPoints();
        startManagingCursor(cursor);
        Log.d(TAG, "start/endPoints: " + cursor.getCount());
        mRecent = (ListView)findViewById(R.id.recent);
        mRecent.setAdapter(new SimpleCursorAdapter(this, R.layout.dropdown_list_row,
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
                R.layout.dropdown_list_row, planner);
        mText.setAdapter(stopAdapter);
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
        }
        @Override
        public void finishOK() {
            Intent i = new Intent();
            i.putExtra("com.markupartist.sthlmtraveling.endPoint", mText.getText());
            setResult(RESULT_OK, i);
            super.finish();
        }
    }
    private static String getAddressFromCurrentPosition(Context context) {
        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (loc == null && gpsLoc != null) {
            loc = gpsLoc;
        } else if (gpsLoc != null && gpsLoc.getTime() > loc.getTime()) {
            // If the gps location is more recent than the network 
            // location use it.
            loc = gpsLoc;
        } else {
            Toast.makeText(context, "Could not determine your position", 10);
            return null;
        }

        Double lat = loc.getLatitude();
        Double lng = loc.getLongitude();
        Geocoder geocoder = new Geocoder(context.getApplicationContext(), Locale.getDefault());
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
            // TODO: Change to dialog
            Toast.makeText(context, "Could not determine your position", 10);
            Log.e(TAG, e.getMessage());
        }
        return addressString;
    }
}
