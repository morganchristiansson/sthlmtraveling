/*
 * Copyright (C) 2009 Johan Nilsson <http://markupartist.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.markupartist.sthlmtraveling;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.util.TimeFormatException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.markupartist.sthlmtraveling.FromToActivity.FromActivity;
import com.markupartist.sthlmtraveling.FromToActivity.ToActivity;
import com.markupartist.sthlmtraveling.SearchRoutesTask.OnSearchRoutesResultListener;
import com.markupartist.sthlmtraveling.provider.HistoryDbAdapter;

public class PlannerActivity extends Activity {
    private static final String TAG = "Planner";
    protected static final int ACTIVITY_FROM = 5;
    protected static final int ACTIVITY_TO = 6;

    private static final int NO_LOCATION = 5;
    private static final String TIME_FORMAT = "%R";

	private Button mFromButton;
    private Button mToButton;
    private Button mSearchNowButton;
    private Button mSearchLaterButton;
    private ImageButton mReverseButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planner);

        mFromButton = (Button)findViewById(R.id.from);
        mFromButton.setOnClickListener(_fromListener);
        mToButton = (Button)findViewById(R.id.to);
        mToButton.setOnClickListener(_toListener);

        HistoryDbAdapter historyDbAdapter = new HistoryDbAdapter(this).open();
        mFromButton.setText(historyDbAdapter.fetchLastStartPoint());
        mToButton.setText(historyDbAdapter.fecthLastEndPoint());

        mSearchNowButton = (Button) findViewById(R.id.search_now);
        mSearchNowButton.setOnClickListener(_searchNowListener);
        mSearchLaterButton = (Button)findViewById(R.id.search_later);
        mSearchLaterButton.setOnClickListener(_searchLaterListener);

        mReverseButton = (ImageButton)findViewById(R.id.reverse);
        mReverseButton.setOnClickListener(new View.OnClickListener() {
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
    private boolean validate() {
        boolean error = false;
        if (mFromButton.getText().length() <= 0) {
            mFromButton.setError(getText(R.string.empty_value));
            error=true;
        }
        if (mToButton.getText().length() <= 0) {
            mToButton.setError(getText(R.string.empty_value));
            error=true;
        }
        return !error;
    }

    View.OnClickListener _searchNowListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!validate()) return;
            Intent i = new Intent(PlannerActivity.this, RoutesActivity.class);
            i.putExtra("com.markupartist.sthlmtraveling.startPoint", mFromButton.getText().toString());
            i.putExtra("com.markupartist.sthlmtraveling.endPoint", mToButton.getText().toString());
            startActivity(i);
        }
    };
    private View.OnClickListener _searchLaterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!validate()) return;
            Intent i = new Intent(PlannerActivity.this, WhenActivity.class);
            i.putExtra("com.markupartist.sthlmtraveling.startPoint", mFromButton.getText().toString());
            i.putExtra("com.markupartist.sthlmtraveling.endPoint", mToButton.getText().toString());
            startActivity(i);
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
        default:
            Log.w(TAG, "Unhandled activity resultCode: "+resultCode);
        }
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch(id) {
        case NO_LOCATION:
            return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getText(R.string.no_location_title))
                .setMessage(getText(R.string.no_location_message))
                .setPositiveButton(android.R.string.ok, null)
                .create();
        }
        return dialog;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.about:
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
 
//    @Override
//    public void onSearchRoutesResult(ArrayList<Route> routes) {
//        String startPoint = mFromButton.getText().toString();
//        String endPoint = mToButton.getText().toString();
//
//        mHistoryDbAdapter.create(HistoryDbAdapter.TYPE_START_POINT, startPoint);
//        mHistoryDbAdapter.create(HistoryDbAdapter.TYPE_END_POINT, endPoint);
//
//        Intent i = new Intent(this, RoutesActivity.class);
//        i.putExtra("com.markupartist.sthlmtraveling.startPoint", startPoint);
//        i.putExtra("com.markupartist.sthlmtraveling.endPoint", endPoint);
//        startActivity(i);
//    }
}
