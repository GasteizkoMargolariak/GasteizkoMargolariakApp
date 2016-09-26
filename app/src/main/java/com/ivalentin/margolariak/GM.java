package com.ivalentin.margolariak;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Final class that contains useful static values to be used across the app.
 * 
 * @author Iñigo Valentin
 *
 */
final class GM {
	
	/**
	 * Address where the web server is
	 */
	static final String SERVER = "http://margolariak.es";

	/**
	 * Code for the location permission request
	 */
	static final int PERMISSION_LOCATION = 1;
	
	/**
	 * Period of time (in seconds) at the end of witch the app will perform a sync.
	 */
	static final int PERIOD_SYNC = 20 * 60 * 1000;
	
	/**
	 * Name of the preference to store the user code with.
	 */
	static final String USER_CODE = "prefcode";

	/**
	 * Constant to diferentiate schedules".
	 */
	static final String SCHEDULE = "schedule";
	
	/**
	 * Constant to set extras on the main intent.
	 */
	static final String EXTRA_TITLE = "title";
	
	/**
	 * Constant to set extras on the main intent.
	 */
	static final String EXTRA_TEXT = "text";
	
	/**
	 * Constant to set extras on the main intent.
	 */
	static final String EXTRA_ACTION = "action";

	/**
	 * Value for {@see GM.EXTRA_ACTION} that opens the lablanca section.
	 */
	static final String EXTRA_ACTION_LABLANCA = "lablanca";

	/**
	 * Value for {@see GM.EXTRA_ACTION} that opens the official schedule.
	 */
	static final String EXTRA_ACTION_CITYSCHEDULE = "cityschedule";
	
	/**
	 * Value for {@see GM.EXTRA_ACTION} that opens the GM schedule.
	 */
	static final String EXTRA_ACTION_GMSCHEDULE = "gmschedule";

	/**
	 * Value for {@see GM.EXTRA_ACTION} that opens the location secton.
	 */
	static final String EXTRA_ACTION_LOCATION = "location";

	/**
	 * Value for {@see GM.EXTRA_ACTION} that opens the blog.
	 */
	static final String EXTRA_ACTION_BLOG = "blog";

	/**
	 * Value for {@see GM.EXTRA_ACTION} that opens the activities section.
	 */
	static final String EXTRA_ACTION_ACTIVITIES = "activities";

	/**
	 * Value for {@see GM.EXTRA_ACTION} that opens the gallery section.
	 */
	static final String EXTRA_ACTION_GALLERY = "gallery";
	
	/**
	 * Constant for section "Home".
	 */
	static final byte SECTION_HOME = 0;

	/**
	 * Constant for La Blanca section "Location".
	 */
	static final byte SECTION_LOCATION = 1;

	/**
	 * Constant for section "La Blanca".
	 */
	static final byte SECTION_LABLANCA = 2;

	/**
	 * Constant for La Blanca section "Festival schedule".
	 */
	static final byte SECTION_LABLANCA_SCHEDULE = 20;

	/**
	 * Constant for La Blanca section "Gasteizko Margolariak Schedule".
	 */
	static final byte SECTION_LABLANCA_GM_SCHEDULE = 21;

	/**
	 * Constant for section "Activities".
	 */
	static final byte SECTION_ACTIVITIES = 3;

	/**
	 * Constant for section "Blog".
	 */
	static final byte SECTION_BLOG = 4;

	/**
	 * Constant for section "Gallery".
	 */
	static final byte SECTION_GALLERY = 5;

	/**
	 * Name of the preference group for the app.
	 */
	static final String PREF = "gmpreferences";
	
	/**
	 * Name of the preference to store the database version with.
	 */
	static final String PREF_DB_VERSION = "prefDbVersion";

	/**
	 * Name of the preference to store the setting that allows user to upload comments.
	 */
	static final String PREF_DB_PHOTOS = "prefDbPhotos";

	/**
	 * Name of the preference that indicates if we are on festivals.
	 */
	static final String PREF_DB_FESTIVALS = "prefDbFestivals";

	/**
	 * Name of the preference indicating if the user wants to to receive notifications intended for the general public.
	 */
	static final String PREF_NOTIFICATION = "prefNotification";

	/**
	 * Default value of the preference indicating if the user wants to to receive notifications intended for the general public.
	 */
	static final int DEFAULT_PREF_NOTIFICATION = 1;

	/**
	 * Header of a preference to indicate received notifications.
	 */
	static final String NOTIFICATION_SEEN_ ="notificationSeen";
	
	/**
	 * Default value for the {//@link GM.PREF_DB_VERSION}  preference
	 */
	static final int DEFAULT_PREF_DB_VERSION = -2;
	
	/**
	 * Name of the preference to store GM location's latitude.
	 */
	static final String PREF_GM_LATITUDE = "gmlat";
	
	/**
	 * Name of the preference to store GM location's longitude.
	 */
	static final String PREF_GM_LONGITUDE = "gmlon";
	
	/**
	 * Name of the preference to store GM last location timestamp.
	 */
	static final String PREF_GM_LOCATION = "gmlocationtime";

	/**
	 * Name of the preference to store GM last location timestamp.
	 */
	static final String DEFAULT_PREF_GM_LOCATION = "19700101000000";

	/**
	 * Name of the preference indicating if the user wants to perform background syncs.
	 */
	static final String PREFERENCE_SYNC_SYNC = "pref_sync_sync";

	/**
	 * Default value for PREFERENCE_SYNC_SYNC.
	 */
	static final boolean DEFAULT_PREFERENCE_SYNC_SYNC = true;

	/**
	 * Name of the preference indicating if the user wants to to receive notifications intended for the general public.
	 */
	static final String PREFERENCE_SYNC_NOTIFICATIONS = "pref_sync_notifications";

	/**
	 * Default value for PREFERENCE_SYNC_NOTIFICATIONS.
	 */
	static final boolean DEFAULT_PREFERENCE_SYNC_NOTIFICATIONS = true;

	/**
	 * Name of the SharedPreference that stores how many kb have been sent in the foreground.
	 */
	static final String STORAGE_TRAFFIC_FG_SENT = "storage_traffic_fg_sent";

	/**
	 * Name of the SharedPreference that stores how many kb have been received in the foreground.
	 */
	static final String STORAGE_TRAFFIC_FG_RECEIVED = "storage_traffic_fg_received";

	/**
	 * Name of the SharedPreference that stores how many kb have been sent in the background.
	 */
	static final String STORAGE_TRAFFIC_BG_SENT = "storage_traffic_bg_received";

	/**
	 * Name of the SharedPreference that stores how many kb have been received in the background.
	 */
	static final String STORAGE_TRAFFIC_BG_RECEIVED = "storage_traffic_bg_received";

	static final String KEY_PREFERENCE_SYNC = "preference_key_sync";

	static final String KEY_PREFERENCE_NOTIFICATION = "preference_key_notification";

	static final String KEY_PREFERENCE_DATA = "preference_key_data";

	static final String KEY_PREFERENCE_VERSION = "preference_key_version";

	static final String KEY_PREFERENCE_SOURCE = "preference_key_source";

	static final String KEY_PREFERENCE_FEEDBACK = "preference_key_feedback";

	/**
	 * Name of the database
	 */
	static final String DB_NAME = "gm";

	/**
	 * The desired accuracy of the GPS coordinates, used to provide location updates, in meters.
	 */
	static final int LOCATION_ACCURACY_SPACE = 10;

	/**
	 * The desired accuracy of the GPS coordinates, used to provide location updates, in milliseconds.
	 */
	static final int LOCATION_ACCURACY_TIME = 10000;

	/**
	 * Gets the language code for sql queries.
	 * Only three values can be returned: es, eu, en.
	 * Defaults to es.
	 *
	 * @return Language two-letter code
	 */
	public static String getLang(){
		String currLang = Locale.getDefault().getISO3Language();//getDisplayLanguage();
		switch (currLang){
			case "eng":
				currLang = "en";
				break;
			case "eus":
				currLang = "eu";
				break;
			default:
				currLang = "es";
		}
		return currLang;
	}

	/**
	 * Formats a datetime format strings and returns a human readable date, depending on the language
	 *
	 * @param dateString String in a datetime format
	 * @param lang Language (es, en, eu)
	 * @param includeTime Also returns the hour and minutes
	 *
	 * @return The fragment view
	 *
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	public static String formatDate(String dateString, String lang, boolean includeTime){

		String output = "";

		String dayNameEs[] = {"dummy", "Domingo",  "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
		String dayNameEn[] = {"dummy", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
		String dayNameEu[] = {"dummy", "igandea", "astelehena", "asteartea", "asteazkena", "osteguna", "ostirala", "larumbata"};
		String monthNameEs[] = {"enero", " febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
		String monthNameEn[] = {"January", "February", "March", "April", "May", "June", "July", "Augost", "September", "Octuber", "November", "December"};
		String monthNameEu[] = {"urtarrilaren", "otsailaren", "martxoaren", "apirilaren", "maiatzaren", "ekainaren", "uztailaren", "abuztuaren", "irailaren", "urriaren", "azaroaren", "abenduaren"};

		if (!lang.equals("es") && !lang.equals("eu")){
			lang = "en";
		}
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
		SimpleDateFormat df;
		Date date;
		try{
			date = format.parse(dateString);
			df = new SimpleDateFormat("yyyy", Locale.US);
			String year = df.format(date);
			df = new SimpleDateFormat("hh:mm", Locale.US);
			String time = df.format(date);
			String dayName, monthName;

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			int dayNumber = calendar.get(Calendar.DAY_OF_WEEK);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int monthNumber = calendar.get(Calendar.MONTH);

			switch (lang){
				case "es":
					dayName = dayNameEs[dayNumber];
					monthName = monthNameEs[monthNumber];
					output = dayName + ", " + day + " de " + monthName + " de " + year;
					if (includeTime){
						output = output + " a las " + time;
					}
					break;
				case "eu":
					dayName = dayNameEu[dayNumber];
					monthName = monthNameEu[monthNumber];
					output = year + "ko" + monthName + " " + day + "an, " + dayName;
					if (includeTime){
						output = output + " " + time + "etan";
					}
					break;
				default:
					dayName = dayNameEn[dayNumber];
					monthName = monthNameEn[monthNumber];
					output = dayName + ", " + monthName + " " + day + ", " + year;
					if (includeTime){
						output = output + " " + time;
					}
					break;
			}
		}
		catch(Exception ex){
			Log.e("Date format error", ex.toString());
		}

		return output;
	}
}
