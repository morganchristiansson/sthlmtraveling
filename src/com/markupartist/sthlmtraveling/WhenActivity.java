package com.markupartist.sthlmtraveling;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;

public class WhenActivity extends Activity {
	static final int TIME_DIALOG_ID = 0;
	private Button _timeButton;
	private Button _go;
	private Calendar _calendar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.when);

		_timeButton = (Button)findViewById(R.id.time);
	    _timeButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            showDialog(TIME_DIALOG_ID);
	        }
	    });
		_calendar = Calendar.getInstance();
		updateTime();

		Spinner date = (Spinner)findViewById(R.id.date);
		ArrayAdapter<CharSequence> dateAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, prepopulateCalendar());
		dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		date.setAdapter(dateAdapter);

		_go = (Button)findViewById(R.id.go);
		_go.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.putExtra("com.markupartist.sthlmtraveling.routeTime", _timeButton.getText());
				setResult(RESULT_OK, i);
				finish();
			}
		});
	}

	private String[] prepopulateCalendar() {
		SimpleDateFormat format = new SimpleDateFormat("dd MMMM EEEE");
		SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEEE");
		_calendar = Calendar.getInstance();
		String dates[] = new String[30];
		dates[0] = getText(R.string.today)+" "+weekdayFormat.format(_calendar.getTime());
		_calendar.add(Calendar.DAY_OF_MONTH, 1);
		dates[1] = getText(R.string.tomorrow)+" "+weekdayFormat.format(_calendar.getTime());
		_calendar.add(Calendar.DAY_OF_MONTH, 1);
		
		for(int i=2; i<30; i++) {
			dates[i] = format.format(_calendar.getTime());
			_calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		return dates;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case TIME_DIALOG_ID:
	        return new TimePickerDialog(this,
	                _timeListener, 17, 00, false);
	    }
	    return null;
	}

	private TimePickerDialog.OnTimeSetListener _timeListener =
		new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        	_calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        	_calendar.set(Calendar.MINUTE, minute);
        	updateTime();
        }
    };
    
	private void updateTime() {
    	_timeButton.setText(_calendar.get(Calendar.HOUR_OF_DAY)+":"+pad(_calendar.get(Calendar.MINUTE)));
	}
    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
}
