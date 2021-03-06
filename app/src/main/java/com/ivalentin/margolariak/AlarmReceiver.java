package com.ivalentin.margolariak;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.content.BroadcastReceiver;
import android.os.Build;
import android.util.Log;

/**
 * Manages alarms to perform actions in the background, such as sync and receive notifications.
 * 
 * @see BroadcastReceiver
 * 
 * @author Iñigo Valentin
 *
 */
public class AlarmReceiver extends BroadcastReceiver {

    /**
     * Actions to be performed when an alarm is received. 
     * Performs a full sync, and gets the pending notifications.
     * 
     * @param context Context of the activity or application
     * @param intent Receiver intent
     * 
     * @see BroadcastReceiver#onReceive(Context, Intent)
     */
    @Override
	@SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent intent) {

		//Open the preferences to be available several times later.
		SharedPreferences preferences = context.getSharedPreferences(GM.PREFERENCES.PREFERNCES, Context.MODE_PRIVATE);
		
		FetchURL fu;
		
		//Check if the user wants to receive notifications
		if (preferences.getBoolean(GM.PREFERENCES.KEY.NOTIFICATIONS, GM.PREFERENCES.DEFAULT.NOTIFICATIONS)){

			//Get the file
			try{
				fu = new FetchURL();
				fu.Run(GM.API.SERVER + GM.API.NOTIFICATION.PATH);
				Log.d("ALARM_RECEIVER", "Fetched notifications");

				//Parse info
				String o = fu.getOutput().toString();

				//Open database
				SQLiteDatabase db;
				try {
					db = context.openOrCreateDatabase(GM.DB.NAME, Activity.MODE_PRIVATE, null);
					if (db.isReadOnly()) {
						Log.e("ALARM_RECEIVER", "Database is locked and in read only mode. Not reading new notifications now, but I'll try to sync.");
						new Sync(context).execute(); // Sync before exiting.
						return;
					}
				}
				catch(SQLiteDatabaseLockedException ex){
					Log.e("ALARM_RECEIVER", "Database is locked and in read only mode. Not reading new notifications now, but I'll try to sync.");
					new Sync(context).execute(); // Sync before exiting.
					return;
				}

				Cursor cursor;

				//Variables for parsing
				String not, title_es, title_en, title_eu, text_es, text_en, text_eu, dtime, action;
				int id, gm, duration;

				//Variables for showinfg the notification
				String lang = GM.getLang();
				String title, text;

				int counter = 0;
				while (counter < 10 && o.contains("\"}")){

					//Get the notifications
					not = o.substring(o.indexOf("{\"") + 2, o.indexOf("\"}"));

					//Extract data
					id = Integer.valueOf(not.substring(not.indexOf("\"id\":") + 6, not.indexOf("\",")));
					title_es = decode(not.substring(not.indexOf("\"title_es\":") + 12, not.indexOf("\"", not.indexOf("\"title_es\":") + 13)));
					title_en = decode(not.substring(not.indexOf("\"title_en\":") + 12, not.indexOf("\"", not.indexOf("\"title_en\":") + 13)));
					title_eu = decode(not.substring(not.indexOf("\"title_eu\":") + 12, not.indexOf("\"", not.indexOf("\"title_eu\":") + 13)));
					text_es = decode(not.substring(not.indexOf("\"text_es\":") + 11, not.indexOf("\"", not.indexOf("\"text_es\":") + 12)));
					text_en = decode(not.substring(not.indexOf("\"text_en\":") + 11, not.indexOf("\"", not.indexOf("\"text_en\":") + 12)));
					text_eu = decode(not.substring(not.indexOf("\"text_eu\":") + 11, not.indexOf("\"", not.indexOf("\"text_eu\":") + 12)));
					dtime = not.substring(not.indexOf("\"dtime\":") + 9, not.indexOf("\"", not.indexOf("\"dtime\":") + 10));
					action = not.substring(not.indexOf("\"action\":") + 10, not.indexOf("\"", not.indexOf("\"action\":") + 11));
					gm = Integer.valueOf(not.substring(not.indexOf("\"gm\":") + 6, not.indexOf("\"", not.indexOf("\"gm\":") + 7)));
					duration = Integer.valueOf(not.substring(not.indexOf("\"duration\":") + 12, not.indexOf("\"", not.indexOf("\"duration\":") + 13)));


					//Compare with the database
					cursor = db.rawQuery("SELECT id FROM notification WHERE id = " + id + ";", null);
					if (cursor.getCount() == 0) {
						db.execSQL("INSERT INTO notification (id, title_es, title_en, title_eu, text_es, text_en, text_eu, dtime, internal, duration, action) VALUES " +
								"(" + id + ", \"" + title_es + "\", \"" + title_en + "\", \"" + title_eu + "\", \"" + text_es + "\", \"" + text_en + "\", \"" + text_eu + "\", \"" + dtime + "\", " + gm + ", " + duration + ", \"" + action + "\");");

						//TODO: Show notification;
						switch (lang) {
							case "en":
								title = title_en;
								text = text_en;
								break;
							case "eu":
								title = title_eu;
								text = text_eu;
								break;
							default:
								title = title_es;
								text = text_es;
								break;
						}

						title = decode(title);
						text = decode(text);

						//Get the notification manager ready
						NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

						//Variables to create intents for each notification
						Intent resultIntent;
						TaskStackBuilder stackBuilder;

						//Send the notification.
						Notification.Builder mBuilder = new Notification.Builder(context)
								.setSmallIcon(R.drawable.ic_notification)
								.setContentTitle(title)
								.setAutoCancel(true)
								.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
								.setVibrate((new long[]{400, 400, 400}))
								.setSubText(context.getString(R.string.app_name))
								.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
								.setContentText(text);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							mBuilder.setColor(context.getResources().getColor(R.color.background_notification));
						}

						// Creates an intent for an Activity to be launched from the notification.
						resultIntent = new Intent(context, MainActivity.class);
						resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

						//Set extras depending on type.
						resultIntent.putExtra(GM.EXTRA.TEXT, text);
						resultIntent.putExtra(GM.EXTRA.TITLE, title);
						resultIntent.putExtra(GM.EXTRA.ACTION, action);

						//Add the intent to the notification.
						stackBuilder = TaskStackBuilder.create(context);
						stackBuilder.addParentStack(MainActivity.class);

						// Adds the Intent that starts the Activity to the top of the stack.
						stackBuilder.addNextIntent(resultIntent);
						PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
						mBuilder.setContentIntent(resultPendingIntent);

						//Actually send the notification.
						if (mNotificationManager != null) {
							mNotificationManager.notify(id, mBuilder.build());
						}

					}
					cursor.close();


					o = o.substring(o.indexOf("\"}") + 3);
					counter ++;
				}
				db.close();

			}
			catch(NumberFormatException ex) {
				Log.e("ALARM_RECEIVER", "Notification: Error parsing remote file: " + ex.toString());
			}
			catch (Exception ex){
				Log.e("ALARM_RECEIVER", "Notification: Error fetching notifications: " + ex.toString());
			}
    	}

		//Perform a background sync
		new Sync(context).execute();
    }

		/**
		 * Decodes unicode characters from a string.
		 * Useful for JSON encoded strings.
		 * @param in String to be decoded.
		 * @return Decoded string.
		 */
		private static String decode(final String in){
			String working = in;
			int index = working.indexOf("\\u");
			while(index > -1){
				int length = working.length();
					if(index > (length-6))
						break;
				int numStart = index + 2;
				int numFinish = numStart + 4;
				String substring = working.substring(numStart, numFinish);
				int number = Integer.parseInt(substring,16);
				String stringStart = working.substring(0, index);
				String stringEnd   = working.substring(numFinish);
				working = stringStart + ((char)number) + stringEnd;
				index = working.indexOf("\\u");
			}
			return working;
		}

    /**
     * Sets a repeating alarm. o

     * When the alarm fires, the app broadcasts an Intent to this WakefulBroadcastReceiver.
     * @param context The context of the app
     */
    public void setAlarm(Context context) {
		//Only do this if the preference is enabled
		SharedPreferences prefs = context.getSharedPreferences(GM.PREFERENCES.PREFERNCES, Context.MODE_PRIVATE);
		if (prefs.getBoolean(GM.PREFERENCES.KEY.SYNC, GM.PREFERENCES.DEFAULT.SYNC)) {

			AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, AlarmReceiver.class);
			PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

			//Set the alarm cycle.
			//TODO: Set the most appropriate interval
			if (alarmMgr != null) {
				alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, GM.PERIOD_SYNC.FESTIVALS, GM.PERIOD_SYNC.FESTIVALS, alarmIntent);
			}

			// Enable SampleBootReceiver to automatically restart the alarm when the device is rebooted.
			ComponentName receiver = new ComponentName(context, BootReceiver.class);
			PackageManager pm = context.getPackageManager();
			pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		}
    }

}
