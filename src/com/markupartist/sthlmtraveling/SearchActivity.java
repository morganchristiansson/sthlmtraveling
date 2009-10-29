package com.markupartist.sthlmtraveling;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class SearchActivity extends Activity {
    private static final String TAG = "Search";
    private static final int DIALOG_START_POINT = 0;
    private static final int DIALOG_END_POINT = 1;
    private static final int DIALOG_NO_ROUTES_FOUND = 2;
    private static final int DIALOG_ABOUT = 3;
    private static final int DIALOG_PROGRESS = 4;

//    private AutoCompleteTextView mFromAutoComplete;
//    private AutoCompleteTextView mToAutoComplete;

    private final Handler mHandler = new Handler();
	private Button _fromBtn;
	private Button _toBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        final Button when = (Button)findViewById(R.id.when);
        when.setOnClickListener(_whenListener);

        Planner planner = Planner.getInstance();
//        mFromAutoComplete = (AutoCompleteTextView) findViewById(R.id.from);
//        AutoCompleteStopAdapter stopAdapter = new AutoCompleteStopAdapter(this, 
//                android.R.layout.simple_dropdown_item_1line, planner);
//        mFromAutoComplete.setAdapter(stopAdapter);
//
//        mToAutoComplete = (AutoCompleteTextView) findViewById(R.id.to);
//        AutoCompleteStopAdapter toAdapter = new AutoCompleteStopAdapter(this, 
//                android.R.layout.simple_dropdown_item_1line, planner);
//        mToAutoComplete.setAdapter(toAdapter);

        final Button search = (Button) findViewById(R.id.search_route);

        search.setOnClickListener(mGetSearchListener);
        _fromBtn = (Button)findViewById(R.id.from);
        _toBtn = (Button)findViewById(R.id.to);
        _fromBtn.setOnClickListener(_fromListener);
        _toBtn.setOnClickListener(_toListener);
//        final ImageButton fromDialog = (ImageButton) findViewById(R.id.from_menu);
//        fromDialog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDialog(DIALOG_START_POINT);
//            }
//        });
//
//        final ImageButton toDialog = (ImageButton) findViewById(R.id.to_menu);
//        toDialog.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDialog(DIALOG_END_POINT);
//            }
//        });
    }

    View.OnClickListener mGetSearchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            if (mFromAutoComplete.getText().length() <= 0) {
//                mFromAutoComplete.setError(getText(R.string.empty_value));
//            } else if (mToAutoComplete.getText().length() <= 0) {
//                mToAutoComplete.setError(getText(R.string.empty_value));
//            } else {
//                Time time = new Time();
//                time.setToNow();
//                searchRoutes(mFromAutoComplete.getText().toString(), 
//                        mToAutoComplete.getText().toString(), time);
//            }
        }
    };
	private View.OnClickListener _whenListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivity(new Intent(SearchActivity.this, WhenActivity.class));
		}
	};
	View.OnClickListener _fromListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivity(new Intent(SearchActivity.this, FromActivity.class));
		}
	};
	View.OnClickListener _toListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startActivity(new Intent(SearchActivity.this, ToActivity.class));
		}
	};

    /**
     * Fires off a thread to do the query. Will call onSearchResult when done.
     * @param startPoint the start point
     * @param endPoint the end point
     */
    private void searchRoutes(final String startPoint, final String endPoint, final Time time) {
        showDialog(DIALOG_PROGRESS);
//        new Thread() {
//            public void run() {
//                try {
//                    Planner.getInstance().findRoutes(startPoint, endPoint, time);
//                    mHandler.post(new Runnable() {
//                        @Override public void run() {
//                            onSearchRoutesResult();
//                        }
//                    });
//                    dismissDialog(DIALOG_PROGRESS);
//                } catch (Exception e) {
//                    dismissDialog(DIALOG_PROGRESS);
//                }
//            }
//        }.start();
    }

    /**
     * Called when we have a search result for routes.
     */
//    private void onSearchRoutesResult() {
//        if (Planner.getInstance().lastFoundRoutes() != null 
//                && !Planner.getInstance().lastFoundRoutes().isEmpty()) {
//            Intent i = new Intent(SearchActivity.this, RoutesActivity.class);
//            i.putExtra("com.markupartist.sthlmtraveling.startPoint", 
//                    mFromAutoComplete.getText().toString());
//            i.putExtra("com.markupartist.sthlmtraveling.endPoint", 
//                    mToAutoComplete.getText().toString());
//            startActivity(i);
//        } else {
//            // TODO: This works for now, but we need to see if there are any
//            // alternative stops available in later on.
//            showDialog(DIALOG_NO_ROUTES_FOUND);
//        }
//    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch(id) {
//        case DIALOG_START_POINT:
//            AlertDialog.Builder startPointDialogBuilder = new AlertDialog.Builder(this);
//            startPointDialogBuilder.setTitle("Choose start point");
//            startPointDialogBuilder.setItems(getMyLocationItems(), new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int item) {
//                    mFromAutoComplete.setText(getAddressFromCurrentPosition());
//                }
//            });
//            dialog = startPointDialogBuilder.create();
//            break;
//        case DIALOG_END_POINT:
//            AlertDialog.Builder endPointDialogBuilder = new AlertDialog.Builder(this);
//            endPointDialogBuilder.setTitle("Choose end point");
//            endPointDialogBuilder.setItems(getMyLocationItems(), new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int item) {
//                    mToAutoComplete.setText(getAddressFromCurrentPosition());
//                }
//            });
//            dialog = endPointDialogBuilder.create();
//            break;
        case DIALOG_NO_ROUTES_FOUND:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            dialog = builder.setTitle("Unfortunately no routes was found")
                .setMessage("If searhing for an address try adding a house number.")
                .setCancelable(true)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
                }).create();
            break;
        case DIALOG_ABOUT:
            PackageManager pm = getPackageManager();
            String version = "";
            try {
                PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
                version = pi.versionName;
            } catch (NameNotFoundException e) {
                Log.e(TAG, "Could not get the package info.");
            }

            dialog = new Dialog(this);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(R.layout.about_dialog);
            dialog.setTitle(getText(R.string.app_name) + " " + version);
            break;
        case DIALOG_PROGRESS:
            ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage(getText(R.string.loading));
            dialog = progress;
            break;
        }
        return dialog;
    }
    
    private CharSequence[] getMyLocationItems() {
        CharSequence[] items = {"My Location"};
        return items;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_search, menu);
        return true;
    }

    private String getAddressFromCurrentPosition() {
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (loc == null && gpsLoc != null) {
            loc = gpsLoc;
        } else if (gpsLoc != null && gpsLoc.getTime() > loc.getTime()) {
            // If we the gps location is more recent than the network 
            // location use it.
            loc = gpsLoc;
        }

        Double lat = loc.getLatitude();
        Double lng = loc.getLongitude();
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
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
            Toast.makeText(this, "Could not determine your position", 10);
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
}