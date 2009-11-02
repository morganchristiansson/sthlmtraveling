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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.markupartist.sthlmtraveling.FromToActivity.FromActivity;
import com.markupartist.sthlmtraveling.FromToActivity.ToActivity;
import com.markupartist.sthlmtraveling.SearchRoutesTask.OnSearchRoutesResultListener;
import com.markupartist.sthlmtraveling.provider.HistoryDbAdapter;

public class PlannerActivity extends Activity implements OnSearchRoutesResultListener {
    private static final String TAG = "Planner";
    protected static final int ACTIVITY_FROM = 5;
    protected static final int ACTIVITY_TO = 6;
    protected static final int ACTIVITY_WHEN = 7;

    private static final int NO_LOCATION = 5;

    private Button mFromButton;
    private Button mToButton;
    private Button mSearchButton;
    private Button mWhenButton;
    private ImageButton mReverseButton;
    static HistoryDbAdapter mHistoryDbAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planner);

        mWhenButton = (Button)findViewById(R.id.when);
        mWhenButton.setOnClickListener(_whenListener);
        mHistoryDbAdapter = new HistoryDbAdapter(this).open();

        mSearchButton = (Button) findViewById(R.id.search_route);
        mSearchButton.setOnClickListener(_searchListener);
        mFromButton = (Button)findViewById(R.id.from);
        mFromButton.setOnClickListener(_fromListener);
        mFromButton.setText(mHistoryDbAdapter.fetchLastStartPoint());
        mToButton = (Button)findViewById(R.id.to);
        mToButton.setOnClickListener(_toListener);
        mToButton.setText(mHistoryDbAdapter.fecthLastEndPoint());
       
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
                 error=true;        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_search, menu);
        return true;
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
