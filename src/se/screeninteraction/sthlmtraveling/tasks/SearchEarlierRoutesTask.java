package se.screeninteraction.sthlmtraveling.tasks;

import java.io.IOException;
import java.util.ArrayList;

import se.screeninteraction.sthlmtraveling.R;
import se.screeninteraction.sthlmtraveling.planner.Planner;
import se.screeninteraction.sthlmtraveling.planner.Route;

import android.app.Activity;
import android.app.AlertDialog;


public class SearchEarlierRoutesTask extends AbstractSearchRoutesTask {

    public SearchEarlierRoutesTask(Activity activity) {
        super(activity);
    }

    @Override
    ArrayList<Route> doSearchInBackground(Object... params) throws IOException {
        return Planner.getInstance().findEarlierRoutes();
    }

    @Override
    protected void onNoRoutesFound() {
        new AlertDialog.Builder(getActivity())
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(getActivity().getText(R.string.attention_label))
            .setMessage(getActivity().getText(R.string.session_timeout_message))
            .setNeutralButton(getActivity().getText(android.R.string.ok), null)
            .create()
            .show();        
    }
}
