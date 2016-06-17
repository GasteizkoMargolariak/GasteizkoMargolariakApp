package com.ivalentin.gm;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Section that allow the user to change the app settings.
 * 
 * @author Inigo Valentin
 *
 */
public class SettingsLayout extends Fragment{

	//The user name
	private String userName;
	
	//Color to store default text color before change them
	private ColorStateList defaultColorTitle, defaultColorSummary;
	
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
		RelativeLayout rlNotificationGm = (RelativeLayout) view.findViewById(R.id.rl_settings_notifications_gm);
		RelativeLayout rlAccountGm = (RelativeLayout) view.findViewById(R.id.rl_settings_gm);
		RelativeLayout rlAccountName = (RelativeLayout) view.findViewById(R.id.rl_settings_account_name);
		final CheckBox cbNotification = (CheckBox) view.findViewById(R.id.cb_settings_notifications);
		final CheckBox cbNotificationGm = (CheckBox) view.findViewById(R.id.cb_settings_notifications_gm);
		final CheckBox cbAccountGm = (CheckBox) view.findViewById(R.id.cb_settings_gm);
		final TextView tvNotification = (TextView) view.findViewById(R.id.tv_settings_notifications_summary);
		final TextView tvNotificationGmTitle = (TextView) view.findViewById(R.id.tv_settings_notifications_gm);
		final TextView tvNotificationGm = (TextView) view.findViewById(R.id.tv_settings_notifications_gm_summary);
		final TextView tvAccountGm = (TextView) view.findViewById(R.id.tv_settings_gm_summary);
		final TextView tvAccountName = (TextView) view.findViewById(R.id.tv_settings_account_name_summary);
		
		//Get TextView defaultColors
		defaultColorTitle = tvNotificationGmTitle.getTextColors();
		defaultColorSummary = tvNotificationGm.getTextColors();
		
		//Set initial state of the settings
		SharedPreferences settings = view.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
		if (settings.getInt(GM.PREF_NOTIFICATION, GM.DEFAULT_PREF_NOTIFICATION) == 1){
			cbNotification.setChecked(true);
			tvNotification.setText(view.getContext().getString(R.string.settings_notification_on));
			cbNotificationGm.setEnabled(true);
		}
		else{
			cbNotification.setChecked(false);
			tvNotification.setText(view.getContext().getString(R.string.settings_notification_off));
			cbNotificationGm.setEnabled(false);
			tvNotificationGm.setTextColor(view.getResources().getColor(R.color.settings_disabled));
			tvNotificationGmTitle.setTextColor(view.getResources().getColor(R.color.settings_disabled));
		}
		
		if (settings.getInt(GM.PREF_NOTIFICATION_GM, GM.DEFAULT_PREF_NOTIFICATION_GM) == 1){
			cbNotificationGm.setChecked(true);
			tvNotificationGm.setText(view.getContext().getString(R.string.settings_notification_gm_on));
		}
		else{
			cbNotificationGm.setChecked(false);
			tvNotificationGm.setText(view.getContext().getString(R.string.settings_notification_gm_off));
		}
		
		if (settings.getInt(GM.PREF_GM, GM.DEFAULT_PREF_GM) == 1){
			cbAccountGm.setChecked(true);
			tvAccountGm.setText(view.getContext().getString(R.string.settings_account_gm_on));
		}
		else{
			cbAccountGm.setChecked(false);
			tvAccountGm.setText(view.getContext().getString(R.string.settings_account_gm_off));
		}
		
		String username = settings.getString(GM.USER_NAME, GM.DEFAULT_PREF_USERNAME);
		if (username.length() > 0){
			tvAccountName.setText(username);
		}
		else{
			tvAccountName.setText(view.getContext().getString(R.string.settings_account_unset));
		}
		
		//Set click listener for "Notification" preference
		rlNotification.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (cbNotification.isChecked()){
					cbNotification.setChecked(false);
					tvNotification.setText(view.getContext().getString(R.string.settings_notification_off));
					cbNotificationGm.setEnabled(false);
					SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = preferences.edit();
					editor.putInt(GM.PREF_NOTIFICATION, 0);
					editor.commit();
					//Change color of disabled preference
					tvNotificationGm.setTextColor(view.getResources().getColor(R.color.settings_disabled));
					tvNotificationGmTitle.setTextColor(view.getResources().getColor(R.color.settings_disabled));
				}
				else{
					cbNotification.setChecked(true);
					tvNotification.setText(view.getContext().getString(R.string.settings_notification_on));
					cbNotificationGm.setEnabled(true);
					SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = preferences.edit();
					editor.putInt(GM.PREF_NOTIFICATION, 1);
					editor.commit();
					//Change color of disabled preferences
					tvNotificationGmTitle.setTextColor(defaultColorTitle);
					tvNotificationGm.setTextColor(defaultColorSummary);
				}
			}
			
		});
		
		//Set click listener for the "GM notifications" preference
		rlNotificationGm.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (cbNotificationGm.isEnabled()){
					if (cbNotificationGm.isChecked()){
						cbNotificationGm.setChecked(false);
						tvNotificationGm.setText(view.getContext().getString(R.string.settings_notification_gm_off));
						SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = preferences.edit();
						editor.putInt(GM.PREF_NOTIFICATION_GM, 0);
						editor.commit();
					}
					else{
						cbNotificationGm.setChecked(true);
						tvNotificationGm.setText(view.getContext().getString(R.string.settings_notification_gm_on));
						SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = preferences.edit();
						editor.putInt(GM.PREF_NOTIFICATION_GM, 1);
						editor.commit();
					}
				}
			}			
		});
		
		//Set click listener for the "I am margolari" preference
		rlAccountGm.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (cbAccountGm.isChecked()){
					cbAccountGm.setChecked(false);
					tvAccountGm.setText(view.getContext().getString(R.string.settings_account_gm_off));
					SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = preferences.edit();
					editor.putInt(GM.PREF_GM, 0);
					editor.commit();
				}
				else{
					cbAccountGm.setChecked(true);
					tvAccountGm.setText(view.getContext().getString(R.string.settings_account_gm_on));
					SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = preferences.edit();
					editor.putInt(GM.PREF_GM, 1);
					editor.commit();
				}
			}	
		});
		
		//Set click listener for the "Name" preference
		rlAccountName.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
				View dialogLayout = inflater.inflate(R.layout.dialog_change_username, null);
				AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
				builder.setTitle(view.getContext().getString(R.string.settings_account_message_title));
				// Set up the input
				final EditText input = (EditText) dialogLayout.findViewById(R.id.et_change_username);
				builder.setView(dialogLayout);

				// Set up the buttons
				builder.setPositiveButton(view.getContext().getString(R.string.settings_account_message_save), new DialogInterface.OnClickListener() { 
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        userName = input.getText().toString();
				        tvAccountName.setText(userName);
				        SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = preferences.edit();
						editor.putString(GM.USER_NAME, userName);
						editor.commit();
				    }
				});
				builder.setNegativeButton(view.getContext().getString(R.string.settings_account_message_cancel), new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        dialog.cancel();
				    }
				});

				builder.show();
			}
		});
		
		
		return view;
	}
}
