package com.ivalentin.margolariak;

import java.util.Locale;

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
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
public class LocationLayout extends Fragment implements OnMapReadyCallback, LocationListener {

	//The map view
	private MapView mapView;
	private MarkerOptions moGm;

	private LocationManager locationManager;

	//Locations for the user and Gasteizko Margolariak
	private LatLng gmLocation;

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

		//Set up
		locationManager = (LocationManager) v.getContext().getSystemService(Context.LOCATION_SERVICE);
		if (!(ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GM.LOCATION.ACCURACY.TIME, GM.LOCATION.ACCURACY.SPACE, this);
		}

		//Set the title
		((MainActivity) getActivity()).setSectionTitle(v.getContext().getString(R.string.menu_location));

		mapView = (MapView) v.findViewById(R.id.mapview);
		mapView.onCreate(savedInstanceState);

		// Gets to GoogleMap from the MapView and does initialization stuff
		mapView.getMapAsync(this);

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
		if (mapView != null) {
			mapView.onResume();
		}
		if (!(ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GM.LOCATION.ACCURACY.TIME, GM.LOCATION.ACCURACY.SPACE, this);
		}
		super.onResume();
	}

	/**
	 * Called when the fragment is paused.
	 * Stops the location manager
	 * @see android.app.Fragment#onPause()
	 */
	@Override
	public void onPause(){
		if (!(ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
			locationManager.removeUpdates(this);
		}
		super.onPause();
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
		if (!(ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
			locationManager.removeUpdates(this);
		}
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

		if (!(ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
			googleMap.setMyLocationEnabled(true);
		}
		
		googleMap.getUiSettings().setMyLocationButtonEnabled(false);
		// Needs to call MapsInitializer before doing any CameraUpdateFactory calls
		try {
			MapsInitializer.initialize(this.getActivity());
		} catch (Exception e) {
			Log.e("Error initializing maps", e.toString());
		}
		
		//Set GM marker
		//TODO: Use db
		SharedPreferences preferences = v.getContext().getSharedPreferences(GM.PREFERENCES.PREFERNCES, Context.MODE_PRIVATE);
		Double lat = 0.0; //Double.longBitsToDouble(preferences.getLong(GM.PREF_GM_LATITUDE, 0));
		Double lon = 0.0; //Double.longBitsToDouble(preferences.getLong(GM.PREF_GM_LONGITUDE, 0));
		gmLocation = new LatLng(lat, lon);
		moGm = new MarkerOptions();
		moGm.title(v.getContext().getString(R.string.app_name));
		moGm.position(gmLocation);
		moGm.icon(BitmapDescriptorFactory.fromResource(R.drawable.pinpoint_gm));
		googleMap.addMarker(moGm);
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 14.0f));

		//Start device location requests
		if (!(ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
			locationManager.requestLocationUpdates(locationManager.getBestProvider(new Criteria(), true), GM.LOCATION.ACCURACY.TIME, GM.LOCATION.ACCURACY.SPACE, this);
			onLocationChanged(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
		}
	}

	private void calCulateDistance(Location userLocation){
		TextView text = (TextView) v.findViewById(R.id.tv_location_distance);
		try {
			Double distance = Distance.calculateDistance(userLocation.getLatitude(), userLocation.getLongitude(), gmLocation.latitude, gmLocation.longitude);
			if (distance <= 2) {
				distance = 1000 * distance;
				text.setText(String.format(getString(R.string.home_section_location_text_short), distance.intValue(), (int) (0.012 * distance.intValue())));
			} else {
				text.setText(String.format(getString(R.string.home_section_location_text_long), String.format(Locale.US, "%.02f", distance)));
			}
		}
		catch(Exception ex){
			Log.e("Distance error", ex.toString());
			text.setText("");
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
		//TODO: Read again the GM Location.
		//TODO From database
		/*SharedPreferences preferences = v.getContext().getSharedPreferences(GM.PREFERENCES.PREFERNCES, Context.MODE_PRIVATE);
		if (!preferences.getString(GM.PREF_GM_LOCATION, GM.DEFAULT_PREF_GM_LOCATION).equals(GM.DEFAULT_PREF_GM_LOCATION)) {

			Double lat = Double.longBitsToDouble(preferences.getLong(GM.PREF_GM_LATITUDE, 0));
			Double lon = Double.longBitsToDouble(preferences.getLong(GM.PREF_GM_LONGITUDE, 0));
			gmLocation = new LatLng(lat, lon);
						calCulateDistance(location);
			if (moGm != null){
				moGm.visible(true);
			}
		}*/
	}

	/**
	 * Called when the location provider changes it's state.
	 * Recalculates the list of events in the around section.
	 *
	 * @param provider The name of the provider
	 * @param status Status code of the provider
	 * @param extras Extras passed
	 *
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		//populateAround();
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
		//populateAround();
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
		//populateAround();
	}



}
