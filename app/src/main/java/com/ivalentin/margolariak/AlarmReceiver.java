package com.ivalentin.margolariak;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
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
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
	@SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent intent) {
        
    	Log.d("Alarm", "Received");
		    	
		//Open the preferences to be available several times later.
		SharedPreferences settings = context.getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
		
		FetchURL fu;
		
		//Check if the user wants to receive notifications
		if (settings.getInt(GM.PREF_NOTIFICATION , GM.DEFAULT_PREF_NOTIFICATION) == 1){

			//Get the file
			try{
				fu = new FetchURL();
				fu.Run(GM.SERVER + "/app/notifications.php");
				Log.d("Alarm", "Fetched notifications");

				//Parse info
				String o = fu.getOutput().toString();
				while (o.contains("<notification>")){
					//Get non-language-dependant fields
					String notification = o.substring(o.indexOf("<notification>") + 14, o.indexOf("</notification>"));
					String id = notification.substring(notification.indexOf("<id>") + 4, notification.indexOf("</id>"));
					String action = notification.substring(notification.indexOf("<action>") + 8, notification.indexOf("</action>"));

					//Is the notification seen already?
					if(!settings.getBoolean(GM.NOTIFICATION_SEEN_ + id, false)){

						String lang = GM.getLang();
						String title = notification.substring(notification.indexOf("<title_" + lang + ">") + 10, notification.indexOf("</title_" + lang + ">"));
						String text = notification.substring(notification.indexOf("<text_" + lang + ">") + 9, notification.indexOf("</text_" + lang + ">"));


						//Get the notification manager ready
						NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

						//Variables to create intents for each notification
						Intent resultIntent;
						TaskStackBuilder stackBuilder;

						//Send the notification.
						NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
								.setSmallIcon(R.drawable.ic_notification)
								.setContentTitle(title)
								.setAutoCancel(true)
								.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
								.setVibrate((new long[] { 400, 400, 400}))
								.setColor(context.getResources().getColor(R.color.background_notification))
								.setSubText(context.getString(R.string.app_name))
								.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
								.setContentText(text);

						// Creates an intent for an Activity to be launched from the notification.
						resultIntent = new Intent(context, MainActivity.class);
						resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

						//Set extras depending on type.
						resultIntent.putExtra(GM.EXTRA_TEXT, text);
						resultIntent.putExtra(GM.EXTRA_TITLE, title);
						resultIntent.putExtra(GM.EXTRA_ACTION, action);

						//Add the intent to the notification.
						stackBuilder = TaskStackBuilder.create(context);
						stackBuilder.addParentStack(MainActivity.class);

						// Adds the Intent that starts the Activity to the top of the stack.
						stackBuilder.addNextIntent(resultIntent);
						PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
						mBuilder.setContentIntent(resultPendingIntent);

						//Actually send the notification.
						mNotificationManager.notify(Integer.parseInt(id), mBuilder.build());

						//Mark as notified
						SharedPreferences.Editor editor = settings.edit();
						editor.putBoolean(GM.NOTIFICATION_SEEN_ + id, true);
						editor.apply();
					}
				o = o.substring(o.indexOf("</notification>") + 15);
				}
			}
			catch(Exception ex) {
				Log.e("Notification error", "Error fetching remote file: " + ex.toString());
			}
    	}
		
		//Get location
		boolean result = false;
		fu = new FetchURL();
		fu.Run(GM.SERVER + "/app/location.php");
		String lat = "", lon = "";
		String o = fu.getOutput().toString();
		Log.d("Alarm", "Fetched location");
		if (o.contains("<location>none</location>"))
			result = false;
		if (o.contains("<lat>") && o.contains("<lon>")){
			lat = o.substring(o.indexOf("<lat>") + 5, o.indexOf("</lat>"));
			lon = o.substring(o.indexOf("<lon>") + 5, o.indexOf("</lon>"));
			result = true;
		}
		if (result){
			SharedPreferences.Editor editor = settings.edit();
	    	editor.putLong(GM.PREF_GM_LATITUDE, Double.doubleToLongBits(Double.parseDouble(lat)));
			editor.putLong(GM.PREF_GM_LONGITUDE, Double.doubleToLongBits(Double.parseDouble(lon)));
			editor.putString(GM.PREF_GM_LOCATION, new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Calendar.getInstance().getTime()));
	    	editor.apply();
		}

		//Perform a background sync
		new Sync(context).execute();
    }

    /**
     * Sets a repeating alarm. 
     * When the alarm fires, the app broadcasts an Intent to this WakefulBroadcastReceiver.
     * @param context The context of the app
     */
    public void setAlarm(Context context) {
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        
        //Set the alarm cycle.
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, GM.PERIOD_SYNC, GM.PERIOD_SYNC, alarmIntent);
        
        // Enable SampleBootReceiver to automatically restart the alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);           
    }
}
