package com.ivalentin.margolariak;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	 * Contains info about the database.
	 */
	final class DB {

		static final String NAME = "gm";

		/**
		 * Column types
		 */
		final class COLUMN {

			/**
			 * INT column.
			 */
			static final int INT = 0;

			/**
			 * VARCHAR column.
			 */
			static final int VARCHAR = 1;

			/**
			 * Datetime column.
			 */
			static final int DATETIME = 2;
		}

		/**
		 * Usefull queries
		 */
		final class QUERY {

			/**
			 * Queries to create tables.
			 */
			final class CREATE {

				/**
				 * Creates the table "activity" on the database.
				 */
				static final String ACTIVITY = "CREATE TABLE IF NOT EXISTS activity (id INT, permalink VARCHAR, date DATETIME, city VARCHAR, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, after_es VARCHAR, after_en VARCHAR, after_eu VARCHAR, price INT, inscription INT, max_people INT, album INT, dtime DATETIME, comments INT);";

				/**
				 * Creates the table "activity_comment" on the database.
				 */
				static final String ACTIVITY_COMMENT = "CREATE TABLE IF NOT EXISTS activity_comment (id INT, activity INT, text VARCHAR, dtime DATETIME, username VARCHAR, lang VARCHAR);";

				/**
				 * Creates the table "activity_image" on the database.
				 */
				static final String ACTIVITY_IMAGE = "CREATE TABLE IF NOT EXISTS activity_image (id INT, activity INT, image VARCHAR, idx INT);";

				/**
				 * Creates the table "activity_itinerary" on the database.
				 */
				static final String ACTIVITY_ITINERARY = "CREATE TABLE IF NOT EXISTS activity_itinerary (id INT, activity INT, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, description_es VARCHAR, description_en VARCHAR, description_eu VARCHAR, start DATETIME, end DATETIME, place INT);";

				/**
				 * Creates the table "activity_tag" on the database.
				 */
				static final String ACTIVITY_TAG = "CREATE TABLE IF NOT EXISTS activity_tag (activity INT, tag VARCHAR);";

				/**
				 * Creates the table "album" on the database.
				 */
				static final String ALBUM = "CREATE TABLE IF NOT EXISTS album (id INT, permalink VARCHAR, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, description_es VARCHAR, description_en VARCHAR, description_eu VARCHAR, open INT, time DATETIME);";

				/**
				 * Creates the table "festival" on the database.
				 */
				static final String FESTIVAL = "CREATE TABLE IF NOT EXISTS festival (id INT, year INT, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, summary_es VARCHAR, summary_en VARCHAR, summary_eu VARCHAR, img VARCHAR);";

				/**
				 * Creates the table "festival_day" on the database.
				 */
				static final String FESTIVAL_DAY = "CREATE TABLE IF NOT EXISTS festival_day (id INT, date DATETIME, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, price INT);";

				/**
				 * Creates the table "festival_event" on the database.
				 */
				static final String FESTIVAL_EVENT = "CREATE TABLE IF NOT EXISTS festival_event (id INT, gm INT, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, description_es VARCHAR, description_en VARCHAR, description_eu VARCHAR, host INT, place INT, start DATETIME, end DATETIME);";

				/**
				 * Creates the table "festival_event_image" on the database.
				 */
				static final String FESTIVAL_EVENT_IMAGE = "CREATE TABLE IF NOT EXISTS festival_event_image (id INT, event INT, image VARCHAR, idx INT);";

				/**
				 * Creates the table "festival_offer" on the database.
				 */
				static final String FESTIVAL_OFFER = "CREATE TABLE IF NOT EXISTS festival_offer (id INT, year INT, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, description_es VARCHAR, description_en VARCHAR, description_eu VARCHAR, days INT, price INT);";

				/**
				 * Creates the table "location" on the database.
				 */
				static final String LOCATION = "CREATE TABLE IF NOT EXISTS location (id INT, dtime DATETIME, lat FLOAT, lon FLOAT, manual INT);";

				/**
				 * Creates the table "notification" on the database.
				 */
				static final String NOTIFICATION = "CREATE TABLE IF NOT EXISTS notification (id INT, dtime DATETIME, duration INT, action VARCHAR, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, internal INT, seen INT DEFAULT 0);";

				/**
				 * Creates the table "people" on the database.
				 */
				static final String PEOPLE = "CREATE TABLE IF NOT EXISTS people (id INT, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, link VARCHAR);";

				/**
				 * Creates the table "photo" on the database.
				 */
				static final String PHOTO = "CREATE TABLE IF NOT EXISTS photo (id INT, file VARCHAR, permalink VARCHAR, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, description_es VARCHAR, description_en VARCHAR, description_eu VARCHAR, dtime DATETIME, uploaded DATETIME, place INT, width INT, height INT, size INT, username VARCHAR);";

				/**
				 * Creates the table "photo_album" on the database.
				 */
				static final String PHOTO_ALBUM = "CREATE TABLE IF NOT EXISTS photo_album (photo INT, album INT);";

				/**
				 * Creates the table "photo_comment" on the database.
				 */
				static final String PHOTO_COMMENT = "CREATE TABLE IF NOT EXISTS photo_comment (id INT, photo INT, text VARCHAR, dtime DATETIME, username VARCHAR, lang VARCHAR);";

				/**
				 * Creates the table "place" on the database.
				 */
				static final String PLACE = "CREATE TABLE IF NOT EXISTS place (id INT, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, address_es VARCHAR, address_en VARCHAR, address_eu VARCHAR, cp VARCHAR, lat FLOAT, lon FLOAT);";

				/**
				 * Creates the table "post" on the database.
				 */
				static final String POST = "CREATE TABLE IF NOT EXISTS post (id INT, permalink VARCHAR, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, comments INT, username VARCHAR, dtime DATETIME);";

				/**
				 * Creates the table "post_comment" on the database.
				 */
				static final String POST_COMMENT = "CREATE TABLE IF NOT EXISTS post_comment (id INT, post INT, text VARCHAR, dtime DATETIME, username VARCHAR, lang VARCHAR);";

				/**
				 * Creates the table "photo" on the database.
				 */
				static final String POST_IMAGE = "CREATE TABLE IF NOT EXISTS post_image (id INT, post INT, image VARCHAR, idx INT);";

				/**
				 * Creates the table "post_tag" on the database.
				 */
				static final String POST_TAG = "CREATE TABLE IF NOT EXISTS post_tag (post INT, tag VARCHAR);";

				/**
				 * Creates the table "settings" on the database.
				 */
				static final String SETTINGS = "CREATE TABLE IF NOT EXISTS settings (name VARCHAR, value VARCHAR);";

				/**
				 * Creates the table "sponsor" on the database.
				 */
				static final String SPONSOR = "CREATE TABLE IF NOT EXISTS sponsor (id INT, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, image VARCHAR, address_es VARCHAR, address_en VARCHAR, address_eu VARCHAR, link VARCHAR, lat FLOAT, lon FLOAT);";

				/**
				 * Creates the table "version" on the database.
				 */
				static final String VERSION = "CREATE TABLE IF NOT EXISTS version (section VARCHAR, version INT);";
			}

			/**
			 * Queries to remove tables.
			 */
			final class DROP {

				/**
				 * Deletes the table "activity" from the database.
				 */
				static final String ACTIVITY = "DROP TABLE activity;";

				/**
				 * Deletes the table "activity_comment" from the database.
				 */
				static final String ACTIVITY_COMMENT = "DROP TABLE activity_comment;";

				/**
				 * Deletes the table "activity_comment" from the database.
				 */
				static final String ACTIVITY_IMAGE = "DROP TABLE activity_image;";

				/**
				 * Deletes the table "activity_comment" from the database.
				 */
				static final String ACTIVITY_ITINERARY = "DROP TABLE activity_itinerary;";

				/**
				 * Deletes the table "activity_comment" from the database.
				 */
				static final String ACTIVITY_TAG = "DROP TABLE activity_tag;";

				/**
				 * Deletes the table "album" from the database.
				 */
				static final String ALBUM = "DROP TABLE album;";

				/**
				 * Deletes the table "festival" from the database.
				 */
				static final String FESTIVAL = "DROP TABLE festival;";

				/**
				 * Deletes the table "festival_day" from the database.
				 */
				static final String FESTIVAL_DAY = "DROP TABLE festival_day;";

				/**
				 * Deletes the table "festival_event" from the database.
				 */
				static final String FESTIVAL_EVENT = "DROP TABLE festival_event;";

				/**
				 * Deletes the table "festival_event_image" from the database.
				 */
				static final String FESTIVAL_EVENT_IMAGE = "DROP TABLE festival_event_image;";

				/**
				 * Deletes the table "festival_offer" from the database.
				 */
				static final String FESTIVAL_OFFER = "DROP TABLE festival_offer;";

				/**
				 * Deletes the table "location" from the database.
				 */
				static final String LOCATION = "DROP TABLE location;";

				/**
				 * Deletes the table "notification" from the database.
				 */
				static final String NOTIFICATION = "DROP TABLE notification;";

				/**
				 * Deletes the table "people" from the database.
				 */
				static final String PEOPLE = "DROP TABLE people;";

				/**
				 * Deletes the table "photo" from the database.
				 */
				static final String PHOTO = "DROP TABLE photo;";

				/**
				 * Deletes the table "photo_album" from the database.
				 */
				static final String PHOTO_ALBUM = "DROP TABLE photo_album;";

				/**
				 * Deletes the table "photo_comment" from the database.
				 */
				static final String PHOTO_COMMENT = "DROP TABLE photo_comment;";

				/**
				 * Deletes the table "place" from the database.
				 */
				static final String PLACE = "DROP TABLE place;";

				/**
				 * Deletes the table "post" from the database.
				 */
				static final String POST = "DROP TABLE post;";

				/**
				 * Deletes the table "post_comment" from the database.
				 */
				static final String POST_COMMENT = "DROP TABLE post_comment;";

				/**
				 * Deletes the table "post_image" from the database.
				 */
				static final String POST_IMAGE = "DROP TABLE post_image;";

				/**
				 * Deletes the table "post_tag" from the database.
				 */
				static final String POST_TAG = "DROP TABLE post_tag;";

				/**
				 * Deletes the table "settings" from the database.
				 */
				static final String SETTINGS = "DROP TABLE settings;";

				/**
				 * Deletes the table "sponsor" from the database.
				 */
				static final String SPONSOR = "DROP TABLE sponsor;";

				/**
				 * Deletes the table "version" from the database.
				 */
				static final String VERSION = "DROP TABLE version;";
			}

			/**
			 * Queries to empty tables.
			 */
			final class EMPTY {
				static final String ACTIVITY = "DELETE FROM activity;";
				static final String ACTIVITY_COMMENT = "DELETE FROM activity_comment;";
				static final String ACTIVITY_IMAGE = "DELETE FROM activity_image;";
				static final String ACTIVITY_ITINERARY = "DELETE FROM activity_itinerary;";
				static final String ACTIVITY_TAG = "DELETE FROM activity_tag;";
				static final String ALBUM = "DELETE FROM album;";
				static final String FESTIVAL = "DELETE FROM festival;";
				static final String FESTIVAL_DAY = "DELETE FROM festival_day;";
				static final String FESTIVAL_EVENT = "DELETE FROM festival_event;";
				static final String FESTIVAL_EVENT_IMAGE = "DELETE FROM festival_event_image;";
				static final String FESTIVAL_OFFER = "DELETE FROM festival_offer;";
				static final String LOCATION = "DELETE FROM location;";
				static final String NOTIFICATION = "DELETE FROM notification;";
				static final String PEOPLE = "DELETE FROM people;";
				static final String PHOTO = "DELETE FROM photo;";
				static final String PHOTO_ALBUM = "DELETE FROM photo_album;";
				static final String PHOTO_COMMENT = "DELETE FROM photo_comment;";
				static final String PLACE = "DELETE FROM place;";
				static final String POST = "DELETE FROM post;";
				static final String POST_COMMENT = "DELETE FROM post_comment;";
				static final String POST_IMAGE = "DELETE FROM post_image;";
				static final String POST_TAG = "DELETE FROM post_tag;";
				static final String SETTINGS = "DELETE FROM settings;";
				static final String SPONSOR = "DELETE FROM sponsor;";
				static final String VERSION = "DELETE FROM version;";
			}

			/**
			 * Queries to remove and the recreate tables.
			 */
			final class RECREATE {
				static final String ACTIVITY = GM.DB.QUERY.DROP.ACTIVITY + GM.DB.QUERY.CREATE.ACTIVITY;
				static final String ACTIVITY_COMMENT = GM.DB.QUERY.DROP.ACTIVITY_COMMENT + GM.DB.QUERY.CREATE.ACTIVITY_COMMENT;
				static final String ACTIVITY_IMAGE = GM.DB.QUERY.DROP.ACTIVITY_IMAGE + GM.DB.QUERY.CREATE.ACTIVITY_IMAGE;
				static final String ACTIVITY_ITINERARY = GM.DB.QUERY.DROP.ACTIVITY_ITINERARY + GM.DB.QUERY.CREATE.ACTIVITY_ITINERARY;
				static final String ACTIVITY_TAG = GM.DB.QUERY.DROP.ACTIVITY_TAG + GM.DB.QUERY.CREATE.ACTIVITY_TAG;
				static final String ALBUM = GM.DB.QUERY.DROP.ALBUM + GM.DB.QUERY.CREATE.ALBUM;
				static final String FESTIVAL = GM.DB.QUERY.DROP.FESTIVAL + GM.DB.QUERY.CREATE.FESTIVAL;
				static final String FESTIVAL_DAY = GM.DB.QUERY.DROP.FESTIVAL_DAY + GM.DB.QUERY.CREATE.FESTIVAL_DAY;
				static final String FESTIVAL_EVENT = GM.DB.QUERY.DROP.FESTIVAL_EVENT + GM.DB.QUERY.CREATE.FESTIVAL_EVENT;
				static final String FESTIVAL_EVENT_IMAGE = GM.DB.QUERY.DROP.FESTIVAL_EVENT_IMAGE + GM.DB.QUERY.CREATE.FESTIVAL_EVENT_IMAGE;
				static final String FESTIVAL_OFFER = GM.DB.QUERY.DROP.FESTIVAL_OFFER + GM.DB.QUERY.CREATE.FESTIVAL_OFFER;
				static final String LOCATION = GM.DB.QUERY.DROP.LOCATION + GM.DB.QUERY.CREATE.LOCATION;
				static final String NOTIFICATION = GM.DB.QUERY.DROP.NOTIFICATION + GM.DB.QUERY.CREATE.NOTIFICATION;
				static final String PEOPLE = GM.DB.QUERY.DROP.PEOPLE + GM.DB.QUERY.CREATE.PEOPLE;
				static final String PHOTO = GM.DB.QUERY.DROP.PHOTO + GM.DB.QUERY.CREATE.PHOTO;
				static final String PHOTO_ALBUM = GM.DB.QUERY.DROP.PHOTO_ALBUM + GM.DB.QUERY.CREATE.PHOTO_ALBUM;
				static final String PHOTO_COMMENT = GM.DB.QUERY.DROP.PHOTO_COMMENT + GM.DB.QUERY.CREATE.PHOTO_COMMENT;
				static final String PLACE = GM.DB.QUERY.DROP.PLACE + GM.DB.QUERY.CREATE.PLACE;
				static final String POST = GM.DB.QUERY.DROP.POST + GM.DB.QUERY.CREATE.POST;
				static final String POST_COMMENT = GM.DB.QUERY.DROP.POST_COMMENT + GM.DB.QUERY.CREATE.POST_COMMENT;
				static final String POST_IMAGE = GM.DB.QUERY.DROP.POST_IMAGE + GM.DB.QUERY.CREATE.POST_IMAGE;
				static final String POST_TAG = GM.DB.QUERY.DROP.POST_TAG + GM.DB.QUERY.CREATE.POST_TAG;
				static final String SETTINGS = GM.DB.QUERY.DROP.SETTINGS + GM.DB.QUERY.CREATE.SETTINGS;
				static final String SPONSOR = GM.DB.QUERY.DROP.SPONSOR + GM.DB.QUERY.CREATE.SPONSOR;
				static final String VERSION = GM.DB.QUERY.DROP.VERSION + GM.DB.QUERY.CREATE.VERSION;
			}
		}
	}

	/**
	 * Keys and values of data that the app needs to store.
	 */
	static final class DATA {

		/**
		 * Name of the file where data will be stored.
		 */
		static final String DATA = "data";

		/**
		 * Keys of data that the app needs to store.
		 */
		static final class KEY {

			static final String USER = "user_id";
			static final String PREVIOUS_APP_VERSION = "previous_app_version";
			static final String LABLANCA = "lablanca";
		}

		/**
		 * Default values of data that the app needs to store.
		 */
		static final class DEFAULT {

			static final String USER = "";
			static final int PREVIOUS_APP_VERSION = 0;
			static final boolean LABLANCA = false;
		}
	}

	/**
	 * Keys and values of the app settings.
	 */
	static final class PREFERENCES {

		static final String PREFERNCES = "preferences";

		/**
		 * Keys of the app settings.
		 */
		static final class KEY {

			static final String SYNC = "performBackgroundSyncs";
			static final String NOTIFICATIONS = "recieveNotificacions";
		}

		/**
		 * Default values of the app settings.
		 */
		static final class DEFAULT {

			static final boolean SYNC = true;
			static final boolean NOTIFICATIONS = true;
		}
	}

	/**
	 * Used URLs.
	 */
	static class URL {
		static final String GITHUB = "https://github.com/GasteizkoMargolariak/GasteizkoMargolariakApp";
	}

	/**
	 * Info about the API.
	 */
	public static final class API {


		static final String CLIENT = "com.ivalentin.margolariak";

		static final String SERVER = "http://margolariak.com";

		static final String SYNC = "/API/v1/sync.php";

		static final String ACTION = "sync";
		static final String SECTION = "all";
		static final String FORMAT = "json";

		static final class KEY{

			static final String CLIENT = "client";
			static final String USER = "user";
			static final String ACTION = "action";
			static final String SECTION = "section";
			static final String VERSION = "version";
			static final String FOREGROUND = "foreground";
			static final String FORMAT = "format";
			static final String LANG = "lang";
		}




	}

	static final class PERMISSION {

		/**
		 * Code for the location permission request
		 */
		static final int LOCATION = 1;
	}


	static final class PERIOD_SYNC {

		/**
		 * Period of time (in seconds) at the end of witch the app will perform a sync during the festivals.
		 */
		static final int FESTIVALS = 20 * 60 * 1000;

		/**
		 * Period of time (in seconds) at the end of witch the app will perform a sync if no festivals.
		 */
		static final int NORMAL = 60 * 60 * 1000;
	}

	static final class SCHEDULE {

		static final String KEY = "schedule";

		/**
		 * Constant to diferentiate schedules".
		 */
		static final int CITY = 0;
		
		static final int MARGOLARIAK = 1;
	}
	
	static final class EXTRA {

		/**
		 * Constant to set extras on the main intent.
		 */
		static final String TITLE = "title";

		/**
		 * Constant to set extras on the main intent.
		 */
		static final String TEXT = "text";

		/**
		 * Constant to set extras on the main intent.
		 */
		static final String ACTION = "action";

		static final class SECTION {

			/**
			 * Value for {@see GM.EXTRA_ACTION} that opens the lablanca section.
			 */
			static final String LABLANCA = "lablanca";

			/**
			 * Value for {@see GM.EXTRA_ACTION} that opens the official schedule.
			 */
			static final String SCHEDULE = "cityschedule";

			/**
			 * Value for {@see GM.EXTRA_ACTION} that opens the GM schedule.
			 */
			static final String GMSCHEDULE = "gmschedule";

			/**
			 * Value for {@see GM.EXTRA_ACTION} that opens the location secton.
			 */
			static final String LOCATION = "location";

			/**
			 * Value for {@see GM.EXTRA_ACTION} that opens the blog.
			 */
			static final String BLOG = "blog";

			/**
			 * Value for {@see GM.EXTRA_ACTION} that opens the activities section.
			 */
			static final String ACTIVITIES = "activities";

			/**
			 * Value for {@see GM.EXTRA_ACTION} that opens the gallery section.
			 */
			static final String GALLERY = "gallery";
		}
		

	}

	public static final class SECTION {

		/**
		 *Bundle identifier to pass between fragments.
		 */
		static final String BUNDLE = "section";

		/**
		 * Constant for section "Home".
		 */
		static final byte HOME = 0;

		/**
		 * Constant for La Blanca section "Location".
		 */
		static final byte LOCATION = 1;

		/**
		 * Constant for section "La Blanca".
		 */
		static final byte LABLANCA = 2;

		/**
		 * Constant for La Blanca section "Festival schedule".
		 */
		static final byte SCHEDULE = 20;

		/**
		 * Constant for La Blanca section "Gasteizko Margolariak Schedule".
		 */
		static final byte GM_SCHEDULE = 21;

		/**
		 * Constant for section "Activities".
		 */
		static final byte ACTIVITIES = 3;

		/**
		 * Constant for section "Blog".
		 */
		static final byte BLOG = 4;

		/**
		 * Constant for section "Gallery".
		 */
		static final byte GALLERY = 5;
	}



	static class LOCATION {

		static class ACCURACY {
			/**
			 * The desired accuracy of the GPS coordinates, used to provide location updates, in meters.
			 */
			static final int SPACE = 10;

			/**
			 * The desired accuracy of the GPS coordinates, used to provide location updates, in milliseconds.
			 */
			static final int TIME = 10000;
		}

		static final int INTERVAL = 10000;
	}

	static final class IMG {

		/**
		 * Dimensions of the images in the database.
		 */
		static final int MINIATURE = 340;
		static final int PREVIEW = 600;
		static final int THUMB = 180;
		static final int VIEW = 800;
	}


	/**
	 *Programaticall loaded rows needs their margin to also be set programatically
	 */
	static final int ENTRY_MARGIN = 8;

	/**
	 * Gets the language code for sql queries.
	 * Only three values can be returned: es, eu, en.
	 * Defaults to es.
	 *
	 * @return Language two-letter code
	 */
	public static String getLang(){
		String currLang = Locale.getDefault().getISO3Language();
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

	public static Bitmap decodeSampledBitmapFromFile(String path, int size) { // BEST QUALITY MATCH

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		int inSampleSize = 1;

		if (height > size) {
			inSampleSize = Math.round((float)height / (float)size);
		}

		int expectedWidth = width / inSampleSize;

		if (expectedWidth > size) {
			//if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
			inSampleSize = Math.round((float)width / (float)size);
		}


		options.inSampleSize = inSampleSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(path, options);
	}
}
