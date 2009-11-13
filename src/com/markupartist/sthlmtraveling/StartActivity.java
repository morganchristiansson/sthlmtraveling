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

import com.markupartist.sthlmtraveling.util.Tracker;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

public class StartActivity extends TabActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TabHost tabHost = getTabHost();

        tabHost.addTab(tabHost.newTabSpec("search")
                .setIndicator(getText(R.string.search), getResources().getDrawable(R.drawable.ic_tab_search))
                .setContent(new Intent(this, PlannerActivity.class)
                // Setting this to clear solves a java.lang.StackOverflowError
                // issues that is triggered sometimes...
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        tabHost.addTab(tabHost.newTabSpec("favorites")
                .setIndicator(getText(R.string.favorites), getResources().getDrawable(R.drawable.ic_tab_favorites))
                .setContent(new Intent(this, FavoritesActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        Tracker.start(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Tracker.stop();
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
}
