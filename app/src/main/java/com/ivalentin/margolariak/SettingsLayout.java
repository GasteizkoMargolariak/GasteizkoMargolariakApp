package com.ivalentin.margolariak;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Section that allow the user to change the app settings.
 * 
 * @author Inigo Valentin
 *
 */
public class SettingsLayout extends Fragment{

	/**
	 * Run when the fragment is inflated.
	 * Assigns the view and the click listeners. 
	 * 
	 * @param inflater A LayoutInflater to manage views
	 * @param container The container View
	 * @param savedInstanceState Bundle containing the state
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@SuppressLint("InflateParams") //Throws unknown error when done properly.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		//Load the layout
		final View view = inflater.inflate(R.layout.fragment_layout_settings, null);
		
		//Set the title
		((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_settings));
		
		//Get views.
		RelativeLayout rlNotification = (RelativeLayout) view.findViewById(R.id.rl_settings_notifications);
		final CheckBox cbNotification = (CheckBox) view.findViewById(R.id.cb_settings_notifications);
		final TextView tvNotification = (TextView) view.findViewById(R.id.tv_settings_notifications_summary);
		TextView tvLicense = (TextView) view.findViewById(R.id.tv_setting_license);
		TextView tvPrivacy = (TextView) view.findViewById(R.id.tv_setting_privacy);
		TextView tvAbout = (TextView) view.findViewById(R.id.tv_setting_about);
		TextView tvTransparency = (TextView) view.findViewById(R.id.tv_setting_transparency);


		//Set initial state of the notification  settings
		SharedPreferences settings = view.getContext().getSharedPreferences(GM.PREFERENCES.PREFERNCES, Context.MODE_PRIVATE);
		if (settings.getBoolean(GM.PREFERENCES.KEY.NOTIFICATIONS, GM.PREFERENCES.DEFAULT.NOTIFICATIONS)){
			cbNotification.setChecked(true);
			tvNotification.setText(view.getContext().getString(R.string.settings_notification_on));
		}
		else{
			cbNotification.setChecked(false);
			tvNotification.setText(view.getContext().getString(R.string.settings_notification_off));
		}

		//Set click listener for "Notification" preference
		rlNotification.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (cbNotification.isChecked()){
					cbNotification.setChecked(false);
					tvNotification.setText(view.getContext().getString(R.string.settings_notification_off));
					SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREFERENCES.PREFERNCES, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = preferences.edit();
					editor.putBoolean(GM.PREFERENCES.KEY.NOTIFICATIONS, false);
					editor.apply();
				}
				else{
					cbNotification.setChecked(true);
					tvNotification.setText(view.getContext().getString(R.string.settings_notification_on));
					SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREFERENCES.PREFERNCES, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = preferences.edit();
					editor.putBoolean(GM.PREFERENCES.KEY.NOTIFICATIONS, true);
					editor.apply();
				}
			}
			
		});

		//Set listener for the other buttons
		tvLicense.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String title = v.getResources().getString(R.string.settings_license);
				String text = v.getResources().getString(R.string.settings_license_content);
				showDialog(title, text);
			}
		});
		tvPrivacy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String title = v.getResources().getString(R.string.settings_privacy);
				String text = v.getResources().getString(R.string.settings_privacy_content);
				showDialog(title, text);
			}
		});
		tvAbout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String title = v.getResources().getString(R.string.settings_about);
				String text = v.getResources().getString(R.string.settings_about_content);
				showDialog(title, text);
			}
		});
		tvTransparency.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String title = v.getResources().getString(R.string.settings_transparency);
				String text = v.getResources().getString(R.string.settings_transparency_content);
				showDialog(title, text);
			}
		});

		return view;
	}

	private void showDialog(String title, String text){
		//Create the dialog
		final Dialog dialog = new Dialog(getActivity());

		//Set up dialog window
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_settings);

		//Set the custom dialog components - text, image and button
		TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_dialog_settings_title);
		WebView wvText = (WebView) dialog.findViewById(R.id.wv_dialog_settings_text);

		if (Build.VERSION.SDK_INT >= 19) {
			wvText.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
		else {
			wvText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		Button btClose = (Button) dialog.findViewById(R.id.bt_dialog_settings_close);

		//Set text
		tvTitle.setText(title);
		wvText.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);

		//Set close button
		btClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		//Set parameters
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.gravity = Gravity.CENTER;
		lp.dimAmount = 0.4f;
		dialog.getWindow().setAttributes(lp);

		//Show dialog
		dialog.show();

	}
}
