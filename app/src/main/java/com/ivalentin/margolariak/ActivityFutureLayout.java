package com.ivalentin.margolariak;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
public class ActivityFutureLayout extends Fragment implements OnMapReadyCallback {

	//Map stuff for the dialog
	private MapView mapView;
	private GoogleMap map;
	private LatLng location;
	private String markerName = "";
	private final Bundle bund = null;
	private View v;

	@SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Load the layout
        View view = inflater.inflate(R.layout.fragment_layout_activity_future, null);

        //Get bundled id
        Bundle bundle = this.getArguments();
        int id = bundle.getInt("activity", -1);
        if (id == -1){
            Log.e("Activity error", "No such activity: " + id);
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
		if (Build.VERSION.SDK_INT >= 19) {
			wvText.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
		else {
			wvText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
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
        Cursor cursorItinerary = db.rawQuery("SELECT activity_itinerary.id, activity_itinerary.name_" + lang + " AS name, description_" + lang + " AS description, start, place.name_" + lang + " AS placename, address_" + lang + " AS address, lat, lon FROM activity_itinerary, place WHERE activity_itinerary.place = place.id AND activity = " + id + ";", null);
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
                entry = (LinearLayout) factory.inflate(R.layout.row_activity_schedule, null);

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
                if (!cursorItinerary.getString(1).equals(cursorItinerary.getString(2))){
                    tvSchDescription.setVisibility(View.GONE);
                }
                else {
                    tvSchTitle.setText(cursorItinerary.getString(2));
                }

                //Set place
                TextView tvSchPlace = (TextView) entry.findViewById(R.id.tv_row_activity_itinerary_place);
                tvSchPlace.setText(cursorItinerary.getString(4));

                //Set address
                TextView tvSchAddress = (TextView) entry.findViewById(R.id.tv_row_activity_itinerary_address);
                if (!cursorItinerary.getString(4).equals(cursorItinerary.getString(5))){
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

		v = view;
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
        Cursor cursor = db.rawQuery("SELECT activity_itinerary.id, activity_itinerary.name_" + lang + ", description_" + lang + ", place, start, end, place.name_" + lang + ", address_" + lang + ", lat, lon FROM activity_itinerary, place WHERE place = place.id AND activity_itinerary.id = " + id + ";", null);
        if (cursor.getCount() > 0){
            cursor.moveToNext();

            //Set title
            tvTitle.setText(cursor.getString(1));
            markerName = cursor.getString(1);

            //Set description
            tvDescription.setText(cursor.getString(2));

            //Set host field hidden
           	tvHost.setVisibility(View.GONE);

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
                if (cursor.getString(5) == null || cursor.getString(5).length() == 0) {
                    tvTime.setText(timeFormat.format(dateFormat.parse(cursor.getString(4))));
                }
                else {
                    String time = timeFormat.format(dateFormat.parse(cursor.getString(4))) + " - " + timeFormat.format(dateFormat.parse(cursor.getString(5)));
                    tvTime.setText(time);
                }
            }
            catch (ParseException ex){
                Log.e("Error parsing time", ex.toString());
            }

            //Set the place
            tvPlace.setText(cursor.getString(6));
            tvAddress.setText(cursor.getString(7));

            //Set up map
            location = new LatLng(Double.parseDouble(cursor.getString(8)), Double.parseDouble(cursor.getString(9)));
            mapView = (MapView) dialog.findViewById(R.id.mv_dialog_schedule_map);
            mapView.onCreate(bund);

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

            //Actions to take when the dialog is cancelled
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (map != null) {
						if (!(ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
							map.setMyLocationEnabled(false);
						}
					}
                    if (mapView != null){
                        mapView.onResume();
                        mapView.onDestroy();
                    }
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
            startMap();
            mapView.onResume();
            dialog.setOnShowListener(new DialogInterface.OnShowListener(){

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
	 * @see android.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		if (mapView != null)
			mapView.onResume();
		if (map != null) {
			if (!(ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
				map.setMyLocationEnabled(true);
			}
		}
		super.onResume();
	}

	/**
	 * Called when the fragment is destroyed.
	 * Finishes the map.
	 *
	 * @see android.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (map != null) {
			if (!(ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
				map.setMyLocationEnabled(false);
			}
		}
		if (mapView != null){
			mapView.onResume();
			mapView.onDestroy();
		}
	}

	/**
	 * Called when the fragment is paused.
	 * Finishes the map.
	 *
	 * @see android.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		super.onDestroy();
		if (map != null) {
			if (!(ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
				map.setMyLocationEnabled(false);
			}
		}
		if (mapView != null){
			mapView.onPause();
		}
	}

	/**
	 * Called in a situation of low memory.
	 * Lets the map handle this situation.
	 *
	 * @see android.app.Fragment#onLowMemory()
	 */
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (mapView != null){
			if (!(ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
				mapView.onResume();
			}
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

		map.getUiSettings().setMyLocationButtonEnabled(false);
		if (!(ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
			map.setMyLocationEnabled(true);
		}
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