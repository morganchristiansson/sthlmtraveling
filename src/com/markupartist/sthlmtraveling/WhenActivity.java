package com.markupartist.sthlmtraveling;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;

public class WhenActivity extends Activity {
	private TimePicker mTimePicker;
    private Button mGo;
    private Spinner mDateSpinner;
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("'<b>'dd'</b>' MMMM EEEE");
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00'");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.when);

        mTimePicker = (TimePicker)findViewById(R.id.time);
        mTimePicker.setIs24HourView(true);

		ArrayAdapter dateAdapter = new ArrayAdapter(this,
		                                            android.R.layout.simple_spinner_item,
		                                            prepopulateCalendar());
		dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDateSpinner = (Spinner)findViewById(R.id.date);
		mDateSpinner.setAdapter(dateAdapter);

        mGo = (Button)findViewById(R.id.go);
		mGo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                Intent i = new Intent(WhenActivity.this.getIntent());
                i.setClass(WhenActivity.this, RoutesActivity.class);
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, mDateSpinner.getSelectedItemPosition());
				cal.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentHour());
				cal.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
				i.putExtra("com.markupartist.sthlmtraveling.routeTime",
				           dateFormat.format(cal.getTime()).toString());
				myfinish(i);
			}
		});
	}

	protected void myfinish(Intent i) {
        startActivity(i);
    }

    private Spanned[] prepopulateCalendar() {
		SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEEE");
		Calendar cal = Calendar.getInstance();
		Spanned dates[] = new Spanned[30];
		dates[0] = Html.fromHtml("<b>"+getText(R.string.today)+"</b> " +
		                         weekdayFormat.format(cal.getTime()));
		cal.add(Calendar.DAY_OF_MONTH, 1);
		dates[1] = Html.fromHtml("<b>"+getText(R.string.tomorrow)+"</b> " +
		                         weekdayFormat.format(cal.getTime()));
		cal.add(Calendar.DAY_OF_MONTH, 1);

		for(int i=2; i<30; i++) {
			dates[i] = Html.fromHtml(FORMAT.format(cal.getTime()));
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		return dates;
	}

    public static class WithResult extends WhenActivity {
    	@Override
        protected void myfinish(Intent i) {
            setResult(RESULT_OK, i);
            finish();
        }
    }
}
