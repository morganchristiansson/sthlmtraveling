package com.markupartist.sthlmtraveling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
    private static final int DIALOG_START_POINT = 0;
    private static final int DIALOG_END_POINT = 1;
    private static final int DIALOG_ABOUT = 2;
    private static final int DIALOG_START_POINT_HISTORY = 3;
    private static final int DIALOG_END_POINT_HISTORY = 4;
	protected static final int ACTIVITY_FROM = 5;
	protected static final int ACTIVITY_TO = 6;
	protected static final int ACTIVITY_WHEN = 7;

    private final Handler mHandler = new Handler();
	private Button _fromButton;
	private Button _toButton;
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
        _fromButton = (Button)findViewById(R.id.from);
        _fromButton.setOnClickListener(_fromListener);
        _fromButton.setText(mHistoryDbAdapter.fetchLastStartPoint());
        _toButton = (Button)findViewById(R.id.to);
        _toButton.setOnClickListener(_toListener);
        _toButton.setText(mHistoryDbAdapter.fecthLastEndPoint());
        
        _reverseButton = (ImageButton)findViewById(R.id.reverse);
        _reverseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                reverse();
			}
		});
    }

    protected void reverse() {
    	String startPoint = _fromButton.getText().toString();
        String endPoint = _toButton.getText().toString();
        _fromButton.setText(endPoint);
        _toButton.setText(startPoint);
    }

	View.OnClickListener _searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	boolean error = false;
            if (_fromButton.getText().length() <= 0) {
                _fromButton.setError(getText(R.string.empty_value));
                error=true;
            }
            if (_toButton.getText().length() <= 0) {
                _toButton.setError(getText(R.string.empty_value));
                error=true;
            }
        	if(error) return;
            Time time = new Time();
            time.setToNow();

            SearchRoutesTask searchRoutesTask = 
                new SearchRoutesTask(PlannerActivity.this)
                    .setOnSearchRoutesResultListener(PlannerActivity.this);
            searchRoutesTask.execute(_fromButton.getText().toString(), 
                    _toButton.getText().toString(), time);
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
		switch(requestCode) {
		case ACTIVITY_FROM:
			_fromButton.setText(data.getCharSequenceExtra("com.markupartist.sthlmtraveling.startPoint"));
			break;
		case ACTIVITY_TO:
			_toButton.setText(data.getCharSequenceExtra("com.markupartist.sthlmtraveling.endPoint"));
			break;
		case ACTIVITY_WHEN:
			_whenButton.setText(data.getCharSequenceExtra("com.markupartist.sthlmtraveling.routeTime"));
			break;
		default:
			Log.w(TAG, "Unhandled activity resultCode: "+resultCode);
		}
	}

	@Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch(id) {
        case DIALOG_ABOUT:
            PackageManager pm = getPackageManager();
            String version = "";
            try {
                PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
                version = pi.versionName;
            } catch (NameNotFoundException e) {
                Log.e(TAG, "Could not get the package info.");
            }

            return new AlertDialog.Builder(this)
                .setTitle(getText(R.string.app_name) + " " + version)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(getText(R.string.about_this_app))
                .setCancelable(true)
                .setPositiveButton(getText(android.R.string.ok), null)
                .setNeutralButton(getText(R.string.donate), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://pledgie.com/campaigns/6527"));
                        startActivity(browserIntent);
                    }
                })
                .setNegativeButton(getText(R.string.feedback), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                        emailIntent .setType("plain/text");
                        emailIntent .putExtra(android.content.Intent.EXTRA_EMAIL,
                                new String[]{"sthlmtraveling@markupartist.com"});
                        emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT,
                                "iglaset feedback");
                        startActivity(Intent.createChooser(emailIntent,
                                getText(R.string.send_email)));
                    }
                })
                .create();
        }
        return dialog;
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
                showDialog(DIALOG_ABOUT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHistoryDbAdapter.close();
    }

    @Override
    public void onSearchRoutesResult(ArrayList<Route> routes) {
        String startPoint = _fromButton.getText().toString();
        String endPoint = _toButton.getText().toString();

        mHistoryDbAdapter.create(HistoryDbAdapter.TYPE_START_POINT, startPoint);
        mHistoryDbAdapter.create(HistoryDbAdapter.TYPE_END_POINT, endPoint);

        Intent i = new Intent(PlannerActivity.this, RoutesActivity.class);
        i.putExtra("com.markupartist.sthlmtraveling.startPoint", startPoint);
        i.putExtra("com.markupartist.sthlmtraveling.endPoint", endPoint);
        startActivity(i);
    }
    public abstract static class FromToActivity extends Activity {
		protected Button _goButton;
		protected AutoCompleteTextView _text;
		protected ListView _recent;
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			_text = (AutoCompleteTextView)findViewById(R.id.text);
	        _goButton = (Button) findViewById(R.id.go);
	        _goButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
	        _text.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					finish();
				}
			});

        	final Cursor cursor = getCursor();
        	startManagingCursor(cursor);
        	Log.d(TAG, "start/endPoints: " + cursor.getCount());
        	_recent = (ListView)findViewById(R.id.recent);
        	_recent.setAdapter(new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
        		cursor, new String[]{HistoryDbAdapter.KEY_NAME}, new int[]{android.R.id.text1}));
        	_recent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    	@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    		int index = cursor.getColumnIndex(HistoryDbAdapter.KEY_NAME);
		    		_text.setText(cursor.getString(index));
		    		finish();
		    	}
        	});
		};
		protected abstract Cursor getCursor();
    }
    public static class FromActivity extends FromToActivity {
		private Button _myLocationButton;
		@Override
		protected void onCreate(Bundle savedInstanceState) {
    		setContentView(R.layout.from);
			super.onCreate(savedInstanceState);
	        Planner planner = Planner.getInstance();
	        
	        AutoCompleteStopAdapter stopAdapter = new AutoCompleteStopAdapter(this,
	        		android.R.layout.simple_dropdown_item_1line, planner);
	        _text.setAdapter(stopAdapter);
	        
	        _myLocationButton = (Button)findViewById(R.id.from_my_location);
	        _myLocationButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					_text.setText(getAddressFromCurrentPosition(FromActivity.this));
					finish();
				}
			});
		}
		@Override
		public void finish() {
			Intent i = new Intent();
			i.putExtra("com.markupartist.sthlmtraveling.startPoint", _text.getText());
			setResult(RESULT_OK, i);
			super.finish();
		}
		@Override
		protected Cursor getCursor() {
			return mHistoryDbAdapter.fetchAllStartPoints();
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
			_text.setAdapter(stopAdapter);
		}
		@Override
		public void finish() {
			Intent i = new Intent();
			i.putExtra("com.markupartist.sthlmtraveling.endPoint", _text.getText());
			setResult(RESULT_OK, i);
			super.finish();
		}
		@Override
		protected Cursor getCursor() {
			return mHistoryDbAdapter.fetchAllEndPoints();
		}
    }
}
