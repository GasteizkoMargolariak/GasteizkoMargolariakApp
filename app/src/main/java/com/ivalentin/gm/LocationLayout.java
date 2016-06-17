package com.ivalentin.gm;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

//TODO: Try to fetch a new location report when created

/**
 * Section that shows a map with the location of Gasteizko Margolariak. 
 * If no recent report location, it will explain that and will show the 
 * next scheduled activity. 
 * 
 * @author Iñigo Valentin
 * 
 * @see Fragment
 * @see OnMapReadyCallback
 *
 */
public class LocationLayout extends Fragment implements OnMapReadyCallback{
	
	//The map view
	private MapView mapView;
	
	//The map
	private GoogleMap map;
	
	//Locations for the user and Gasteizko Margolariak
	private LatLng location, gmLocation;
	
	//The main View
	private View v;

	/**
	 * Run when the fragment is inflated.
	 * 
	 * @param inflater A LayoutInflater to manage views
	 * @param container The container View
	 * @param savedInstanceState Bundle containing the state
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		//Get the view
		v = inflater.inflate(R.layout.fragment_layout_location, container, false);
		
		//Set the title
		((MainActivity) getActivity()).setSectionTitle(v.getContext().getString(R.string.menu_lablanca_location));
		
		//LinearLayouts to be shown or hidden
		LinearLayout llReport = (LinearLayout) v.findViewById(R.id.ll_fragment_location_reported);
		LinearLayout llNoReport = (LinearLayout) v.findViewById(R.id.ll_fragment_location_no_reported);
		
		//Get preferences
		SharedPreferences settings = v.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
		
		//Get current date and time string formatters
		Calendar cal;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd-", Locale.US);
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
		Date date = new Date();
		
		//Variable to indicate if there is a report
		boolean reporting = false;
		
		//Button to show the GM schedule if no report
		Button btSchedule = (Button) v.findViewById(R.id.bt_location_no_report);
		btSchedule.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).loadSection(GM.SECTION_LABLANCA_GM_SCHEDULE, false);
			}
		});
		
		//Try to get last location time
		try{
			Date locationDate = dateFormat.parse(settings.getString(GM.PREF_GM_LOCATION, "1970-01-01 00:00:00"));
			cal = Calendar.getInstance();
		    cal.setTime(date);
		    cal.add(Calendar.MINUTE, -10);
		    
		    //If the last location report is recent
		    if (cal.getTime().before(locationDate))
		    	reporting = true;
		}
		catch (Exception ex){
			Log.e("Error parsing d ate", ex.toString());
		}
		
		//If there is a location report
		if (reporting){
			llNoReport.setVisibility(View.GONE);
			//Get location passed to the app
			Bundle bundle = this.getArguments();
			location = new LatLng(bundle.getDouble("lat", 0), bundle.getDouble("lon", 0));
			
			mapView = (MapView) v.findViewById(R.id.mapview);
			mapView.onCreate(savedInstanceState);
	 
			// Gets to GoogleMap from the MapView and does initialization stuff
			mapView.getMapAsync(this);
	    }
		
		//If there is no location report, try to show the next event where Margolariak will be
	    else{
	    	llReport.setVisibility(View.GONE);
	    	TextView tvLocation = (TextView) v.findViewById(R.id.tv_location_schedule);
	    	
	    	//Initialize the database
	    	SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);;
			Cursor cursor = db.rawQuery("SELECT schedule, gm, event.name, event.description, place.name, address, lat, lon, start, end, host FROM event, place WHERE gm = 1 AND event.place = place.id ORDER BY start;", null);
			Date startMinus24, startPlus30, start, end;
			boolean found = false;
			
			//Loop db entries until we find the next event
			while (cursor.moveToNext() && found == false){
				try{
					
					//Set limit dates to see if the event is suitable
					start = dateFormat.parse(cursor.getString(8));
					cal = Calendar.getInstance();
				    cal.setTime(dateFormat.parse(cursor.getString(8)));
				    cal.add(Calendar.HOUR_OF_DAY, -24);
				    startMinus24 = cal.getTime();
				    cal = Calendar.getInstance();
				    cal.setTime(dateFormat.parse(cursor.getString(8)));
				    cal.add(Calendar.MINUTE, 30);
				    startPlus30 = cal.getTime();
				    
				    //If end set and now between start and end
				    if (found == false && cursor.getString(9) != null){
				    	end = dateFormat.parse(cursor.getString(9));
				    	if (date.after(start) && date.before(end)){
				    		found = true;
				    		//Set text
				    		tvLocation.setText(String.format(v.getContext().getString(R.string.location_now), cursor.getString(4)));
				    	}
				    }
				    
				    //If end not set and now between start and start + 30 min
				    if (found == false && cursor.getString(9) == null){
				    	if (date.after(start) && date.before(startPlus30)){
				    		found = true;
				    		//Set text
				    		tvLocation.setText(String.format(v.getContext().getString(R.string.location_now), cursor.getString(4)));
				    	}
				    }
				    
				    //If now between start -24h and start
				    if (found == false && date.after(startMinus24) && date.before(start)){
				    	found = true;
				    	//Get date start
				    	Date dateS = dateFormat.parse((cursor.getString(8)));
				    	//Switch between today, tomorrow
				    	if (dayFormat.format(start).equals(dayFormat.format(date)))
				    		tvLocation.setText(String.format(v.getContext().getString(R.string.location_today), timeFormat.format(dateS), cursor.getString(4)));
				    	else
				    		tvLocation.setText(String.format(v.getContext().getString(R.string.location_tomorrow), timeFormat.format(dateS), cursor.getString(4)));
				    }
					
				}
				catch (Exception e){
					Log.e("Error parsing date", e.toString());
				}
				
				//If no events found, hide text.
				if (found == false){
					tvLocation.setVisibility(View.GONE);
				}
				else{
					tvLocation.setVisibility(View.VISIBLE);
				}
			}
			
			//Close the cursor
			cursor.close();
	    	
	    }
		
		//Return the view
		return v;
	}

	/**
	 * Called when the fragment is bought back to the foreground. 
	 * Ensures that the map is displayed then.
	 * 
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		if (mapView != null)
			mapView.onResume();
		super.onResume();
	}

	/**
	 * Called when the fragment is destroyed. 
	 * Ensures that the map is destroyed then.
	 * 
	 * @see android.support.v4.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mapView != null){
			mapView.onResume();
			mapView.onDestroy();
		}
	}

	/**
	 * Called in a situation of low memory. 
	 * It let's the mapView handle the situation.
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
	 * Called when the map is ready to be used. 
	 * Initializes it and sets the Gasteizko Margolariak marker.
	 * 
	 * @param googleMap the map
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
		SharedPreferences settings = v.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
		Double lat = Double.parseDouble(settings.getString(GM.PREF_GM_LATITUDE, ""));
		Double lon = Double.parseDouble(settings.getString(GM.PREF_GM_LONGITUDE, ""));
		gmLocation = new LatLng(lat, lon);
		MarkerOptions moGm = new MarkerOptions();
		moGm.title(v.getContext().getString(R.string.app_name));
		moGm.position(gmLocation);
		moGm.icon(BitmapDescriptorFactory.fromResource(R.drawable.pinpoint_gm));
		map.addMarker(moGm);
		
	}

}
