package com.ivalentin.margolariak;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
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
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;

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

		//Open preferences
		preferences = getSharedPreferences(GM.PREFERENCES.PREFERNCES, MODE_PRIVATE);
		editor = preferences.edit();

		//Set up sync preferences. Incidentally, also sets initial state for notifications.
		if (preferences.getBoolean(GM.PREFERENCES.KEY.SYNC, GM.PREFERENCES.DEFAULT.SYNC)){
			tvSync.setText(R.string.preferences_sync_sync_on);
			cbSync.setChecked(true);
			enableNotifications();
		}
		else{
			tvSync.setText(R.string.preferences_sync_sync_off);
			cbSync.setChecked(false);
			disableNotifications();
		}

		//Set up version preference
		tvVersion.setText(com.ivalentin.margolariak.BuildConfig.VERSION_NAME);
		llVersion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				blink(v);
				//TODO: Show changelog
			}
		});

		//Set up source preference
		llSource.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				blink(v);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(GM.URL.GITHUB));
				startActivity(i);
			}
		});

		//Set up feedback preference
		llFeedback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				blink(v);
				//TODO: Show dialog to enter text
			}
		});
	}

	private void blink(View v){
		final View view = v;

		//These are the colors of the menu background
		final int r = 0;
		final int g = 120;
		final int b = 255;

		new Thread(){

			@Override
			public void run() {

				//Alpha
				int a  = 250;

				try {
					while (a >= 0) {
						final int passAlpha = a;
						runOnUiThread(new Runnable() {
										  @Override
										  public void run() {
											  view.setBackgroundColor(Color.argb(passAlpha, r, g, b));
											  Log.e("PASS", "Alpha: " + passAlpha);
										  }
									  });

						a -= 10;
						sleep(10);
					}
				}
				catch (InterruptedException e) {
					view.setBackgroundColor(Color.argb(0, r, g, b));
				}
			}
		}.start();

	}

	public void toggleSync(View v){
		blink(llSync);
		editor.putBoolean(GM.PREFERENCES.KEY.SYNC, !preferences.getBoolean(GM.PREFERENCES.KEY.SYNC, GM.PREFERENCES.DEFAULT.SYNC));
		editor.apply();
		if (preferences.getBoolean(GM.PREFERENCES.KEY.SYNC, GM.PREFERENCES.DEFAULT.SYNC)){
			tvSync.setText(R.string.preferences_sync_sync_on);
			cbSync.setChecked(true);
			enableNotifications();
		}
		else{
			tvSync.setText(R.string.preferences_sync_sync_off);
			cbSync.setChecked(false);
			disableNotifications();
		}
	}

	public void toggleNotifications(View v){
		if (preferences.getBoolean(GM.PREFERENCES.KEY.SYNC, GM.PREFERENCES.DEFAULT.SYNC)) {
			blink(llNotifications);
			editor.putBoolean(GM.PREFERENCES.KEY.NOTIFICATIONS, !preferences.getBoolean(GM.PREFERENCES.KEY.NOTIFICATIONS, GM.PREFERENCES.DEFAULT.NOTIFICATIONS));
			editor.apply();
			if (preferences.getBoolean(GM.PREFERENCES.KEY.NOTIFICATIONS, GM.PREFERENCES.DEFAULT.NOTIFICATIONS)) {
				tvNotifications.setText(R.string.preferences_sync_notifications_on);
				cbNotifications.setChecked(true);
			} else {
				tvNotifications.setText(R.string.preferences_sync_notifications_off);
				cbNotifications.setChecked(false);
			}
		}
		else{
			cbNotifications.setChecked(false);
		}
	}

	private void enableNotifications(){
		llNotifications.setAlpha(1);
		if (preferences.getBoolean(GM.PREFERENCES.KEY.NOTIFICATIONS, GM.PREFERENCES.DEFAULT.NOTIFICATIONS)){
			tvNotifications.setText(R.string.preferences_sync_notifications_on);
			cbNotifications.setChecked(true);
		}
		else{
			tvNotifications.setText(R.string.preferences_sync_notifications_off);
			cbNotifications.setChecked(false);
		}

	}

	private void disableNotifications(){
		llNotifications.setAlpha(0.4f);
		tvNotifications.setText(R.string.preferences_sync_notifications_disabled);
		cbNotifications.setChecked(false);
	}

	public void finish(View v){
		finish();
	}
}