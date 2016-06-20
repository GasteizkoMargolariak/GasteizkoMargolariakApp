package com.ivalentin.gm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Async task to download images on the fly.
 * The remote image file, the local path, and the ImageView where the image will be loaded are set on the constructor.
 *
 * @author Iñigo Valentin
 *
 */
class HomeSectionLocation extends AsyncTask<Void, Void, Void> {

	private boolean isLocationReported;
	private LatLng coord;
	private View view;
	private Activity activity;

	/**
	 * Constructor.
	 *
	 * @param v Application top view.
	 *
	 * @see android.widget.ImageView
	 */
	public HomeSectionLocation(View v, Activity a) {
		super();
		this.activity = a;
		this.view = v;
	}

	/**
	 * Before starting background thread do nothing.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	/**
	 * Downloading file in background thread.
	 */
	@Override
	protected Void doInBackground(Void... v) {

		String o = "";

		try {

			FetchURL fu = new FetchURL();
			fu.Run(GM.SERVER + "/app/location.php");
			//All the info
			o = fu.getOutput().toString();

		} catch (Exception e) {
			Log.e("Location error: ", e.getMessage());
		}

		if (o.contains("<reporting>1</reporting>")){
			isLocationReported = true;
			String latString = o.substring(o.indexOf("<lat>") + 5, o.indexOf("</lat>"));
			String lonString = o.substring(o.indexOf("<lon>") + 5, o.indexOf("</lon>"));
			coord = new LatLng(Double.parseDouble(latString), Double.parseDouble(lonString));
		}

		return null;
	}



	/**
	 * After completing background task, set the image on the ImageView.
	 */
	@Override
	protected void onPostExecute(Void v) {
		if (isLocationReported){

			SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putLong(GM.PREF_GM_LATITUDE, Double.doubleToLongBits(coord.latitude));
			editor.putLong(GM.PREF_GM_LONGITUDE, Double.doubleToLongBits(coord.longitude));
			editor.putString(GM.PREF_GM_LOCATION, new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime()));
			editor.apply();

			//Show menu entry
			LinearLayout menuEntry = (LinearLayout) activity.findViewById(R.id.ll_menu_location);
			menuEntry.setVisibility(View.VISIBLE);

			//Set up home view section
			LinearLayout section = (LinearLayout) view.findViewById(R.id.ll_home_section_location);
			section.setVisibility(View.VISIBLE);

		}
	}

}