package com.ivalentin.margolariak;

import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

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
		if (prefSync.isChecked() == false){
			prefNotification.setEnabled(false);
		}
		else {
			prefNotification.setEnabled(true);
			if(prefSync.isChecked() == false){

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


		//Set up data preference
		//TODO


		//Set up version preference
		Preference prefVersion = getPreferenceManager().findPreference(GM.KEY_PREFERENCE_VERSION);
		prefVersion.setSummary(com.ivalentin.margolariak.BuildConfig.VERSION_NAME);
		prefNotification.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				//TODO
				return false;
			}
		});

		//Set up source preference
		//TODO

		//Set up feedback preference
		//TODO

	}
}