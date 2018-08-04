package com.ivalentin.margolariak;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;

/**
 * Fragment to be inflated showing the festivals schedule.
 * Contains a date selector and a ScrollView with all the activities for the day.
 *
 * @author Inigo Valentin
 *
 */
public class ScheduleLayout extends Fragment{

	private final String dates[] = new String[40];
	private int dateCount = 0;
	private int selected = 0;
	
	private View view;

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
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Calendar calendar = Calendar.getInstance();
		String year = Integer.toString(calendar.get(Calendar.YEAR));

		//Load the layout
		view = inflater.inflate(R.layout.fragment_layout_schedule, container, false);
		view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				ScrollView sv = (ScrollView) v.findViewById(R.id.sv_schedule);
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
			cursor = db.rawQuery("SELECT DISTINCT date(start) AS daydate FROM festival_event_city WHERE strftime('%Y', start) = '" + year + "' AND strftime('%H', start) > '06' ORDER BY daydate;", null);
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
	//Views are added from a loop: I can't specify the parent when inflating.
	private void populateSchedule(final int schedule) {

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
			return;
		}

		//Search filter
		String filter = ((EditText) view.findViewById(R.id.et_schedule_filter)).getText().toString().toLowerCase(Locale.US);

		Calendar calendar = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
		try {
			calendar.setTime(dateFormat.parse(dates[selected] + " 06:00:00"));
		} catch (Exception ex) {
			Log.e("SCHEDULE_LAYOUT", "Datetime error: " + ex.toString());
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
		int i = calendar.get(Calendar.MONTH);
		if (i == Calendar.JULY) {
			tvDayMonth.setText(getString(R.string.schedule_month_july));

		}
		else if (i == Calendar.AUGUST) {
			tvDayMonth.setText(getString(R.string.schedule_month_august));

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
			Log.e("SCHEDULE_LAYOUT", "Date parsing error: " + ex.toString());
			return;
		}
		c.add(Calendar.DATE, 1);  // number of days to add
		String endDate = sdf.format(c.getTime());

		String query;

		if (schedule == GM.SCHEDULE.MARGOLARIAK) {
			query = "SELECT festival_event_gm.id AS id, title_" + lang + " AS title, description_" + lang + " AS description, place AS place_id, start, end, name_" + lang + " AS place, address_" + lang + " AS address, lat, lon, route FROM festival_event_gm, place WHERE place = place.id AND start >= '" + dates[selected] + " 06:00:00' AND start < '" + endDate + " 05:59:59'";
		}
		else {
			query = "SELECT festival_event_city.id AS id, title_" + lang + " AS title, description_" + lang + " AS description, place AS place_id, start, end, name_" + lang + " AS place, address_" + lang + " AS address, lat, lon, route FROM festival_event_city, place WHERE place = place.id AND start >= '" + dates[selected] + " 06:00:00' AND start < '" + endDate + " 05:59:59'";
		}
		if (filter.length() > 0) {
			query = query + "AND (title_" + lang + " like '%" + filter + "%' OR description_" + lang + " like '%" + filter + "%' OR name_" + lang + " like '%" + filter + "%' OR address_" + lang + " like '%" + filter + "%') ";
		}
		query = query + " ORDER BY start;";
		Cursor cursor = db.rawQuery(query, null);
		/*
		 * 0: id
		 * 1: title
		 * 2: description
		 * 3: place_id
		 * 4: start
		 * 5: end
		 * 6: place
		 * 7: address
		 * 8: lat
		 * 9: lon
		 * 10: route
		 */

		//Clear the list
		list = (LinearLayout) view.findViewById(R.id.ll_schedule_list);
		list.removeAllViews();

		while (cursor.moveToNext()) {

			entry = (LinearLayout) factory.inflate(R.layout.row_schedule, list, false);

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
			}
			else {
				tvRowDesc.setText(cursor.getString(2));
			}

			//Set place
			tvRowPlace = (TextView) entry.findViewById(R.id.tv_row_schedule_place);
			tvRowPlace.setText(cursor.getString(6));

			//Set address
			tvRowAddress = (TextView) entry.findViewById(R.id.tv_row_schedule_address);
			if (cursor.getString(7) == null || cursor.getString(7).length() <= 0 || cursor.getString(7).equalsIgnoreCase(cursor.getString(6))) {
				tvRowAddress.setVisibility(View.GONE);
			}
			else {
				tvRowAddress.setText(cursor.getString(7));
			}

			//Set icon
			if (cursor.getString(10) != null){
				ImageView pinPoint = (ImageView) entry.findViewById(R.id.iv_row_schedule_pinpoint);
				pinPoint.setImageResource(R.drawable.pinpoint_route);
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
			Log.e("SCHEDULE_LAYOUT", "Culdn't get scrollview to scroll: " + ex.toString());
		}

	}

	/**
	 * Shows a dialog with info about the selected event.
	 *
	 * @param id The event id
	 * @param schedule GM.SCHEDULE.GM or GM.SCHEDULE.CITY
	 */
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
		TextView tvSponsor = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_sponsor);
		TextView tvDate = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_date);
		TextView tvTime = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_time);
		LinearLayout llPlace = (LinearLayout) dialog.findViewById(R.id.ll_dialog_schedule_place);
		TextView tvPlace = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_place);
		TextView tvAddress = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_address);
		Button btClose = (Button) dialog.findViewById(R.id.bt_schedule_close);
		TextView tvOsm = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_osm);
		MapView mapView = (MapView) dialog.findViewById(R.id.mv_dialog_schedule_map);

		// Basic map configuration
		mapView.setMultiTouchControls(true);
		IMapController mapController = mapView.getController();

		//Get info about the event
		SQLiteDatabase db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath(GM.DB.NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
		String lang = GM.getLang();
		Cursor cursor;
		if (schedule == GM.SCHEDULE.MARGOLARIAK){
			//cursor = db.rawQuery("SELECT festival_event_gm.id, title_" + lang + ", description_" + lang + ", place, route, start, end, name_" + lang + ", address_" + lang + ", lat, lon, host, sponsor FROM festival_event_gm, place WHERE place = place.id AND festival_event_gm.id = " + id + ";", null);
			cursor = db.rawQuery("SELECT id, title_" + lang + ", description_" + lang + ", place, route, start, end, host, sponsor FROM festival_event_gm WHERE id = " + id + ";", null);
		}
		else{
			//cursor = db.rawQuery("SELECT festival_event_city.id, title_" + lang + ", description_" + lang + ", place, route, start, end, name_" + lang + ", address_" + lang + ", lat, lon, host, sponsor FROM, sponsor festival_event_city, place WHERE place = place.id AND festival_event_city.id = " + id + ";", null);
			cursor = db.rawQuery("SELECT id, title_" + lang + ", description_" + lang + ", place, route, start, end, host, sponsor FROM festival_event_city WHERE id = " + id + ";", null);
		}
		if (cursor.getCount() > 0){
			cursor.moveToNext();

			String title = cursor.getString(1);
			String description = "";
			if (!cursor.isNull(2)){
				description = cursor.getString(2);
			}
			String strStart = cursor.getString(5);
			String strEnd = "";
			if (!cursor.isNull(6)){
				strEnd = cursor.getString(6);
			}
			int placeId = 0;
			if (!cursor.isNull(3)){
				placeId = cursor.getInt(3);
			}
			int routeId = 0;
			if (!cursor.isNull(4)){
				routeId = cursor.getInt(4);
			}
			int hostId = 0;
			if (!cursor.isNull(7)){
				hostId = cursor.getInt(7);
			}
			int sponsorId = 0;
			if (!cursor.isNull(8)){
				sponsorId = cursor.getInt(8);
			}

			//Set title
			tvTitle.setText(title);

			//Set description
			if (description.length() > 0) {
				tvDescription.setText(description);
			}
			else{
				tvDescription.setVisibility(View.GONE);
			}

			//Set host
			if (hostId != 0){
				Cursor hostCursor = db.rawQuery("SELECT name_" + lang + " FROM people WHERE id = " + hostId + ";", null);
				if (hostCursor.moveToNext()){
					tvHost.setVisibility(View.VISIBLE);
					tvHost.setText(String.format(getString(R.string.schedule_host), hostCursor.getString(0)));
				}
				hostCursor.close();
			}
			else{
				tvHost.setVisibility(View.GONE);
			}

			//Set sponsor
			if (sponsorId != 0){
				Cursor sponsorCursor = db.rawQuery("SELECT name_" + lang + " FROM people WHERE id = " + sponsorId + ";", null);
				if (sponsorCursor.moveToNext()){
					tvSponsor.setVisibility(View.VISIBLE);
					tvSponsor.setText(String.format(getString(R.string.schedule_sponsor), sponsorCursor.getString(0)));
				}
				sponsorCursor.close();
			}
			else{
					tvSponsor.setVisibility(View.GONE);
			}


			//Set date
			try{
				Date day = dateFormat.parse(strStart);
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
				Log.e("SCHEDULE_LAYOUT", "Error parsing date: " + ex.toString());
			}

			//Set time
			try{
				if (strEnd.length() == 0) {
					tvTime.setText(timeFormat.format(dateFormat.parse(strStart)));
				}
				else {
					String time = timeFormat.format(dateFormat.parse(strStart)) + " - " + timeFormat.format(dateFormat.parse(strEnd));
					tvTime.setText(time);
				}
			}
			catch (ParseException ex){
				Log.e("SCHEDULE_LAYOUT", "Error parsing time: " + ex.toString());
			}




			//Set the place
			if (routeId != 0){
				llPlace.setVisibility(View.GONE);
				Cursor routeCursor = db.rawQuery("SELECT id, c_lat, c_lon, zoom FROM route WHERE id = " + routeId + ";", null);
				if (routeCursor.moveToNext()) {

					// Read from route_point and draw the route on the map.
					Cursor pointCursor = db.rawQuery("SELECT lat_o, lon_o, lat_d, lon_d FROM route_point WHERE route = " + routeId + " ORDER BY part;", null);
					final ArrayList<GeoPoint> points = new ArrayList<>();
					GeoPoint lastPoint = new GeoPoint(0.0, 0.0);
					GeoPoint firstPoint = null;
					while (pointCursor.moveToNext()){
						points.add(new GeoPoint(pointCursor.getDouble(0), pointCursor.getDouble(1)));
						lastPoint = new GeoPoint(pointCursor.getDouble(2), pointCursor.getDouble(3));
						if (firstPoint == null){
							firstPoint = new GeoPoint(pointCursor.getDouble(0), pointCursor.getDouble(1));
						}
					}
					// For the last one, also take destination
					points.add(lastPoint);

					pointCursor.close();

					// Center map
					mapController.setCenter(firstPoint);
					mapController.setZoom(routeCursor.getInt(3));

					// Create path
					Polyline line = new Polyline();
					line.setPoints(points);
					line.setColor(getResources().getColor(R.color.map_route));
					line.setWidth(30.0f);
					mapView.getOverlays().add(line);

					// Set markers
					OverlayItem startOverlay = new OverlayItem(title, title, firstPoint);
					Drawable startMarker = this.getResources().getDrawable(R.drawable.pinpoint_start);
					startOverlay.setMarker(startMarker);
					OverlayItem endOverlay = new OverlayItem(title, title, lastPoint);
					Drawable endMarker = this.getResources().getDrawable(R.drawable.pinpoint_end);
					endOverlay.setMarker(endMarker);
					final ArrayList<OverlayItem> markers = new ArrayList<>();
					markers.add(startOverlay);
					markers.add(endOverlay);
                                        ItemizedIconOverlay markersOverlay = new ItemizedIconOverlay<>(markers,
					  new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
					  	public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
							return true;
						}
						public boolean onItemLongPress(final int index, final OverlayItem item) {
							return true;
						}
					  }, view.getContext());
					mapView.getOverlays().add(markersOverlay);

				}
				routeCursor.close();

			}
			else{
				Cursor placeCursor = db.rawQuery("SELECT name_" + lang + ", address_" + lang + ", lat, lon FROM place WHERE id = " + placeId + ";", null);
				if (placeCursor.moveToNext()){
					tvPlace.setText(placeCursor.getString(0));
					tvAddress.setText(placeCursor.getString(1));

					// Configure map marker
					mapController.setZoom(17);
					GeoPoint center = new GeoPoint(placeCursor.getDouble(2), placeCursor.getDouble(3));
					mapController.setCenter(center);
					OverlayItem locationOverlayItem = new OverlayItem(title, placeCursor.getString(0), center);
					Drawable locationMarker = this.getResources().getDrawable(R.drawable.pinpoint);
					locationOverlayItem.setMarker(locationMarker);
					final ArrayList<OverlayItem> items = new ArrayList<>();
					items.add(locationOverlayItem);
					ItemizedIconOverlay locationOverlay = new ItemizedIconOverlay<>(items,
							new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
								public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
									return true;
								}
								public boolean onItemLongPress(final int index, final OverlayItem item) {
									return true;
								}
							}, view.getContext());
					mapView.getOverlays().add(locationOverlay);
				}
				placeCursor.close();
			}

			//Set up OpenStreetMap attribution
			tvOsm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(GM.URL.OSM_COPYRIGHT));
					startActivity(i);
				}
			});


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
				}
			});

			//Show the dialog
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			try {
				//noinspection ConstantConditions
				lp.copyFrom(dialog.getWindow().getAttributes());
			}
			catch(NullPointerException e){
				Log.e("SCHEDULE_LAYOUT", "Error setting dialog parameters: " + e.toString());
			}
			lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
			lp.gravity = Gravity.CENTER;
			lp.dimAmount = 0.0f;
			dialog.getWindow().setAttributes(lp);

			//Show dialog
			dialog.show();

		}
	}


	/**
	 * Called when the activity is resumed.
	 * Lets the map handle this situation.
	 *
	 * @see android.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		Configuration.getInstance().load(view.getContext(), PreferenceManager.getDefaultSharedPreferences(view.getContext()));
		super.onResume();
	}

}
