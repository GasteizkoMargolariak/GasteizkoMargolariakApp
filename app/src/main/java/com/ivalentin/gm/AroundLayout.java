//TODO: Rework this file

package com.ivalentin.gm;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Fragment;
//import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Layout that show event close to the user in both time and space. 
 * It implements LocationListener so it can detect changes in the user location. 
 * It implements OnMapReadyCallback so a map can be shown when an item is clicked.
 * 
 * @see Fragment
 * @see LocationListener
 * @see OnMapReadyCallback
 * 
 * @author Iñigo Valentin
 *
 */
public class AroundLayout extends Fragment implements LocationListener, OnMapReadyCallback{

	//The location of the user
	private double[] coordinates = new double[2];
	
	//LocationManager to provide the location
	private LocationManager locationManager;
	
	//The location provider
	private String provider;
	
	//Layout where event rows will be added
	private LinearLayout list;
	
	//Layout to be shown when no GPS detected
	private LinearLayout noGps;
	
	//Layout to be shown when no events
	private LinearLayout noEvents;
	
	//Main View
	private View view;
	
	//Map stuff for the dialog
	private MapView mapView;
	private Bundle bund;
	private GoogleMap map;
	private LatLng location;
	private String markerName = "";

	/**
	 * Remove the location listener updates when Activity is paused.
	 * 
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		if (map != null)
			map.setMyLocationEnabled(false);
		if (mapView != null){
			mapView.onPause();
		}
		super.onPause();
		locationManager.removeUpdates(this);
	}
	
	/**
	 * Run when the fragment is inflated.
	 * Assigns views, gets the date and does the first call to the {@link @populate function}.
	 * 
	 * @param inflater A LayoutInflater to manage views
	 * @param container The container View
	 * @param savedInstanceState Bundle containing the state
	 * 
	 * @return The fragment view
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@SuppressLint("InflateParams") //Throws unknown error when done properly.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		//Load the layout
		view = inflater.inflate(R.layout.fragment_layout_around, null);		
		
		//Set the title
		((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_lablanca_around));
		
		//Get the location passed from MainActivity so we don't have to wait for it to be acquired.
		Bundle bundle = this.getArguments();
		coordinates[0] = bundle.getDouble("lat", 0);
		coordinates[1] = bundle.getDouble("lon", 0);
		
		//Assign parent layout
		list = (LinearLayout) view.findViewById(R.id.ll_around_list);
		noGps = (LinearLayout) view.findViewById(R.id.ll_around_no_gps);
		noEvents = (LinearLayout) view.findViewById(R.id.ll_around_no_events);
		
		//"Open Settings" button, shown when GPS is off.
		Button btSettings = (Button) view.findViewById(R.id.bt_around_no_gps);
		btSettings.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				view.getContext().startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
			}
		});
		
		//"View Schedule" button, shown when no events.
		Button btSchedule = (Button) view.findViewById(R.id.bt_around_no_events);
		btSchedule.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).loadSection(GM.SECTION_LABLANCA_SCHEDULE, false);
			}
			
		});
		
		//Set Location manager.
		locationManager = (LocationManager) view.getContext().getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, this);
		
		//Show and hide sections depending on the GPS status.
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			//Populate the activity list
			list.setVisibility(View.VISIBLE);
			noGps.setVisibility(View.GONE);
			populateAround(list);
		}
		else{
			list.setVisibility(View.GONE);
			noGps.setVisibility(View.VISIBLE);
		}
						
		//Return the fragment view
		return view;
	}
	
	/**
	 * Populates the list of activities around. 
	 * It uses the default layout as parent.
	 */
	public void populateAround(){
		LinearLayout list = (LinearLayout) this.getView().findViewById(R.id.ll_around_list);
		populateAround(list);
	}
	
	/**
	 * Populates the list of activities around.
	 * 
	 * @param list The layout where the event will be placed.
	 */
	@SuppressLint("InflateParams") //Views are added from a loop: I can't specify the parent when inflating.
	private void populateAround(LinearLayout list){
		
		//A list of events.
		ArrayList<Event> eventList = new ArrayList<Event>();
		Event event;
		
		//A layout to be populated with an event.
		LinearLayout entry;
		
		//Touchable layout of each entry
		LinearLayout llTouch;
		
		//An inflater
        LayoutInflater factory = LayoutInflater.from(view.getContext());
        
        //TextViews in each row
        TextView tvRowTitle, tvRowDescription, tvRowAddress, tvRowDistance, tvRowTime, tvRowId;
        
        //Icon next to the location text
        Drawable icon = getResources().getDrawable(R.drawable.pinpoint);
        icon.setBounds(0, 0, 80, 80);
				
		//Read from database
		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = db.rawQuery("SELECT schedule, gm, event.name, event.description, place.name, address, lat, lon, start, end, host, event.id FROM event, place WHERE schedule = 1 AND event.place = place.id;", null);
		
		//Make the list empty, in case we are not populating it for the first time.
        list.removeAllViews();
        
        //Elements to parse, format and calculate times.
        Calendar cal;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd-", Locale.US);
		Date date = new Date();
		Date startMinus30, endMinus15, startPlus30;
		
		//Decimal formatter to write text
		DecimalFormat df = new DecimalFormat("#.#");
		
		//Holds the distance between the user and the event
		double distance;
        
        
        //Loop thorough every returned row, creating an event for each suitable row.
        while (cursor.moveToNext()){
        	
        	//Operate with dates to see if the event close in time.       	
        	try{
				cal = Calendar.getInstance();
			    cal.setTime(dateFormat.parse(cursor.getString(8)));
			    cal.add(Calendar.MINUTE, -30);
			    startMinus30 = cal.getTime();
			    cal = Calendar.getInstance();
			    cal.setTime(dateFormat.parse(cursor.getString(8)));
			    cal.add(Calendar.MINUTE, 30);
			    startPlus30 = cal.getTime();
			    
			    //Events with end date
			    if (cursor.getString(9) != null){
			    	cal = Calendar.getInstance();
				    cal.setTime(dateFormat.parse(cursor.getString(9)));
				    cal.add(Calendar.MINUTE, -15);
				    endMinus15 = cal.getTime();
				    
				    //If in range
				    if (date.after(startMinus30) && date.before(endMinus15)){
				    	event = new Event(cursor.getInt(11), cursor.getString(2), cursor.getString(3), cursor.getInt(1), cursor.getInt(0), cursor.getString(4), cursor.getString(10), new double[] {cursor.getDouble(6), cursor.getDouble(7)}, cursor.getString(8), cursor.getString(9));
			        	eventList.add(event);
				    }
			    }
			    //Events without end time
			    else{
			    	if (date.after(startMinus30) && date.before(startPlus30)){
			    		event = new Event(cursor.getInt(11), cursor.getString(2), cursor.getString(3), cursor.getInt(1), cursor.getInt(0), cursor.getString(4), cursor.getString(10), new double[] {cursor.getDouble(6), cursor.getDouble(7)}, cursor.getString(8), cursor.getString(9));
			        	eventList.add(event);
			    	}
			    }
				
			}
			catch (ParseException e){
				Log.e("Error parsing date", e.toString());
			}
        }
        
        //Close the cursor and the database
        cursor.close();
        db.close();
        
        //Sort the event list.
        Collections.sort(eventList);
        
        //Loop events in the list.
        if (eventList.size() == 0){
        	//If no events around
        	list.setVisibility(View.GONE);
        	noEvents.setVisibility(View.VISIBLE);
        }
        else{
        	//If there are events being hold now or close in time.
        	list.setVisibility(View.VISIBLE);
        	noEvents.setVisibility(View.GONE);
        	
        	//Loop events
	        for(int i = 0; i < eventList.size(); i++){
	        	
	        	//Create a new row
	        	entry = (LinearLayout) factory.inflate(R.layout.row_around, null);
	        	
	        	//Set id
	        	tvRowId = (TextView) entry.findViewById(R.id.tv_row_around_id);
	        	tvRowId.setText(Integer.toString(eventList.get(i).getId()));
	        	
	        	//Set title
	        	tvRowTitle = (TextView) entry.findViewById(R.id.tv_row_around_title);
	        	tvRowTitle.setText(eventList.get(i).getName());
	        	
	        	//Set description
	        	tvRowDescription = (TextView) entry.findViewById(R.id.tv_row_around_description);
	        	tvRowDescription.setText(eventList.get(i).getDescription());
	        	
	        	//Set address
	        	tvRowAddress = (TextView) entry.findViewById(R.id.tv_row_around_address);
	        	tvRowAddress.setText(eventList.get(i).getPlace());
	        	
	        	//Set distance
	        	tvRowDistance = (TextView) entry.findViewById(R.id.tv_row_around_distance);
	        	distance = eventList.get(i).getDistance(coordinates);
	        	if (distance < 1000){
	        		tvRowDistance.setText(String.format(getResources().getString(R.string.around_distance), Math.round(distance), getResources().getString(R.string.meters), Math.round(distance * 0.012)));
	        	}
	        	else
	        		tvRowDistance.setText(String.format(getResources().getString(R.string.around_distance), df.format(distance / 1000), getResources().getString(R.string.kilometers), Math.round(distance * 0.012)));
	        	
	        	//Set time
	        	tvRowTime = (TextView) entry.findViewById(R.id.tv_row_around_time);
	        	Date tm;
				try {
					tm = eventList.get(i).getStart();
					if (dayFormat.format(tm).equals(dayFormat.format(date)))
		        		tvRowTime.setText(view.getContext().getString(R.string.today) + " " + timeFormat.format(tm));
		        	else
		        		tvRowTime.setText(view.getContext().getString(R.string.tomorrow) + " " + timeFormat.format(tm));
				}
				catch (Exception e) {
					Log.e("Around event date error", e.toString());
				}
	        	
				//Set touch event that will show a dialog.
				llTouch = (LinearLayout) entry.findViewById(R.id.ll_row_around_content);
				llTouch.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
		            	TextView tvId = (TextView) v.findViewById(R.id.tv_row_around_id);
		            	int id = Integer.parseInt(tvId.getText().toString());
		            	Log.e("TOUCH", "#" +id);
						showDialog(id);
					}
	        	});
				
	        	//Add the entry to the list.
	        	list.addView(entry);
			}
        }
	}
	
	/**
	 * Shows a dialog with info about an event.
	 * 
	 * @param id The id of the event
	 */
	private void showDialog(int id){
		
		//Create the dialog
		final Dialog dialog = new Dialog(getActivity());
		
		//Set window
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_around);
		
		//Date formatters
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd-", Locale.US);
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
 
		//Set the custom dialog components - text, image and button
		TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_dialog_around_title);
		TextView tvDescription = (TextView) dialog.findViewById(R.id.tv_dialog_around_description);
		TextView tvDate = (TextView) dialog.findViewById(R.id.tv_dialog_around_date);
		TextView tvTime = (TextView) dialog.findViewById(R.id.tv_dialog_around_time);
		TextView tvPlace = (TextView) dialog.findViewById(R.id.tv_dialog_around_place);
		TextView tvAddress = (TextView) dialog.findViewById(R.id.tv_dialog_around_address);
		Button btClose = (Button) dialog.findViewById(R.id.bt_around_close);
		
		//Get info about the event
		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = db.rawQuery("SELECT event.id, event.name, description, start, end, place.name, address, lat, lon FROM event, place WHERE place.id = event.place AND event.id = " + id + ";", null);
		if (cursor.getCount() > 0){
			cursor.moveToNext();
		
			//Set title
			tvTitle.setText(cursor.getString(1));
			markerName = cursor.getString(1);
			
			//Set description
			tvDescription.setText(cursor.getString(2));
			
			//Set date
			try{
				Date day = dateFormat.parse(cursor.getString(3));
				Date date = new Date();
				
				//If the event is today, show "Today" instead of the date.
				if (dayFormat.format(day).equals(dayFormat.format(date))){
					tvDate.setText(dialog.getContext().getString(R.string.today));
				}
				else{
					Calendar cal = Calendar.getInstance();
				    cal.setTime(date);
				    cal.add(Calendar.HOUR_OF_DAY, 24);
				    
				    //If the event is tomorrow, show "Tomorrow" instead of the date.
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
					tvTime.setText(timeFormat.format(dateFormat.parse(cursor.getString(3))) + " - " + timeFormat.format(timeFormat.parse(cursor.getString(4))));
			}
			catch (ParseException ex){
				Log.e("Error parsing time", ex.toString());
			}
			
			//Set place
			tvPlace.setText(cursor.getString(5));
			tvAddress.setText(cursor.getString(6));
			
			//Set up map
			location = new LatLng(Double.parseDouble(cursor.getString(7)), Double.parseDouble(cursor.getString(8)));
			mapView = (MapView) dialog.findViewById(R.id.mv_dialog_around_map);
			mapView.onCreate(bund);
			
			//Close db connection
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
		Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
		locationManager.requestLocationUpdates(provider, 5000, 10, this);
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
	
	/**
	 * Called when the user location changes. 
	 * Recalculates the list of events.
	 * 
	 * @param location The new location
	 * 
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	@Override
    public void onLocationChanged(Location location) {
    	double lat = location.getLatitude();
		double lng = location.getLongitude();
		coordinates[0] = lat;
		coordinates[1] = lng;
		//Check GPS status
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			//Populate the activity list
			list.setVisibility(View.VISIBLE);
			noGps.setVisibility(View.GONE);
			noEvents.setVisibility(View.GONE);
			populateAround(list);
		}
		else{
			list.setVisibility(View.GONE);
			noEvents.setVisibility(View.GONE);
			noGps.setVisibility(View.VISIBLE);
		}
    }
	
	
    /**
     * Called when a location provider is disabled. 
     * Recalculates the list of events.
     * 
     * @param provider The name of the provider
     * 
     * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
     */
    @Override
    public void onProviderDisabled(String provider) {
    	//Check GPS status
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			//Populate the activity list
			list.setVisibility(View.VISIBLE);
			noGps.setVisibility(View.GONE);
			noEvents.setVisibility(View.GONE);
			populateAround(list);
		}
		else{
			list.setVisibility(View.GONE);
			noEvents.setVisibility(View.GONE);
			noGps.setVisibility(View.VISIBLE);
		}
    }
    
    /**
     * Called when a location provider is enabled.
     * Recalculates the list of events.
     * 
     * @param provider The name of the provider
     * 
     * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled(String provider) {
    	//Check GPS status
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			//Populate the activity list
			list.setVisibility(View.VISIBLE);
			noGps.setVisibility(View.GONE);
			noEvents.setVisibility(View.GONE);
			populateAround(list);
		}
		else{
			list.setVisibility(View.GONE);
			noEvents.setVisibility(View.GONE);
			noGps.setVisibility(View.VISIBLE);
		}
    }
    
    /**
     * Called when the location provider changes it's state. 
     * Does nothing, all useful cases are handled outside here.
     * 
     * @param provider The name of the provider
     * @param status Status code of the provider
     * @param extras Extras passed 
     * 
     * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }
		
}

