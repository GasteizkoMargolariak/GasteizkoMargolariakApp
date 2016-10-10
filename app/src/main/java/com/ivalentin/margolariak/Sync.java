package com.ivalentin.margolariak;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
	private int newVersion;
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
			SharedPreferences preferences = myContextRef.getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
			if (preferences.getInt(GM.PREF_DB_VERSION, GM.DEFAULT_PREF_DB_VERSION) == GM.DEFAULT_PREF_DB_VERSION){
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

	/**
	 * Stores version data for each section in the database.
	 * Uses a custom JSON parser.
	 *
	 * @param db Database store the data.
	 * @param versions List of strings with data about the versions.
	 * @return False if there were errors, true otherwise.
	 */
	protected boolean saveVersions(SQLiteDatabase db, String versions){
		String str = versions;
		String key;
		boolean error = false;
		int value;
		int totalVersions = 0;
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
			if (totalVersions > 0){
				db.execSQL("CREATE TABLE IF NOT EXISTS version (section VARCHAR, version INT);");
				db.execSQL("DELETE FROM version;");
				for (int i = 0; i < totalVersions; i ++){
					db.execSQL("INSERT INTO version VALUES ('" + keys[i] + "', " + values[i] + ");");
				}
			}

		}
		catch (Exception ex){
			error = true;
			Log.e("saveVersions", "Error saving the remote db versions: " + ex.toString());
		}

		return error;
	}

	/**
	 * Stores version data for each section in the database.
	 * Uses a custom JSON parser.
	 *
	 * @param db Database store the data.
	 * @param data List of strings in json format with the tables.
	 * @return False if there were errors, true otherwise.
	 */
	protected boolean saveData(SQLiteDatabase db, String data){

		return true;
	}


	/**
	 * The sweet stuff. Actually performs the sync. 
	 */
	@Override
	protected Void doInBackground(Void... params) {

		//Get preferences
		SharedPreferences preferences = myContextRef.getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor;
		prefEditor = preferences.edit();

		//Get usefull data for the uri
		String userCode = preferences.getString(GM.USER_CODE, "");
		int dbVersion = preferences.getInt(GM.PREF_DB_VERSION, GM.DEFAULT_PREF_DB_VERSION);

		//Get database. Stop if it's locked
		SQLiteDatabase db = myContextRef.openOrCreateDatabase(GM.DB_NAME, Activity.MODE_PRIVATE, null);
		if (db.isReadOnly()){
			Log.e("Db ro", "Database is locked and in read only mode. Skipping sync.");
			return null;
		}

		URL url;
		String uri = buildUrl(userCode, dbVersion, fg, GM.getLang());
		try {
			url = new URL(uri);

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

			int httpCode = urlConnection.getResponseCode();
			switch (httpCode){
				case 400:	//Client error: Bad request
					Log.e("Sync error", "The server returned a 400 code (Client Error: Bad request) for the url \"" + uri + "\"");
					break;
				case 403:	//Client error: Forbidden
					Log.e("Sync error", "The server returned a 403 code (Client Error: Forbidden) for the url \"" + uri + "\"");
					break;
				case 204:	//Success: No content
					Log.d("Sync success", "The server returned a 204 code (Success: No content) for the url \"" + uri + "\". Stoping sync process...");
					break;
				case 200:	//Success: OK
					Log.d("Sync", "The server returned a 200 code (Success: OK) for the url \"" + uri + "\". Now syncing...");
					InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String o;
					while ((o = br.readLine()) != null)
						sb.append(o);

					//Get the string with the sync json. (The whole page)
					String strSync = sb.toString();

					//Get the JSON object from the string.
					JSONObject jsonSync = new JSONObject(strSync);
					jsonSync = new JSONObject(jsonSync.get("sync").toString().substring(1, jsonSync.get("sync").toString().length() - 1));

					//Get the string wit the versions in JSON format.
					String strVersion = jsonSync.get("version").toString();

					//Get the string with the data in JSON format,
					saveVersions(db, strVersion);
					//saveData(db, data);

					break;
				default:
					Log.e("Sync error", "The server returned an unexpected code (" + httpCode + ") for the url \"" + uri + "\"");
			}
			urlConnection.disconnect();
			return null;

		}
		catch (MalformedURLException e) {
			Log.e("Sync error", "Malformed URL (" + uri + "): " + e.toString());
			e.printStackTrace();
		}
		catch (IOException e) {
			Log.e("Sync error", "IOException for URL (" + uri + "): " + e.toString());
			e.printStackTrace();
		}
		catch (org.json.JSONException e) {
			Log.e("Sync error", "JSONException for URL (" + uri + "): " + e.toString());
			e.printStackTrace();
		}


		/*//Open the database. f its locked, exit.
		SQLiteDatabase db = myContextRef.openOrCreateDatabase(GM.DB_NAME, Activity.MODE_PRIVATE, null);
		if (db.isReadOnly()){
			Log.e("Db ro", "Database is locked and in read only mode. Skipping sync.");
			return null;
		}

		publishProgress();

		//Gets the remote page.
    	FetchURL fu;

		//The lines of the received web page.
		String o = null;
		
		//List of SQL queries to be performed, as received from the web page.
		List<String> queryList = new ArrayList<>();
		
		//Preferences.
		SharedPreferences preferences = myContextRef.getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor;
		prefEditor = preferences.edit();

		//Boolean that will prevent the process to go on if something goes wrong.
		boolean success = true;
		
		//Get the file
		try{
			publishProgress();

			String code = preferences.getString(GM.USER_CODE, "");
			int dbVersion = preferences.getInt(GM.PREF_DB_VERSION, GM.DEFAULT_PREF_DB_VERSION);
			fu = new FetchURL();
			fu.Run(GM.SERVER + "/app/sync.php?os=android&code=" + code + "&fg=" + fg + "&v=" + dbVersion + "&lang=" + GM.getLang());
			//All the info
			o = fu.getOutput().toString();
			publishProgress();
		}
		catch(Exception ex){
			publishProgress();
			Log.e("Sync error", "Error fetching remote file: " + ex.toString());
			success = false;
		}

		int errorCount = 0;

		if (success){

			publishProgress();

			//Parse the contents of the page
			//Check if the database is synced
			if (o.contains("<synced>1</synced>")){
				Log.i("SYNC", "Database is already at the latest version");
				return null;
			}

			//Try to separate the file by tables (<table></table>)
			try{
				String table, tableName, row, fieldName, fieldValue, line, query, queryFields, queryValues;

				//Get db version
				newVersion = Integer.parseInt(o.substring(o.indexOf("<version>") + 9, o.indexOf("</version>")));

				//Get other preferences and store them
				int prefPhotos = Integer.parseInt(o.substring(o.indexOf("<photos>") + 8, o.indexOf("</photos>")));
				int prefFestivals = Integer.parseInt(o.substring(o.indexOf("<festivals>") + 11, o.indexOf("</festivals>")));
				prefEditor.putInt(GM.PREF_DB_PHOTOS, prefPhotos);
				prefEditor.putInt(GM.PREF_DB_FESTIVALS, prefFestivals);
				prefEditor.apply();


				while (o.contains("<table>")){

					publishProgress();

					try {
						table = o.substring(o.indexOf("<table>"), o.indexOf("</table>") + 8);
						tableName = table.substring(table.indexOf("<name>") + 6, table.indexOf("</name>"));
						queryList.add("DELETE FROM " + tableName + ";");
						while (table.contains("<row>")) {

							try {
								if (table.length() < 5) {
									break;
								}
								row = table.substring(table.indexOf("<row>") + 5, table.indexOf("</row>"));

								//if (row.indexOf("Gracias a todos vosotros no hemos hecho") != -1)
								//	Log.e("Row", row);

								query = "INSERT INTO " + tableName + " ";
								queryFields = "(";
								queryValues = "(";
								while (row.contains(">,")) {

									publishProgress();

									try {
										if (row.length() < 5) {
											break;
										}
										line = row.substring(0, row.indexOf(">, \t") + 1);
										line = line.substring(line.indexOf("<"));

										fieldName = line.substring(line.indexOf("<") + 1, line.indexOf(">"));
										queryFields = queryFields + fieldName + ", ";
										fieldValue = line.substring(line.indexOf("<" + fieldName + ">") + 2 + fieldName.length());
										//fieldValue = fieldValue.substring(0, fieldValue.length() - fieldName.length() - 2);
										fieldValue = fieldValue.substring(0, fieldValue.indexOf("</" + fieldName + ">"));
										//if (fieldValue.indexOf("Gracias a todos vosotros no hemos hecho") != -1)
										//Log.e("Fieldvalue", fieldValue);

										if (fieldValue.length() == 0)
											fieldValue = "null";
										if (fieldValue.charAt(0) == '\'' && fieldValue.charAt(fieldValue.length() - 1) == '\'') {
											fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
											fieldValue = fieldValue.replace("'", "''");
											fieldValue = "\'" + fieldValue + "\'";
										}
										//Log.e(fieldName, fieldValue);
										queryValues = queryValues + fieldValue + ", ";
										row = row.substring(row.indexOf(">, \t") + 4);
									}
									catch(Exception ex){
										Log.e("Parsing error", "Error getting values from row: " + ex.toString());
										errorCount ++;
										break;
									}
								}

								queryFields = queryFields.substring(0, queryFields.length() - 2) + ")";
								queryValues = queryValues.substring(0, queryValues.length() - 2) + ")";
								query = query + queryFields + " VALUES " + queryValues + ";";
								queryList.add(query);
								//Log.e("Query", query);

								table = table.substring(table.indexOf("</row>") + 6);
							}
							catch(Exception ex){
								Log.e("Parsing error", "Error getting rows from tables: " + ex.toString());
								errorCount ++;
								break;
							}
						}
					}
					catch(Exception ex){
						Log.e("Parsing error", "Error getting tables from the sync file: " + ex.toString());
						errorCount ++;
						break;
					}
					o = o.substring(o.indexOf("</table>") + 8);
				}
			}
			catch(Exception ex){
				Log.e("Sync error", "Error parsing remote info: " + ex.toString());
				errorCount ++;
			}
		}
		try{

			for(int i = 0; i < queryList.size(); i++){
				publishProgress();
				try {
					db.execSQL(queryList.get(i));
				}
				catch(Exception ex){
					Log.e("Query Error", "Error on sync query: " + ex.toString());
					errorCount ++;
				}
			}
			db.close();

			//Set current database version in preferences.
			if (errorCount == 0){
				prefEditor.putInt(GM.PREF_DB_VERSION, newVersion);
				prefEditor.apply();
			}
			else{
				Log.e("Db update", "Not updating db version because there were errors");
				if (preferences.getInt(GM.PREF_DB_VERSION, GM.DEFAULT_PREF_DB_VERSION) == GM.DEFAULT_PREF_DB_VERSION) {
					prefEditor.putInt(GM.PREF_DB_VERSION, GM.DEFAULT_PREF_DB_VERSION + 1);
					prefEditor.apply();
				}
			}
		}
		catch(Exception ex){
			Log.e("Sync error", "Error updating info: " + ex.toString());
		}*/
		return null;
    	
	}
}
