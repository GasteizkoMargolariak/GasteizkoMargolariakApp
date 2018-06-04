package com.ivalentin.margolariak;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.preference.PreferenceManager;
import android.net.Uri;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ArrayList;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;

/**
 * Fragment to display upcoming activities.
 * The id of the activity to display is passes in a bundle.
 * Everything is done on the onCreateView method.
 *
 * @author Iñigo Valentin
 *
 * @see Fragment
 *
 */
public class ActivityFutureLayout extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//Load the layout
		View view = inflater.inflate(R.layout.fragment_layout_activity_future, container, false);

		//Get bundled id
		Bundle bundle = this.getArguments();
		int id = bundle.getInt("activity", -1);
		if (id == -1){
			Log.e("ACTIVITY_FUTURE_LAYOUT", "No such activity: " + id);
			this.getActivity().onBackPressed();
		}

		//Get data from database
		SQLiteDatabase db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath(GM.DB.NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
		final Cursor cursor;
		String lang = GM.getLang();

		cursor = db.rawQuery("SELECT id, title_" + lang+ " AS title, text_" + lang + " AS text, date, city, price, permalink FROM activity WHERE id = " + id + ";", null);
		cursor.moveToFirst();

		//Get display elements
		ImageView images[] = new ImageView[5];
		images[0] = (ImageView) view.findViewById(R.id.iv_activity_future_0);
		images[1] = (ImageView) view.findViewById(R.id.iv_activity_future_1);
		images[2] = (ImageView) view.findViewById(R.id.iv_activity_future_2);
		images[3] = (ImageView) view.findViewById(R.id.iv_activity_future_3);
		images[4] = (ImageView) view.findViewById(R.id.iv_activity_future_4);

		TextView tvTitle = (TextView) view.findViewById(R.id.tv_activity_future_title);
		TextView tvDate = (TextView) view.findViewById(R.id.tv_activity_future_date);
		TextView tvCity = (TextView) view.findViewById(R.id.tv_activity_future_city);
		TextView tvPrice = (TextView) view.findViewById(R.id.tv_activity_future_price);
		WebView wvText = (WebView) view.findViewById(R.id.wv_activity_future_text);

		//Set fields
		tvTitle.setText(cursor.getString(1));
		((MainActivity) getActivity()).setSectionTitle(cursor.getString(1));
		((MainActivity) getActivity()).setShareLink(String.format(getString(R.string.share_with_title), cursor.getString(1)), GM.SHARE.ACTIVITIES + cursor.getString(6));
		wvText.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		wvText.loadDataWithBaseURL(null, cursor.getString(2), "text/html", "utf-8", null);
		//wvText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		tvDate.setText(GM.formatDate(cursor.getString(3) + " 00:00:00", lang, true, false, false));
		tvPrice.setText(String.format(getString(R.string.price), cursor.getInt(5)));
		tvCity.setText(cursor.getString(4));

		//Get images
		Cursor imageCursor = db.rawQuery("SELECT image, idx FROM activity_image WHERE activity = " + id + " ORDER BY idx LIMIT 5;", null);
		int i = 0;
		String image;
		while (imageCursor.moveToNext()) {
			image = imageCursor.getString(0);

			//Check if image exists
			File f;
			f = new File(this.getActivity().getFilesDir().toString() + "/img/actividades/preview/" + image);
			if (f.exists()){
				//If the image exists, set it.
				Bitmap myBitmap = BitmapFactory.decodeFile(this.getActivity().getFilesDir().toString() + "/img/actividades/preview/" + image);
				images[i].setImageBitmap(myBitmap);
			}
			else {
				//If not, create directories and download asynchronously
				File fpath;
				fpath = new File(this.getActivity().getFilesDir().toString() + "/img/actividades/preview/");
				//noinspection ResultOfMethodCallIgnored
				fpath.mkdirs();
				new DownloadImage(GM.API.SERVER + "/img/actividades/preview/" + image, this.getActivity().getFilesDir().toString() + "/img/actividades/preview/" + image, images[i], GM.IMG.SIZE.PREVIEW).execute();
			}
			images[i].setVisibility(View.VISIBLE);
			i ++;
		}
		imageCursor.close();
		cursor.close();

		//Get itinerary
		Cursor cursorItinerary = db.rawQuery("SELECT activity_itinerary.id AS id, activity_itinerary.name_" + lang + " AS name, description_" + lang + " AS description, start, place.name_" + lang + " AS placename, address_" + lang + " AS address, lat, lon, route FROM activity_itinerary, place WHERE activity_itinerary.place = place.id AND activity = " + id + ";", null);
		/*
		 * 0: id
		 * 1: name
		 * 2: description
		 * 3: start
		 * 4: place
		 * 5: address
		 * 6: lat
		 * 7: lon
		 * 8: route
		 */
		if (cursorItinerary.getCount() <= 0){
			LinearLayout sch = (LinearLayout) view.findViewById(R.id.ll_activity_future_schedule);
			sch.setVisibility(View.GONE);
		}
		else{
			LinearLayout list = (LinearLayout) view.findViewById(R.id.ll_activity_future_schedule_list);
			LinearLayout entry;
			LayoutInflater factory = LayoutInflater.from(getActivity());

			while (cursorItinerary.moveToNext()) {
				//Create a new row
				entry = (LinearLayout) factory.inflate(R.layout.row_activity_schedule, list, false);

				//Set time
				TextView tvSchTime = (TextView) entry.findViewById(R.id.tv_row_activity_itinerary_time);
				tvSchTime.setText(cursorItinerary.getString(3).substring(11, 16));

				//Set title
				TextView tvSchTitle = (TextView) entry.findViewById(R.id.tv_row_activity_itinerary_title);
				tvSchTitle.setText(cursorItinerary.getString(1));

				//Set Id
				TextView tvSchId = (TextView) entry.findViewById(R.id.tv_row_activity_itinerary_id);
				tvSchId.setText(cursorItinerary.getString(0));

				//Set description
				TextView tvSchDescription = (TextView) entry.findViewById(R.id.tv_row_activity_itinerary_description);
				if (cursorItinerary.getString(1).equals(cursorItinerary.getString(2))){
					tvSchDescription.setVisibility(View.GONE);
				}
				else {
					tvSchDescription.setText(cursorItinerary.getString(2));
				}

				//Set place
				TextView tvSchPlace = (TextView) entry.findViewById(R.id.tv_row_activity_itinerary_place);
				tvSchPlace.setText(cursorItinerary.getString(4));

				// Set route indicator
				if (cursorItinerary.getString(8) != null && cursorItinerary.getString(8).length() > 0){
					ImageView ivPinpoint = (ImageView) entry.findViewById(R.id.iv_row_activity_itinerary_pinpoint);
					ivPinpoint.setImageResource(R.drawable.pinpoint_route);
				}

				//Set address
				TextView tvSchAddress = (TextView) entry.findViewById(R.id.tv_row_activity_itinerary_address);
				if (cursorItinerary.getString(4).equals(cursorItinerary.getString(5))){
					tvSchAddress.setVisibility(View.GONE);
				}
				else {
					tvSchAddress.setText(cursorItinerary.getString(5));
				}

				//Set click listener
				entry.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						TextView tvId = (TextView) v.findViewById(R.id.tv_row_activity_itinerary_id);
						int id = Integer.parseInt(tvId.getText().toString());
						showDialog(id);
					}
				});

				list.addView(entry);
			}
		}


		cursorItinerary.close();
		db.close();

		return view;
	}

	/**
	 * Shows a dialog with info about the selected event.
	 *
	 * @param id The event id
	 */
	@SuppressWarnings("ConstantConditions")
	private void showDialog(final int id){

		//Create the dialog
		final Dialog dialog = new Dialog(getActivity());

		//Set up dialog window
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_schedule);

		//Date formatters
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

		//Set the custom dialog components - text, image, buttons, map
		LinearLayout llPlace = (LinearLayout) dialog.findViewById(R.id.ll_dialog_schedule_place);
		TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_title);
		TextView tvDescription = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_description);
		TextView tvHost = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_host);
		TextView tvDate = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_date);
		TextView tvTime = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_time);
		TextView tvPlace = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_place);
		TextView tvAddress = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_address);
		TextView tvOsm = (TextView) dialog.findViewById(R.id.tv_dialog_schedule_osm);
		Button btClose = (Button) dialog.findViewById(R.id.bt_schedule_close);
		MapView mapView = (MapView) dialog.findViewById(R.id.mv_dialog_schedule_map);

		// Basic map setup
		mapView.setMultiTouchControls(true);
		IMapController mapController = mapView.getController();

		//Get info about the event
		SQLiteDatabase db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath(GM.DB.NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
		String lang = GM.getLang();
		Cursor cursor = db.rawQuery("SELECT id, name_" + lang + ", description_" + lang + ", place, route, start, end FROM activity_itinerary WHERE id = " + id + " ORDER BY start;", null);
		if (cursor.getCount() > 0){

			cursor.moveToNext();

			// Get data
			String title = cursor.getString(1);
			String description = "";
			if (!cursor.isNull(2)){
				description = cursor.getString(2);
			}
			int placeId = 0;
			if (!cursor.isNull(3)){
				placeId = cursor.getInt(3);
			}
			int routeId = 0;
			if (!cursor.isNull(4)){
				routeId = cursor.getInt(4);
			}
			String strStart = cursor.getString(5);
			String strEnd = "";
			if (!cursor.isNull(6)){
				strEnd = cursor.getString(6);
			}

			//Set title
			tvTitle.setText(title);

			//Set description
			tvDescription.setText(description);

			//Hide the host and date fields
		   	tvHost.setVisibility(View.GONE);
			tvDate.setVisibility(View.GONE);

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
				Log.e("ACTIVITY_FUTURE_LAYOUT", "Error parsing time: " + ex.toString());
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
						if (firstPoint == null){
							firstPoint = new GeoPoint(pointCursor.getDouble(0), pointCursor.getDouble(1));
						}
						points.add(new GeoPoint(pointCursor.getDouble(0), pointCursor.getDouble(1)));
						lastPoint = new GeoPoint(pointCursor.getDouble(2), pointCursor.getDouble(3));
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
					line.setColor(getResources().getColor(R.color.map_route, null));
					line.setWidth(30.0f);
					mapView.getOverlays().add(line);

					// Set markers
					OverlayItem startOverlay = new OverlayItem(title, title, firstPoint);
					Drawable startMarker = this.getResources().getDrawable(R.drawable.pinpoint_start, null);
					startOverlay.setMarker(startMarker);
					OverlayItem endOverlay = new OverlayItem(title, title, lastPoint);
					Drawable endMarker = this.getResources().getDrawable(R.drawable.pinpoint_end, null);
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
					  }, getContext());
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
					Drawable locationMarker = this.getResources().getDrawable(R.drawable.pinpoint, null);
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
			btClose.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
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
		Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));
		super.onResume();
	}

}
