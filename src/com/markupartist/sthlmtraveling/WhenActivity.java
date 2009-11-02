package com.markupartist.sthlmtraveling;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;

public class WhenActivity extends Activity {
	static final int TIME_DIALOG_ID = 0;
	private TimePicker mTime;
    private Calendar mCalendar;
    private Button mGo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.when);

        mTime = (TimePicker)findViewById(R.id.time);
        mTime.setIs24HourView(true);

        mCalendar = Calendar.getInstance();

		Spinner date = (Spinner)findViewById(R.id.date);
		ArrayAdapter<CharSequence> dateAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, prepopulateCalendar());
		dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		date.setAdapter(dateAdapter);

		mGo = (Button)findViewById(R.id.go);
		mGo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.putExtra("com.markupartist.sthlmtraveling.routeTime",
				           ""+mTime.getCurrentHour()+":"+mTime.getCurrentMinute());
				setResult(RESULT_OK, i);
				finish();
			}
		});
	}

	private String[] prepopulateCalendar() {
		SimpleDateFormat format = new SimpleDateFormat("dd MMMM EEEE");
		SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEEE");
		mCalendar = Calendar.getInstance();
		String dates[] = new String[30];
		dates[0] = getText(R.string.today)+" "+weekdayFormat.format(mCalendar.getTime());
		mCalendar.add(Calendar.DAY_OF_MONTH, 1);
		dates[1] = getText(R.string.tomorrow)+" "+weekdayFormat.format(mCalendar.getTime());
		mCalendar.add(Calendar.DAY_OF_MONTH, 1);
		
		for(int i=2; i<30; i++) {
			dates[i] = format.format(mCalendar.getTime());
			mCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		return dates;
	}
}
