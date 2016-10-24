package com.ivalentin.margolariak;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {

	//Assign menu items
	private LinearLayout llSync, llNotifications, llVersion, llSource, llFeedback;
	private CheckBox cbSync, cbNotifications;
	private TextView tvSync, tvNotifications, tvVersion;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Remove title bar.
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		//Set layout.
		setContentView(R.layout.activity_settings);

		//Assign views
		llSync = (LinearLayout) findViewById(R.id.ll_settings_sync);
		llNotifications = (LinearLayout) findViewById(R.id.ll_settings_notifications);
		llVersion = (LinearLayout) findViewById(R.id.ll_settings_version);
		llSource = (LinearLayout) findViewById(R.id.ll_settings_source);
		llFeedback = (LinearLayout) findViewById(R.id.ll_settings_feedback);
		tvSync = (TextView) findViewById(R.id.tv_settings_sync);
		tvNotifications = (TextView) findViewById(R.id.tv_settings_notifications);
		tvVersion = (TextView) findViewById(R.id.tv_settings_version);
		cbSync = (CheckBox) findViewById(R.id.cb_settings_sync);
		cbNotifications = (CheckBox) findViewById(R.id.cb_settings_notifications);

		//Set up version preference
		tvVersion.setText(com.ivalentin.margolariak.BuildConfig.VERSION_NAME);
		llVersion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO: Show changelog
			}
		});

		//Set up source preference
		llSource.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(GM.URL.GITHUB));
				startActivity(i);
			}
		});

		//Set up feedback preference
		llFeedback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO: Show dialog to enter text
			}
		});

		//TODO: Set up sync and notifications
	}
}