package com.ivalentin.margolariak;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Activity that allows the user to change the app settings.
 *
 * @author Iñigo Valentin
 * @see Activity
 */
public class SettingsActivity extends Activity {

	//Assign menu items
	private LinearLayout llSync;
	private LinearLayout llNotifications;
	private CheckBox cbSync, cbNotifications;
	private TextView tvSync;
	private TextView tvNotifications;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;

	/**
	 * Run when the app is created. Assigns views, configures their initial
	 * states, and sets listeners
	 *
	 * @param savedInstanceState Activity state.
	 *
	 * @see Activity#onCreate(Bundle)
	 */
	@SuppressLint("CommitPrefEdits") // apply() is called in other methods
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
		LinearLayout llVersion = (LinearLayout) findViewById(R.id.ll_settings_version);
		LinearLayout llSource = (LinearLayout) findViewById(R.id.ll_settings_source);
		LinearLayout llFeedback = (LinearLayout) findViewById(R.id.ll_settings_feedback);
		tvSync = (TextView) findViewById(R.id.tv_settings_sync);
		tvNotifications = (TextView) findViewById(R.id.tv_settings_notifications);
		TextView tvVersion = (TextView) findViewById(R.id.tv_settings_version);
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
				showChangelog();
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

	/**
	 * UI feedback for touched settings.
	 * Just makes them blink.
	 *
	 * @param v The view to be made to blink
	 */
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

	/**
	 * Not called from code, but referenced from activity_settings.xml.
	 * Toggles the value of the sync preferences, changing the actual
	 * preference, the ui, and, if set to disabled, it also disables the
	 * notification setting.
	 *
	 * @param v Ignored
	 */
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

	/**
	 * Not called from code, but referenced from activity_settings.xml.
	 * Toggles the value of the notification preferences, changing the
	 * actual preference and the ui.
	 *
	 * @param v Ignored
	 */
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

	/**
	 * Enables the notification setting toggler. Must be called when
	 * the sync preference changes to true.
	 */
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

	/**
	 * Enables the notification setting toggler. Must be called when
	 * the sync preference changes to false.
	 */
	private void disableNotifications(){
		llNotifications.setAlpha(0.4f);
		tvNotifications.setText(R.string.preferences_sync_notifications_disabled);
		cbNotifications.setChecked(false);
	}

	private void showChangelog(){

		//Create the dialog
		final Dialog dialog = new Dialog(SettingsActivity.this);

		//Set up dialog window
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_changelog);

		//Set the web view
		WebView wv = (WebView) dialog.findViewById(R.id.wv_changelog);
		wv.getSettings().setAllowContentAccess(true);
		Log.e("URL", "file:///android_assets/changelog_" + GM.getLang() + ".html");
		wv.loadUrl("file:///android_asset/changelog_" + GM.getLang() + ".html");

		//Set close button
		Button bt = (Button) dialog.findViewById(R.id.bt_changelog);
		bt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		//Prep the dialog
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.gravity = Gravity.CENTER;
		//lp.dimAmount = 0.0f;
		dialog.getWindow().setAttributes(lp);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		//Show dialog
		dialog.show();

	}

	/**
	 * Not called from code, but referenced from activity_settings.xml.
	 * Finishes the activity.
	 *
	 * @param v Ignored
	 *
	 * @see Activity#finish()
	 */
	public void finish(View v){
		finish();
	}
}