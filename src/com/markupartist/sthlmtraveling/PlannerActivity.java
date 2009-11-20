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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.markupartist.sthlmtraveling.FromToActivity.FromActivity;
import com.markupartist.sthlmtraveling.FromToActivity.ToActivity;
import com.markupartist.sthlmtraveling.provider.HistoryDbAdapter;
import com.markupartist.sthlmtraveling.util.Tracker;

public class PlannerActivity extends Activity {
    private static final String TAG = "Planner";
    protected static final int ACTIVITY_FROM = 5;
    protected static final int ACTIVITY_TO = 6;

    private Button mFromButton;
    private Button mToButton;
    private Button mSearchNowButton;
    private Button mSearchLaterButton;
    private ImageButton mReverseButton;
	private HistoryDbAdapter mHistoryDbAdapter;
    private boolean mCreateShortcut;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planner);
        // If the activity was started with the "create shortcut" action, we
        // remember this to change the behavior upon a search.
        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
            mCreateShortcut = true;
        }

        mFromButton = (Button)findViewById(R.id.from);
        mFromButton.setOnClickListener(_fromListener);
        mToButton = (Button)findViewById(R.id.to);
        mToButton.setOnClickListener(_toListener);

        mHistoryDbAdapter = new HistoryDbAdapter(this).open();
        mFromButton.setText(mHistoryDbAdapter.fetchLastStartPoint());
        mToButton.setText(mHistoryDbAdapter.fecthLastEndPoint());

        mSearchNowButton = (Button) findViewById(R.id.search_now);
        mSearchNowButton.setOnClickListener(_searchNowListener);
        mSearchLaterButton = (Button)findViewById(R.id.search_later);
        mSearchLaterButton.setOnClickListener(_searchLaterListener);

        mReverseButton = (ImageButton)findViewById(R.id.reverse);
        mReverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reverse();
                Tracker.trackPageView("Reverse");
            }
        });
        Tracker.trackPageView("Planner");
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
			String startPoint = mFromButton.getText().toString();
			String endPoint = mToButton.getText().toString();
			
			mHistoryDbAdapter.create(HistoryDbAdapter.TYPE_START_POINT, startPoint);
			mHistoryDbAdapter.create(HistoryDbAdapter.TYPE_END_POINT, endPoint);

			Intent i = new Intent(PlannerActivity.this, RoutesActivity.class)
			           .setData(RoutesActivity.createRoutesUri(startPoint, endPoint));
            startActivity(i);
        }
    };
    private View.OnClickListener _searchLaterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!validate()) return;
            Intent i = new Intent(PlannerActivity.this, WhenActivity.class);
            i.putExtra(RoutesActivity.EXTRA_START_POINT, mFromButton.getText().toString());
            i.putExtra(RoutesActivity.EXTRA_END_POINT, mToButton.getText().toString());
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

    /**
     * Setup a search short cut.
     * @param startPoint the start point
     * @param endPoint the end point
     */
    protected void onCreateShortCut(String startPoint, String endPoint) {
        Uri routesUri = RoutesActivity.createRoutesUri(startPoint, endPoint);
        Intent shortcutIntent = new Intent(Intent.ACTION_VIEW, routesUri,
                this, RoutesActivity.class);
        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Then, set up the container intent (the response to the caller)
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, startPoint + " " + endPoint);
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(
                this, R.drawable.icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

        // Now, return the result to the launcher
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHistoryDbAdapter.close();
    }
}
