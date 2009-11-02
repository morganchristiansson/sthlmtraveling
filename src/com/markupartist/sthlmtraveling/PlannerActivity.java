package com.markupartist.sthlmtraveling;

import java.io.IOException;
import java.util.ArrayList;
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
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.markupartist.sthlmtraveling.SearchRoutesTask.OnSearchRoutesResultListener;
import com.markupartist.sthlmtraveling.provider.HistoryDbAdapter;

public class PlannerActivity extends Activity implements OnSearchRoutesResultListener {
    private static final String TAG = "Search";
    private static final int DIALOG_ABOUT = 2;
	protected static final int ACTIVITY_FROM = 5;
	protected static final int ACTIVITY_TO = 6;
	protected static final int ACTIVITY_WHEN = 7;

    private final Handler mHandler = new Handler();
	private Button mFromButton;
	private Button mToButton;
    private static HistoryDbAdapter mHistoryDbAdapter;
	private Button _searchButton;
	private Button _whenButton;
	private ImageButton _reverseButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planner);

        _whenButton = (Button)findViewById(R.id.when);
        _whenButton.setOnClickListener(_whenListener);
        mHistoryDbAdapter = new HistoryDbAdapter(this).open();

        _searchButton = (Button) findViewById(R.id.search_route);
        _searchButton.setOnClickListener(_searchListener);
        mFromButton = (Button)findViewById(R.id.from);
        mFromButton.setOnClickListener(_fromListener);
        mFromButton.setText(mHistoryDbAdapter.fetchLastStartPoint());
        mToButton = (Button)findViewById(R.id.to);
        mToButton.setOnClickListener(_toListener);
        mToButton.setText(mHistoryDbAdapter.fecthLastEndPoint());
        
        _reverseButton = (ImageButton)findViewById(R.id.reverse);
        _reverseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                reverse();
			}
		});
    }

    protected void reverse() {
    	String startPoint = mFromButton.getText().toString();
        String endPoint = mToButton.getText().toString();
        mFromButton.setText(endPoint);
        mToButton.setText(startPoint);
    }

	View.OnClickListener _searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	boolean error = false;
            if (mFromButton.getText().length() <= 0) {
                mFromButton.setError(getText(R.string.empty_value));
                error=true;
            }
            if (mToButton.getText().length() <= 0) {
                mToButton.setError(getText(R.string.empty_value));
                error=true;
            }
        	if(error) return;
            Time time = new Time();
            time.setToNow();

            SearchRoutesTask searchRoutesTask = 
                new SearchRoutesTask(PlannerActivity.this)
                    .setOnSearchRoutesResultListener(PlannerActivity.this);
            searchRoutesTask.execute(mFromButton.getText().toString(), 
                    mToButton.getText().toString(), time);
        }
    };
	private View.OnClickListener _whenListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivityForResult(new Intent(PlannerActivity.this, WhenActivity.class), ACTIVITY_WHEN);
		}
	};
	View.OnClickListener _fromListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivityForResult(new Intent(PlannerActivity.this, FromActivity.class), ACTIVITY_FROM);
		}
	};
	View.OnClickListener _toListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivityForResult(new Intent(PlannerActivity.this, ToActivity.class), ACTIVITY_TO);
		}
	};

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode != RESULT_OK) return;
		switch(requestCode) {
		case ACTIVITY_FROM:
			mFromButton.setText(data.getCharSequenceExtra("com.markupartist.sthlmtraveling.startPoint"));
			break;
		case ACTIVITY_TO:
			mToButton.setText(data.getCharSequenceExtra("com.markupartist.sthlmtraveling.endPoint"));
			break;
		case ACTIVITY_WHEN:
			_whenButton.setText(data.getCharSequenceExtra("com.markupartist.sthlmtraveling.routeTime"));
			break;
		default:
			Log.w(TAG, "Unhandled activity resultCode: "+resultCode);
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_search, menu);
        return true;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.about:
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHistoryDbAdapter.close();
    }

    @Override
    public void onSearchRoutesResult(ArrayList<Route> routes) {
        String startPoint = mFromButton.getText().toString();
        String endPoint = mToButton.getText().toString();

        mHistoryDbAdapter.create(HistoryDbAdapter.TYPE_START_POINT, startPoint);
        mHistoryDbAdapter.create(HistoryDbAdapter.TYPE_END_POINT, endPoint);

        Intent i = new Intent(this, RoutesActivity.class);
        i.putExtra("com.markupartist.sthlmtraveling.startPoint", startPoint);
        i.putExtra("com.markupartist.sthlmtraveling.endPoint", endPoint);
        startActivity(i);
    }
    public abstract static class FromToActivity extends Activity {
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

        	final Cursor cursor = mHistoryDbAdapter.fetchAllPoints();
        	startManagingCursor(cursor);
        	Log.d(TAG, "start/endPoints: " + cursor.getCount());
        	mRecent = (ListView)findViewById(R.id.recent);
        	mRecent.setAdapter(new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
        		cursor, new String[]{HistoryDbAdapter.KEY_NAME}, new int[]{android.R.id.text1}));
        	mRecent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    	@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    		int index = cursor.getColumnIndex(HistoryDbAdapter.KEY_NAME);
		    		mText.setText(cursor.getString(index));
		    		finishOK();
		    	}
        	});

        	// Do not enable soft-keyboard automatically
        	InputMethodManager inputManager = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE); 
        	inputManager.hideSoftInputFromWindow(mText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); 
		};
		protected abstract void finishOK();
    }
    public static class FromActivity extends FromToActivity {
		private Button mMyLocationButton;
		@Override
		protected void onCreate(Bundle savedInstanceState) {
    		setContentView(R.layout.from);
			super.onCreate(savedInstanceState);
	        Planner planner = Planner.getInstance();
	        
	        AutoCompleteStopAdapter stopAdapter = new AutoCompleteStopAdapter(this,
	        		android.R.layout.simple_dropdown_item_1line, planner);
	        mText.setAdapter(stopAdapter);
	        
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
			Planner planner = Planner.getInstance();
    		
			AutoCompleteStopAdapter stopAdapter = new AutoCompleteStopAdapter(this, 
			        android.R.layout.simple_dropdown_item_1line, planner);
			mText.setAdapter(stopAdapter);
		}
		@Override
		public void finishOK() {
			Intent i = new Intent();
			i.putExtra("com.markupartist.sthlmtraveling.endPoint", mText.getText());
			setResult(RESULT_OK, i);
			super.finish();
		}
    }
}
