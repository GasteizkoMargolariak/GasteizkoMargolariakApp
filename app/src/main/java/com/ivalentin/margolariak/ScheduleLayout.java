package com.ivalentin.margolariak;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * Fragment to be inflated showing the festivals schedule.
 * Contains a date selector and a ScrollView with all the activities for the day.
 *
 * @author Inigo Valentin
 *
 */
public class ScheduleLayout extends Fragment{

	private Bundle bund;

	private final String dates[] = new String[40];
	private int dateCount = 0;
	private int selected = 0;

	//Map stuff for the dialog
	private MapView mapView;
	private int route;
	private View view;
	private String markerName = "";

	/**
	 * Run when the fragment is inflated.
	 * Assigns views, gets the date and does the first call to the populateSchedule function.
	 *
	 * @param inflater A LayoutInflater to manage views
	 * @param container The container View
	 * @param savedInstanceState Bundle containing the state
	 *
	 * @see Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@SuppressLint("InflateParams") //Throws unknown error when done properly.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Calendar calendar = Calendar.getInstance();
		String year = Integer.toString(calendar.get(Calendar.YEAR));


		//Set bundle for the map
		bund = savedInstanceState;

		//Load the layout
		view = inflater.inflate(R.layout.fragment_layout_schedule, null);
		view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				CustomScrollView sv = (CustomScrollView) v.findViewById(R.id.sv_schedule);
				Log.d("SV", "INVALIDATED");
				sv.invalidate();
			}
		});

		//Get schedule type
		TextView title = (TextView) view.findViewById(R.id.tv_schedule_type);
		Bundle bundle = this.getArguments();
		final int schedule = bundle.getInt(GM.SCHEDULE.KEY, GM.SCHEDULE.CITY);

		//Set the title
		if (schedule == GM.SCHEDULE.CITY) {
			((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_lablanca_schedule));
			title.setText(R.string.menu_lablanca_schedule);
		}
		else{
			((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_lablanca_gm_schedule));
			title.setText(R.string.menu_lablanca_gm_schedule);
		}
		((MainActivity) getActivity()).setShareLink(getString(R.string.share_lablanca), GM.SHARE.LABLANCA);

		//Populate the dates array
		SQLiteDatabase db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath(GM.DB.NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
		Cursor cursor;
		if (schedule == GM.SCHEDULE.MARGOLARIAK) {
			cursor = db.rawQuery("SELECT DISTINCT date(start) AS daydate FROM festival_event_gm WHERE strftime('%Y', start) = '" + year + "' AND strftime('%H', start) > '06' ORDER BY daydate;", null);
		}
		else{
			cursor = db.rawQuery("SELECT DISTINCT date(start) AS daydate FROM festival_event_vity WHERE strftime('%Y', start) = '" + year + "' AND strftime('%H', start) > '06' ORDER BY daydate;", null);
		}


		//Get current date in the same format as retrieved from the database
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		Date today = Calendar.getInstance().getTime();
		String curDate = df.format(today);

		while (cursor.moveToNext()){
			if (dateCount < 40) {
				dates[dateCount] = cursor.getString(0);
				if (curDate.equals(cursor.getString(0))) {
					selected = dateCount;
				}
			}
			dateCount ++;
		}
		cursor.close();
		db.close();

		//Assign buttons
		Button btL = (Button) view.findViewById(R.id.bt_schedule_l);
		Button btR = (Button) view.findViewById(R.id.bt_schedule_r);
		btL.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selected --;
				populateSchedule(schedule);
			}
		});
		btR.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selected ++;
				populateSchedule(schedule);
			}
		});

		//Assign the filter text
		EditText etFilter = (EditText) view.findViewById(R.id.et_schedule_filter);
		etFilter.addTextChangedListener(new TextWatcher() {

	        @Override
	        public void afterTextChanged(Editable s) { }

	        @Override
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

	        @Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	//Repopulate when the text is changed
	        	populateSchedule(schedule);
	        }
		});

		//Populate the activity list
		populateSchedule(schedule);

		//Return the fragment view
		return view;
	}

	/**
	 * Populates the list of activities with the ones in the selected day.
	 *
	 * @param schedule GM.SCHEDULE.GM or GM.SCHEDULE.CITY
	 */
	@SuppressLint({"InflateParams", "SwitchIntDef"})
	//Views are added from a loop: I can't specify the parent when inflating.
	private int populateSchedule(final int schedule) {

		int eventCount = 0;

		//Hide or show buttons
		Button btNext = (Button) view.findViewById(R.id.bt_schedule_r);
		Button btPrevious = (Button) view.findViewById(R.id.bt_schedule_l);
		if (selected <= 0) {
			btPrevious.setVisibility(View.INVISIBLE);
		} else {
			btPrevious.setVisibility(View.VISIBLE);
		}
		if (selected >= dateCount - 1) {
			btNext.setVisibility(View.INVISIBLE);
		} else {
			btNext.setVisibility(View.VISIBLE);
		}

		//If there are no events,return now
		if (selected < 0 || selected >= dateCount) {
			return eventCount;
		}

		//Search filter
		String filter = ((EditText) view.findViewById(R.id.et_schedule_filter)).getText().toString().toLowerCase(Locale.US);

		Calendar calendar = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
		try {
			calendar.setTime(dateFormat.parse(dates[selected] + " 06:00:00"));
		} catch (Exception ex) {
			Log.e("Datetime error", ex.toString());
		}

		SQLiteDatabase db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath(GM.DB.NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);

		String lang = GM.getLang();

		//A layout to be populated with one activity
		LinearLayout entry, list;

		//An inflater
		LayoutInflater factory = LayoutInflater.from(getActivity());

		//Views in each row
		TextView tvRowTitle, tvRowTime, tvRowDesc, tvRowPlace, tvRowId, tvRowAddress;

		//Set date selector texts
		TextView tvDayNumber = (TextView) view.findViewById(R.id.tv_schedule_day_number);
		TextView tvDayMonth = (TextView) view.findViewById(R.id.tv_schedule_day_month);
		TextView tvDayTitle = (TextView) view.findViewById(R.id.tv_schedule_day_title);


		tvDayNumber.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
		switch (calendar.get(Calendar.MONTH)) {
			case 6:
				tvDayMonth.setText(getString(R.string.schedule_month_july));
				break;
			case 7:
				tvDayMonth.setText(getString(R.string.schedule_month_august));
				break;
		}
		if (schedule == GM.SCHEDULE.MARGOLARIAK) {
			Cursor cursorDay = db.rawQuery("SELECT name_" + lang + " FROM festival_day WHERE date(date) = '" + dates[selected] + "';", null);
			if (cursorDay.getCount() > 0) {
				cursorDay.moveToFirst();
				tvDayTitle.setText(cursorDay.getString(0));
			} else {
				tvDayTitle.setText("");
			}
			cursorDay.close();
		}


		//Get day end date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(sdf.parse(dates[selected]));
		} catch (Exception ex) {
			Log.e("Date parsing error", ex.toString());
			return eventCount;
		}
		c.add(Calendar.DATE, 1);  // number of days to add
		String endDate = sdf.format(c.getTime());

		String query = "";;

		if (schedule == GM.SCHEDULE.MARGOLARIAK) {
			query = "SELECT festival_event_gm.id, title_" + lang + ", description_" + lang + ", place, start, end, name_" + lang + ", address_" + lang + ", lat, lon FROM festival_event_gm, place WHERE place = place.id AND start >= '" + dates[selected] + " 06:00:00' AND start < '" + endDate + " 05:59:59'";
		}
		else {
			query = "SELECT festival_event_city.id, title_" + lang + ", description_" + lang + ", place, start, end, name_" + lang + ", address_" + lang + ", lat, lon FROM festival_event_city, place WHERE place = place.id AND start >= '" + dates[selected] + " 06:00:00' AND start < '" + endDate + " 05:59:59'";
		}
		if (filter.length() > 0) {
			query = query + "AND (title_" + lang + " like '%" + filter + "%' OR description_" + lang + " like '%" + filter + "%' OR name_" + lang + " like '%" + filter + "%' OR address_" + lang + " like '%" + filter + "%') ";
		}
		query = query + " ORDER BY start;";
		Cursor cursor = db.rawQuery(query, null);

		//Clear the list
		list = (LinearLayout) view.findViewById(R.id.ll_schedule_list);
		list.removeAllViews();

		while (cursor.moveToNext()) {
			eventCount++;

			entry = (LinearLayout) factory.inflate(R.layout.row_schedule, null);

			//Set id
			tvRowId = (TextView) entry.findViewById(R.id.tv_row_schedule_id);
			tvRowId.setText(cursor.getString(0));

			//Set title
			tvRowTitle = (TextView) entry.findViewById(R.id.tv_row_schedule_title);
			tvRowTitle.setText(cursor.getString(1));

			//Set description
			tvRowDesc = (TextView) entry.findViewById(R.id.tv_row_schedule_description);
			if (cursor.getString(2) == null || cursor.getString(2).length() <= 0 || cursor.getString(2).equals(cursor.getString(1))) {
				tvRowDesc.setVisibility(View.GONE);
			} else {
				tvRowDesc.setText(cursor.getString(2));
			}

			//Set place
			tvRowPlace = (TextView) entry.findViewById(R.id.tv_row_schedule_place);
			tvRowPlace.setText(cursor.getString(6));

			//Set address
			tvRowAddress = (TextView) entry.findViewById(R.id.tv_row_schedule_address);
			if (cursor.getString(7) == null || cursor.getString(7).length() <= 0 || cursor.getString(7).equals(cursor.getString(6))) {
				tvRowAddress.setVisibility(View.GONE);
			} else {
				tvRowAddress.setText(cursor.getString(7));
			}

			//Set icon
			if (schedule == GM.SCHEDULE.MARGOLARIAK) {
				ImageView pinPoint = (ImageView) entry.findViewById(R.id.iv_row_schedule_pinpoint);
				pinPoint.setImageResource(getResources().getIdentifier("com.ivalentin.margolariak:drawable/pinpoint_gm", null, null));
			}


			//Set time
			tvRowTime = (TextView) entry.findViewById(R.id.tv_row_schedule_time);
			String tm = cursor.getString(4).substring(cursor.getString(4).length() - 8, cursor.getString(4).length() - 3);
			tvRowTime.setText(tm);

			//Set clisk listener
			entry.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					TextView tvId = (TextView) v.findViewById(R.id.tv_row_schedule_id);
					int id = Integer.parseInt(tvId.getText().toString());
					showDialog(id, schedule);
				}
			});

			//Add the view
			list.addView(entry);
		}

		cursor.close();
		db.close();

		//Scroll to top
		try {
			ScrollView sw = (ScrollView) list.getParent().getParent();
			sw.scrollTo(0, 0);
		}
		catch(Exception ex){
			Log.e("Schedule scroll", "Culdnt get scrollview to scroll: " + ex.toString());
		}

		return eventCount;

	}

	/**
	 * Shows a dialog with info about the selected event.
	 *
	 * @param id The event id
	 * @param schedule GM.SCHEDULE.GM or GM.SCHEDULE.CITY
	 */
	@SuppressWarnings("ConstantConditions")
	private void showDialog(final int id, final int schedule){

		//Create the dialog
		final Dialog dialog = new Dialog(getActivity());

		//Set up dialog window
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_schedule);

		//Date formatters
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd-", Locale.US);
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

		//Set the custom dialog components - text, image and button
		TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_title);
		TextView tvDescription = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_description);
		TextView tvHost = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_host);
		TextView tvDate = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_date);
		TextView tvTime = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_time);
		TextView tvPlace = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_place);
		TextView tvAddress = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_address);
		Button btClose = (Button) dialog.findViewById(R.id.bt_schedule_close);

		//Get info about the event
		SQLiteDatabase db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath(GM.DB.NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
		String lang = GM.getLang();
		Cursor cursor;
		if (schedule == GM.SCHEDULE.MARGOLARIAK){
			cursor = db.rawQuery("SELECT festival_event_gm.id, title_" + lang + ", description_" + lang + ", place, route, start, end, name_" + lang + ", address_" + lang + ", lat, lon, host, sponsor FROM festival_event_gm, place WHERE place = place.id AND festival_event_gm.id = " + id + ";", null);
		}
		else{
			cursor = db.rawQuery("SELECT festival_event_city.id, title_" + lang + ", description_" + lang + ", place, route, start, end, name_" + lang + ", address_" + lang + ", lat, lon, host, sponsor FROM, sponsor festival_event_city, place WHERE place = place.id AND festival_event_city.id = " + id + ";", null);
		}
		if (cursor.getCount() > 0){
			cursor.moveToNext();

			//Set title
			tvTitle.setText(cursor.getString(1));
			markerName = cursor.getString(1);

			//Set description
			if (cursor.getString(2) != null && cursor.getString(2).length() > 0) {
				tvDescription.setText(cursor.getString(2));
			}
			else{
				tvDescription.setVisibility(View.GONE);
			}

			//Set host
			if (cursor.getString(10) != null){
				Cursor hostCursor = db.rawQuery("SELECT name_" + lang + " FROM people WHERE id = " + cursor.getString(10) + ";", null);
				if (hostCursor.moveToNext()){
					tvHost.setVisibility(View.VISIBLE);
					tvHost.setText(String.format(getString(R.string.schedule_host), hostCursor.getString(0)));
				}
				hostCursor.close();
				// TODO. Add sponsor.
			}
			else{
				tvHost.setVisibility(View.GONE);
			}

			//Set date
			try{
				Date day = dateFormat.parse(cursor.getString(4));
				Date date = new Date();
				//If the event is today, show "Today" instead of the date
				if (dayFormat.format(day).equals(dayFormat.format(date)))
					tvDate.setText(dialog.getContext().getString(R.string.today));
				else{
					Calendar cal = Calendar.getInstance();
				    cal.setTime(date);
				    cal.add(Calendar.HOUR_OF_DAY, 24);

				    //If the event is tomorrow, show "Tomorrow" instead of the date
				    if (dayFormat.format(cal.getTime()).equals(dayFormat.format(date))){
				    	tvDate.setText(dialog.getContext().getString(R.string.tomorrow));
				    }

				    //Else, show the date
				    else{
						SimpleDateFormat printFormat;
						switch (lang){
							case "en":
								printFormat = new SimpleDateFormat("MMMM dd", Locale.US);
								tvDate.setText(printFormat.format(day));
								break;
							case "eu":
								printFormat = new SimpleDateFormat("MMMM dd", new Locale("eu", "ES"));
								tvDate.setText(printFormat.format(day));
								break;
							default:
								printFormat = new SimpleDateFormat("dd", new Locale("es", "ES"));
								String str = printFormat.format(day) + " de ";
								printFormat = new SimpleDateFormat("MMMM", new Locale("es", "ES"));
								str = str + printFormat.format(day);
								tvDate.setText(str);
						}

				    }
				}
			}
			catch (Exception ex){
				Log.e("Error parsing date", ex.toString());
			}

			//Set time
			try{
				if (cursor.getString(6) == null || cursor.getString(6).length() == 0) {
					tvTime.setText(timeFormat.format(dateFormat.parse(cursor.getString(5))));
				}
				else {
					String time = timeFormat.format(dateFormat.parse(cursor.getString(5))) + " - " + timeFormat.format(dateFormat.parse(cursor.getString(6)));
					tvTime.setText(time);
				}
			}
			catch (ParseException ex){
				Log.e("Error parsing time", ex.toString());
			}

			//Set the place
			tvPlace.setText(cursor.getString(7));
			tvAddress.setText(cursor.getString(8));

			//Set up map
			// TODO if route...
			//location = new LatLng(Double.parseDouble(cursor.getString(8)), Double.parseDouble(cursor.getString(9)));
			//mapView = (MapView) dialog.findViewById(R.id.mv_dialog_schedule_map);
			//mapView.onCreate(bund);

			mapView = (MapView) dialog.findViewById(R.id.mv_dialog_schedule_map);

			mapView.setMultiTouchControls(true);
			IMapController mapController = mapView.getController();
			mapController.setZoom(15);
			GeoPoint center = new GeoPoint(cursor.getDouble(9), cursor.getDouble(10));
			mapController.setCenter(center);

			//Close the db connection
			cursor.close();
			db.close();

			//Set close button
	        	btClose.setOnClickListener(new OnClickListener() {
	    			@Override
	    			public void onClick(View v) {
    					dialog.dismiss();
    				}
    			});

			//Actions to take when the dialog is cancelled
			dialog.setOnCancelListener(new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface dialog) {
				/*	if (map != null) {
						if (!(ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
							map.setMyLocationEnabled(false);
						}
					}
					if (mapView != null){
    						mapView.onResume();
    						mapView.onDestroy();
    					}*/
				}
			});

			//Show the dialog
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			lp.copyFrom(dialog.getWindow().getAttributes());
			lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
			lp.gravity = Gravity.CENTER;
			lp.dimAmount = 0.0f;
			dialog.getWindow().setAttributes(lp);

			//Show dialog
			dialog.show();

			//Start the map
			//startMap();
			//mapView.onResume();
			dialog.setOnShowListener(new OnShowListener(){

				@Override
				public void onShow(DialogInterface dialog) {
					//startMap();

				}
			});

		}
	}

	/**
	 * Starts the map in the dialog.
	 */
	private void startMap(){
		//mapView.getMapAsync(this);
	}





	/**
	 * Called in a situation of low memory.
	 * Lets the map handle this situation.
	 *
	 * @see android.app.Fragment#onLowMemory()
	 */
	/*@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (mapView != null){
			mapView.onResume();
			mapView.onLowMemory();
		}
	}*/

	/**
	 * Called when the activity is resumed.
	 * Lets the map handle this situation.
	 *
	 * @see android.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));
		super.onResume();
	}



	/**
	 * Checks app permission to access the user location.
	 * @return true if the permission has been granted, false otherwise.
	 */
	private boolean checkLocationPermission(){
		return getContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && getContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}
}
