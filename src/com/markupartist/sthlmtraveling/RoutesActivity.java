package com.markupartist.sthlmtraveling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
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
import com.markupartist.sthlmtraveling.provider.FavoritesDbAdapter;

public class RoutesActivity extends ListActivity {
    private final String TAG = "RoutesActivity";
    private static final int DIALOG_NO_ROUTE_DETAILS_FOUND = 0;

    private static final int ADAPTER_EARLIER = 0;
    private static final int ADAPTER_ROUTES = 1;
    private static final int ADAPTER_LATER = 2;

    private final int SECTION_CHANGE_TIME = 1;
    private final int SECTION_ROUTES = 2;

    private final int CHANGE_TIME = 0;

    private final Handler mHandler = new Handler();
    private RoutesAdapter mRouteAdapter;
    private MultipleListAdapter mMultipleListAdapter;
    private TextView mFromView;
    private TextView mToView;
    private ArrayList<HashMap<String, String>> mDateAdapterData;
    private Time mTime;
    private FavoritesDbAdapter mFavoritesDbAdapter;
    private FavoriteButtonHelper mFavoriteButtonHelper;

    /**
     * Holds the current selected route, this is referenced by 
     * RouteDetailActivity.
     */
    public static Route route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routes_list);

        mFavoritesDbAdapter = new FavoritesDbAdapter(this).open();

        mFromView = (TextView) findViewById(R.id.route_from);
        mToView = (TextView) findViewById(R.id.route_to);

        Bundle extras = getIntent().getExtras();
        mFromView.setText(extras.getString("com.markupartist.sthlmtraveling.startPoint"));
        mToView.setText(extras.getString("com.markupartist.sthlmtraveling.endPoint"));

        mFavoriteButtonHelper = new FavoriteButtonHelper(
                this, mFavoritesDbAdapter, 
                mFromView.getText().toString(), mToView.getText().toString());
        mFavoriteButtonHelper.loadImage();

        createSections();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFavoriteButtonHelper.loadImage();
    }

    private void createSections() {
        // Date and time adapter.

        // For now just get the current date time.
        mTime = new Time();
        mTime.setToNow();
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

        // Routes
        ArrayList<Route> routes = Planner.getInstance().lastFoundRoutes();        
        mRouteAdapter = new RoutesAdapter(this, routes);

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

    /**
     * Updates routes in the UI after a search.
     */
    final Runnable mUpdateRoutes = new Runnable() {
        @Override public void run() {
            final ArrayList<Route> routes = Planner.getInstance().lastFoundRoutes();
            mRouteAdapter.refill(routes);
            mSectionedAdapter.notifyDataSetChanged();
        }
    };

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
                final ProgressDialog earlierProgress = ProgressDialog.show(RoutesActivity.this, "", getText(R.string.loading), true);
                earlierProgress.setCancelable(true);
                new Thread() {
                    public void run() {
                        try {
                            Planner.getInstance().findEarlierRoutes();
                            mHandler.post(mUpdateRoutes);
                            earlierProgress.dismiss();
                        } catch (Exception e) {
                            earlierProgress.dismiss();
                        }
                    }
                }.start(); 
                break;
            case ADAPTER_LATER:
                final ProgressDialog laterProgress = ProgressDialog.show(RoutesActivity.this, "", getText(R.string.loading), true);
                new Thread() {
                    public void run() {
                        try {
                            Planner.getInstance().findLaterRoutes();
                            mHandler.post(mUpdateRoutes);
                            laterProgress.dismiss();
                        } catch (Exception e) {
                            laterProgress.dismiss();
                        }
                    }
                }.start();
                break;
            case ADAPTER_ROUTES:
                route = (Route) mSectionedAdapter.getItem(position);
                findRouteDetails(route);
                break;
            }
            break;
        case SECTION_CHANGE_TIME:
            Intent i = new Intent(this, ChangeRouteTimeActivity.class);

            i.putExtra("com.markupartist.sthlmtraveling.routeTime", mTime.format2445());
            i.putExtra("com.markupartist.sthlmtraveling.startPoint", mFromView.getText());
            i.putExtra("com.markupartist.sthlmtraveling.endPoint", mToView.getText());
            startActivityForResult(i, CHANGE_TIME);
            break;
        }
    }

    /**
     * Find route details. Calls onSearchRouteDetailsResult when done.
     * @param route the route to find details for
     */
    private void findRouteDetails(final Route route) {
        final ProgressDialog progressDialog = 
            ProgressDialog.show(this, "", getText(R.string.loading), true);
        new Thread() {
            public void run() {
                try {
                    Planner.getInstance().findRouteDetails(route);
                    mHandler.post(new Runnable() {
                        @Override public void run() {
                            onSearchRouteDetailsResult();
                        }
                    });
                    progressDialog.dismiss();
                } catch (Exception e) {
                    progressDialog.dismiss();
                }
            }
        }.start();
    }

    /**
     * Called when we got a route details search result.
     */
    private void onSearchRouteDetailsResult() {
        if (Planner.getInstance().lastFoundRouteDetail() != null 
                && !Planner.getInstance().lastFoundRoutes().isEmpty()) {
            Intent i = new Intent(RoutesActivity.this, RouteDetailActivity.class);
            i.putExtra("com.markupartist.sthlmtraveling.startPoint", mFromView.getText().toString());
            i.putExtra("com.markupartist.sthlmtraveling.endPoint", mToView.getText().toString());
            startActivity(i);
        } else {
            showDialog(DIALOG_NO_ROUTE_DETAILS_FOUND);
        }
    }

    /**
     * This method is called when the sending activity has finished, with the
     * result it supplied.
     * 
     * @param requestCode The original request code as given to
     *                    startActivity().
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
                final ArrayList<Route> routes = Planner.getInstance().lastFoundRoutes();

                String newTime = data.getStringExtra("com.markupartist.sthlmtraveling.routeTime");
                mTime.parse(newTime);

                mRouteAdapter.refill(routes);

                HashMap<String, String> item = mDateAdapterData.get(0);
                item.put("title", mTime.format("%R %x"));

                mSectionedAdapter.notifyDataSetChanged();
            }
        }
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_search :
                final Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch(id) {
        case DIALOG_NO_ROUTE_DETAILS_FOUND:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            dialog = builder.setTitle("Unfortunately no route details was found")
                .setMessage("Most likely your session has timed out.")
                .setCancelable(true)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
                }).create();
            break;
        }
        return dialog;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFavoritesDbAdapter.close();
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
        }
    }

    private class RouteAdapterView extends LinearLayout {

        public RouteAdapterView(Context context, Route route) {
            super(context);
            this.setOrientation(VERTICAL);

            this.setPadding(10, 10, 10, 10);

            TextView routeDetail = new TextView(context);
            routeDetail.setText(route.toString());
            routeDetail.setTextColor(Color.WHITE);
            routeDetail.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

            LinearLayout routeChanges = new LinearLayout(context);
            routeChanges.setPadding(0, 3, 0, 0);

            /*
            TextView changesView = new TextView(context);
            changesView.setText(route.changes + " changes:");
            changesView.setPadding(0, 0, 5, 0);
            routeChanges.addView(changesView);
            */

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

            this.addView(routeDetail);
            this.addView(routeChanges);
        }

    }
}
