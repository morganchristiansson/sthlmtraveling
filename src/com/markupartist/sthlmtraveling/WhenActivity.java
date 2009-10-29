package com.markupartist.sthlmtraveling;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;

public class WhenActivity extends Activity {
	static final int TIME_DIALOG_ID = 0;
	private Button _time;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.when);

		_time = (Button)findViewById(R.id.time);
	    _time.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            showDialog(TIME_DIALOG_ID);
	        }
	    });

		Spinner date = (Spinner)findViewById(R.id.date);
		ArrayAdapter<CharSequence> dateAdapter = ArrayAdapter.createFromResource(this, R.array.dates, android.R.layout.simple_spinner_item);
		dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		date.setAdapter(dateAdapter);
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
        	_time.setText(hourOfDay+":"+pad(minute));
        }
    };
    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
}
