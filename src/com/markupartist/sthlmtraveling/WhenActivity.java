package com.markupartist.sthlmtraveling;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.format.Time;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;

public class WhenActivity extends Activity {
	private TimePicker mTimePicker;
    //private Calendar mCalendar;
    private Button mGo;
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("'<b>'dd'</b>' MMMM EEEE");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.when);

        mTimePicker = (TimePicker)findViewById(R.id.time);
        mTimePicker.setIs24HourView(true);
        /*
        mCalendar = Calendar.getInstance();

		Spinner date = (Spinner)findViewById(R.id.date);
		ArrayAdapter<CharSequence> dateAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, prepopulateCalendar());
		dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		date.setAdapter(dateAdapter);
        */

        mGo = (Button)findViewById(R.id.go);
		mGo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				Time time = new Time();
				time.setToNow();
				time.hour = mTimePicker.getCurrentHour();
				time.minute = mTimePicker.getCurrentMinute();
				time.second = 0;
                i.putExtra("com.markupartist.sthlmtraveling.routeTime", time.format2445());
				setResult(RESULT_OK, i);
				finish();
			}
		});
	}

	/*
	private Spanned[] prepopulateCalendar() {
		SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEEE");
		mCalendar = Calendar.getInstance();
		Spanned dates[] = new Spanned[30];
		dates[0] = Html.fromHtml("<b>"+getText(R.string.today)+"</b> "+weekdayFormat.format(mCalendar.getTime()));
		mCalendar.add(Calendar.DAY_OF_MONTH, 1);
		dates[1] = Html.fromHtml("<b>"+getText(R.string.tomorrow)+"</b> "+weekdayFormat.format(mCalendar.getTime()));
		mCalendar.add(Calendar.DAY_OF_MONTH, 1);
		
		for(int i=2; i<30; i++) {
			dates[i] = Html.fromHtml(FORMAT.format(mCalendar.getTime()));
			mCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		return dates;
	}
	*/
}
