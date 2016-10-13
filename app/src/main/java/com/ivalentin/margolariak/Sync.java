package com.ivalentin.margolariak;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
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
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute(){
		if (pbSync != null) {
			pbSync.setVisibility(View.VISIBLE);
			ivSync.setVisibility(View.GONE);
		}
		if (dialog != null) {
			dialog.show();
			strings = new String[10];
			strings[0] = myContextRef.getString(R.string.dialog_sync_text_0);
			strings[1] = myContextRef.getString(R.string.dialog_sync_text_1);
			strings[2] = myContextRef.getString(R.string.dialog_sync_text_2);
			strings[3] = myContextRef.getString(R.string.dialog_sync_text_3);
			strings[4] = myContextRef.getString(R.string.dialog_sync_text_4);
			strings[5] = myContextRef.getString(R.string.dialog_sync_text_5);
			strings[6] = myContextRef.getString(R.string.dialog_sync_text_6);
			strings[7] = myContextRef.getString(R.string.dialog_sync_text_7);
			strings[8] = myContextRef.getString(R.string.dialog_sync_text_8);
			strings[9] = myContextRef.getString(R.string.dialog_sync_text_8); //In case I get a 9;
			tv = (TextView) dialog.findViewById(R.id.tv_dialog_sync_text);
			int idx = (int) (Math.random() * 9);
			tv.setText(strings[idx]);
			doProgress = true;
		}
		Log.d("Sync", "Starting full sync");
	}
	
	/**
	 * Things to do after sync. Namely, hiddng the spinning progress bar.
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPostExecute(Void v){
		if (pbSync != null) {
			pbSync.setVisibility(View.GONE);
			ivSync.setVisibility(View.VISIBLE);
		}
		if (dialog != null){
			dialog.dismiss();
			//Check db version agan
			//SharedPreferences preferences = myContextRef.getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);

			SQLiteDatabase db = myContextRef.openOrCreateDatabase(GM.DB_NAME, Activity.MODE_PRIVATE, null);
			if (db.isReadOnly()){
				Log.e("Db ro", "Database is locked and in read only mode. Skipping sync.");
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

			if (dbVersion == GM.DEFAULT_PREF_DB_VERSION){
				//Create a dialog
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
				
				//Show the dialog
				dial.show();
			}
			else{
				activity.loadSection(GM.SECTION_HOME);
			}
		}
		Log.d("Sync", "Full sync finished");
	}

	/**
	 * Called when the AsyncTask is created.
	 *
	 * @param myContextRef The Context of the calling activity.
	 */
	public Sync(Activity myContextRef){
		this.myContextRef = myContextRef;
	}

    /**
     * Called when the AsyncTask is created.
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
     * because a dialog will block the UI.
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
	/**
	 * Called when the AsyncTask is updated.
	 * Used to change text in the sync window.
	 */
	protected void onProgressUpdate(Void...progress) {
		if (doProgress){
			if (millis + 600 < System.currentTimeMillis()) {
				millis = System.currentTimeMillis();
				int idx = (int) (Math.random() * ((4) + 1));
				tv.setText(strings[idx]);
			}
		}
	}

	/**
	 * Creates the URL required to performa a sync. Uses static data and data passed as arguments.
	 *
	 * @param user A unique user identifier.
	 * @param version The db version of the client.
	 * @param foreground 1 if the sync is done while the app is running, 0 otherwise.
	 * @param lang Two letter language identifier.
	 * @return The URL that will be used for syncing.
	 */
	private String buildUrl(String user, int version, int foreground, String lang){
		String url = "";
		try {
			url = GM.SERVER + GM.SERVER_SYNC + "?" +
					GM.SERVER_SYNC_KEY_CLIENT + "=" + URLEncoder.encode(GM.CLIENT, "UTF-8") + "&" +
					GM.SERVER_SYNC_KEY_USER + "=" + URLEncoder.encode(user, "UTF-8") + "&" +
					GM.SERVER_SYNC_KEY_ACTION + "=" + URLEncoder.encode(GM.SERVER_SYNC_VALUE_ACTION, "UTF-8") + "&" +
					GM.SERVER_SYNC_KEY_SECTION + "=" + URLEncoder.encode(GM.SERVER_SYNC_VALUE_SECTION, "UTF-8") + "&" +
					GM.SERVER_SYNC_KEY_VERSION + "=" + version + "&" +
					GM.SERVER_SYNC_KEY_FOREGROUND + "=" + foreground + "&" +
					GM.SERVER_SYNC_KEY_FORMAT + "=" + URLEncoder.encode(GM.SERVER_SYNC_VALUE_FORMAT, "UTF-8") + "&" +
					GM.SERVER_SYNC_KEY_LANG + "=" + URLEncoder.encode(lang, "UTF-8");
		}
		catch (java.io.UnsupportedEncodingException ex){
			Log.e("UTF-8", "Error encoding url for sync \"" + url + "\" - " + ex.toString());
		}
		return url;
	}

	private boolean recreateDb(SQLiteDatabase db) {
		boolean result = true;
		try {
			//Log.e("RECREATE", "START DROP");
			db.execSQL(GM.DB.QUERY.DROP.ACTIVITY);
			db.execSQL(GM.DB.QUERY.DROP.ACTIVITY_COMMENT);
			db.execSQL(GM.DB.QUERY.DROP.ACTIVITY_IMAGE);
			db.execSQL(GM.DB.QUERY.DROP.ACTIVITY_ITINERARY);
			db.execSQL(GM.DB.QUERY.DROP.ACTIVITY_TAG);
			db.execSQL(GM.DB.QUERY.DROP.ALBUM);
			db.execSQL(GM.DB.QUERY.DROP.FESTIVAL);
			db.execSQL(GM.DB.QUERY.DROP.FESTIVAL_DAY);
			db.execSQL(GM.DB.QUERY.DROP.FESTIVAL_EVENT);
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
			db.execSQL(GM.DB.QUERY.DROP.SETTINGS);
			db.execSQL(GM.DB.QUERY.DROP.SPONSOR);
			db.execSQL(GM.DB.QUERY.DROP.VERSION);
			//Log.e("RECREATE", "FINISH DROP");
			Thread.sleep(1000);
			//Log.e("RECREATE", "START CREATE");
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY_COMMENT);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY_IMAGE);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY_ITINERARY);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY_TAG);
			db.execSQL(GM.DB.QUERY.CREATE.ALBUM);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL_DAY);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL_EVENT);
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
			db.execSQL(GM.DB.QUERY.CREATE.SETTINGS);
			db.execSQL(GM.DB.QUERY.CREATE.SPONSOR);
			db.execSQL(GM.DB.QUERY.CREATE.VERSION);
			//Log.e("RECREATE", "STOP CREATE");
			Thread.sleep(1000);
			//Log.e("RECREATE", "CONTINUE");
		} catch (InterruptedException e) {
			Log.e("SYNC", "Couldn't wait for the database to be recreated: " + e.toString());
		}
		catch (Exception ex) {
			result = false;
			Log.e("SYNC", "Error recreating the database: " + ex.toString());
		}
		return result;
	}

	private final int getColumnType(SQLiteDatabase db, String table, int idx) {
		String type = "";
		Cursor typeCursor;
		try {
			//typeCursor = db.rawQuery("SELECT typeof (" + column + ") FROM " + table + ";", null);
			//typeCursor.moveToFirst();
			//type = typeCursor.getString(0);
			String Query = "PRAGMA table_info(" + table + ")";
			Cursor my_cursor  = db.rawQuery(Query, null);
			my_cursor.moveToPosition(idx);
			//Column_name = my_cursor.getString(my_cursor.getColumnIndex("name"));
			type = my_cursor.getString(2);
			my_cursor.close();
		}
		catch(Exception ex){
			Log.e("SYNC", "Unable to reliably determine the type of the column '" + idx + "' of the table " + table + ": " + ex.toString());
		}
		//Log.e("Returned type", "#" + type);
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
	 * @param versions List of strings with data about the versions.
	 * @return False if there were errors, true otherwise.
	 */
	private boolean saveVersions(SQLiteDatabase db, String versions){
		String str = versions;
		String key;
		boolean result = true;
		int value;
		int totalVersions = 0;

		//If I ever have a database with more than 99 public sections (not tables), I'll have to change this. Also, ask for a raise.
		int[] values = new int[99];
		String[] keys = new String[99];
		try {
			while (str.indexOf("{") > 0){
				key = str.substring(str.indexOf("{\"") + 2, str.indexOf("\":"));
				str = str.substring(str.indexOf("\":\"") + 3);
				value = Integer.parseInt(str.substring(0, str.indexOf("\"")));
				str = str.substring(str.indexOf("}") + 1);
				keys[totalVersions] = key;
				values[totalVersions] = value;
				totalVersions ++;
			}
			if (totalVersions > 0 && totalVersions < 99){
				db.execSQL("CREATE TABLE IF NOT EXISTS version (section VARCHAR, version INT);");
				db.execSQL("DELETE FROM version;");
				for (int i = 0; i < totalVersions; i ++){
					db.execSQL("INSERT INTO version VALUES ('" + keys[i] + "', " + values[i] + ");");
				}
			}
			else{
				result = false;
			}

		}
		catch (Exception ex){
			result = false;
			Log.e("SYNC", "Error saving the remote db versions: " + ex.toString());
		}
		return result;
	}

	/**
	 * Stores version data for each section in the database.
	 * Uses a custom JSON parser.
	 *http://forum.xda-developers.com/apps/supersu/suhide-t3450396
	 * @param db Database store the data.
	 * @param data List of strings in json format with the tables.
	 * @return False if there were errors, true otherwise.
	 */
	private boolean saveData(SQLiteDatabase db, String data){
		String str;
		String key;
		String value;
		boolean result = true;
		try{
			str = data.substring(data.indexOf("[") + 1, data.lastIndexOf("]"));
			while (str.indexOf("}") > 0){
				try {
					Log.e("R", "1");
					Log.e("PostR1", str);
					key = str.substring(str.indexOf("\"") + 1, str.indexOf("\"", str.indexOf(":") - 1));
					Log.e("R", "2");
					value = str.substring(str.indexOf("[") + 1, str.indexOf("]"));
					Log.e("KEY/VALUE", key + "/" + value.substring(0, 30) + "..." + value.substring(3 * value.length() / 4));
					Log.e("R", "3");
					str = str.substring(str.indexOf("]") + 1);
					Log.e("R", "4");

					if (!saveTable(db, key, value)){
						//Finish the loop if there is an error
						return false;
					}
					Log.e("R", "5");

					str = str.substring(str.indexOf("}") + 1);
					Log.e("R", "6");
				}
				catch (Exception ex){
					//End of string
					Log.d("SYNC", "End of string:" + ex.toString());
					str = "";
				}
			}
		}
		catch (Exception ex){
			Log.e("SYNC", "Error saving the remote db data: " + ex.toString());
			result = false;
		}

		return result;
	}

	/**
	 * Stores a table data into the database.
	 * Uses a custom JSON parser.
	 *http://forum.xda-developers.com/apps/supersu/suhide-t3450396
	 * @param db Database to store the data.
	 * @param table Name of the table.
	 * @param data String in JSON format with the data of the table.
	 * @return False if there were errors, true otherwise.
	 */
	private boolean saveTable(SQLiteDatabase db, String table, String data){
		String str = data;
		//str = str.replace(":null", ":\"\"");
		String key, fields, vals;
		String value;
		int totalFields;
		int type;
		String[] values;
		String[] keys;

		List<String> queries = new ArrayList<>();
		queries.add("DELETE FROM " + table + ";");

		int i = 0;
		try {
			String row;
			while (str.indexOf("{") > 0) {
				//Log.e("STR", str.substring(0, 30) + " ... " + str.substring(3 * str.length() / 4));
				row = str.substring(str.indexOf("{"), str.indexOf("}") + 1);
				totalFields = 0;
				values = new String[99];
				keys = new String[99];

				while (row.indexOf("\",") > 0) {
					key = row.substring(row.indexOf("\"") + 1, row.indexOf("\":"));
					value = row.substring(row.indexOf("\"", row.indexOf(":")), row.indexOf("\"", row.indexOf(":") + 2) + 1);

					type = getColumnType(db, table, totalFields);
					switch (type) {
						case GM.DB.COLUMN.INT:

							row = row.substring(row.indexOf(value) + value.length() + 1);
							//TODO: Remove quotes
							value = value.replace("\"", "");

							if (value.equals("")){
								value = "null";
							}
							//Log.e("COLUMN", table + "INT(" + key + "): " + value);

							break;
						case GM.DB.COLUMN.DATETIME:
							//TODO: Date fromat
							//Log.e("COLUMN", table + "(" + key + "): DATETIME");
							row = row.substring(row.indexOf(value) + value.length() + 1);
							break;
						default: //VARCHAR
							//TODO: Reencode
							//Log.e("COLUMN", table + "(" + key + "): VARCHAR");
							row = row.substring(row.indexOf(value) + value.length() + 1);

					}

					keys[totalFields] = key;
					values[totalFields] = value;
					totalFields++;


				}
				//Do last column
				row = row.substring(row.indexOf("\"") + 1, row.indexOf("}"));
				key = row.substring(0, row.indexOf("\""));
				value = row.substring(row.indexOf(":") + 1);
				keys[totalFields] = key;
				values[totalFields] = value;
				totalFields++;
				//Log.e("REACH", "1");

				//With all the keys and the values, create an INSERT query and add to queries
				fields = "(";
				vals = "(";
				for (int j = 0; j < totalFields; j++) {
					fields = fields + keys[j] + ", ";
					vals = vals + values[j] + ", ";
				}
				//Log.e("REACH", "2");
				fields = fields.substring(0, fields.length() - 2) + ")";
				vals = vals.substring(0, vals.length() - 2) + ")";
				//Log.e("REACH", "3");
				queries.add("INSERT INTO " + table + " " + fields + " VALUES " + vals + ";");
				//Log.e("REACH", "4");

				i++;
				//Log.e("REM", str);
				try{
					str = str.substring(str.indexOf("{"));
				}
				catch (Exception ex){
					Log.e("SYNC", "End of string for table '" + table + "': " + ex.toString());
				}
			}

		}
		catch (Exception ex){
			Log.e("SYNC", "Error parsing remote table " + table + ": " + ex.toString());
			return false;
		}

		//If I get to this point, there were no errors, and I can safely execute the queries
		try {
			int totalQueries = queries.size();
			for (i = 0; i < totalQueries; i++) {
				//Log.e("QUERY", queries.get(i));
				db.execSQL(queries.get(i));
			}
		}
		catch (Exception ex){
			Log.e("SYNC", "Error inserting data from remote table " + table + " into the local db: " + ex.toString());
			return false;
		}

		return true;
	}


	/**
	 * The sweet stuff. Actually performs the sync. 
	 */
	@Override
	protected Void doInBackground(Void... params) {

		//Get preferences
		SharedPreferences preferences = myContextRef.getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);

		//Get usefull data for the uri
		String userCode = preferences.getString(GM.USER_CODE, "");

		//Get database. Stop if it's locked
		SQLiteDatabase db = myContextRef.openOrCreateDatabase(GM.DB_NAME, Activity.MODE_PRIVATE, null);
		if (db.isReadOnly()){
			Log.e("SYNC", "Database is locked and in read only mode. Skipping sync.");
			return null;
		}

		//Get database version
		Cursor cursor;
		int dbVersion;
		cursor = db.rawQuery("SELECT sum(version) AS v FROM version;", null);
		cursor.moveToFirst();
		dbVersion = cursor.getInt(0);
		cursor.close();

		URL url;
		String uri = buildUrl(userCode, dbVersion, fg, GM.getLang());
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
					Log.d("SYNC", "The server returned a 204 code (Success: No content) for the url \"" + uri + "\". Stoping sync process...");
					break;
				case 200:	//Success: OK
					Log.d("SYNC", "The server returned a 200 code (Success: OK) for the url \"" + uri + "\". Now syncing...");
					BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String o;
					while ((o = br.readLine()) != null)
						sb.append(o);

					//Get the string with the sync json. (The whole page)
					String strSync = sb.toString();
					strSync = strSync.replace(":null", ":\"\"");

					//Get the JSON object from the string.
					JSONObject jsonSync = new JSONObject(strSync);
					jsonSync = new JSONObject(jsonSync.get("sync").toString().substring(1, jsonSync.get("sync").toString().length() - 1));

					//Get the string wit the versions in JSON format.
					String strVersion = jsonSync.get("version").toString();

					//Handmade parser, because with:
					//String strData = jsonSync.get("data").toString();
					//I get an error: "No value for data"
					String strData = strSync.substring(1, strSync.length() - 1);
					strData = strData.substring(strData.indexOf("{\"data\":"));

					//If the data is correctly parsed and stored, commit changes to the database.
					db.beginTransaction();
					//recreateDb(db);
					if (saveVersions(db, strVersion) && saveData(db, strData)){
						db.setTransactionSuccessful();
						Log.d("SYNC", "The sync process finished correctly. Changes to the database will be commited");
					}
					else{
						Log.e("SYNC", "The sync process did not finish correctly. Any changes made to the database will be reverted");
					}
					db.endTransaction();

					break;
				default:
					Log.e("SYNC", "The server returned an unexpected code (" + httpCode + ") for the url \"" + uri + "\"");
			}
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
		catch (org.json.JSONException e) {
			Log.e("SYNC", "JSONException for URL (" + uri + "): " + e.toString());
			e.printStackTrace();
		}

		db.close();
		return null;
    	
	}
}
