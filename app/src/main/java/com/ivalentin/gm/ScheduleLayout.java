package com.ivalentin.gm;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Fragment to be inflated showing the festivals schedule.
 * Contains a date selector and a ScrollView with all the activities for the day.
 * 
 * @author Inigo Valentin
 *
 */
public class ScheduleLayout extends Fragment implements OnMapReadyCallback{

	
	//Array with data about the days of the festival
	private String[][] days = new String[GM.TOTAL_DAYS][4];
	
	//Currently selected day
	private int selected;
	
	//Indicates official schedule
	private int schedule;
	
	//Arrow buttons
	private Button btL, btR;
	
	//TextViews in the date selector
	private TextView tvDayNumber, tvDayTitle, tvDayMonth;
	
	//Map stuff for the dialog
	private MapView mapView;
	private Bundle bund;
	private GoogleMap map;
	private LatLng location;
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
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@SuppressLint("InflateParams") //Throws unknown error when done properly.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		//Set bundle for the map
		bund = savedInstanceState;
		
		//Load the layout
		view = inflater.inflate(R.layout.fragment_layout_schedule, null);		
		
		//Get schedule type
		Bundle bundle = this.getArguments();
		schedule = bundle.getInt(GM.SCHEDULE, GM.SECTION_LABLANCA_SCHEDULE);
		
		//Set the title
		if (schedule == GM.SECTION_LABLANCA_SCHEDULE)
			((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_lablanca_schedule));
		else
			((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_lablanca_gm_schedule));
		
		
		//Populate days array
		days[GM.DAY_25] = new String[] {"2015-07-25", "25", getString(R.string.month_07), getString(R.string.day_title_25)};
		days[GM.DAY_4] = new String[] {"2015-08-04", "4", getString(R.string.month_08), getString(R.string.day_title_4)};
		days[GM.DAY_5] = new String[] {"2015-08-05", "5", getString(R.string.month_08), getString(R.string.day_title_5)};
		days[GM.DAY_6] = new String[] {"2015-08-06", "6", getString(R.string.month_08), getString(R.string.day_title_6)};
		days[GM.DAY_7] = new String[] {"2015-08-07", "7", getString(R.string.month_08), getString(R.string.day_title_7)};
		days[GM.DAY_8] = new String[] {"2015-08-08", "8", getString(R.string.month_08), getString(R.string.day_title_8)};
		days[GM.DAY_9] = new String[] {"2015-08-09", "9", getString(R.string.month_08), getString(R.string.day_title_9)};
		
		//Assign parent layout
		final LinearLayout list = (LinearLayout) view.findViewById(R.id.ll_schedule_list);
		
		//Assign textViews
		tvDayNumber = (TextView) view.findViewById(R.id.tv_schedule_day_number);
		tvDayMonth = (TextView) view.findViewById(R.id.tv_schedule_day_month);
		tvDayTitle = (TextView) view.findViewById(R.id.tv_schedule_day_title);
		
		//Assign buttons
		btL = (Button) view.findViewById(R.id.bt_schedule_l);
		btR = (Button) view.findViewById(R.id.bt_schedule_r);
		btL.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selected > GM.DAY_25){
					selected --;
					
					//If August 4 selected and GM schedule, skip
					if (selected == GM.DAY_4 && schedule != GM.SECTION_LABLANCA_SCHEDULE)
						selected --;
					
					String filter = ((EditText) view.findViewById(R.id.et_schedule_filter)).getText().toString();
					populateSchedule(selected, list, schedule, filter);
				}
			}
		});
		btR.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selected < GM.DAY_9){
					selected ++;
					
					//If August 4 selected and GM schedule, skip
					if (selected == GM.DAY_4 && schedule != GM.SECTION_LABLANCA_SCHEDULE)
						selected ++;
					
					String filter = ((EditText) view.findViewById(R.id.et_schedule_filter)).getText().toString();
					populateSchedule(selected, list, schedule, filter);
				}
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
	        	populateSchedule(selected, list, schedule, s.toString());
	          
	        }
		});
		
		//Get the date
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		Date date = new Date();
		String curDate = dateFormat.format(date);
		
		//Try to find out if the current date is before, after, or in the middle of the festivals.
		try {
			if (dateFormat.parse(curDate).before(dateFormat.parse(days[GM.DAY_25][0]))){
				selected = GM.DAY_25;
			}
			else if (dateFormat.parse(curDate).after(dateFormat.parse(days[GM.DAY_9][0]))){
				selected = GM.DAY_9;
			}
			else{
				for (int i = 0; i < GM.TOTAL_DAYS; i++){
					if (curDate.equals(days[i][0])){
						selected = i;
					}
				}
			}
			//If August 4 and Margolari schedule, move one day forward
			if (selected == GM.DAY_4 && schedule != GM.SECTION_LABLANCA_SCHEDULE){
				selected ++;
			}
		}
		catch (ParseException e) {
			selected = GM.DAY_25;
			Log.e("Error selecting date", e.toString());
		}
		
		//Populate the activity list
		populateSchedule(selected, list, schedule, "");
		
		//Return the fragment view
		return view;
	}
	
	/**
	 * Populates the list of activities with the ones n the selected day.
	 * 
	 * @param selected The day to show activities from
	 * @param list The layout where the items will be added
	 * @param schedule GM.SECTION_SCHEDULE if city schedule, GM.SECTION_GM_SCHEDULE if gm schedule
	 * @param f Search filter texts
	 */
	@SuppressLint("InflateParams") //Views are added from a loop: I can't specify the parent when inflating.
	private void populateSchedule(int selected, LinearLayout list, int schedule, String f){
		
		//A layout to be populated with one activity
		LinearLayout entry, content;
		
		//The filter
		String filter = f.toLowerCase(Locale.US);
		
		//An inflater
        LayoutInflater factory = LayoutInflater.from(getActivity());
        
        //Views in each row
        TextView tvRowTitle, tvRowTime, tvRowDesc, tvRowPlace, tvRowId;
        
        //Icon next to the location text
        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.pinpoint, null);
        //icon.setBounds(0, 0, 80, 80);
		
		//Set date selector texts
		tvDayNumber.setText(days[selected][1]);
		tvDayMonth.setText(days[selected][2]);
		tvDayTitle.setText(days[selected][3]);
		
		//Show or hide the arrow buttons
		switch (selected){
			case GM.DAY_25:
				btL.setVisibility(View.INVISIBLE);
				btR.setVisibility(View.VISIBLE);
				break;
			case GM.DAY_9:
				btL.setVisibility(View.VISIBLE);
				btR.setVisibility(View.INVISIBLE);
				break;
			default:
				btL.setVisibility(View.VISIBLE);
				btR.setVisibility(View.VISIBLE);				
		}
		
		//Read from database
		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor;
		
		//Elements to parse, format and operate with dates
		Calendar cal;
		Date maxDate, minDate;
		String maxDateStr, minDateStr;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		cal = Calendar.getInstance();
		
		//Get date of the selected daye
	    try {
			cal.setTime(dayFormat.parse(days[selected][0]));
		} catch (ParseException e) {
			Log.e("Error parsing date", e.toString());
		}
	    
	    //Set margins so the days goes from 6:00 in the morning to 5:59 the next day, instead of from 00:00 to 23:59
	    cal.add(Calendar.HOUR_OF_DAY, 6); 
	    minDate = cal.getTime();
	    
	    //If its the last day, include events of the next one
	    if (selected == GM.DAY_9)
	    	cal.add(Calendar.HOUR_OF_DAY, 36);
	    else
	    	cal.add(Calendar.HOUR_OF_DAY, 24);
	    maxDate = cal.getTime();
	    maxDateStr = dateFormat.format(maxDate);
	    minDateStr = dateFormat.format(minDate);
	    
	    //Get data from database
	    if (schedule == GM.SECTION_LABLANCA_GM_SCHEDULE)
	    	cursor = db.rawQuery("SELECT event.id, event.name, event.description, event.place, place.id, place.name, event.start FROM " + GM.DB_EVENT + ", " + GM.DB_PLACE + " WHERE " + GM.DB_EVENT_GM + " = 1 AND place.id = event.place AND start BETWEEN '" + minDateStr + "' AND '" + maxDateStr + "' AND (lower(event.name) LIKE '%" + filter + "%' OR lower(event.description) LIKE '%" + filter + "%' OR lower(place.name) LIKE '%" + filter + "%') ORDER BY " + GM.DB_EVENT_START + ";", null);
	    else
	    	cursor = db.rawQuery("SELECT event.id, event.name, event.description, event.place, place.id, place.name, event.start FROM " + GM.DB_EVENT + ", " + GM.DB_PLACE + " WHERE " + GM.DB_EVENT_SCHEDULE + " = 1 AND place.id = event.place AND start BETWEEN '" + minDateStr + "' AND '" + maxDateStr + "' AND (lower(event.name) LIKE '%" + filter + "%' OR lower(event.description) LIKE '%" + filter + "%' OR lower(place.name) LIKE '%" + filter + "%') ORDER BY " + GM.DB_EVENT_START + ";", null);
		
	    //Clear the list
		list.removeAllViews();
		
        //Loop
        while (cursor.moveToNext()){
        	
        	
        	//Create a new row
        	entry = (LinearLayout) factory.inflate(R.layout.row_schedule, null);
        	
        	//Set id        	
        	tvRowId = (TextView) entry.findViewById(R.id.tv_row_schedule_id);
        	tvRowId.setText(cursor.getString(0));
        	
        	//Set title
        	tvRowTitle = (TextView) entry.findViewById(R.id.tv_row_schedule_title);
        	tvRowTitle.setText(cursor.getString(1));
        	
        	//Set description
        	tvRowDesc = (TextView) entry.findViewById(R.id.tv_row_schedule_description);
        	tvRowDesc.setText(cursor.getString(2));
        	
        	//Set place
        	tvRowPlace = (TextView) entry.findViewById(R.id.tv_row_schedule_place);
        	tvRowPlace.setText(cursor.getString(5));
        	icon.setBounds(0, 0, (int) (tvRowPlace.getTextSize() * 1.5), (int) (tvRowPlace.getTextSize() * 1.5));
        	tvRowPlace.setCompoundDrawables(icon, null, null, null);
        	
        	//Set time
        	tvRowTime = (TextView) entry.findViewById(R.id.tv_row_schedule_time);
        	String tm = cursor.getString(6).substring(cursor.getString(6).length() - 8, cursor.getString(6).length() - 3);
        	tvRowTime.setText(tm);
        	
        	//Set onClick event
        	content = (LinearLayout) entry.findViewById(R.id.ll_row_schedule_content);
        	content.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
	            	TextView tvName = (TextView) v.findViewById(R.id.tv_row_schedule_id);
	            	int id = Integer.parseInt(tvName.getText().toString());
					showDialog(id);
				}
        	});
        	
        	//Add to the list
        	list.addView(entry);
        }
        
        //Invalidate the list so it is redrawn
        list.invalidate();
        
        //Close cursor and database
        cursor.close();
        db.close();
	}
	
	
	/**
	 * Shows a dialog with info about the selected event.
	 * 
	 * @param id The event id
	 */
	private void showDialog(final int id){
		
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
		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = db.rawQuery("SELECT event.id, event.name, description, start, end, place.name, address, lat, lon, host FROM event, place WHERE place.id = event.place AND event.id = " + id + ";", null);
		if (cursor.getCount() > 0){
			cursor.moveToNext();
		
			//Set title
			tvTitle.setText(cursor.getString(1));
			markerName = cursor.getString(1);
			
			//Set description
			tvDescription.setText(cursor.getString(2));
			
			//Set host
			if (cursor.getString(9) != null){
				Cursor hostCursor = db.rawQuery("SELECT name FROM people WHERE id = " + cursor.getString(9) + ";", null);
				if (hostCursor.moveToNext()){
					tvHost.setVisibility(View.VISIBLE);
					tvHost.setText(String.format(getString(R.string.schedule_host), hostCursor.getString(0)));
				}
			}
			else{
				tvHost.setVisibility(View.GONE);
			}				
			
			//Set date
			try{
				Date day = dateFormat.parse(cursor.getString(3));
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
				    	SimpleDateFormat printFormat = new SimpleDateFormat("dd MMMM", Locale.US);
				    	tvDate.setText(printFormat.format(day));
				    }
				}
			}
			catch (Exception ex){
				Log.e("Error parsing date", ex.toString());
			}
			
			//Set time
			try{
				if (cursor.getString(4) == null)
					tvTime.setText(timeFormat.format(dateFormat.parse(cursor.getString(3))));
				else
					tvTime.setText(timeFormat.format(dateFormat.parse(cursor.getString(3))) + " - " + timeFormat.format(dateFormat.parse(cursor.getString(4))));
			}
			catch (ParseException ex){
				Log.e("Error parsing time", ex.toString());
			}
			
			//Set the place
			tvPlace.setText(cursor.getString(5));
			tvAddress.setText(cursor.getString(6));
			
			//Set up map
			location = new LatLng(Double.parseDouble(cursor.getString(7)), Double.parseDouble(cursor.getString(8)));
			mapView = (MapView) dialog.findViewById(R.id.mv_dialog_schedule_map);
			mapView.onCreate(bund);
			
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
					if (map != null)
						map.setMyLocationEnabled(false);
					if (mapView != null){
    					mapView.onResume();
    					mapView.onDestroy();
    				}					
				}
			});
			
			//Show the dialog
			WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
			lp.dimAmount = 0.0f;
			dialog.show();
			
			//Start the map
			startMap();
			mapView.onResume();
			dialog.setOnShowListener(new OnShowListener(){

				@Override
				public void onShow(DialogInterface dialog) {
					// Gets to GoogleMap from the MapView and does initialization stuff
					startMap();
					
				}
			});		
			
		}
	}
	
	/**
	 * Starts the map in the dialog.
	 */
	private void startMap(){
		mapView.getMapAsync(this);
	}
	
	/**
	 * Called when the fragment is brought back into the foreground. 
	 * Resumes the map and the location manager.
	 * 
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		if (mapView != null)
			mapView.onResume();
		if (map != null)
			map.setMyLocationEnabled(true);
		super.onResume();
	}

	/**
	 * Called when the fragment is destroyed. 
	 * Finishes the map. 
	 * 
	 * @see android.support.v4.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (map != null)
			map.setMyLocationEnabled(false);
		if (mapView != null){
			mapView.onResume();
			mapView.onDestroy();
		}
	}
	
	/**
	 * Called when the fragment is paused. 
	 * Finishes the map. 
	 * 
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		super.onDestroy();
		if (map != null)
			map.setMyLocationEnabled(false);
		if (mapView != null){
			mapView.onPause();
		}
	}

	/**
	 * Called in a situation of low memory.
	 * Lets the map handle this situation.
	 * 
	 * @see android.support.v4.app.Fragment#onLowMemory()
	 */
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (mapView != null){
			mapView.onResume();
			mapView.onLowMemory();
		}
	}

	/**
	 * Called when the map is ready to be displayed. 
	 * Sets the map options and a marker for the map.
	 * 
	 * @param googleMap The map to be shown
	 * 
	 * @see com.google.android.gms.maps.OnMapReadyCallback#onMapReady(com.google.android.gms.maps.GoogleMap)
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.map = googleMap;
		map.setMyLocationEnabled(true);
		
		map.getUiSettings().setMyLocationButtonEnabled(false);
		map.setMyLocationEnabled(true);
		// Needs to call MapsInitializer before doing any CameraUpdateFactory calls
		try {
			MapsInitializer.initialize(this.getActivity());
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 14);
			map.animateCamera(cameraUpdate);
		} catch (Exception e) {
			Log.e("Error initializing maps", e.toString());
		}
		//Set GM marker
		MarkerOptions mo = new MarkerOptions();
		mo.title(markerName);
		mo.position(location);
		map.addMarker(mo);
		
	}
		
}
