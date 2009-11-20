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
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.SimpleAdapter.ViewBinder;

import com.markupartist.sthlmtraveling.SectionedAdapter.Section;
import com.markupartist.sthlmtraveling.planner.Route;
import com.markupartist.sthlmtraveling.provider.FavoritesDbAdapter;
import com.markupartist.sthlmtraveling.tasks.OnSearchRoutesResultListener;
import com.markupartist.sthlmtraveling.tasks.SearchEarlierRoutesTask;
import com.markupartist.sthlmtraveling.tasks.SearchLaterRoutesTask;
import com.markupartist.sthlmtraveling.tasks.SearchRoutesTask;
import com.markupartist.sthlmtraveling.util.Tracker;

import com.markupartist.sthlmtraveling.utils.BarcodeScannerIntegrator;

/**
 * Routes activity
 * <br/>
 * Accepts a routes data URI in the format:
 * <code>journeyplanner://routes?startpoint=STARTPOINT&endpoint=ENDPOINT&time=TIME</code>
 * All parameters needs to be url encoded. Time is optional, but if provided it must be in
 * RFC 2445 format.
 */
public class RoutesActivity extends ListActivity implements OnSearchRoutesResultListener {
    /**
     * The start point for the search.
     */
    static final String EXTRA_START_POINT = "com.markupartist.sthlmtraveling.start_point";
    /**
     * The end point for the search.
     */
    static final String EXTRA_END_POINT = "com.markupartist.sthlmtraveling.end_point";
    /**
     * Departure time in RFC 2445 format.
     */
    static final String EXTRA_TIME = "com.markupartist.sthlmtraveling.time";

    private final String TAG = "RoutesActivity";

    private static final int DIALOG_ILLEGAL_PARAMETERS = 0;

    private static final int ADAPTER_EARLIER = 0;
    private static final int ADAPTER_ROUTES = 1;
    private static final int ADAPTER_LATER = 2;

    private final int SECTION_CHANGE_TIME = 1;
    private final int SECTION_ROUTES = 2;

    private final int CHANGE_TIME = 0;

    private RoutesAdapter mRouteAdapter;
    private MultipleListAdapter mMultipleListAdapter;
    private TextView mFromView;
    private TextView mToView;
    private ArrayList<HashMap<String, String>> mDateAdapterData;
    private Time mTime = new Time();
    private FavoritesDbAdapter mFavoritesDbAdapter;
    private FavoriteButtonHelper mFavoriteButtonHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routes_list);

        // Parse data URI
        final Uri uri = getIntent().getData();
        String startPoint  = uri.getQueryParameter("start_point");
        String endPoint    = uri.getQueryParameter("end_point");
        String time        = uri.getQueryParameter("time");

        if (startPoint == null || endPoint == null) {
            showDialog(DIALOG_ILLEGAL_PARAMETERS);
            // If passed with bad parameters, break the execution.
            return;
        }

        mTime = new Time();
        if (time != null ) {
            mTime.parse(time);
        } else {
            mTime.setToNow();
        }

        mFavoritesDbAdapter = new FavoritesDbAdapter(this).open();

        mFromView = (TextView) findViewById(R.id.route_from);
        mFromView.setText(startPoint);
        mToView = (TextView) findViewById(R.id.route_to);
        mToView.setText(endPoint);

        Bundle extras = getIntent().getExtras();

        mFavoriteButtonHelper = new FavoriteButtonHelper(this, mFavoritesDbAdapter, 
                startPoint, endPoint);
        mFavoriteButtonHelper.loadImage();

        initRoutes(startPoint, endPoint, mTime);
        Tracker.trackPageView("Routes");
    }

    /**
     * Search for routes. Will first check if we already have data stored.
     * @param startPoint the start point
     * @param endPoint the end point
     * @param time the time
     */
    private void initRoutes(String startPoint, String endPoint, Time time) {
        @SuppressWarnings("unchecked")
        final ArrayList<Route> routes = (ArrayList<Route>) getLastNonConfigurationInstance();
        if (routes != null) {
            onSearchRoutesResult(routes);
        } else {
            SearchRoutesTask searchRoutesTask = new SearchRoutesTask(this);
            searchRoutesTask.setOnSearchRoutesResultListener(this);
            searchRoutesTask.execute(startPoint, endPoint, time);
        }
    }

    /**
     * Called before this activity is destroyed, returns the previous details. This data is used 
     * if the screen is rotated. Then we don't need to ask for the data again.
     * @return route details
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        return mRouteAdapter.getRoutes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Could be null if bad parameters was passed to the search.
        if (mFavoriteButtonHelper != null) {
            mFavoriteButtonHelper.loadImage();
        }
    }

    private void createSections() {
        // Date and time adapter.
        String timeString = mTime.format("%R %x"); // %r
        mDateAdapterData = new ArrayList<HashMap<String,String>>(1); 
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("title", timeString);
        mDateAdapterData.add(item);
        SimpleAdapter dateTimeAdapter = new SimpleAdapter(
                this,
                mDateAdapterData,
                R.layout.date_and_time,
                new String[] { "title" },
                new int[] { R.id.date_time } );

        // Earlier routes
        SimpleAdapter earlierAdapter = createEarlierLaterAdapter(android.R.drawable.arrow_up_float);

        // Later routes
        SimpleAdapter laterAdapter = createEarlierLaterAdapter(android.R.drawable.arrow_down_float);

        mMultipleListAdapter = new MultipleListAdapter();
        mMultipleListAdapter.addAdapter(ADAPTER_EARLIER, earlierAdapter);
        mMultipleListAdapter.addAdapter(ADAPTER_ROUTES, mRouteAdapter);
        mMultipleListAdapter.addAdapter(ADAPTER_LATER, laterAdapter);

        mSectionedAdapter.addSection(SECTION_CHANGE_TIME, "Date & Time", dateTimeAdapter);
        mSectionedAdapter.addSection(SECTION_ROUTES, "Routes", mMultipleListAdapter);

        setListAdapter(mSectionedAdapter);
    }

    SectionedAdapter mSectionedAdapter = new SectionedAdapter() {
        protected View getHeaderView(Section section, int index, 
                View convertView, ViewGroup parent) {
            TextView result = (TextView) convertView;

            if (convertView == null)
                result = (TextView) getLayoutInflater().inflate(R.layout.header, null);

            result.setText(section.caption);
            return (result);
        }
    };

    /**
     * Helper to create earlier or later adapter.
     * @param resource the image resource to show in the list
     * @return a prepared adapter
     */
    private SimpleAdapter createEarlierLaterAdapter(int resource) {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("image", resource);
        list.add(map);

        SimpleAdapter adapter = new SimpleAdapter(this, list, 
                R.layout.earlier_later_routes_row,
                new String[] { "image"},
                new int[] { 
                    R.id.earlier_later,
                }
        );

        adapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                    String textRepresentation) {
                switch (view.getId()) {
                case R.id.earlier_later:
                    ImageView imageView = (ImageView) view;
                    imageView.setImageResource((Integer) data);
                    return true;
                }
                return false;
            }
        });
        return adapter;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Section section = mSectionedAdapter.getSection(position);
        int sectionId = section.id;
        int innerPosition = mSectionedAdapter.getSectionIndex(position);
        Adapter adapter = section.adapter;

        switch (sectionId) {
        case SECTION_ROUTES:
            MultipleListAdapter multipleListAdapter = (MultipleListAdapter) adapter;
            int adapterId = multipleListAdapter.getAdapterId(innerPosition);
            switch(adapterId) {
            case ADAPTER_EARLIER:
                SearchEarlierRoutesTask serTask = new SearchEarlierRoutesTask(this);
                serTask.setOnSearchRoutesResultListener(this);
                serTask.execute();
                break;
            case ADAPTER_LATER:
                SearchLaterRoutesTask slrTask = new SearchLaterRoutesTask(this);
                slrTask.setOnSearchRoutesResultListener(this);
                slrTask.execute();
                break;
            case ADAPTER_ROUTES:
                Route route = (Route) mSectionedAdapter.getItem(position);
                findRouteDetails(route);
                break;
            }
            break;
        case SECTION_CHANGE_TIME:
            Intent i = new Intent(this, WhenActivity.WithResult.class);
            i.putExtra(EXTRA_TIME, mTime.format2445());
            //i.putExtra(EXTRA_START_POINT, mFromView.getText());
            //i.putExtra(EXTRA_END_POINT, mToView.getText());
            startActivityForResult(i, CHANGE_TIME);
            break;
        }
    }

    @Override
    public void onSearchRoutesResult(ArrayList<Route> routes) { 
        if (mRouteAdapter == null) {
            mRouteAdapter = new RoutesAdapter(this, routes);
            createSections();
        } else {
            mRouteAdapter.refill(routes);
            mSectionedAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Find route details. Will start {@link RouteDetailActivity}. 
     * @param route the route to find details for 
     */
    private void findRouteDetails(final Route route) {
        Intent i = new Intent(RoutesActivity.this, RouteDetailActivity.class);
        i.putExtra(RouteDetailActivity.EXTRA_START_POINT, mFromView.getText().toString());
        i.putExtra(RouteDetailActivity.EXTRA_END_POINT, mToView.getText().toString());
        i.putExtra(RouteDetailActivity.EXTRA_ROUTE, route);
        startActivity(i);
    }

    /**
     * This method is called when the sending activity has finished, with the
     * result it supplied.
     * 
     * @param requestCode The original request code as given to startActivity().
     * @param resultCode From sending activity as per setResult().
     * @param data From sending activity as per setResult().
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                Intent data) {
        if (requestCode == CHANGE_TIME) {
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Change time activity cancelled.");
            } else {
//                String startPoint = data.getStringExtra(EXTRA_START_POINT);
//                String endPoint = data.getStringExtra(EXTRA_END_POINT);
                String newTime = data.getStringExtra(EXTRA_TIME);

                mTime.parse(newTime);
                HashMap<String, String> item = mDateAdapterData.get(0);
                item.put("title", mTime.format("%R %x"));

                searchRoutes();
            }
        }
    }

	/**
	 * Fires off a thread to do the query. Will call onSearchResult when done.
	 */
	private void searchRoutes() {
	    searchRoutes(mFromView.getText(), mToView.getText(), mTime);
	}

	/**
	 * Fires off a thread to do the query. Will call onSearchResult when done.
	 * @param startPoint the start point.
	 * @param endPoint the end point.
	 * @param time the time to base the search on.
	 */
	private void searchRoutes(final CharSequence startPoint, final CharSequence endPoint, 
	        final Time time) {
        SearchRoutesTask searchRoutesTask = new SearchRoutesTask(this);
        searchRoutesTask.setOnSearchRoutesResultListener(this);
        searchRoutesTask.execute(startPoint, endPoint, mTime);
	}

///**
// * Called when we have a search result for routes.
// */
//@Override
//public void onSearchRoutesResult(ArrayList<Route> routes) {
//    createSections();
//    //final ArrayList<Route> routes = Planner.getInstance().lastFoundRoutes();
//    mRouteAdapter.refill(routes);
//
//    HashMap<String, String> item = mDateAdapterData.get(0);
//    item.put("title", mTime.format("%R %x"));
//
//    mSectionedAdapter.notifyDataSetChanged();
//}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_routes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_search :
                Intent i = new Intent(this, StartActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.reverse_start_end :
                String startPoint = mFromView.getText().toString();
                String endPoint = mToView.getText().toString();

                /*
                 * Note: To launch a new intent won't work because sl.se would need to have a new
                 * ident generated to be able to search for route details in the next step.
                 */

                SearchRoutesTask searchRoutesTask = new SearchRoutesTask(this);
                searchRoutesTask.setOnSearchRoutesResultListener(this);
                searchRoutesTask.execute(endPoint, startPoint, mTime);
                mFromView.setText(endPoint);
                mToView.setText(startPoint);

                // Update the favorite button
                mFavoriteButtonHelper.setStartPoint(endPoint).setEndPoint(startPoint).loadImage();
                return true;
            case R.id.show_qr_code :
                Uri routesUri = createRoutesUri(
                        Uri.encode(mFromView.getText().toString()), 
                        Uri.encode(mToView.getText().toString()));
                BarcodeScannerIntegrator.shareText(this, routesUri.toString(),
                        R.string.install_barcode_scanner_title,
                        R.string.requires_barcode_scanner_message,
                        R.string.yes, R.string.no);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_ILLEGAL_PARAMETERS:
            return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getText(R.string.attention_label))
                .setMessage(getText(R.string.bad_routes_parameters_message))
                .setCancelable(true)
                .setNeutralButton(getText(android.R.string.ok), null)
                .create();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFavoritesDbAdapter != null) {
            mFavoritesDbAdapter.close();
        }
    }

    /**
     * Constructs a search routes data Uri.
     * @param startPoint the start point
     * @param endPoint the end point
     * @return a search routes data uri
     */
    public static Uri createRoutesUri(String startPoint, String endPoint) {
        return createRoutesUri(startPoint, endPoint, (String)null);
    }

    public static Uri createRoutesUri(String startPoint, String endPoint, Time time) {
        return createRoutesUri(startPoint, endPoint, time.format2445());
    }
    /**
     * Constructs a search routes data URI.
     * @param startPoint the start point
     * @param endPoint the end point
     * @param time the time
     * @return a search routes data URI
     */
    public static Uri createRoutesUri(String startPoint, String endPoint, String time) {
        Uri routesUri;
        if (time != null) {
            routesUri = Uri.parse(
                    String.format("journeyplanner://routes?start_point=%s&end_point=%s&time=%s",
                            startPoint, endPoint, time));
        } else {
            routesUri = Uri.parse(
                    String.format("journeyplanner://routes?start_point=%s&end_point=%s",
                            startPoint, endPoint));
        }

        return routesUri;
    }

    private class RoutesAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<Route> mRoutes;

        public RoutesAdapter(Context context, ArrayList<Route> routes) {
            mContext = context;
            mRoutes = routes;
        }

        public void refill(ArrayList<Route> routes) {
            mRoutes = routes;
        }

        public ArrayList<Route> getRoutes() {
            return mRoutes;
        }

        @Override
        public int getCount() {
            return mRoutes.size();
        }

        @Override
        public Object getItem(int position) {
            return mRoutes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Route route = mRoutes.get(position);
            return new RouteAdapterView(mContext, route);
            //return createView(mContext, route);
        }

        /*
        private View createView(Context context, Route route) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

            View layout = inflater.inflate(R.layout.routes_row, null);

            TextView startPoint = (TextView) layout.findViewById(R.id.route_startpoint_label);
            startPoint.setText(route.from);
            TextView startPointDeparture = (TextView) layout.findViewById(R.id.route_startpoint_departure);
            startPointDeparture.setText(route.departure);

            TextView endPoint = (TextView) layout.findViewById(R.id.route_endpoint_label);
            endPoint.setText(route.to);
            TextView endPointArrival = (TextView) layout.findViewById(R.id.route_endpoint_arrival);
            endPointArrival.setText(route.arrival);

            TextView durationAndChanges = (TextView) layout.findViewById(R.id.route_duration_and_changes);
            durationAndChanges.setText(route.duration);

            LinearLayout routeChangesDrawables = (LinearLayout) findViewById(R.id.route_changes);
            int currentTransportCount = 1;
            int transportCount = route.transports.size();
            for (Route.Transport transport : route.transports) {
                ImageView change = new ImageView(context);
                change.setImageResource(transport.imageResource());
                change.setPadding(0, 0, 5, 0);
                routeChangesDrawables.addView(change);

                if (transportCount > currentTransportCount) {
                    ImageView separator = new ImageView(context);
                    separator.setImageResource(R.drawable.transport_separator);
                    separator.setPadding(0, 5, 5, 0);
                    routeChangesDrawables.addView(separator);
                }

                currentTransportCount++;
            }
            
            return layout;
        }
        */
    }

    private class RouteAdapterView extends LinearLayout {

        public RouteAdapterView(Context context, Route route) {
            super(context);
            this.setOrientation(VERTICAL);

            this.setPadding(10, 10, 10, 10);

            TextView routeDetail = new TextView(context);
            routeDetail.setText(route.toString());
            routeDetail.setTextColor(Color.WHITE);
            //routeDetail.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

            TextView startAndEndPoint = new TextView(context);
            startAndEndPoint.setText(route.from + " - " + route.to);
            startAndEndPoint.setTextColor(Color.GRAY);
            startAndEndPoint.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);

            LinearLayout routeChanges = new LinearLayout(context);
            routeChanges.setPadding(0, 5, 0, 0);

            int currentTransportCount = 1;
            int transportCount = route.transports.size();
            for (Route.Transport transport : route.transports) {
                ImageView change = new ImageView(context);
                change.setImageResource(transport.imageResource());
                change.setPadding(0, 0, 5, 0);
                routeChanges.addView(change);

                if (transportCount > currentTransportCount) {
                    ImageView separator = new ImageView(context);
                    separator.setImageResource(R.drawable.transport_separator);
                    separator.setPadding(0, 5, 5, 0);
                    routeChanges.addView(separator);
                }

                currentTransportCount++;
            }

            this.addView(startAndEndPoint);
            this.addView(routeDetail);
            this.addView(routeChanges);
        }
    }
}
