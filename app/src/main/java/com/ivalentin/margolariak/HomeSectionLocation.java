package com.ivalentin.margolariak;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Async task to download images on the fly.
 * The remote image file, the local path, and the ImageView where the image will be loaded are set on the constructor.
 *
 * @author Iñigo Valentin
 *
 */
class HomeSectionLocation extends AsyncTask<Void, Void, Void> {

	private boolean isLocationReported;
	//private LatLng coord;
	private final View view;

	/**
	 * Constructor.
	 *
	 * @param v Application top view.
	 *
	 * @see android.widget.ImageView
	 */
	public HomeSectionLocation(View v) {
		super();
		this.view = v;
	}

	/**
	 * Downloading file in background thread.
	 */
	@Override
	protected Void doInBackground(Void... v) {

		String o = "";

		try {

			FetchURL fu = new FetchURL();
			fu.Run(GM.API.SERVER + "/app/location.php");
			//All the info
			o = fu.getOutput().toString();

		} catch (Exception e) {
			Log.e("Location error: ", e.getMessage());
		}

		if (o.contains("<reporting>1</reporting>")){
			isLocationReported = true;
			String latString = o.substring(o.indexOf("<lat>") + 5, o.indexOf("</lat>"));
			String lonString = o.substring(o.indexOf("<lon>") + 5, o.indexOf("</lon>"));
			//coord = new LatLng(Double.parseDouble(latString), Double.parseDouble(lonString));
		}

		return null;
	}



	/**
	 * After completing background task, set the image on the ImageView.
	 */
	@Override
	protected void onPostExecute(Void v) {
		if (isLocationReported){

			//Set up home view section
			LinearLayout section = (LinearLayout) view.findViewById(R.id.ll_home_section_location);
			section.setVisibility(View.VISIBLE);
			section.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					((MainActivity) v.getContext()).loadSection(GM.SECTION.LOCATION);
				}
			});

		}
	}

}
