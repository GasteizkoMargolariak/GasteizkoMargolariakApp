package com.ivalentin.gm;

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
public final class GM {
	
	/**
	 * Address where the web server is
	 */
	static final String SERVER = "http://margolariak.es";
	
	/**
	 * Duration of the menu sliding animation.
	 */
	static final int MENU_SLIDING_DURATION = 500;
	
	/**
	 * Interval between menu sliding queries. 
	 */
	static final int MENU_QUERY_INTERVAL = 16;
	
	/**
	 * Period of time (in seconds) at the end of witch the app will perform a sync.
	 */
	static final int PERIOD_SYNC = 10 * 60 * 1000;
	
	/**
	 * Name of the preference to store the user code with.
	 */
	static final String USER_CODE = "prefcode";
	
	/**
	 * Name of the preference to store the user name with.
	 */
	static final String USER_NAME = "prefname";
	
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
	 * Value for {@see GM.EXTRA_ACTION} that opens the oficial schedule.
	 */
	static final String EXTRA_ACTION_SCHEDULE = "schedule";
	
	/**
	 * Value for {@see GM.EXTRA_ACTION} that opens the GM schedule.
	 */
	static final String EXTRA_ACTION_GM = "gm";
	
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
	 * Constant for La Blanca section "Around me".
	 */
	static final byte SECTION_LABLANCA_AROUND = 22;

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
	 * Constant for section "About".
	 */
	static final byte SECTION_ABOUT = 6;

	/**
	 * Constant for section "Settings".
	 */
	static final byte SECTION_SETTINGS = 7;

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
	 * Name of the preference indicating if the user wants to to receive notifications intended for margolari people.
	 */
	static final String PREF_NOTIFICATION_GM = "prefNotificationGm";
	
	/**
	 * Default value of the preference indicating if the user wants to to receive notifications intended for margolari people.
	 */
	static final int DEFAULT_PREF_NOTIFICATION_GM = 1;
	
	/**
	 * Name of the preference indicating if the user is Margolari.
	 */
	static final String PREF_GM = "prefGM";
	
	/**
	 * Default value of the preference indicating if the user is Margolari.
	 */
	static final int DEFAULT_PREF_GM = 0;
	
	/**
	 * Name of the preference indicating the user name.
	 */
	static final String PREF_USERNAME = "prefUsername";
	
	/**
	 * Default value of the preference indicating the user name.
	 */
	static final String DEFAULT_PREF_USERNAME = "";
	
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
	 * Day constant. Refers to July the 25.
	 */
	static final int DAY_25 = 0;
	
	/**
	 * Day constant. Refers to August the 4.
	 */
	static final int DAY_4 = 1;
	
	/**
	 * Day constant. Refers to August the 5.
	 */
	static final int DAY_5 = 2;
	
	/**
	 * Day constant. Refers to August the 6.
	 */
	static final int DAY_6 = 3;
	
	/**
	 * Day constant. Refers to August the 7.
	 */
	static final int DAY_7 = 4;
	
	/**
	 * Day constant. Refers to August the 8.
	 */
	static final int DAY_8 = 5;
	
	/**
	 * Day constant. Refers to August the 9.
	 */
	static final int DAY_9 = 6;
	
	/**
	 * Total number of days of the festivals.
	 */
	static final int TOTAL_DAYS = 7;
	
	/**
	 * Name of the database
	 */
	static final String DB_NAME = "gm";
	
	/**
	 * Name of the table "place" in the database.
	 */
	static final String DB_PLACE = "place";
	
	/**
	 * Name of the table "people" in the database.
	 */
	static final String DB_PEOPLE = "people";
	
	/**
	 * Name of the table "event" in the database.
	 */
	static final String DB_EVENT = "event";
	
	/**
	 * Name of the table "notification" in the database.
	 */
	static final String DB_NOTIFICATION = "notification";
	
	/**
	 * Name of the column "id" in the table "place" in the database.
	 */
	static final String DB_PLACE_ID = "id";
	
	/**
	 * Index of the column "id" in the table "place" in the database.
	 */
	static final int BD_PLACE_ID_INDEX = 0;
	
	/**
	 * Name of the column "name" in the table "place" in the database.
	 */
	static final String DB_PLACE_NAME = "name";
	
	/**
	 * Index of the column "name" in the table "place" in the database.
	 */
	static final int BD_PLACE_NAME_INDEX = 1;
	
	/**
	 * Name of the column "address" in the table "place" in the database.
	 */
	static final String DB_PLACE_ADDRESS = "address";
	
	/**
	 * Index of the column "address" in the table "place" in the database.
	 */
	static final int BD_PLACE_ADDRESS_INDEX = 2;
	
	/**
	 * Name of the column "cp" in the table "place" in the database.
	 */
	static final String DB_PLACE_CP = "cp";
	
	/**
	 * Index of the column "cp" in the table "place" in the database.
	 */
	static final int BD_PLACE_CP_INDEX = 3;
	
	/**
	 * Name of the column "lat" in the table "place" in the database.
	 */
	static final String DB_PLACE_LATITUDE = "lat";
	
	/**
	 * Index of the column "lat" in the table "place" in the database.
	 */
	static final int BD_PLACE_LATITUDE_INDEX = 4;
	
	/**
	 * Name of the column "lon" in the table "place" in the database.
	 */
	static final String DB_PLACE_LONGITUDE = "lon";
	
	/**
	 * Index of the column "lon" in the table "place" in the database.
	 */
	static final int BD_PLACE_LONGITUDE_INDEX = 5;
	
	/**
	 * Name of the column "id" in the table "people" in the database.
	 */
	static final String DB_PEOPLE_ID = "id";
	
	/**
	 * Index of the column "id" in the table "people" in the database.
	 */
	static final int BD_PEOPLE_ID_INDEX = 0;
	
	/**
	 * Name of the column "name" in the table "people" in the database.
	 */
	static final String DB_PEOPLE_NAME = "name";
	
	/**
	 * Index of the column "name" in the table "people" in the database.
	 */
	static final int BD_PEOPLE_NAME_INDEX = 1;
	
	/**
	 * Name of the column "link" in the table "people" in the database.
	 */
	static final String DB_PEOPLE_LINK = "link";
	
	/**
	 * Index of the column "link" in the table "people" in the database.
	 */
	static final int BD_PEOPLE_LINK_INDEX = 2;
	
	/**
	 * Name of the column "id" in the table "event" in the database.
	 */
	static final String DB_EVENT_ID = "id";
	
	/**
	 * Index of the column "id" in the table "event" in the database.
	 */
	static final int BD_EVENT_ID_INDEX = 0;
	
	/**
	 * Name of the column "schedule" in the table "event" in the database.
	 */
	static final String DB_EVENT_SCHEDULE = "schedule";
	
	/**
	 * Index of the column "schedule" in the table "event" in the database.
	 */
	static final int BD_EVENT_SCHEDULE_INDEX = 1;
	
	/**
	 * Name of the column "gm" in the table "event" in the database.
	 */
	static final String DB_EVENT_GM = "gm";
	
	/**
	 * Index of the column "gm" in the table "event" in the database.
	 */
	static final int BD_EVENT_GM_INDEX = 2;
	
	/**
	 * Name of the column "name" in the table "event" in the database.
	 */
	static final String DB_EVENT_NAME = "name";
	
	/**
	 * Index of the column "name" in the table "event" in the database.
	 */
	static final int BD_EVENT_NAME_INDEX = 3;
	
	/**
	 * Name of the column "description" in the table "event" in the database.
	 */
	static final String DB_EVENT_DESCRIPTION = "description";
	
	/**
	 * Index of the column "description" in the table "event" in the database.
	 */
	static final int BD_EVENT_DESCRIPTION_INDEX = 4;
	
	/**
	 * Name of the column "host" in the table "event" in the database.
	 */
	static final String DB_EVENT_HOST = "host";
	
	/**
	 * Index of the column "host" in the table "event" in the database.
	 */
	static final int BD_EVENT_HOST_INDEX = 5;
	
	/**
	 * Name of the column "place" in the table "event" in the database.
	 */
	static final String DB_EVENT_PLACE = "place";
	
	/**
	 * Index of the column "place" in the table "event" in the database.
	 */
	static final int BD_EVENT_PLACE_INDEX = 6;
	
	/**
	 * Name of the column "start" in the table "event" in the database.
	 */
	static final String DB_EVENT_START = "start";
	
	/**
	 * Index of the column "start" in the table "event" in the database.
	 */
	static final int BD_EVENT_START_INDEX = 7;
	
	/**
	 * Name of the column "end" in the table "event" in the database.
	 */
	static final String DB_EVENT_END = "end";
	
	/**
	 * Index of the column "end" in the table "event" in the database.
	 */
	static final int BD_EVENT_END_INDEX = 8;
	
	/**
	 * Name of the column "id" in the table "notification" in the database.
	 */
	static final String DB_NOTIFICATION_ID = "id";
	
	/**
	 * Index of the column "id" in the table "notification" in the database.
	 */
	static final int BD_NOTIFICATION_ID_INDEX = 0;
	
	/**
	 * Name of the column "user" in the table "notification" in the database.
	 */
	static final String DB_NOTIFICATION_USER = "user";
	
	/**
	 * Index of the column "user" in the table "notification" in the database.
	 */
	static final int BD_NOTIFICATION_USER_INDEX = 1;
	
	/**
	 * Name of the column "time" in the table "notification" in the database.
	 */
	static final String DB_NOTIFICATION_TIME = "time";
	
	/**
	 * Index of the column "time" in the table "notification" in the database.
	 */
	static final int BD_NOTIFICATION_TIME_INDEX = 2;
	
	/**
	 * Name of the column "duration" in the table "notification" in the database.
	 */
	static final String DB_NOTIFICATION_DURATION = "duration";
	
	/**
	 * Index of the column "id" in the table "notification" in the database.
	 */
	static final int BD_NOTIFICATION_DURATION_INDEX = 3;
	
	/**
	 * Name of the column "type" in the table "notification" in the database.
	 */
	static final String DB_NOTIFICATION_TYPE = "type";
	
	/**
	 * Index of the column "type" in the table "notification" in the database.
	 */
	static final int BD_NOTIFICATION_TYPE_INDEX = 4;
	
	/**
	 * Name of the column "title" in the table "notification" in the database.
	 */
	static final String DB_NOTIFICATION_TITLE = "title";
	
	/**
	 * Index of the column "title" in the table "notification" in the database.
	 */
	static final int BD_NOTIFICATION_TITLE_INDEX = 5;
	
	/**
	 * Name of the column "text" in the table "notification" in the database.
	 */
	static final String DB_NOTIFICATION_TEXT = "text";
	
	/**
	 * Index of the column "text" in the table "notification" in the database.
	 */
	static final int BD_NOTIFICATION_TEXT_INDEX = 6;
	
	/**
	 * Name of the column "gm" in the table "notification" in the database.
	 */
	static final String DB_NOTIFICATION_GM = "gm";
	
	/**
	 * Index of the column "text" in the table "notification" in the database.
	 */
	static final int BD_NOTIFICATION_GM_INDEX = 7;
	
	/**
	 * The desired accuracy of the GPS coordinates, used to provide location updates, in meters.
	 */
	static final int LOCATION_ACCURACY = 20;

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
	public static final String formatDate(String dateString, String lang, boolean includeTime){

		String output = "";

		String dayNameEs[] = {"dummy", "Domingo",  "Lunes", "Martes", "Mi&eacute;rcoles", "Jueves", "Viernes", "S&aacute;bado"};
		String dayNameEn[] = {"dummy", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
		String dayNameEu[] = {"dummy", "igandea", "astelehena", "asteartea", "asteazkena", "osteguna", "ostirala", "larumbata"};
		String monthNameEs[] = {"enero", " febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
		String monthNameEn[] = {"January", "February", "March", "April", "May", "June", "July", "Augost", "September", "Octuber", "November", "December"};
		String monthNameEu[] = {"urtarrilaren", "otsailaren", "martxoaren", "apirilaren", "maiatzaren", "ekainaren", "uztailaren", "abuztuaren", "irailaren", "urriaren", "azaroaren", "abenduaren"};

		if (lang != "es" && lang != "eu"){
			lang = "en";
		}
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
		SimpleDateFormat df;
		Date date;
		try{
			date = format.parse(dateString);
			df = new SimpleDateFormat("yyyy");
			String year = df.format(date);
			//df = new SimpleDateFormat("uu");
			//int dayNumber = Integer.parseInt(df.format(date));
			df = new SimpleDateFormat("dd");
			String day = df.format(date);
			//df = new SimpleDateFormat("dd");
			//int monthNumber = Integer.parseInt(df.format(date));
			df = new SimpleDateFormat("hh:mm");
			String time = df.format(date);
			String dayName, monthName;

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			int dayNumber = calendar.get(Calendar.DAY_OF_WEEK);
			int monthNumber = calendar.get(Calendar.MONTH);

			switch (lang){
				case "es":
					dayName = dayNameEs[dayNumber];
					monthName = monthNameEs[monthNumber];
					output = dayName + ", " + dayNumber + " de " + monthName + " de " + year;
					if (includeTime){
						output = output + " a las " + time;
					}
					break;
				case "eu":
					dayName = dayNameEu[dayNumber];
					monthName = monthNameEu[monthNumber];
					output = year + "ko" + monthName + " " + dayNumber + "an, " + dayName;
					if (includeTime){
						output = output + " " + time + "etan";
					}
					break;
				default:
					dayName = dayNameEn[dayNumber];
					monthName = monthNameEn[monthNumber];
					output = dayName + ", " + monthName + " " + dayNumber + ", " + year;
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
