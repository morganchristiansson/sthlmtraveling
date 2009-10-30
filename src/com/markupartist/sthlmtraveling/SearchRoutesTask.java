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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.format.Time;

/**
 * Background task for searching for routes.
 */
public class SearchRoutesTask extends AsyncTask<Object, Void, ArrayList<Route>>{
    private Activity mActivity;
    private ProgressDialog mProgress;
    private OnSearchRoutesResultListener mOnSearchRoutesResultListener;

    public SearchRoutesTask(Activity activity) {
        mActivity = activity;

        mProgress = new ProgressDialog(mActivity);
        mProgress.setMessage(mActivity.getText(R.string.loading));
    }

    @Override
    protected ArrayList<Route> doInBackground(Object... params) {
        publishProgress();

        return Planner.getInstance().findRoutes((String) params[0], 
                (String) params[1], (Time) params[2]);
    }

    @Override
    public void onProgressUpdate(Void... values) {
        mProgress.show();
    }

    @Override
    protected void onPostExecute(ArrayList<Route> result) {
        mProgress.dismiss();

        if (result != null && !result.isEmpty()) {
            mOnSearchRoutesResultListener.onSearchRoutesResult(result);
        } else {
            onNoRoutesFound();
        }
    }

    public SearchRoutesTask setOnSearchRoutesResultListener(
            OnSearchRoutesResultListener listener) {
        mOnSearchRoutesResultListener = listener;
        return this;
    }

    private void onNoRoutesFound() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Unfortunately no routes was found")
            .setMessage("If searhing for an address try adding a house number.")
            .setCancelable(true)
            .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
               }
            }).create().show();        
    }

    /**
     * The callback used when there is a search result.
     */
    public interface OnSearchRoutesResultListener {
        /**
         * Called when we have a search result.
         * @param routes the routes
         */
        public void onSearchRoutesResult(ArrayList<Route> routes);
    }
}
