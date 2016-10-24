package com.ivalentin.margolariak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences sp = getPreferenceScreen().getSharedPreferences();

		//Set up Sync preference
		CheckBoxPreference prefSync = (CheckBoxPreference) getPreferenceManager().findPreference(GM.KEY_PREFERENCE_SYNC);
		if (prefSync.isChecked()){
			prefSync.setSummary(getString(R.string.preferences_sync_sync_on));
		}
		else{
			prefSync.setSummary(getString(R.string.preferences_sync_sync_off));
		}
		prefSync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				CheckBoxPreference prefNotification = (CheckBoxPreference) getPreferenceManager().findPreference(GM.KEY_PREFERENCE_NOTIFICATION);
				if (newValue.toString().equals("true")) {
					preference.setSummary(getString(R.string.preferences_sync_sync_on));
					if (prefNotification.isChecked()){
						prefNotification.setSummary(getString(R.string.preferences_sync_notifications_on));
					}
					else{
						prefNotification.setSummary(getString(R.string.preferences_sync_notifications_off));
					}
					prefNotification.setEnabled(true);
				} else {
					prefNotification.setEnabled(false);
					prefNotification.setSummary(getString(R.string.preferences_sync_notifications_disabled));
					preference.setSummary(getString(R.string.preferences_sync_sync_off));
				}
				return true;
			}
		});

		//Set up Notification preference
		CheckBoxPreference prefNotification = (CheckBoxPreference) getPreferenceManager().findPreference(GM.KEY_PREFERENCE_NOTIFICATION);
		if (!prefSync.isChecked()){
			prefNotification.setEnabled(false);
		}
		else {
			prefNotification.setEnabled(true);
			if(!prefSync.isChecked()){
				//TODO
			}
			if (prefNotification.isChecked()) {
				prefNotification.setSummary(getString(R.string.preferences_sync_notifications_on));
			} else {
				prefNotification.setSummary(getString(R.string.preferences_sync_notifications_off));
			}
		}

		prefNotification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue.toString().equals("true")) {
					preference.setSummary(getString(R.string.preferences_sync_notifications_on));
				} else {
					preference.setSummary(getString(R.string.preferences_sync_notifications_off));
				}
				return true;
			}
		});

		//Set up version preference
		Preference prefVersion = getPreferenceManager().findPreference(GM.KEY_PREFERENCE_VERSION);
		prefVersion.setSummary(com.ivalentin.margolariak.BuildConfig.VERSION_NAME);
		prefVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				//TODO: Show changelog
				return false;
			}
		});

		//Set up source preference
		Preference prefSource = getPreferenceManager().findPreference(GM.KEY_PREFERENCE_VERSION);
		prefSource.setSummary(com.ivalentin.margolariak.BuildConfig.VERSION_NAME);
		prefSource.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(GM.URL.GITHUB));
				startActivity(i);
				return false;
			}
		});

		//Set up feedback preference
		Preference prefFeedback = getPreferenceManager().findPreference(GM.KEY_PREFERENCE_VERSION);
		prefFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				//TODO: Show dialog
				return false;
			}
		});
	}
}