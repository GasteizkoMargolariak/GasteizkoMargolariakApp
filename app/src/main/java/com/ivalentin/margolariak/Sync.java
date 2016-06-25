package com.ivalentin.margolariak;

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
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * AsyncTask that synchronizes the online database to the device.
 * Is run every time the app is started, and periodically in the background.
 * 
 * @author Inigo Valentin
 *
 */
public class Sync extends AsyncTask<Void, Void, Void> {
	
	private Context myContextRef;
	private ProgressBar pbSync;
	private Dialog dialog;
	private MainActivity activity;
	private boolean fg;
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
		if (pbSync != null)
			pbSync.setVisibility(View.VISIBLE);
		if (dialog != null) {
			dialog.show();
			strings = new String[5];
			strings[0] = myContextRef.getString(R.string.dialog_sync_text_0);
			strings[1] = myContextRef.getString(R.string.dialog_sync_text_1);
			strings[2] = myContextRef.getString(R.string.dialog_sync_text_2);
			strings[3] = myContextRef.getString(R.string.dialog_sync_text_3);
			strings[4] = myContextRef.getString(R.string.dialog_sync_text_4);
			tv = (TextView) dialog.findViewById(R.id.tv_dialog_sync_text);
			int idx = (int) (Math.random() * ((4) + 1));
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
		if (pbSync != null)
			pbSync.setVisibility(View.INVISIBLE);
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
				activity.loadSection(GM.SECTION_HOME, false);
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
    public Sync(Activity myContextRef, ProgressBar pb) {
        this.myContextRef = myContextRef;
        dialog = null;
        pbSync = pb;
        fg = true;
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
    public Sync(Activity myContextRef, ProgressBar pb, Dialog d, MainActivity activity) {
    	this.dialog = d;
    	this.activity = activity;
        this.myContextRef = myContextRef;
        pbSync = pb;
        fg = true;
    }
    
    /**
     * Called when the AsyncTask is created.
     * 
     * @param context The Context of the calling activity.
     */
    public Sync(Context context) {
        this.myContextRef = context;
        pbSync = null;
        fg = false;
    }

	@Override
	/**
	 * Called when the AsyncTask is updated.
	 * Used to change text in the sync window.
	 *
	 */
	protected void onProgressUpdate(Void...progress) {
		if (doProgress){
			if (millis + 300 < System.currentTimeMillis()) {
				millis = System.currentTimeMillis();
				int idx = (int) (Math.random() * ((4) + 1));
				tv.setText(strings[idx]);
			}
		}
	}

	/**
	 * The sweet stuff. Actually performs the sync. 
	 * 
	 * //@see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(Void... params) {

		//Open the database. f its locked, exit.
		SQLiteDatabase db = myContextRef.openOrCreateDatabase(GM.DB_NAME, Activity.MODE_PRIVATE, null);
		if (db.isReadOnly()){
			Log.e("DB LOCK", "L");
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

			String user = preferences.getString(GM.USER_NAME, "");
			String code = preferences.getString(GM.USER_CODE, "");
			int dbVersion = preferences.getInt(GM.PREF_DB_VERSION, GM.DEFAULT_PREF_DB_VERSION);
			fu = new FetchURL();
			fu.Run(GM.SERVER + "/app/sync.php?user=" + user + "&code=" + code + "&fg=" + fg + "&v=" + dbVersion);
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
								query = "INSERT INTO " + tableName + " ";
								queryFields = "(";
								queryValues = "(";
								while (row.contains(">,")) {

									publishProgress();

									try {
										if (row.length() < 5) {
											break;
										}
										line = row.substring(0, row.indexOf(">,"));
										line = line.substring(line.indexOf("<"));
										fieldName = line.substring(line.indexOf("<") + 1, line.indexOf(">"));
										queryFields = queryFields + fieldName + ", ";
										fieldValue = line.substring(line.indexOf("<" + fieldName + ">") + 2 + fieldName.length());
										fieldValue = fieldValue.substring(0, fieldValue.length() - fieldName.length() - 2);
										if (fieldValue.length() == 0)
											fieldValue = "null";
										if (fieldValue.charAt(0) == '\'' && fieldValue.charAt(fieldValue.length() - 1) == '\'') {
											fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
											fieldValue = fieldValue.replace("'", "''");
											fieldValue = "\'" + fieldValue + "\'";
										}
										queryValues = queryValues + fieldValue + ", ";
										row = row.substring(row.indexOf(">,") + 2);
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
				Log.e("DB UPDATE", "Not updating db version because there were errors");
				if (preferences.getInt(GM.PREF_DB_VERSION, GM.DEFAULT_PREF_DB_VERSION) == GM.DEFAULT_PREF_DB_VERSION) {
					prefEditor.putInt(GM.PREF_DB_VERSION, GM.DEFAULT_PREF_DB_VERSION + 1);
					prefEditor.apply();
				}
			}
		}
		catch(Exception ex){
			Log.e("Sync error", "Error updating info: " + ex.toString());
		}
		return null;
    	
	}
}
