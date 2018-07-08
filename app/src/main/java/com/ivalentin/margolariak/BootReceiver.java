package com.ivalentin.margolariak;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver that receives the BOOT_COMPLETED event and sets an alarm.
 * 
 * @see BroadcastReceiver
 * 
 * @author Iñigo Valentin
 *
 */
public class BootReceiver extends BroadcastReceiver {

	/**
	 * Called when the BroadCasteReceiver is receiving an Intent broadcast. 
	 * Starts an alarm. 
	 * 
	 * @param context Context of the application
	 * @param intent Receiver intent
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		//If the event is BOOT_COMPLETED...
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			//... set the alarm.
			AlarmReceiver alarm = new AlarmReceiver();
			alarm.setAlarm(context);
		}
	}
}