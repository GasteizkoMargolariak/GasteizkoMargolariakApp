package com.ivalentin.margolariak;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.*;

/**
 * AsyncTask that synchronizes the online database to the device.
 * Is run every time the app is started, and periodically in the background.
 * 
 * @author Inigo Valentin
 *
 */
class Sync extends AsyncTask<Void, Void, Void> {
	
	private final Context myContextRef;
	private ProgressBar pbSync;
	private ImageView ivSync;
	private Dialog dialog;
	private MainActivity activity;
	private int fg;
	private String strings[];
	private boolean doProgress = false;
	private long millis = 0;
	private TextView tv;
	
	
	/**
	 * Things to do before sync. Namely, displaying a spinning progress bar.
	 *
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute(){

		//If pbSync and dialog are not null, it means that the sync is being done on the foreground
		if (pbSync != null) {

			//Show the spinning bar
			pbSync.setVisibility(View.VISIBLE);
			ivSync.setVisibility(View.GONE);
		}
		if (dialog != null) {

			//Show the dialog and assign the elements on it
			dialog.show();
			strings = new String[15];
			strings[0] = myContextRef.getString(R.string.dialog_sync_text_0);
			strings[1] = myContextRef.getString(R.string.dialog_sync_text_1);
			strings[2] = myContextRef.getString(R.string.dialog_sync_text_2);
			strings[3] = myContextRef.getString(R.string.dialog_sync_text_3);
			strings[4] = myContextRef.getString(R.string.dialog_sync_text_4);
			strings[5] = myContextRef.getString(R.string.dialog_sync_text_5);
			strings[6] = myContextRef.getString(R.string.dialog_sync_text_6);
			strings[7] = myContextRef.getString(R.string.dialog_sync_text_7);
			strings[8] = myContextRef.getString(R.string.dialog_sync_text_8);
			strings[9] = myContextRef.getString(R.string.dialog_sync_text_9);
			strings[10] = myContextRef.getString(R.string.dialog_sync_text_10);
			strings[11] = myContextRef.getString(R.string.dialog_sync_text_11);
			strings[12] = myContextRef.getString(R.string.dialog_sync_text_12);
			strings[13] = myContextRef.getString(R.string.dialog_sync_text_13);
			strings[14] = myContextRef.getString(R.string.dialog_sync_text_0); //In case I get a 13;
			tv = (TextView) dialog.findViewById(R.id.tv_dialog_sync_text);
			int idx = (int) (Math.random() * 13);
			tv.setText(strings[idx]);
			doProgress = true;

		}
		Log.d("SYNC", "Starting full sync");
	}
	
	/**
	 * Things to do after sync. Namely, hiding the spinning progress bar.
	 *
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPostExecute(Void v){

		//If pbSync and dialog are not null, it means that the sync is being done on the foreground
		if (pbSync != null) {

			//Hide the spinner
			pbSync.setVisibility(View.GONE);
			ivSync.setVisibility(View.VISIBLE);
		}
		if (dialog != null){

			//Close the dialog
			dialog.dismiss();

			//Check db version again
			SQLiteDatabase db = myContextRef.openOrCreateDatabase(GM.DB.NAME, Activity.MODE_PRIVATE, null);
			if (db.isReadOnly()){
				Log.e("SYNC", "Database is in read only mode. Skipping sync.");
				return;
			}

			//Get database version
			Cursor cursor;
			int dbVersion;
			cursor = db.rawQuery("SELECT sum(version) AS v FROM version;", null);
			cursor.moveToFirst();
			dbVersion = cursor.getInt(0);
			cursor.close();
			db.close();

			//If the database is on it's initial version (i.e: There is no data, new or old)
			if (dbVersion == GM.DB.INITIAL_VERSION){

				Log.e("SYNC", "Full sync failed");

				//Create a message dialog
				final Dialog dial = new Dialog(activity);
				dial.setCancelable(false);
				
				//Set up the window
				dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dial.setContentView(R.layout.dialog_sync_failed);
				
				//Set button
				Button btClose = (Button) dial.findViewById(R.id.bt_dialog_sync_failed_close);
				btClose.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
					dial.dismiss();
					activity.finish();
					}
				});

				//Set dialog parameters
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
				//noinspection ConstantConditions
				lp.copyFrom(dial.getWindow().getAttributes());
				lp.width = WindowManager.LayoutParams.MATCH_PARENT;
				lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
				lp.gravity = Gravity.CENTER;
				dial.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				dial.getWindow().setAttributes(lp);

				//Show the dialog
				dial.show();
			}
			else{
				Log.d("SYNC", "Full sync finished");
				activity.loadSection(GM.SECTION.HOME);
			}
		}

	}

	/**
	 * Called when the AsyncTask is created.
	 * This constructor is intended for syncs on the background.
	 *
	 * @param myContextRef The Context of the calling activity.
	 */
	public Sync(Activity myContextRef){
		this.myContextRef = myContextRef;
	}

    /**
     * Called when the AsyncTask is created.
	 * This constructor is intended for syncs done from the foreground,
	 * because it shows and hides a spinner (except for the initial one).
     * 
     * @param myContextRef The Context of the calling activity.
     * @param pb The progress bar that will be shown while the sync goes on.
     */
    public Sync(Activity myContextRef, ProgressBar pb, ImageView iv) {
        this.myContextRef = myContextRef;
        dialog = null;
        pbSync = pb;
		ivSync = iv;
        fg = 1;
    }
    
    /**
     * Called when the AsyncTask is created. 
     * This constructor is intended to use only in the first sync,
     * from the foreground, because a dialog will block the UI.
     * 
     * @param myContextRef The Context of the calling activity.
     * @param d Dialog of the initial sync
     * @param pb The progress bar that will be shown while the sync goes on.
     * @param activity The calling MainActvity
     */
    public Sync(Activity myContextRef, ProgressBar pb, ImageView iv, Dialog d, MainActivity activity) {
    	this.dialog = d;
    	this.activity = activity;
        this.myContextRef = myContextRef;
        pbSync = pb;
		ivSync = iv;
        fg = 1;
    }
    
    /**
     * Called when the AsyncTask is created.
     * 
     * @param context The Context of the calling activity.
     */
    public Sync(Context context) {
        this.myContextRef = context;
        pbSync = null;
        fg = 0;
    }

	@Override
	/*
	  Called when the AsyncTask is updated.
	  Used to change text in the sync window.
	  @param progress Unused.
	 */
	protected void onProgressUpdate(Void...progress) {
		if (doProgress){
			if (millis + 900 < System.currentTimeMillis()) {
				millis = System.currentTimeMillis();
				int idx = (int) (Math.random() * 13);
				tv.setText(strings[idx]);
			}
		}
	}

	/**
	 * Creates the URL required to performa a sync. Uses static data and data passed as arguments.
	 *
	 * @param user A unique user identifier.
	 * @param foreground 1 if the sync is done while the app is running, 0 otherwise.
	 *
	 * @return The URL that will be used for syncing.
	 */
	private String buildUrl(SQLiteDatabase db, String user, int foreground){
		String url = "";
		try {

			// Common parameters
			url = GM.API.SERVER;
			if (pbSync != null) { // First sync
				url = url + GM.API.SYNC.PATH.FAST;
			}
			else{ // Regular sync
				url = url + GM.API.SYNC.PATH.COMPLETE;
			}
			url = url + "?" +
					GM.API.SYNC.KEY.CLIENT + "=" + URLEncoder.encode(GM.API.CLIENT, "UTF-8") + "&" +
					GM.API.SYNC.KEY.USER + "=" + URLEncoder.encode(user, "UTF-8") + "&" +
					GM.API.SYNC.KEY.FOREGROUND + "=" + foreground + "&";

			// Versions of the tables
			Cursor cursor;

			// Check if the table "versions" exists.
			cursor = db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '" + GM.DB.TABLE.VERSION + "'", null);
			if(cursor.getCount() > 0) {
				cursor = db.rawQuery("SELECT section, version FROM version;", null);

				// Get table list
				String tables[] = {GM.DB.TABLE.ACTIVITY,             GM.DB.TABLE.ACTIVITY_COMMENT,    GM.DB.TABLE.ACTIVITY_IMAGE,
				                   GM.DB.TABLE.ACTIVITY_ITINERARY,   GM.DB.TABLE.ACTIVITY_TAG,        GM.DB.TABLE.ALBUM,
				                   GM.DB.TABLE.ALBUM,                GM.DB.TABLE.FESTIVAL,            GM.DB.TABLE.FESTIVAL_DAY,
				                   GM.DB.TABLE.FESTIVAL_DAY,         GM.DB.TABLE.FESTIVAL_EVENT_CITY, GM.DB.TABLE.FESTIVAL_EVENT_GM,
				                   GM.DB.TABLE.FESTIVAL_EVENT_IMAGE, GM.DB.TABLE.FESTIVAL_OFFER,      GM.DB.TABLE.PEOPLE,
				                   GM.DB.TABLE.PHOTO,                GM.DB.TABLE.PHOTO_ALBUM,         GM.DB.TABLE.PHOTO_COMMENT,
				                   GM.DB.TABLE.PLACE,                GM.DB.TABLE.POST,                GM.DB.TABLE.POST_COMMENT,
				                   GM.DB.TABLE.POST_IMAGE,           GM.DB.TABLE.POST_TAG,            GM.DB.TABLE.ROUTE,
				                   GM.DB.TABLE.ROUTE_POINT,          GM.DB.TABLE.SETTINGS,            GM.DB.TABLE.SPONSOR};
				List<String> tableList = Arrays.asList(tables);
				while (cursor.moveToNext()) {
					if(tableList.contains(cursor.getString(0))){
						url = url + "&" + cursor.getString(0) + "=" + cursor.getString(1);
					}
				}
			}
			cursor.close();
		}
		catch (java.io.UnsupportedEncodingException ex){
			Log.e("UTF-8", "Error encoding url for sync \"" + url + "\" - " + ex.toString());
		}
		return url;
	}

	/**
	 * Recreates the database, dropping and then creating all the tables containing
	 * data. "location" and "notification" tables are untouched.
	 *
	 * @param db A database connection.
	 *
	 * @return False if there were errors, true otherwise.
	 */
	@SuppressWarnings("unused")
	private boolean recreateDb(SQLiteDatabase db) {
		boolean result = true;
		try {
			db.beginTransactionNonExclusive();
			db.execSQL(GM.DB.QUERY.DROP.ACTIVITY);
			db.execSQL(GM.DB.QUERY.DROP.ACTIVITY_COMMENT);
			db.execSQL(GM.DB.QUERY.DROP.ACTIVITY_IMAGE);
			db.execSQL(GM.DB.QUERY.DROP.ACTIVITY_ITINERARY);
			db.execSQL(GM.DB.QUERY.DROP.ACTIVITY_TAG);
			db.execSQL(GM.DB.QUERY.DROP.ALBUM);
			db.execSQL(GM.DB.QUERY.DROP.FESTIVAL);
			db.execSQL(GM.DB.QUERY.DROP.FESTIVAL_DAY);
			db.execSQL(GM.DB.QUERY.DROP.FESTIVAL_EVENT_CITY);
			db.execSQL(GM.DB.QUERY.DROP.FESTIVAL_EVENT_GM);
			db.execSQL(GM.DB.QUERY.DROP.FESTIVAL_EVENT_IMAGE);
			db.execSQL(GM.DB.QUERY.DROP.FESTIVAL_OFFER);
			db.execSQL(GM.DB.QUERY.DROP.PEOPLE);
			db.execSQL(GM.DB.QUERY.DROP.PHOTO);
			db.execSQL(GM.DB.QUERY.DROP.PHOTO_ALBUM);
			db.execSQL(GM.DB.QUERY.DROP.PHOTO_COMMENT);
			db.execSQL(GM.DB.QUERY.DROP.PLACE);
			db.execSQL(GM.DB.QUERY.DROP.POST);
			db.execSQL(GM.DB.QUERY.DROP.POST_COMMENT);
			db.execSQL(GM.DB.QUERY.DROP.POST_IMAGE);
			db.execSQL(GM.DB.QUERY.DROP.POST_TAG);
			db.execSQL(GM.DB.QUERY.DROP.ROUTE);
			db.execSQL(GM.DB.QUERY.DROP.ROUTE_POINT);
			db.execSQL(GM.DB.QUERY.DROP.SETTINGS);
			db.execSQL(GM.DB.QUERY.DROP.SPONSOR);
			db.execSQL(GM.DB.QUERY.DROP.VERSION);
			db.setTransactionSuccessful();
			db.endTransaction();
			publishProgress();
			db.beginTransactionNonExclusive();
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY_COMMENT);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY_IMAGE);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY_ITINERARY);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY_TAG);
			db.execSQL(GM.DB.QUERY.CREATE.ALBUM);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL_DAY);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL_EVENT_CITY);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL_EVENT_GM);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL_EVENT_IMAGE);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL_OFFER);
			db.execSQL(GM.DB.QUERY.CREATE.PEOPLE);
			db.execSQL(GM.DB.QUERY.CREATE.PHOTO);
			db.execSQL(GM.DB.QUERY.CREATE.PHOTO_ALBUM);
			db.execSQL(GM.DB.QUERY.CREATE.PHOTO_COMMENT);
			db.execSQL(GM.DB.QUERY.CREATE.PLACE);
			db.execSQL(GM.DB.QUERY.CREATE.POST);
			db.execSQL(GM.DB.QUERY.CREATE.POST_COMMENT);
			db.execSQL(GM.DB.QUERY.CREATE.POST_IMAGE);
			db.execSQL(GM.DB.QUERY.CREATE.POST_TAG);
			db.execSQL(GM.DB.QUERY.CREATE.ROUTE);
			db.execSQL(GM.DB.QUERY.CREATE.ROUTE_POINT);
			db.execSQL(GM.DB.QUERY.CREATE.SETTINGS);
			db.execSQL(GM.DB.QUERY.CREATE.SPONSOR);
			db.execSQL(GM.DB.QUERY.CREATE.VERSION);
			db.setTransactionSuccessful();
			db.endTransaction();
		}
		catch (Exception ex) {
			result = false;
			Log.e("SYNC", "Error recreating the database: " + ex.toString());
		}
		return result;
	}

	/**
	 * Finds out the type of a column of a table in the database.
	 *
	 * @param db A database connection.
	 * @param table The name of the table.
	 * @param idx Index of the column in the database (starting on 0).
	 *
	 * @return GM.DB.COLUMN.INT, GM.DB.COLUMN.DATETIME, GM.DB.COLUMN.VARCHAR (default).
	 */
	private int getColumnType(SQLiteDatabase db, String table, int idx) {
		String type = "";
		Cursor typeCursor;
		try {
			String Query = "PRAGMA table_info(" + table + ")";
			typeCursor  = db.rawQuery(Query, null);
			typeCursor.moveToPosition(idx);
			type = typeCursor.getString(2);
			typeCursor.close();
		}
		catch(Exception ex){
			Log.e("SYNC", "Unable to reliably determine the type of the column '" + idx + "' of the table " + table + ": " + ex.toString());
		}
		if (type.equals("INT")) {
			return GM.DB.COLUMN.INT;
		}
		if (type.equals("DATETIME")) {
			return GM.DB.COLUMN.DATETIME;
		}
		return GM.DB.COLUMN.VARCHAR;
	}

	/**
	 * Stores version data for each section in the database.
	 * Uses a custom JSON parser.
	 *
	 * @param db Database store the data.
	 * @param data JSON formated string with the information for the database.
	 *
	 * @return False if there were errors, true otherwise.
	 */
	private boolean saveData(SQLiteDatabase db, String data){

		String str = data;
		String name;
		String content;
		boolean result = true;
		try{
			while (str.indexOf("}") > 0){
				try {
					name = str.substring(str.indexOf("\"") + 1, str.indexOf(":") -1);
					content = str.substring(str.indexOf("[{"), str.indexOf("}]") + 2);

					if (!saveTable(db, name, content)){
						//Finish the loop if there is an error
						return false;
					}

					str = str.substring(str.indexOf("}]") + 2);
				}
				catch (Exception ex){
					//End of string
					Log.d("SYNC", "End of data string:" + ex.toString());
					str = "";
				}
				publishProgress();
			}
		}
		catch (Exception ex){
			Log.e("SYNC", "Error saving the remote db data: " + ex.toString());
			result = false;
		}

		return result;
	}

	/**
	 * When the table "settings cames up, dont make a table, but store required
	 * values as data.
	 *
	 * @param settings JSON string with the contents of the "settings" table.
	 * @return true if values could be saved, false otherwise.
	 */
	private boolean saveSettings(String settings){

		SharedPreferences sharedData = myContextRef.getSharedPreferences(GM.DATA.DATA, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedData.edit();
		try{
			String value;
			if (settings.contains("\"name\":\"comments\",\"value\":\"")){
				value = settings.substring(settings.indexOf("\"name\":\"comments\",\"value\":\"") + 27, settings.indexOf("\"name\":\"comments\",\"value\":\"") + 28);
				if ("0".equals(value) || "1".equals(value)){
					Log.d("SYNC", "Setting found: comments = " + value);
					if ("0".equals(value)) {
						editor.putBoolean(GM.DATA.KEY.COMMENTS, false);
					}
					else {
						editor.putBoolean(GM.DATA.KEY.COMMENTS, true);
					}
				}
			}
			if (settings.contains("\"name\":\"festivals\",\"value\":\"")){
				value = settings.substring(settings.indexOf("\"name\":\"festivals\",\"value\":\"") + 28, settings.indexOf("\"name\":\"festivals\",\"value\":\"") + 29);
				if ("0".equals(value) || "1".equals(value)){
					Log.d("SYNC", "Setting found: lablanca = " + value);
					if ("0".equals(value)) {
						editor.putBoolean(GM.DATA.KEY.LABLANCA, false);
					}
					else {
						editor.putBoolean(GM.DATA.KEY.LABLANCA, true);
					}
				}
			}
			if (settings.contains("\"name\":\"photos\",\"value\":\"")){
				value = settings.substring(settings.indexOf("\"name\":\"photos\",\"value\":\"") + 25, settings.indexOf("\"name\":\"photos\",\"value\":\"") + 26);
				if ("0".equals(value) || "1".equals(value)){
					Log.d("SYNC" ,"Setting found: photos = " + value);
					if ("0".equals(value)) {
						editor.putBoolean(GM.DATA.KEY.PHOTOS, false);
					}
					else {
						editor.putBoolean(GM.DATA.KEY.PHOTOS, true);
					}
				}
			}

			editor.apply();
			publishProgress();
			Log.d("SYNC", "Settings saved.");
			return true;
		}
		catch (Exception ex){
			editor.apply();
			Log.e("SYNC", "Unable to save settings: " + ex.toString());
			return false;
		}
	}


	/**
	 * Stores the new contents of the table 'version' into the database.
	 * Uses a custom JSON parser.
	 *
	 * @param db Database to store the data.
	 * @param data String in JSON format with the data of the table.
	 * @return False if there were errors, true otherwise.
	 */
	private boolean saveTableVersion(SQLiteDatabase db, String data) {

		JSONObject jsonObj;
		String section, row, version;
		String str = data;
		Cursor cursor;
		List<String> queries = new ArrayList<>();

		//Process each row
		while (str.contains("{\"") && str.contains("\"}")) {
			row = str.substring(str.indexOf("{\""), str.indexOf("\"}") + 2);
			try {

				section = row.substring(row.indexOf("\"section\"") + 11, row.indexOf(",") + -1);
				version = row.substring(row.indexOf("\"version\"") + 11, row.lastIndexOf("\""));
				// Check if the value already exists in the database.
				cursor = db.rawQuery("SELECT section FROM version WHERE version = '" + section + "';", null);
				if (cursor.getCount() == 0) {
					queries.add("INSERT INTO version (section, version) VALUES ('" + section + "', " + version + ");");
				} else {
					queries.add("UPDATE version SET version = " + version + " WHERE section  ='" + section + "';");
				}
				cursor.close();
			}
			catch (Exception ex) {
				Log.e("SYNC", "Error parsing special table 'version': " + ex.toString());
				return false;
			}

			//Process str for the next loop pass.
			str = str.substring(str.indexOf("\"}") + 2);

			publishProgress();
		}

		//If I get to this point, there were no errors, and I can safely execute the queries
		try {
			int totalQueries = queries.size();
			for (int i = 0; i < totalQueries; i++) {
				db.execSQL(queries.get(i));
			}
		} catch (Exception ex) {
			Log.e("SYNC", "Error inserting data from special table 'version' into the local db: " + ex.toString());

			//I don't put a 'return false;' here because I dont want to loose the whole table for just one row.
		}

		return true;
	}


	/**
	 * Stores a table data into the database.
	 * Uses a custom JSON parser.
	 *
	 * @param db Database to store the data.
	 * @param table Name of the table.
	 * @param data String in JSON format with the data of the table.
	 * @return False if there were errors, true otherwise.
	 */
	private boolean saveTable(SQLiteDatabase db, String table, String data) {

		//If table, do something else
		if (GM.DB.TABLE.SETTINGS.equals(table)){
			Log.d("SYNC", "Got the settings table. Special treatment...");
			return saveSettings(data);
		}

		if (GM.DB.TABLE.VERSION.equals(table)){
			Log.d("SYNC", "Got the settings table. Special treatment...");
			return saveTableVersion(db, data);
		}

		int type, i;
		JSONObject jsonObj;
		String[] values = new String[99];
		String[] columns = new String[99];
		String val, row;
		String str = data;
		List<String> queries = new ArrayList<>();

		//The first query has to delete all entries
		queries.add("DELETE FROM " + table + ";");

		//Process each row
		str = str.replace(":null", ":\"null\"");

		while (str.contains("{\"") && (str.contains("\"}") || str.contains("null}"))) {
			if (str.contains("\"}")) {
				row = str.substring(str.indexOf("{\""), str.indexOf("\"}") + 2);
			}
			else{
				row = str.substring(str.indexOf("{\""), str.indexOf("null}") + 5);
			}

			i = 0;

			try {

				//Create a json object with the collumn names and values of the row...
				jsonObj = new JSONObject(row);
				Iterator<?> keys = jsonObj.keys();

				//... and loop through it.
				while (keys.hasNext()) {
					String key = (String) keys.next();
					String value = jsonObj.get(key).toString();
					columns[i] = key;
					values[i] = value;
					i++;
				}

				//Start building the query
				String q = "INSERT INTO " + table + "(";

				//Add column names
				for (int j = 0; j < i; j++) {
					q = q + columns[j] + ", ";
				}
				q = q.substring(0, q.length() - 2) + ") VALUES (";

				//Loop trough values
				for (int j = 0; j < i; j++) {

					//If the value is empty or null, I don't care what type is it, it will be 'null'.
					if (values[j].length() == 0 || "null".equalsIgnoreCase(values[j])) {
						val = "null";
						q = q + val + ", ";
					}

					//If the value is not empty, I need the type
					else {
						type = getColumnType(db, table, j);

						switch (type) {
							case GM.DB.COLUMN.INT:

								//Check that is really a number to prevent injection.
								if (Pattern.matches("-?\\d+", values[j]))
									q = q + values[j] + ", ";
								else{
									//If its not and actually number, just insert null to avoid problems.
									q = q + "null, ";
								}
								break;

							case GM.DB.COLUMN.DATETIME:
								//The API formats ('YYYY-MM-DD' or 'YYYY-MM-DD HH:MM:SS') are good for inserting. Just escape and add quotes.
								q = q + DatabaseUtils.sqlEscapeString(values[j]) + ", ";
								break;

							default: //VARCHAR
								//Just escape and add quotes.
								q = q + DatabaseUtils.sqlEscapeString(values[j]) + ", ";
						}
					}

				}

				//End the query string and add it to the array.
				q = q.substring(0, q.length() - 2) + ")";
				queries.add(q);

			}
			catch (JSONException e) {
				Log.e("SYNC", "Error parsing table '" + table + "' (JSONException): " + e.toString());
				return false;
			}
			catch (Exception ex) {
				Log.e("SYNC", "Error parsing table '" + table + "': " + ex.toString());
				return false;
			}

			//Process str for the next loop pass.
			str = str.substring(str.indexOf("\"}") + 2);

			publishProgress();
		}

		//If I get to this point, there were no errors, and I can safely execute the queries
		try {
			int totalQueries = queries.size();
			for (i = 0; i < totalQueries; i++) {
				db.execSQL(queries.get(i));
			}
		} catch (Exception ex) {
			Log.e("SYNC", "Error inserting data from remote table " + table + " into the local db: " + ex.toString());

			//I don't put a 'return false;' here because I dont want to loose the whole table for just one row.
		}

		return true;
	}

	/**
	 * The sweet stuff. Actually performs the sync. 
	 */
	@Override
	protected Void doInBackground(Void... params) {

		//Get preferences
		SharedPreferences preferences = myContextRef.getSharedPreferences(GM.DATA.DATA, Context.MODE_PRIVATE);

		//Get useful data for the uri
		String userCode = preferences.getString(GM.DATA.KEY.USER, GM.DATA.DEFAULT.USER);

		//Get database. Stop if it's locked
		SQLiteDatabase db;
		try {
			db = myContextRef.openOrCreateDatabase(GM.DB.NAME, Activity.MODE_PRIVATE, null);
			if (db.isReadOnly()) {
				Log.e("SYNC", "Database is in read only mode. Skipping sync.");
				return null;
			}
		}
		catch(SQLiteDatabaseLockedException ex){
			Log.e("SYNC", "Database is locked . Skipping sync: " + ex.toString());
			return null;
		}

		publishProgress();

		URL url;
		String uri = buildUrl(db, userCode, fg);
		try {
			url = new URL(uri);

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

			int httpCode = urlConnection.getResponseCode();
			switch (httpCode){
				case 400:	//Client error: Bad request
					Log.e("SYNC", "The server returned a 400 code (Client Error: Bad request) for the url \"" + uri + "\"");
					break;
				case 403:	//Client error: Forbidden
					Log.e("SYNC", "The server returned a 403 code (Client Error: Forbidden) for the url \"" + uri + "\"");
					break;
				case 204:	//Success: No content
					Log.d("SYNC", "The server returned a 204 code (Success: No content) for the url \"" + uri + "\". Stopping sync process...");
					break;
				case 200:	//Success: OK
					Log.d("SYNC", "The server returned a 200 code (Success: OK) for the url \"" + uri + "\". Now syncing...");
					BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String o;
					while ((o = br.readLine()) != null)
						sb.append(o);

					publishProgress();

					//Get the string with the sync json. (The whole page)
					String strSync = sb.toString();
					publishProgress();

					//If the data is correctly parsed and stored, commit changes to the database.
					try {
						db.beginTransactionNonExclusive();


						if (saveData(db, strSync)) {
							db.setTransactionSuccessful();
							Log.d("SYNC", "The sync process finished correctly. Changes to the database will be commited");
						} else {
							Log.e("SYNC", "The sync process did not finish correctly. Any changes made to the database will be reverted");
						}
						db.endTransaction();
					}
					catch(SQLiteDatabaseLockedException ex){
						Log.e("SYNC", "Database is locked . Ending sync now: " + ex.toString());
					}

					break;
				default:
					Log.e("SYNC", "The server returned an unexpected code (" + httpCode + ") for the url \"" + uri + "\"");
			}

			publishProgress();
			urlConnection.disconnect();
			return null;

		}
		catch (MalformedURLException e) {
			Log.e("SYNC", "Malformed URL (" + uri + "): " + e.toString());
			e.printStackTrace();
		}
		catch (IOException e) {
			Log.e("SYNC", "IOException for URL (" + uri + "): " + e.toString());
			e.printStackTrace();
		}

		db.close();
		return null;
    	
	}
}
