package com.ivalentin.margolariak;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * AsyncTask to get Gasteizko Margolariak location.
 *
 * Uses the Location V1 API.
 *
 * @author Inigo Valentin
 *
 * @see AsyncTask
 */

class ReceiveLocation extends AsyncTask<Void, Void, Void> {

	private MainActivity activity;
	private boolean report = false;

	/**
	 * Called when the AsyncTask is created.
	 *
	 * @param activity The calling activity.
	 */
	ReceiveLocation(MainActivity activity){
		this.activity = activity;
	}

	@Override
	protected Void doInBackground(Void... params) {

		try {
			String uri = GM.API.SERVER + GM.API.LOCATION.PATH;
			URL url = new URL(uri);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

			int httpCode = urlConnection.getResponseCode();
			switch (httpCode) {
				case 400: //Client error: Bad request
					Log.e("RECEIVE_LOCATION", "The server returned a 400 code (Client Error: Bad request) for the url \"" + uri + "\"");
					break;
				case 204: //Success: No content
					Log.d("RECEIVE_LOCATION", "The server returned a 204 code (Success: No content) for the url \"" + uri + "\". Not getting location...");
					break;
				case 200: //Success: OK
					Log.d("RECEIVE_LOCATION", "The server returned a 200 code (Success: OK) for the url \"" + uri + "\". Now getting location...");
					BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String o;
					while ((o = br.readLine()) != null) {
						sb.append(o);
					}

					//Get the string with the sync json. (The whole page)
					String strLocation = sb.toString();
					String lat = strLocation.substring(strLocation.indexOf("\"lat\":\"") + 7, strLocation.indexOf("\"", strLocation.indexOf("\"lat\":\"") + 8));
					String lon = strLocation.substring(strLocation.indexOf("\"lon\":\"") + 7, strLocation.indexOf("\"", strLocation.indexOf("\"lon\":\"") + 8));
					String dtime = strLocation.substring(strLocation.indexOf("\"dtime\":\"") + 9, strLocation.indexOf("\"", strLocation.indexOf("\"dtime\":\"") + 10));
					Log.d("RECEIVE_LOCATION", "Found location [" +lat + ", " + lon + "] sent " + dtime);

					//Insert into the database
					SQLiteDatabase db = activity.openOrCreateDatabase(GM.DB.NAME, Activity.MODE_PRIVATE, null);
					if (db.isReadOnly()){
						Log.e("RECEIVE_LOCATION", "Database is in read only mode. Skipping.");
						return null;
					}
					db.execSQL("INSERT INTO location VALUES ('" + dtime + "', " + lat + ", " + lon + ")");
					db.close();

					//Set to true so the menu and relevant sections are hidden.
					report = true;
			}


		}
		catch (MalformedURLException e) {
			Log.e("RECEIVE_LOCATION", "Unable to get location, malformed URL: " + e.toString());
		}
		catch (IOException e) {
			Log.e("RECEIVE_LOCATION", "Unable to get location, IO exception: " + e.toString());
		}
		catch (Exception e) {
			Log.e("RECEIVE_LOCATION", "Unable to get location, unknown exception: " + e.toString());
		}


		return null;
	}

	/**
	 * Things to do after sync. Shows or hides the menu entry and relevant sections.
	 *
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPostExecute(Void v){
		activity.gotLocation(report);
	}

}
