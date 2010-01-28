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

package se.screeninteraction.sthlmtraveling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.screeninteraction.sthlmtraveling.R;
import se.screeninteraction.sthlmtraveling.planner.Planner;


import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

public class AutoCompleteStopAdapter extends ArrayAdapter<String> implements Filterable {
    private static String TAG = "AutoCompleteStopAdapter";
    private Planner mPlanner;

    public AutoCompleteStopAdapter(Context context, int textViewResourceId, Planner planner) {
        super(context, textViewResourceId);
        this.mPlanner = planner;
    }

    @Override
    public Filter getFilter() {
        Filter nameFilter = new Filter() {
            private boolean mWasSuccess = true; // We are optimistic ones...

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();

                if (constraint != null) {
                    //Log.d(TAG, "Searching for " + constraint);
                    ArrayList<String> list;
                    try {
                        list = mPlanner.findStop(constraint.toString());
                        filterResults.values = list;
                        filterResults.count = list.size();
                    } catch (IOException e) {
                        mWasSuccess = false;
                    }
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    clear();
                    for (String value : (List<String>)results.values) {
                        add(value);
                    }
                    notifyDataSetChanged();
                } else if (!mWasSuccess) {
                    Toast.makeText(getContext(), 
                            getContext().getResources().getText(R.string.network_problem_message), 
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        return nameFilter;
    }
}
