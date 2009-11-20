package se.screeninteraction.sthlmtraveling.util;

import android.content.Context;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Simple wrappar around the GoogleAnalyticsTracker.
 * @author johan
 *
 */
public class Tracker {
    private static final String TAG = "Tracker";
    private static final String TRACKER_ID = "UA-11566858-1";
    private static final int DEFAULT_TRACKER_INTERVAL = 20;
    private static final String CATEGORY_CLICKS = "Clicks";
    private static final String ACTION_BUTTON = "Button";
    private static final String ACTION_MENU_ITEM = "MenuItem";
    private static GoogleAnalyticsTracker sTracker;

    private Tracker() {}

    public static void start(Context context) {
        sTracker = GoogleAnalyticsTracker.getInstance();
        sTracker.start(TRACKER_ID, DEFAULT_TRACKER_INTERVAL, context);
    }

    public static void stop() {
        sTracker.stop();
    }

    public static void trackEvent(CharSequence category, CharSequence action, 
                              CharSequence label, int value) {
        sTracker.trackEvent((String) category, (String) action, (String) label, value);
    }

    public static void trackEvent(MenuItem item) {
        sTracker.trackEvent(CATEGORY_CLICKS, ACTION_MENU_ITEM, item.getTitle().toString(), 0);
    }

    public static void trackEvent(Button button) {
        sTracker.trackEvent(CATEGORY_CLICKS, ACTION_BUTTON, button.getText().toString(), 0);
    }

    public static void trackPageView(String page) {
        sTracker.trackPageView(page);
        Log.i(TAG, "trackPageView(): "+page);
    }
}
