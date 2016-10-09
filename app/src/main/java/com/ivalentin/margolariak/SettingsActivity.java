package com.ivalentin.margolariak;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import java.text.DecimalFormat;
import java.text.NumberFormat;

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


		//Set up data preference
		long bgData = sp.getLong(GM.STORAGE_TRAFFIC_BG_RECEIVED, 0) + sp.getLong(GM.STORAGE_TRAFFIC_BG_SENT, 0);
		long fgData = sp.getLong(GM.STORAGE_TRAFFIC_FG_RECEIVED, 0) + sp.getLong(GM.STORAGE_TRAFFIC_FG_SENT, 0);
		String value;
		if (bgData < 1500){
			value = bgData + getString(R.string.Kb);
		}
		else if(bgData < 5 * 1024){
			NumberFormat formatter = new DecimalFormat("#0.00");
			value = (formatter.format(((float) bgData) / 1024.0)) + getString(R.string.Mb);
		}
		else{
			value = String.valueOf((int) (((float) bgData) / 1024.0)) + getString(R.string.Mb);
		}
		Preference prefData = getPreferenceManager().findPreference(GM.KEY_PREFERENCE_DATA);
		prefData.setSummary(value);



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