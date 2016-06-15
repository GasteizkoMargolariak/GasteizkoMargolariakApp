package com.ivalentin.gm;

import java.io.File;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Section that will be seen when the app is started. 
 * Contains info from almost every other section.
 * 
 * @author Iñigo Valentin
 * 
 * @see Fragment
 *
 */
public class HomeLayout extends Fragment implements LocationListener, OnMapReadyCallback{

	//The location manager
	LocationManager locationManager;
	Location listener;
	
	//The location of the user
	private double[] coordinates = new double[2];
	
	//Layouts for each section
	private LinearLayout llLocation, llLablanca, llGmschedule, llCityschedule, llAround, llGallery, llSocial;

	//Language
	private String currLang;
	
	//Map stuff for the dialog
	private MapView mapView;
	private Bundle bund;
	private GoogleMap map;
	private LatLng location;
	private String markerName = "";
	
	/**
	 * Run when the fragment is inflated.
	 * Assigns views, gets the date and does the first call to the {//@link populate function}.
	 * 
	 * @param inflater A LayoutInflater to handle the views
	 * @param container The parent View
	 * //@param sanvedInstanceState Bundle with the saved state
	 * 
	 * @return The fragment view
	 * 
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@SuppressLint("InflateParams") //Throws unknown error when done properly.
	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

		//Load the layout.
		View view = inflater.inflate(R.layout.fragment_layout_home, null);

		//Get language
		currLang = Locale.getDefault().getDisplayLanguage();
		if (!currLang.equals("es") && !currLang.equals("eu")){
			currLang = "en";
		}
				
		//Set Location manager
		locationManager = (LocationManager) view.getContext().getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, this);
		
		//Set the title
		((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_home));
	    
		//Get the location passed from MainActivity so we dont have to wait for it to be aquired.
		Bundle bundle = this.getArguments();
		coordinates[0] = bundle.getDouble("lat", 0);
		coordinates[1] = bundle.getDouble("lon", 0);
		
		//Get current date
		Calendar cal;
		Date maxDate;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd-", Locale.US);
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
		Date date = new Date();
		
		//Assign the layouts
		llLocation = (LinearLayout) view.findViewById(R.id.ll_home_section_location);
		llLablanca = (LinearLayout) view.findViewById(R.id.ll_home_section_lablanca);
		llGmschedule = (LinearLayout) view.findViewById(R.id.ll_home_section_gmschedule);
		llCityschedule = (LinearLayout) view.findViewById(R.id.ll_home_section_cityschedule);
		llAround = (LinearLayout) view.findViewById(R.id.ll_home_section_around);


		llGallery = (LinearLayout) view.findViewById(R.id.ll_home_section_gallery);
		llSocial = (LinearLayout) view.findViewById(R.id.ll_home_section_social);

		//Set onClick events for links
		//TODO: Dont do it like this, do it in separate functions
		llLocation.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).loadSection(GM.SECTION_LOCATION, false);
			}
		});
		llLablanca.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).loadSection(GM.SECTION_LABLANCA, false);
			}
		});
		llGmschedule.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).loadSection(GM.SECTION_LABLANCA_GM_SCHEDULE, false);
			}
		});
		llCityschedule.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).loadSection(GM.SECTION_LABLANCA_SCHEDULE, false);
			}
		});
		llAround.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).loadSection(GM.SECTION_LABLANCA_AROUND, false);
			}
		});


		//Set up the blog section
		setUpBlog(view);

		//Set up activities sections
		if (setUpFutureActivities(view) == 0){
			setUpPastActivities(view);
		}

		//Set up the gallery section
		setUpGallery(view);


		//TODO: Add listeners for social

	    //Return the view itself.
		return view;
	}

	/**
	 * Populates the Gallery section of the home screen.
	 * Loads four photos.
	 * Tapping them takes to the respective album, tapping anywhere else, to the gallery section.
	 *
	 * @return The number of entries shown
	 */
	private int setUpGallery(View view) {
		int counter = 0;

		//Set onClick listener of section
		LinearLayout llGallery = (LinearLayout) view.findViewById(R.id.ll_home_section_gallery);
		//Set click listener
		llGallery.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).loadSection(GM.SECTION_GALLERY, false);
			}
		});

		//Assign views
		ImageView ivPhoto[] = new ImageView[4];
		ivPhoto[0] = (ImageView) view.findViewById(R.id.iv_home_section_gallery_0);
		ivPhoto[1] = (ImageView) view.findViewById(R.id.iv_home_section_gallery_1);
		ivPhoto[2] = (ImageView) view.findViewById(R.id.iv_home_section_gallery_2);
		ivPhoto[3] = (ImageView) view.findViewById(R.id.iv_home_section_gallery_3);

		TextView tvHidden[] = new TextView[4];
		tvHidden[0] = (TextView) view.findViewById(R.id.tv_home_section_gallery_hidden_0);
		tvHidden[1] = (TextView) view.findViewById(R.id.tv_home_section_gallery_hidden_1);
		tvHidden[2] = (TextView) view.findViewById(R.id.tv_home_section_gallery_hidden_2);
		tvHidden[3] = (TextView) view.findViewById(R.id.tv_home_section_gallery_hidden_3);

		//Get data from the database of the future activities
		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = db.rawQuery("SELECT id, album, file, uploaded FROM photo, photo_album WHERE id = photo ORDER BY uploaded DESC LIMIT 4;", null);

		while (cursor.moveToNext()){

			//Set id
			ivPhoto[counter].setTag(R.string.key_0, cursor.getString(0));
			ivPhoto[counter].setTag(R.string.key_1, cursor.getString(1));

			//Set image
			String image = cursor.getString(2);
			//Check if image exists
			File f;
			f = new File(this.getContext().getFilesDir().toString() + "/img/galeria/preview/" + image);
			if (f.exists()) {
				//If the image exists, set it.
				Bitmap myBitmap = BitmapFactory.decodeFile(this.getContext().getFilesDir().toString() + "/img/galeria/preview/" + image);
				ivPhoto[counter].setImageBitmap(myBitmap);
			} else {
				//If not, create directories and download asynchronously
				File fpath;
				fpath = new File(this.getContext().getFilesDir().toString() + "/img/galeria/preview/");
				fpath.mkdirs();
				new DownloadImage(GM.SERVER + "/img/galeria/preview/" + image, this.getContext().getFilesDir().toString() + "/img/galeria/preview/" + image, ivPhoto[counter]).execute();
			}

			//Set listeners for images
			ivPhoto[counter].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Fragment fragment = new AlbumLayout();
					Bundle bundle = new Bundle();

					int photoId = Integer.parseInt((String) v.getTag(R.string.key_0));
					int albumId = Integer.parseInt((String) v.getTag(R.string.key_1));

					bundle.putInt("album", albumId);
					bundle.putInt("photo", photoId);

					fragment.setArguments(bundle);

					FragmentManager fm = HomeLayout.this.getActivity().getSupportFragmentManager();
					FragmentTransaction ft = fm.beginTransaction();

					ft.replace(R.id.activity_main_content_fragment, fragment);
					ft.addToBackStack("album_" + albumId);
					ft.commit();
				}
			});

			counter ++;
		}

		//Close db
		cursor.close();
		db.close();

		return counter;
	}

	/**
	 * Populates the Past Activities section of the home screen.
	 * Makes the section visible and adds two rows with the two last activities.
	 * Tapping them takes to the post, tapping anywhere else, to the blog section.
	 *
	 * @return The number of entries shown
	 */
	private int setUpPastActivities(View view){
		int counter = 0;

		//Get data from the database of the future activities
		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = db.rawQuery("SELECT id, date, city, title_" + currLang + " AS title, text_" + currLang + " AS text, price, after_" + currLang + " AS after FROM activity WHERE date < date('now') ORDER BY date LIMIT 2;", null);

		//Show section
		LinearLayout llActivitiesPast = (LinearLayout) view.findViewById(R.id.ll_home_section_activities_past);
		llActivitiesPast.setVisibility(View.VISIBLE);

		//Set click listener
		llActivitiesPast.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).loadSection(GM.SECTION_ACTIVITIES, false);
			}
		});

		LinearLayout llList = (LinearLayout) view.findViewById(R.id.ll_home_section_activities_past_content);
		LinearLayout entry;

		//An inflater
		LayoutInflater factory = LayoutInflater.from(getActivity());

		//Print rows
		while (cursor.moveToNext()) {
			//Create a new row
			entry = (LinearLayout) factory.inflate(R.layout.row_home_activities, null);

			//Set margins
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(10, 10, 10, 25);
			entry.setLayoutParams(layoutParams);

			//Set title
			TextView tvTitle = (TextView) entry.findViewById(R.id.tv_row_home_activity_title);
			tvTitle.setText(cursor.getString(3));

			//Set city
			TextView tvCity = (TextView) entry.findViewById(R.id.tv_row_home_activity_city);
			tvCity.setText(cursor.getString(2));

			//Hide price
			TextView tvPrice = (TextView) entry.findViewById(R.id.tv_row_home_activity_price);
			tvPrice.setVisibility(View.GONE);
			//tvPrice.setText(cursor.getString(5) + "€");

			//Set text
			String text;
			if (cursor.getString(6).length() < 1) {
				text = Html.fromHtml(cursor.getString(4)).toString();
			}
			else{
				text = Html.fromHtml(cursor.getString(6)).toString();
			}
			if (text.length() > 100) {
				text = text.substring(0, 100) + "...";
			}
			TextView tvText = (TextView) entry.findViewById(R.id.tv_row_home_activity_text);
			tvText.setText(text);

			//Set date
			TextView tvDate = (TextView) entry.findViewById(R.id.tv_row_home_activity_date);
			tvDate.setText(GM.formatDate(cursor.getString(1) + " 00:00:00", currLang, false));

			//Set hidden id
			TextView tvId = (TextView) entry.findViewById(R.id.tv_row_home_activity_hidden);
			tvId.setText(cursor.getString(0));

			//Get image
			ImageView iv = (ImageView) entry.findViewById(R.id.iv_row_home_activity);
			Cursor cursorImage = db.rawQuery("SELECT image FROM activity_image WHERE activity = " + cursor.getString(0) + " ORDER BY idx LIMIT 1;", null);
			if (cursorImage.getCount() > 0) {
				cursorImage.moveToFirst();
				String image = cursorImage.getString(0);

				//Check if image exists
				File f;
				f = new File(this.getContext().getFilesDir().toString() + "/img/actividades/miniature/" + image);
				if (f.exists()) {
					//If the image exists, set it.
					Bitmap myBitmap = BitmapFactory.decodeFile(this.getContext().getFilesDir().toString() + "/img/actividades/miniature/" + image);
					iv.setImageBitmap(myBitmap);
				} else {
					//If not, create directories and download asynchronously
					File fpath;
					fpath = new File(this.getContext().getFilesDir().toString() + "/img/actividades/miniature/");
					fpath.mkdirs();
					new DownloadImage(GM.SERVER + "/img/actividades/miniature/" + image, this.getContext().getFilesDir().toString() + "/img/actividades/miniature/" + image, iv).execute();
				}
			}

			//Set onCLickListener
			entry.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
				//Toast.makeText(getActivity(), "OPENING POST", Toast.LENGTH_LONG).show();
				Fragment fragment = new ActivityPastLayout();
				Bundle bundle = new Bundle();
				//Pass post id
				int id = Integer.parseInt(((TextView) v.findViewById(R.id.tv_row_home_activity_hidden)).getText().toString());
				bundle.putInt("activity", id);
				fragment.setArguments(bundle);

				FragmentManager fm = HomeLayout.this.getActivity().getSupportFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();

				ft.replace(R.id.activity_main_content_fragment, fragment);
				ft.addToBackStack("activity_" + id);
				ft.commit();
				}
			});

			//Add to the list
			llList.addView(entry);
			counter++;
		}

		//Close db
		cursor.close();
		db.close();

		return counter;
	}

	/**
	 * Populates the Future activities section of the home screen.
	 * If there are future activities, makes the section visible.
	 * Adds two rows with the two next activities.
	 * Tapping them takes to the post, tapping anywhere else, to the blog section.
	 *
	 * @return The number of entries shown
	 */
	private int setUpFutureActivities(View view){
		int counter = 0;

		LinearLayout llList = (LinearLayout) view.findViewById(R.id.ll_home_section_activities_future_content);
		LinearLayout entry;

		//An inflater
		LayoutInflater factory = LayoutInflater.from(getActivity());

		//Get data from the database of the future activities
		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = db.rawQuery("SELECT id, date, city, title_" + currLang+ " AS title, text_" + currLang + " AS text, price FROM activity WHERE date >= date('now') ORDER BY date DESC LIMIT 2;", null);

		//If there are future activities...
		if (cursor.getCount() > 0) {

			//Show section
			LinearLayout llActivitiesFuture = (LinearLayout) view.findViewById(R.id.ll_home_section_activities_future);
			llActivitiesFuture.setVisibility(View.VISIBLE);

			//Set click listener
			llActivitiesFuture.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					((MainActivity) getActivity()).loadSection(GM.SECTION_ACTIVITIES, false);
				}
			});

			//Print rows
			while (cursor.moveToNext()) {
				//Create a new row
				entry = (LinearLayout) factory.inflate(R.layout.row_home_activities, null);

				//Set margins
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(10, 10, 10, 25);
				entry.setLayoutParams(layoutParams);

				//Set title
				TextView tvTitle = (TextView) entry.findViewById(R.id.tv_row_home_activity_title);
				tvTitle.setText(cursor.getString(3));

				//Set city
				TextView tvCity = (TextView) entry.findViewById(R.id.tv_row_home_activity_city);
				tvCity.setText(cursor.getString(2));

				//Set price
				TextView tvPrice = (TextView) entry.findViewById(R.id.tv_row_home_activity_price);
				tvPrice.setText(cursor.getString(5) + "€");

				//Set text
				String text = Html.fromHtml(cursor.getString(4)).toString();
				if (text.length() > 100) {
					text = text.substring(0, 100) + "...";
				}
				TextView tvText = (TextView) entry.findViewById(R.id.tv_row_home_activity_text);
				tvText.setText(text);

				//Set date
				TextView tvDate = (TextView) entry.findViewById(R.id.tv_row_home_activity_date);
				tvDate.setText(GM.formatDate(cursor.getString(1) + " 00:00:00", currLang, false));

				//Set hidden id
				TextView tvId = (TextView) entry.findViewById(R.id.tv_row_home_activity_hidden);
				tvId.setText(cursor.getString(0));

				//Get image
				ImageView iv = (ImageView) entry.findViewById(R.id.iv_row_home_activity);
				Cursor cursorImage = db.rawQuery("SELECT image FROM activity_image WHERE activity = " + cursor.getString(0) + " ORDER BY idx LIMIT 1;", null);
				if (cursorImage.getCount() > 0) {
					cursorImage.moveToFirst();
					String image = cursorImage.getString(0);

					//Check if image exists
					File f;
					f = new File(this.getContext().getFilesDir().toString() + "/img/actividades/miniature/" + image);
					if (f.exists()) {
						//If the image exists, set it.
						Bitmap myBitmap = BitmapFactory.decodeFile(this.getContext().getFilesDir().toString() + "/img/actividades/miniature/" + image);
						iv.setImageBitmap(myBitmap);
					} else {
						//If not, create directories and download asynchronously
						File fpath;
						fpath = new File(this.getContext().getFilesDir().toString() + "/img/actividades/miniature/");
						fpath.mkdirs();
						new DownloadImage(GM.SERVER + "/img/actividades/miniature/" + image, this.getContext().getFilesDir().toString() + "/img/actividades/miniature/" + image, iv).execute();
					}
				}

				//Set onCLickListener
				entry.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						//Toast.makeText(getActivity(), "OPENING POST", Toast.LENGTH_LONG).show();
						Fragment fragment = new ActivityFutureLayout();
						Bundle bundle = new Bundle();
						//Pass post id
						int id = Integer.parseInt(((TextView) v.findViewById(R.id.tv_row_activity_future_hidden)).getText().toString());
						bundle.putInt("activity", id);
						fragment.setArguments(bundle);

						FragmentManager fm = HomeLayout.this.getActivity().getSupportFragmentManager();
						FragmentTransaction ft = fm.beginTransaction();

						ft.replace(R.id.activity_main_content_fragment, fragment);
						ft.addToBackStack("activity_" + id);
						ft.commit();
					}
				});

				//Add to the list
				llList.addView(entry);
				counter++;
			}
		}

		//Close db
		cursor.close();
		db.close();

		return counter;
	}

	/**
	 * Populates the Blog section of the home screen.
	 * Adds two rows with the two latest posts. Tapping them takes to the post, tapping anywhere else, to the blog section.
	 *
	 * @return The number of entries shown
	 */
	private int setUpBlog(View view){

		int counter = 0;

		LinearLayout llBlog = (LinearLayout) view.findViewById(R.id.ll_home_section_blog);
		llBlog.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).loadSection(GM.SECTION_BLOG, false);
			}
		});

		//Assign elements
		LinearLayout llList = (LinearLayout) view.findViewById(R.id.ll_home_section_blog_content);
		LinearLayout entry;

		//An inflater
		LayoutInflater factory = LayoutInflater.from(getActivity());

		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		final Cursor cursor;

		//Get data from the database
		cursor = db.rawQuery("SELECT id, title_" + currLang+ " AS title, text_" + currLang + " AS text, dtime FROM post ORDER BY dtime DESC LIMIT 2;", null);

		//Loop
		while (cursor.moveToNext()){

			//Create a new row
			entry = (LinearLayout) factory.inflate(R.layout.row_home_blog, null);

			//Set margins
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(10, 10, 10, 25);
			entry.setLayoutParams(layoutParams);

			//Set title
			TextView tvTitle = (TextView) entry.findViewById(R.id.tv_row_home_blog_title);
			tvTitle.setText(cursor.getString(1));

			//Set text
			String text = Html.fromHtml(cursor.getString(2)).toString();
			if (text.length() > 100){
				text = text.substring(0, 100) + "...";
			}
			TextView tvText = (TextView) entry.findViewById(R.id.tv_row_home_blog_text);
			tvText.setText(text);

			//Set date
			TextView tvDate = (TextView) entry.findViewById(R.id.tv_row_home_blog_date);
			tvDate.setText(GM.formatDate(cursor.getString(3), currLang, false));

			//Set hidden id
			TextView tvId = (TextView) entry.findViewById(R.id.tv_row_home_blog_hidden);
			tvId.setText(cursor.getString(0));

			//Get image
			ImageView iv = (ImageView) entry.findViewById(R.id.iv_row_home_blog_image);
			Cursor cursorImage = db.rawQuery("SELECT image FROM post_image WHERE post = " + cursor.getString(0) +" ORDER BY idx LIMIT 1;", null);
			if (cursorImage.getCount() > 0){
				cursorImage.moveToFirst();
				String image = cursorImage.getString(0);

				//Check if image exists
				File f;
				f = new File(this.getContext().getFilesDir().toString() + "/img/blog/miniature/" + image);
				if (f.exists()){
					//If the image exists, set it.
					Bitmap myBitmap = BitmapFactory.decodeFile(this.getContext().getFilesDir().toString() + "/img/blog/miniature/" + image);
					iv.setImageBitmap(myBitmap);
				}
				else {
					//If not, create directories and download asynchronously
					File fpath;
					fpath = new File(this.getContext().getFilesDir().toString() + "/img/blog/miniature/");
					fpath.mkdirs();
					new DownloadImage(GM.SERVER + "/img/blog/miniature/" + image, this.getContext().getFilesDir().toString() + "/img/blog/miniature/" + image, iv).execute();
				}
			}

			//Set onCLickListener
			entry.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
				Fragment fragment = new PostLayout();
				Bundle bundle = new Bundle();
				//Pass post id
				int id = Integer.parseInt(((TextView) v.findViewById(R.id.tv_row_home_blog_hidden)).getText().toString());
				bundle.putInt("post", id);
				fragment.setArguments(bundle);

				FragmentManager fm = HomeLayout.this.getActivity().getSupportFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();

				ft.replace(R.id.activity_main_content_fragment, fragment);
				ft.addToBackStack("post_" + id);
				ft.commit();
				}
			});

			//Add to the list
			llList.addView(entry);

			counter ++;
		}

		//Close database
		cursor.close();
		db.close();

		return counter;
	}

	/**
	 * Calculates the total prize in the prices dialog and shows the total.
	 * 
	 * @param v A view to be able to get a context.
	 * 
	 * @return The total price
	 */
	private int calculatePrice(Dialog v){
		
		//Locate views
		CheckBox[] cbDayName = new CheckBox[6];
		cbDayName[0] = (CheckBox) v.findViewById(R.id.cb_dialog_prices_0);
		cbDayName[1] = (CheckBox) v.findViewById(R.id.cb_dialog_prices_1);
		cbDayName[2] = (CheckBox) v.findViewById(R.id.cb_dialog_prices_2);
		cbDayName[3] = (CheckBox) v.findViewById(R.id.cb_dialog_prices_3);
		cbDayName[4] = (CheckBox) v.findViewById(R.id.cb_dialog_prices_4);
		cbDayName[5] = (CheckBox) v.findViewById(R.id.cb_dialog_prices_5);
		TextView tvTotal = (TextView) v.findViewById(R.id.tv_dialog_prices_total);
		
		//Open database
		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = db.rawQuery("SELECT price FROM day ORDER BY id;", null);
		
		//Loop simultaneously the db entries and the checkboxes
		ArrayList<Integer> price = new ArrayList<Integer>();
		int i = 0;
		int total = 0;
		int selected = 0;
		while (cursor.moveToNext() && i < 6){
			if (cbDayName[i].isChecked()){
				price.add(cursor.getInt(0));
				selected ++;
				total = total + cursor.getInt(0);
			}
			i ++;
		}
		
		//Get offers
		cursor = db.rawQuery("SELECT price, days FROM offer ORDER BY id;", null);
		boolean offerApplied = false;
		while (cursor.moveToNext()){
			
			//If selected days equal the days in the ofer, fix the price
			if (cursor.getInt(1) == selected){
				total = cursor.getInt(0);
				offerApplied = true;
			}
		}
		
		//Offer + single days
		if (offerApplied == false){
			Collections.sort(price);
			Cursor closestOffer = db.rawQuery("SELECT price, days FROM offer WHERE days < " + Integer.toString(selected) + " ORDER BY days DESC LIMIT 1;", null);
			if (closestOffer.getCount() == 1){
				closestOffer.moveToNext();
				total = closestOffer.getInt(0);
				int difference = selected - closestOffer.getInt(1);
				i = 0;
				while (i < difference && price.size() > i){
					total = total + price.get(i);
					i ++;
				}
			}
		}
		
		//Set text
		tvTotal.setText(v.getContext().getResources().getString(R.string.prices_total) + " " + total + v.getContext().getResources().getString(R.string.eur));
		
		//Return the total price
		return total;
	}

	/**
	 * Starts the map in the dialogs.
	 */
	public void startMap(){
		mapView.getMapAsync(this);
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
		}
		catch (Exception e) {
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
	 * Recalculates the list of around events and calls updateLocation() to 
	 * update the distance in the location section.
	 * 
	 * @param location The new location
	 * 
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 */
	@Override
	public void onLocationChanged(Location location) {
		coordinates[0] = location.getLatitude();
		coordinates[1] = location.getLongitude();
		//updateLocation();
		//populateAround();
		
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
	
	/**
	 * Called when the fragment is paused. 
	 * Stops the location manager
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause(){
		locationManager.removeUpdates(this);
		if (map != null)
			map.setMyLocationEnabled(false);
		if (mapView != null){
			mapView.onPause();
		}
		super.onPause();
	}
	
	/**
	 * Called when the fragment is brought back into the foreground. 
	 * Resumes the map and the location manager.
	 * 
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume(){
		if (mapView != null)
			mapView.onResume();
		if (map != null)
			map.setMyLocationEnabled(true);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, this);
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
}
