package com.ivalentin.margolariak;

import java.util.Locale;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.drawable.Drawable;

import org.osmdroid.api.IMapController;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * Section that shows a map with the location of Gasteizko Margolariak.
 *
 * @author Iñigo Valentin
 *
 * @see Fragment
 *
 */
public class LocationLayout extends Fragment implements LocationListener {

	//The map view stuff
	private MapView mapView;
	private Drawable locationMarker;

	// The location manager
	private LocationManager locationManager;

	//Locations for the user and Gasteizko Margolariak
	private GeoPoint gmLocation;

	//The main View
	private View v;

	// Handler to refresh the location.
	private final Handler markerHandler = new Handler();
	private final Runnable resetMarker = new Runnable() {
		@Override
		public void run() {

			if (mapView != null) {
				if (refreshLocation()) {
					
					//mapController.setCenter(self.gmLocation);
					OverlayItem locationOverlayItem = new OverlayItem("Gasteizko Margolariak", "", gmLocation);
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
							}, getContext());
					mapView.getOverlays().add(locationOverlay);
				}
			}
			markerHandler.postDelayed(resetMarker, GM.LOCATION.INTERVAL);
		}
	};


	/**
	 * Run when the fragment is inflated.
	 *
	 * @param inflater A LayoutInflater to manage views
	 * @param container The container View
	 * @param savedInstanceState Bundle containing the state
	 *
	 * @see Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

		//Get the view
		v = inflater.inflate(R.layout.fragment_layout_location, container, false);

		//Set up
		locationManager = (LocationManager) v.getContext().getSystemService(Context.LOCATION_SERVICE);
		if (checkLocationPermission()){
			locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GM.LOCATION.ACCURACY.TIME, GM.LOCATION.ACCURACY.SPACE, this);
		}

		//Set the title
		((MainActivity) getActivity()).setSectionTitle(v.getContext().getString(R.string.menu_location));

		//Refresh the location.
		refreshLocation();

		//Periodically check map
		markerHandler.post(resetMarker);

		// Set up the map
		this.mapView = (MapView) v.findViewById(R.id.mapview);
		this.mapView.setMultiTouchControls(true);
		IMapController mapController = mapView.getController();
		mapController.setZoom(17);
		mapController.setCenter(gmLocation);
		this.locationMarker = this.getResources().getDrawable(R.drawable.pinpoint, null);

		//Return the view
		return v;
	}


	/**
	 * Called when the fragment is bought back to the foreground. 
	 * Ensures that the map is displayed then, and activates the location manager.
	 *
	 * @see android.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		if (checkLocationPermission()){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GM.LOCATION.ACCURACY.TIME, GM.LOCATION.ACCURACY.SPACE, this);
		}
		super.onResume();
		markerHandler.post(resetMarker);
	}


	/**
	 * Called when the fragment is paused.
	 * Stops the location manager
	 * @see android.app.Fragment#onPause()
	 */
	@Override
	public void onPause(){
		if (checkLocationPermission()) {
			locationManager.removeUpdates(this);
		}
		super.onPause();

		markerHandler.removeCallbacks(resetMarker);
	}


	/**
	 * Called when the fragment is destroyed. 
	 * Ensures that the map is destroyed then.
	 * 
	 * @see Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (checkLocationPermission()) {
			locationManager.removeUpdates(this);
		}

		markerHandler.removeCallbacks(resetMarker);
	}


	/**
	 * Refresh the location of Gasteizko Margolariak, by updating the class attribute gmLocation.
	 *
	 * Uses the most recent data in the database, if they are not older than 10 minutes.
	 *
	 * @return true if there is a location from less than 10 minutes ago, 0 otherwise.
	 */
	private boolean refreshLocation(){

		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB.NAME, Activity.MODE_PRIVATE, null);
		Cursor cursor;
		cursor = db.rawQuery("SELECT lat, lon FROM location WHERE dtime >= Datetime('now', '-10 minutes') ORDER BY dtime DESC LIMIT 1;", null);
		if (cursor.getCount() == 0){
			cursor.close();
			db.close();
			return false;
		}
		else {
			cursor.moveToFirst();
			gmLocation = new GeoPoint(cursor.getDouble(0), cursor.getDouble(1));
			cursor.close();
			db.close();
			return true;
		}
	}


	/**
	 * Recalculates the distance between the user and Gasteizko Margolariak.
	 * Prints the distance to a TextView.
	 *
	 * @param userLocation The new user location.
	 *
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	private void calculateDistance(Location userLocation){
		TextView text = (TextView) v.findViewById(R.id.tv_location_distance);
		if (userLocation != null){
			try {
				Double distance = Distance.calculateDistance(userLocation.getLatitude(), userLocation.getLongitude(), gmLocation.getLatitude(), gmLocation.getLongitude());
				if (distance <= 2) {
					distance = 1000 * distance;
					text.setText(String.format(getString(R.string.home_section_location_text_short), distance.intValue(), (int) (0.012 * distance.intValue())));
				} else {
					text.setText(String.format(getString(R.string.home_section_location_text_long), String.format(Locale.US, "%.02f", distance)));
				}
			}
			catch(Exception ex){
				Log.e("LOCATION_LAYOUT", "Distance error: " + ex.toString());
				text.setText("");
			}
		}
	}


	/**
	 * Called when the user location changes.
	 * Recalculates the list of around events and calls updateLocation() to
	 * update the distance in the location section.
	 *
	 * @param location The new location
	 *
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	@Override
	public void onLocationChanged(Location location) {
		calculateDistance(location);
	}


	/**
	 * Checks app permission to access the user location.
	 * @return true if the permission has been granted, false otherwise.
	 *
	 * @see android.app.Fragment#onResume()
	 */
	private boolean checkLocationPermission(){
		return getContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && getContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}


	/**
	 * Called when the location provider changes it's state.
	 * Does nothing.
	 *
	 * @param provider The name of the provider
	 * @param status Status code of the provider
	 * @param extras Extras passed
	 *
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 */
	@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
	}


	/**
	 * Called when a location provider is enabled.
	 * Does nothing.
	 *
	 * @param provider The name of the provider
	 *
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
		public void onProviderEnabled(String provider) {
	}


	/**
	 * Called when a location provider is disabled.
	 * Does nothing.
	 *
	 * @param provider The name of the provider
	 *
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {
	}
}
