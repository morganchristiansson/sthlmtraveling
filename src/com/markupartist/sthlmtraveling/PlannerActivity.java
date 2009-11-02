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
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.markupartist.sthlmtraveling.FromToActivity.FromActivity;
import com.markupartist.sthlmtraveling.FromToActivity.ToActivity;
import com.markupartist.sthlmtraveling.SearchRoutesTask.OnSearchRoutesResultListener;
import com.markupartist.sthlmtraveling.provider.HistoryDbAdapter;

public class PlannerActivity extends Activity implements OnSearchRoutesResultListener {
    private static final String TAG = "Planner";
	protected static final int ACTIVITY_FROM = 5;
	protected static final int ACTIVITY_TO = 6;
	protected static final int ACTIVITY_WHEN = 7;

	private Button mFromButton;
	private Button mToButton;
    static HistoryDbAdapter mHistoryDbAdapter;
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
}
