package com.ivalentin.margolariak;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.Fragment;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Section that will be seen when the app is started. 
 * Contains info from almost every other section.
 *
 * @author Iñigo Valentin
 *
 * @see Fragment
 *
 */
public class HomeLayout extends Fragment implements LocationListener {

	//The location manager
	private LocationManager locationManager;

	private View view;

	/**
	 * Run when the fragment is inflated.
	 * Assigns views, gets the date and does the first call to the {//@link populate function}.
	 *
	 * @param inflater A LayoutInflater to handle the views
	 * @param container The parent View
	 * @param savedInstanceState Bundle with the saved state
	 *
	 * @return The fragment view
	 *
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@SuppressLint("InflateParams") //Throws unknown error when done properly.
	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

		//Load the layout.
		view = inflater.inflate(R.layout.fragment_layout_home, null);

		//Variable to know if I need a location manager
		boolean requestLocation = false;

		//Request location permissions if not set
		if (ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GM.PERMISSION_LOCATION);
		}

		//Set Location manager
		//TODO: Only do this if location is required
		locationManager = (LocationManager) view.getContext().getSystemService(Context.LOCATION_SERVICE);
		if ((ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) == false) {
			locationManager.requestLocationUpdates(locationManager.getBestProvider(new Criteria(), true), GM.LOCATION_ACCURACY_TIME, GM.LOCATION_ACCURACY_SPACE, this);
			onLocationChanged(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
		}

		//Set the title
		((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_home));

		//TODO: This methods is empty
		setUpLablanca(view);

		//Set up the blog section
		setUpBlog(view);

		//Set up activities sections
		if (setUpFutureActivities(view) == 0) {
			setUpPastActivities(view);
		}

		//Set up the gallery section
		setUpGallery(view);

		//Asynchronously set up location section
		//This will trigger onLocationChanged, that will trigger setUpLocation
		new HomeSectionLocation(view, this.getActivity()).execute();

		//Set up schedule sections
		setUpSchedule(1, view);
		setUpSchedule(0, view);

		//Setup the social section
		setUpSocial(view);

		//Return the view itself.
		return view;
	}

	/**
	 * Populates the schedule sections of the home screen.
	 *
	 * @return The number of entries shown.
	 */
	@SuppressLint("InflateParams")
	private int setUpSchedule(int gm, View view) {
		int count = 0;

		SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
		if (preferences.getInt(GM.PREF_DB_FESTIVALS, 0) == 0) {
			return count;
		}

		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		Calendar calendar = Calendar.getInstance();
		Calendar calendarEnd;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		String nowString = dateFormat.format(calendar.getTime());
		String endString;
		String query;

		//An inflater
		LayoutInflater factory = LayoutInflater.from(getActivity());

		//Views in each row
		TextView tvRowTitle, tvRowTime, tvRowPlace, tvRowId;

		//Assign layouts
		LinearLayout section, listNow, listNext, now, next, entry;
		if (gm == 1) {
			section = (LinearLayout) view.findViewById(R.id.ll_home_section_gmschedule);
			listNow = (LinearLayout) view.findViewById(R.id.ll_home_section_gmschedule_now_list);
			listNext = (LinearLayout) view.findViewById(R.id.ll_home_section_gmschedule_later_list);
			now = (LinearLayout) view.findViewById(R.id.ll_home_section_gmschedule_now_content);
			next = (LinearLayout) view.findViewById(R.id.ll_home_section_gmschedule_later_content);

		} else {
			section = (LinearLayout) view.findViewById(R.id.ll_home_section_cityschedule);
			listNow = (LinearLayout) view.findViewById(R.id.ll_home_section_cityschedule_now_list);
			listNext = (LinearLayout) view.findViewById(R.id.ll_home_section_cityschedule_later_list);
			now = (LinearLayout) view.findViewById(R.id.ll_home_section_cityschedule_now_content);
			next = (LinearLayout) view.findViewById(R.id.ll_home_section_cityschedule_later_content);
		}

		//Set query for current events
		String lang = GM.getLang();
		calendarEnd = calendar;
		calendarEnd.add(Calendar.MINUTE, -40);
		endString = dateFormat.format(calendarEnd.getTime());
		query = "SELECT festival_event.id, title_" + lang + ", description_" + lang + ", place, start, end, name_" + lang + ", address_" + lang + ", lat, lon FROM festival_event, place WHERE place = place.id AND ((start <= '" + nowString + "' AND end > '" + nowString + "') OR (start > '" + endString + "' AND start < '" + nowString + "')) AND gm = " + gm + " ORDER BY start LIMIT 2";

		//Execute query and loop
		Cursor cursorNow = db.rawQuery(query, null);
		if (cursorNow.getCount() == 0) {
			now.setVisibility(View.GONE);
		}
		while (cursorNow.moveToNext()) {
			count++;

			entry = (LinearLayout) factory.inflate(R.layout.row_home_schedule, null);

			//Set id
			tvRowId = (TextView) entry.findViewById(R.id.tv_row_home_schedule_id);
			tvRowId.setText(cursorNow.getString(0));

			//Set title
			tvRowTitle = (TextView) entry.findViewById(R.id.tv_row_home_schedule_title);
			tvRowTitle.setText(cursorNow.getString(1));

			//Set place
			tvRowPlace = (TextView) entry.findViewById(R.id.tv_row_home_schedule_place);
			tvRowPlace.setText(cursorNow.getString(6));

			//Set icon
			if (gm == GM.SECTION_LABLANCA_GM_SCHEDULE) {
				ImageView pinPoint = (ImageView) entry.findViewById(R.id.iv_row_home_schedule_pinpoint);
				pinPoint.setImageResource(getResources().getIdentifier("com.ivalentin.gm:drawable/pinpoint_gm", null, null));
			}

			//Set time
			tvRowTime = (TextView) entry.findViewById(R.id.tv_row_home_schedule_time);
			String tm = cursorNow.getString(4).substring(cursorNow.getString(4).length() - 8, cursorNow.getString(4).length() - 3);
			tvRowTime.setText(tm);

			//Add the view
			listNow.addView(entry);
		}
		cursorNow.close();

		//Prepare query for upcaming events
		calendarEnd = calendar;
		calendarEnd.add(Calendar.MINUTE, 320);
		endString = dateFormat.format(calendarEnd.getTime());
		query = "SELECT festival_event.id, title_" + lang + ", description_" + lang + ", place, start, end, name_" + lang + ", address_" + lang + ", lat, lon FROM festival_event, place WHERE place = place.id AND start > '" + nowString + "' AND start < '" + endString + "' AND gm = " + gm + " ORDER BY start LIMIT 2";

		//Execute query and loop
		Cursor cursorNext = db.rawQuery(query, null);
		if (cursorNext.getCount() == 0) {
			next.setVisibility(View.GONE);
		}
		while (cursorNext.moveToNext()) {
			count++;

			entry = (LinearLayout) factory.inflate(R.layout.row_home_schedule, null);

			//Set id
			tvRowId = (TextView) entry.findViewById(R.id.tv_row_home_schedule_id);
			tvRowId.setText(cursorNext.getString(0));

			//Set title
			tvRowTitle = (TextView) entry.findViewById(R.id.tv_row_home_schedule_title);
			tvRowTitle.setText(cursorNext.getString(1));

			//Set place
			tvRowPlace = (TextView) entry.findViewById(R.id.tv_row_home_schedule_place);
			tvRowPlace.setText(cursorNext.getString(6));

			//Set icon
			if (gm == GM.SECTION_LABLANCA_GM_SCHEDULE) {
				ImageView pinPoint = (ImageView) entry.findViewById(R.id.iv_row_home_schedule_pinpoint);
				pinPoint.setImageResource(getResources().getIdentifier("com.ivalentin.gm:drawable/pinpoint_gm", null, null));
			}


			//Set time
			tvRowTime = (TextView) entry.findViewById(R.id.tv_row_home_schedule_time);
			String tm = cursorNext.getString(4).substring(cursorNext.getString(4).length() - 8, cursorNext.getString(4).length() - 3);
			tvRowTime.setText(tm);

			//Add the view
			listNext.addView(entry);
		}
		cursorNext.close();


		if (count > 0) {
			section.setVisibility(View.VISIBLE);
			if (gm == 1) {
				section.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						((MainActivity) getActivity()).loadSection(GM.SECTION_LABLANCA_GM_SCHEDULE, false);
					}
				});

			} else {
				section.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						((MainActivity) getActivity()).loadSection(GM.SECTION_LABLANCA_SCHEDULE, false);
					}
				});
			}
		}

		db.close();
		return count;
	}

	/**
	 * Populates the Social section of the home screen.
	 */
	private void setUpSocial(View view) {
		ImageView phone = (ImageView) view.findViewById(R.id.iv_home_section_social_phone);
		ImageView mail = (ImageView) view.findViewById(R.id.iv_home_section_social_mail);
		ImageView whatsapp = (ImageView) view.findViewById(R.id.iv_home_section_social_whatsapp);
		ImageView facebook = (ImageView) view.findViewById(R.id.iv_home_section_social_facebook);
		ImageView twitter = (ImageView) view.findViewById(R.id.iv_home_section_social_twitter);
		ImageView googleplus = (ImageView) view.findViewById(R.id.iv_home_section_social_googleplus);
		ImageView youtube = (ImageView) view.findViewById(R.id.iv_home_section_social_youtube);
		ImageView instagram = (ImageView) view.findViewById(R.id.iv_home_section_social_instagram);

		phone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = v.getContext().getString(R.string.social_phone_link);
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse(url));
				startActivity(intent);
			}
		});

		mail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = v.getContext().getString(R.string.social_mail_link);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		whatsapp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				try {
					Uri mUri = Uri.parse(getString(R.string.social_whatsapp_link));
					Intent mIntent = new Intent(Intent.ACTION_SENDTO, mUri);
					mIntent.setPackage(getString(R.string.social_whatsapp_package));
					mIntent.putExtra(getString(R.string.social_whatsapp_chat), true);
					startActivity(mIntent);
				} catch (Exception e) {
					Toast.makeText(v.getContext(), getString(R.string.social_whatsapp_error), Toast.LENGTH_LONG).show();
				}
			}
		});

		facebook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = v.getContext().getString(R.string.social_facebook_link);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		twitter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = v.getContext().getString(R.string.social_twitter_link);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		googleplus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = v.getContext().getString(R.string.social_googleplus_link);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		youtube.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = v.getContext().getString(R.string.social_youtube_link);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		instagram.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = v.getContext().getString(R.string.social_instagram_link);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
	}

	/**
	 * Populates the La Blanca section of the home screen.
	 *
	 * @return True if the section has been shown, false otherwise.
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private boolean setUpLablanca(View view) {
		SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
		Boolean set = false;
		if (preferences.getInt(GM.PREF_DB_FESTIVALS, 0) == 1) {
			LinearLayout llSection = (LinearLayout) view.findViewById(R.id.ll_home_section_lablanca);
			TextView tvText = (TextView) view.findViewById(R.id.tv_home_section_lablanca_text);
			ImageView ivImage = (ImageView) view.findViewById(R.id.iv_home_section_lablanca_image);

			//Get data from the database of the future activities
			SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
			int year = Calendar.getInstance().get(Calendar.YEAR);
			String lang = GM.getLang();
			Cursor cursor = db.rawQuery("SELECT text_" + lang + ", img FROM festival WHERE year = " + year + ";", null);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				llSection.setVisibility(View.VISIBLE);

				//Set text
				String text = Html.fromHtml(cursor.getString(0)).toString();
				String shortText = text.substring(0, 80);
				if (text.equals(shortText)) {
					tvText.setText(text);
				}
				else{
					shortText = shortText + "...";
					tvText.setText(shortText);
				}

				set = true;

				//Set image
				String image = cursor.getString(1);
				if (image.length() > 0){

					//Check if image exists
					File f;
					f = new File(this.getActivity().getFilesDir().toString() + "/img/fiestas/preview/" + image);
					if (f.exists()) {
						//If the image exists, set it.
						Bitmap myBitmap = BitmapFactory.decodeFile(this.getActivity().getFilesDir().toString() + "/img/fiestas/preview/" + image);
						ivImage.setImageBitmap(myBitmap);
					} else {
						//If not, create directories and download asynchronously
						File fpath;
						fpath = new File(this.getActivity().getFilesDir().toString() + "/img/fiestas/preview/");
						fpath.mkdirs();
						new DownloadImage(GM.SERVER + "/img/fiestas/preview/" + image, this.getActivity().getFilesDir().toString() + "/img/fiestas/preview/" + image, ivImage).execute();
					}
				}
				else {
					ivImage.setVisibility(View.GONE);
				}
			}
			cursor.close();
			db.close();


		}
		return set;
	}

	/**
	 * Populates the Location section of the home screen.
	 *
	 * @return True if the section has been shown, false otherwise.
	 */
	private boolean setUpLocation(Location location, View view) {
		SharedPreferences preferences = view.getContext().getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
		if (preferences.getString(GM.PREF_GM_LOCATION, "").length() > 0) {
			Double lat = Double.longBitsToDouble(preferences.getLong(GM.PREF_GM_LATITUDE, 0));
			Double lon = Double.longBitsToDouble(preferences.getLong(GM.PREF_GM_LONGITUDE, 0));
			Double distance = Distance.calculateDistance(lat, lon, location.getLatitude(), location.getLongitude());
			TextView text = (TextView) view.findViewById(R.id.tv_home_section_location_distance);
			if (distance <= 2) {
				distance = 1000 * distance;
				text.setText(String.format(getString(R.string.home_section_location_text_short), distance.intValue(), (int) (0.012 * distance.intValue())));
			} else {
				text.setText(String.format(getString(R.string.home_section_location_text_long), String.format(Locale.US, "%.02f", distance)));
			}

		}
		return false;
	}

	/**
	 * Populates the Gallery section of the home screen.
	 * Loads four photos.
	 * Tapping them takes to the respective album, tapping anywhere else, to the gallery section.
	 *
	 * @return The number of entries shown
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private int setUpGallery(View view) {
		int counter = 0;

		//Set onClick listener of section
		LinearLayout llGallery = (LinearLayout) view.findViewById(R.id.ll_home_section_gallery);
		//Set click listener
		llGallery.setOnClickListener(new OnClickListener() {
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

		//Get data from the database of the future activities
		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = db.rawQuery("SELECT id, album, file, uploaded FROM photo, photo_album WHERE id = photo ORDER BY uploaded DESC LIMIT 4;", null);

		while (cursor.moveToNext()) {

			//Set id
			ivPhoto[counter].setTag(R.string.key_0, cursor.getString(0));
			ivPhoto[counter].setTag(R.string.key_1, cursor.getString(1));

			//Set image
			String image = cursor.getString(2);
			//Check if image exists
			File f;
			f = new File(this.getActivity().getFilesDir().toString() + "/img/galeria/preview/" + image);
			if (f.exists()) {
				//If the image exists, set it.
				Bitmap myBitmap = BitmapFactory.decodeFile(this.getActivity().getFilesDir().toString() + "/img/galeria/preview/" + image);
				ivPhoto[counter].setImageBitmap(myBitmap);
			} else {
				//If not, create directories and download asynchronously
				File fpath;
				fpath = new File(this.getActivity().getFilesDir().toString() + "/img/galeria/preview/");
				fpath.mkdirs();
				new DownloadImage(GM.SERVER + "/img/galeria/preview/" + image, this.getActivity().getFilesDir().toString() + "/img/galeria/preview/" + image, ivPhoto[counter]).execute();
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

					FragmentManager fm = HomeLayout.this.getActivity().getFragmentManager();
					FragmentTransaction ft = fm.beginTransaction();

					ft.replace(R.id.activity_main_content_fragment, fragment);
					ft.addToBackStack("album_" + albumId);
					ft.commit();
				}
			});

			counter++;
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
	@SuppressLint("InflateParams") //Throws unknown error when done properly.
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private int setUpPastActivities(View view) {
		int counter = 0;
		String lang = GM.getLang();

		//Get data from the database of the future activities
		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = db.rawQuery("SELECT id, date, city, title_" + lang + " AS title, text_" + lang + " AS text, price, after_" + lang + " AS after FROM activity WHERE date < date('now') ORDER BY date DESC LIMIT 2;", null);

		//Show section
		LinearLayout llActivitiesPast = (LinearLayout) view.findViewById(R.id.ll_home_section_activities_past);
		llActivitiesPast.setVisibility(View.VISIBLE);

		//Set click listener
		llActivitiesPast.setOnClickListener(new OnClickListener() {
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
			} else {
				text = Html.fromHtml(cursor.getString(6)).toString();
			}
			if (text.length() > 100) {
				text = text.substring(0, 100) + "...";
			}
			TextView tvText = (TextView) entry.findViewById(R.id.tv_row_home_activity_text);
			tvText.setText(text);

			//Set date
			TextView tvDate = (TextView) entry.findViewById(R.id.tv_row_home_activity_date);
			tvDate.setText(GM.formatDate(cursor.getString(1) + " 00:00:00", lang, false));

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
				f = new File(this.getActivity().getFilesDir().toString() + "/img/actividades/miniature/" + image);
				if (f.exists()) {
					//If the image exists, set it.
					Bitmap myBitmap = BitmapFactory.decodeFile(this.getActivity().getFilesDir().toString() + "/img/actividades/miniature/" + image);
					iv.setImageBitmap(myBitmap);
				} else {
					//If not, create directories and download asynchronously
					File fpath;
					fpath = new File(this.getActivity().getFilesDir().toString() + "/img/actividades/miniature/");
					fpath.mkdirs();
					new DownloadImage(GM.SERVER + "/img/actividades/miniature/" + image, this.getActivity().getFilesDir().toString() + "/img/actividades/miniature/" + image, iv).execute();
				}
			}
			cursorImage.close();

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

					FragmentManager fm = HomeLayout.this.getActivity().getFragmentManager();
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
	@SuppressLint("InflateParams") //Throws unknown error when done properly.
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private int setUpFutureActivities(View view) {
		int counter = 0;
		String lang = GM.getLang();

		LinearLayout llList = (LinearLayout) view.findViewById(R.id.ll_home_section_activities_future_content);
		LinearLayout entry;

		//An inflater
		LayoutInflater factory = LayoutInflater.from(getActivity());

		//Get data from the database of the future activities
		SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
		Cursor cursor = db.rawQuery("SELECT id, date, city, title_" + lang + " AS title, text_" + lang + " AS text, price FROM activity WHERE date >= date('now') ORDER BY date LIMIT 2;", null);

		//If there are future activities...
		if (cursor.getCount() > 0) {

			//Show section
			LinearLayout llActivitiesFuture = (LinearLayout) view.findViewById(R.id.ll_home_section_activities_future);
			llActivitiesFuture.setVisibility(View.VISIBLE);

			//Set click listener
			llActivitiesFuture.setOnClickListener(new OnClickListener() {
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
				tvPrice.setText(String.format(getString(R.string.price), cursor.getInt(5)));

				//Set text
				String text = Html.fromHtml(cursor.getString(4)).toString();
				if (text.length() > 100) {
					text = text.substring(0, 100) + "...";
				}
				TextView tvText = (TextView) entry.findViewById(R.id.tv_row_home_activity_text);
				tvText.setText(text);

				//Set date
				TextView tvDate = (TextView) entry.findViewById(R.id.tv_row_home_activity_date);
				tvDate.setText(GM.formatDate(cursor.getString(1) + " 00:00:00", lang, false));

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
					f = new File(this.getActivity().getFilesDir().toString() + "/img/actividades/miniature/" + image);
					if (f.exists()) {
						//If the image exists, set it.
						Bitmap myBitmap = BitmapFactory.decodeFile(this.getActivity().getFilesDir().toString() + "/img/actividades/miniature/" + image);
						iv.setImageBitmap(myBitmap);
					} else {
						//If not, create directories and download asynchronously
						File fpath;
						fpath = new File(this.getActivity().getFilesDir().toString() + "/img/actividades/miniature/");
						fpath.mkdirs();
						new DownloadImage(GM.SERVER + "/img/actividades/miniature/" + image, this.getActivity().getFilesDir().toString() + "/img/actividades/miniature/" + image, iv).execute();
					}
				}

				cursorImage.close();

				//Set onCLickListener
				entry.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Fragment fragment = new ActivityFutureLayout();
						Bundle bundle = new Bundle();
						//Pass post id
						int id = Integer.parseInt(((TextView) v.findViewById(R.id.tv_row_home_activity_hidden)).getText().toString());
						bundle.putInt("activity", id);
						fragment.setArguments(bundle);

						FragmentManager fm = HomeLayout.this.getActivity().getFragmentManager();
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
	@SuppressLint("InflateParams") //Throws unknown error when done properly.
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private int setUpBlog(View view) {
		int counter = 0;
		String lang = GM.getLang();

		LinearLayout llBlog = (LinearLayout) view.findViewById(R.id.ll_home_section_blog);
		llBlog.setOnClickListener(new OnClickListener() {
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
		cursor = db.rawQuery("SELECT id, title_" + lang + " AS title, text_" + lang + " AS text, dtime FROM post ORDER BY dtime DESC LIMIT 2;", null);

		//Loop
		while (cursor.moveToNext()) {

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
			if (text.length() > 100) {
				text = text.substring(0, 100) + "...";
			}
			TextView tvText = (TextView) entry.findViewById(R.id.tv_row_home_blog_text);
			tvText.setText(text);

			//Set date
			TextView tvDate = (TextView) entry.findViewById(R.id.tv_row_home_blog_date);
			tvDate.setText(GM.formatDate(cursor.getString(3), lang, false));

			//Set hidden id
			TextView tvId = (TextView) entry.findViewById(R.id.tv_row_home_blog_hidden);
			tvId.setText(cursor.getString(0));

			//Get image
			ImageView iv = (ImageView) entry.findViewById(R.id.iv_row_home_blog_image);
			Cursor cursorImage = db.rawQuery("SELECT image FROM post_image WHERE post = " + cursor.getString(0) + " ORDER BY idx LIMIT 1;", null);
			if (cursorImage.getCount() > 0) {
				cursorImage.moveToFirst();
				String image = cursorImage.getString(0);

				//Check if image exists
				File f;
				f = new File(this.getActivity().getFilesDir().toString() + "/img/blog/miniature/" + image);
				if (f.exists()) {
					//If the image exists, set it.
					Bitmap myBitmap = BitmapFactory.decodeFile(this.getActivity().getFilesDir().toString() + "/img/blog/miniature/" + image);
					iv.setImageBitmap(myBitmap);
				} else {
					//If not, create directories and download asynchronously
					File fpath;
					fpath = new File(this.getActivity().getFilesDir().toString() + "/img/blog/miniature/");
					fpath.mkdirs();
					new DownloadImage(GM.SERVER + "/img/blog/miniature/" + image, this.getActivity().getFilesDir().toString() + "/img/blog/miniature/" + image, iv).execute();
				}
			}

			cursorImage.close();

			//Set onCLickListener
			entry.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Fragment fragment = new PostLayout();
					Bundle bundle = new Bundle();
					//Pass post id
					int id = Integer.parseInt(((TextView) v.findViewById(R.id.tv_row_home_blog_hidden)).getText().toString());
					bundle.putInt("post", id);
					fragment.setArguments(bundle);

					FragmentManager fm = HomeLayout.this.getActivity().getFragmentManager();
					FragmentTransaction ft = fm.beginTransaction();

					ft.replace(R.id.activity_main_content_fragment, fragment);
					ft.addToBackStack("post_" + id);
					ft.commit();
				}
			});

			//Add to the list
			llList.addView(entry);

			counter++;
		}

		//Close database
		cursor.close();
		db.close();

		return counter;
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
		setUpLocation(location, view);
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
	 * @see android.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		//TODO: Only do this if location is required
		if (!(ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
			locationManager.removeUpdates(this);
		}
		super.onPause();
	}

	/**
	 * Called when the fragment is destroyed.
	 * Stops the location manager
	 * @see android.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		//TODO: Only do this if location is required
		if (!(ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
			locationManager.removeUpdates(this);
		}
		super.onDestroy();
	}
	
	/**
	 * Called when the fragment is brought back into the foreground. 
	 * Resumes the map and the location manager.
	 * 
	 * @see android.app.Fragment#onResume()
	 */
	@Override
	public void onResume(){
		//TODO: Only do this if location is required
		if (!(ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
		}
		super.onResume();
	}
}
