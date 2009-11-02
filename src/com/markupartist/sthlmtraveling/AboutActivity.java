package com.markupartist.sthlmtraveling;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends Activity {
	private WebView _web;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		_web = (WebView)findViewById(R.id.web);
		_web.loadUrl("file:///android_asset/about.html");
	}
}
