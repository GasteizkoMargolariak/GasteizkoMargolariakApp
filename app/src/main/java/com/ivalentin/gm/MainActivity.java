package com.ivalentin.gm;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * The main Activity of the app. It's actually the only activity, and it loads other fragments.
 * 
 * @author Iñigo Valentin
 *
 */
public class MainActivity extends AppCompatActivity implements LocationListener{
		
	//Set of GPS coordinates
	private double[] coordinates = new double[2];
	
	//The main layout of the app
	private MainLayout mLayout;
	
	//The location manager to get GPS coordinates
	private LocationManager locationManager;
	
	//GPS coordinates provider
	private String provider;
	
	//Alarm to get location
	AlarmReceiver alarm;
		
	/**
	 * Loads a section in the main screen.
	 * 
	 * @param section Section identifier {@see GM}
	 * @param fromSliderMenu Indicates if the call has been made from the slider menu, so it can be closed.
	 */
	public void loadSection (byte section, boolean fromSliderMenu){
		FragmentManager fm = MainActivity.this.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment = null;
		String title = "";
		Bundle bundle = new Bundle();
		switch (section){
			case GM.SECTION_HOME:
				fragment = new HomeLayout();
				//Pass current GPS coordinates
				bundle.putDouble("lat", coordinates[0]);
				bundle.putDouble("lon", coordinates[1]);
				fragment.setArguments(bundle);
				title = getString(R.string.menu_home);
				break;

			case GM.SECTION_LOCATION:
				fragment = new LocationLayout();
				//Pass current GPS coordinates
				bundle.putDouble("lat", coordinates[0]);
				bundle.putDouble("lon", coordinates[1]);
				fragment.setArguments(bundle);
				title = getString(R.string.menu_lablanca_location);
				break;

			case GM.SECTION_LABLANCA:
				fragment = new LablancaLayout();
				//Pass current GPS coordinates
				bundle.putDouble("lat", coordinates[0]);
				bundle.putDouble("lon", coordinates[1]);
				fragment.setArguments(bundle);
				title = getString(R.string.menu_lablanca);
				break;

			case GM.SECTION_LABLANCA_SCHEDULE:
				fragment = new ScheduleLayout();
				bundle.putInt(GM.SCHEDULE, GM.SECTION_LABLANCA_SCHEDULE);
				fragment.setArguments(bundle);
				title = getString(R.string.menu_lablanca_schedule);
				break;

			case GM.SECTION_LABLANCA_GM_SCHEDULE:
				fragment = new ScheduleLayout();
				bundle.putInt(GM.SCHEDULE, GM.SECTION_LABLANCA_GM_SCHEDULE);
				fragment.setArguments(bundle);
				title = getString(R.string.menu_lablanca_gm_schedule);
				break;

			case GM.SECTION_LABLANCA_AROUND:
				fragment = new AroundLayout();
				//Pass current GPS coordinates
				bundle.putDouble("lat", coordinates[0]);
				bundle.putDouble("lon", coordinates[1]);
				fragment.setArguments(bundle);
				title = getString(R.string.menu_lablanca_around);
				break;

			case GM.SECTION_ACTIVITIES:
				fragment = new ActivityLayout();
				title = getString(R.string.menu_activities);
				break;

			case GM.SECTION_BLOG:
				fragment = new BlogLayout();
				title = getString(R.string.menu_blog);
				break;

			case GM.SECTION_GALLERY:
				fragment = new GalleryLayout();
				title = getString(R.string.menu_blog);
				break;

			case GM.SECTION_ABOUT:
				fragment = new AboutLayout();
				title = getString(R.string.menu_about);
				break;

			case GM.SECTION_SETTINGS:
				fragment = new SettingsLayout();
				title = getString(R.string.menu_settings);
				break;
		}
		
		//Replace the fragment.
		ft.replace(R.id.activity_main_content_fragment, fragment);
		ft.addToBackStack(title);
		ft.commit();
		setSectionTitle(title);
		
		//If calling from the menu, close it.
		if (fromSliderMenu)
			mLayout.toggleMenu();
	}
	
	
	/**
	 * Sets the title of the current section.
	 * 
	 * @param title The title of the current section.
	 */
	public void setSectionTitle(String title){
		TextView tvTitle = (TextView) findViewById(R.id.activity_main_content_title); 
		tvTitle.setText(title);
	}
	
	/** 
	 * Run when the app resumes. It's extended to request
	 * location updates when the app is resumed.
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		alarm.onReceive(this, this.getIntent());
		locationManager.requestLocationUpdates(provider, GM.LOCATION_ACCURACY, 1, this);
	}

	/**
	 * Run when the app is paused.Extended to remove the location
	 * listener updates when the Activity is paused.
	 *  
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	/**
	 * Called when the location is updated.
	 * 
	 * @param location The updated location.
	 * 
	 * @see android.location.LocationListener#onLocationChanged(android.location.Location)
	 * 
	 */
	@Override
	public void onLocationChanged(Location location) {
		coordinates[0] = location.getLatitude();
		coordinates[1] = location.getLongitude();
	}

	/**
	 * Extended for nothing. Needed because the class implements LocationListener.
	 * 
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	/**
	 * Extended for nothing. Needed because the class implements LocationListener.
	 * 
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {}

	/**
	 * Extended for nothing. Needed because the class implements LocationListener.
	 * 
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {}
	  
	  
	/**
	 * Runs when the activity is created. 
	 * 
	 * @param savedInstanceState Saved state of the activity.
	 * 
	 * @see android.support.v7.app.AppCompatActivity#onCreate(android.os.Bundle)
	 */
	//@SuppressLint("TrulyRandom") //I don't care about getting "potentially unsecured numbers" here
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//Set the location manager.
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	   	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, this);
	    
	    //Define the criteria how to select the location provider: use default.
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
	    Location location = locationManager.getLastKnownLocation(provider);

	    //Get intent extras
	    String action = getIntent().getStringExtra(GM.EXTRA_ACTION);
	    String actionText = getIntent().getStringExtra(GM.EXTRA_TEXT);
	    String actionTitle = getIntent().getStringExtra(GM.EXTRA_TITLE);
	    
	    // Initialize the location fields.
	    if (location != null){
	    	//System.out.println("Provider " + provider + " has been selected.");
	    	onLocationChanged(location);
	    }
	    else{
	    	Log.e("Location", "Location not available");
	    }

		//Set an alarm for notifications..
	    alarm = new AlarmReceiver();
		alarm.setAlarm(this);
		alarm.onReceive(this, this.getIntent());
		
		//Remove title bar.
	    this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
	    super.onCreate(savedInstanceState);
	    
	    //Set layout.
		setContentView(R.layout.activity_main);
		mLayout = (MainLayout) findViewById(R.id.main_layout);
		
		//Assign menu button
		ImageButton btMenu = (ImageButton) findViewById(R.id.bt_menu); 
		btMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { mLayout.toggleMenu(); }
		});
		
		//Assign menu items
		TextView menuItem[] = new TextView[8];
		menuItem[0] = (TextView) findViewById(R.id.menu_home);
		menuItem[1] = (TextView) findViewById(R.id.menu_location);
		menuItem[2] = (TextView) findViewById(R.id.menu_lablanca);
		menuItem[3] = (TextView) findViewById(R.id.menu_activities);
		menuItem[4] = (TextView) findViewById(R.id.menu_blog);
		menuItem[5] = (TextView) findViewById(R.id.menu_gallery);
		menuItem[6] = (TextView) findViewById(R.id.menu_about);
		menuItem[7] = (TextView) findViewById(R.id.menu_settings);

		//Assign buttons for la blanca submenu
		TextView menuLablancaItem[] = new TextView[3];
		menuLablancaItem[0] = (TextView) findViewById(R.id.menu_lablanca_schedule);
		menuLablancaItem[1] = (TextView) findViewById(R.id.menu_lablanca_gm_schedule);
		menuLablancaItem[2] = (TextView) findViewById(R.id.menu_lablanca_around);

		//Todo: Read settings and display needed menu entries

		//Set click listers for menu items
		menuItem[0].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_HOME, true); }
		});
		menuItem[1].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_LOCATION, true); }
		});
		menuItem[2].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_LABLANCA, true); }
		});
		menuItem[3].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_ACTIVITIES, true); }
		});
		menuItem[4].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_BLOG, true); }
		});
		menuItem[5].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_GALLERY, true); }
		});
		menuItem[6].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_ABOUT, true); }
		});
		menuItem[7].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_SETTINGS, true); }
		});
		menuLablancaItem[0].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_LABLANCA_SCHEDULE, true); }
		});
		menuLablancaItem[1].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_LABLANCA_GM_SCHEDULE, true); }
		});
		menuLablancaItem[2].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_LABLANCA_AROUND, true); }
		});

		//If the user code is not set, generate one
		SharedPreferences preferences = getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
		if (preferences.getString(GM.USER_CODE, "").length() == 0){
			SecureRandom random = new SecureRandom();
			String newCode = new BigInteger(130, random).toString(32).substring(0, 8);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(GM.USER_CODE, newCode);
			editor.apply();
		}
		
		//If the database doesn't exist, create it.
		if (!databaseExists()){
        	Log.i("DB status", "Database not found. Creating it...");
        	createDatabase();
        }
		
		//If its the first time
		if (preferences.getInt(GM.PREF_DB_VERSION, GM.DEFAULT_PREF_DB_VERSION) == GM.DEFAULT_PREF_DB_VERSION){
			initialSync();
		}
		//If t's not the first time
		else{
			//Sync db
			sync();
			
			//Load initial section
			loadSection(GM.SECTION_HOME, false);
			
			//If the intent had extras (from notifications), do something
			if (actionText != null){
				TextView tvDialogTitle, tvDialogText;
				Button btDialogClose, btDialogAction;
				Drawable dialogIcon;
				if (actionTitle != null){
					
					//Create a dialog
					final Dialog dialog = new Dialog(this);
					
					//Set up dialog window
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog.setContentView(R.layout.dialog_notification);
					
					//Set title
					tvDialogTitle = (TextView) dialog.findViewById(R.id.tv_dialog_notification_title);
					tvDialogTitle.setText(actionTitle);
					
					//Set text
					tvDialogText = (TextView) dialog.findViewById(R.id.tv_dialog_notification_text);
					tvDialogText.setText(actionText);
					
					//Set close button
					btDialogClose = (Button) dialog.findViewById(R.id.bt_dialog_notification_close);
					btDialogClose.setOnClickListener(new OnClickListener() {
		    			@Override
		    			public void onClick(View v) {
		    				dialog.dismiss();
		    			}
		    		});
					
					//Set the action button
					btDialogAction = (Button) dialog.findViewById(R.id.bt_dialog_notification_action);
					if (action != null){
						switch (action) {
							//If the notification action opens the GM schedule
							case GM.EXTRA_ACTION_GM:
								//Set the icon
								dialogIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_gm, null);
								dialogIcon.setBounds(0, 0, (int) (tvDialogTitle.getTextSize() * 1.4), (int) (tvDialogTitle.getTextSize() * 1.4));
								tvDialogTitle.setCompoundDrawables(dialogIcon, null, null, null);
								tvDialogTitle.setCompoundDrawablePadding(20);

								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_gm));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION_LABLANCA_GM_SCHEDULE, false);
									}
								});
								//}
								break;

							//If the notification action opens the city schedule
							case GM.EXTRA_ACTION_SCHEDULE:
								//Set the icon
								dialogIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_program, null);
								dialogIcon.setBounds(0, 0, (int) (tvDialogTitle.getTextSize() * 1.4), (int) (tvDialogTitle.getTextSize() * 1.4));
								tvDialogTitle.setCompoundDrawables(dialogIcon, null, null, null);
								tvDialogTitle.setCompoundDrawablePadding(20);

								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_schedule));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION_LABLANCA_SCHEDULE, false);
									}
								});
								//}
								break;

							//If the notification is just text
							default:
								//Set the icon
								dialogIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_about, null);
								dialogIcon.setBounds(0, 0, (int) (tvDialogTitle.getTextSize() * 1.4), (int) (tvDialogTitle.getTextSize() * 1.4));
								tvDialogTitle.setCompoundDrawables(dialogIcon, null, null, null);
								tvDialogTitle.setCompoundDrawablePadding(20);

								//Hide action button
								btDialogAction.setVisibility(View.GONE);
						}
					}
					else{
						btDialogAction.setVisibility(View.GONE);
					}
	
					//Show the dialog
					dialog.show();
				}
			}
		}
	}
	
	/**
	 * Checks if the app database exists.
	 * 
	 * @return True if the database exists, false otherwise.
	 */
	private boolean databaseExists(){
    	File database = getApplicationContext().getDatabasePath(GM.DB_NAME);
		return database.exists();
    }
    
    /**
     * Creates the app database and fills it with the hard coded, default data.
     */
    private void createDatabase(){
    	SQLiteDatabase db;
    	try {
    		//Create database
    		db = this.openOrCreateDatabase(GM.DB_NAME, MODE_PRIVATE, null);

			db.execSQL("CREATE TABLE IF NOT EXISTS activity (id INT, permalink VARCHAR, date DATETIME, city VARCHAR, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, after_es VARCHAR, after_en VARCHAR, after_eu VARCHAR, price INT, inscription INT, max_people INT, album INT, dtime DATETIME, comments INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS activity_comment (id INT, activity INT, text VARCHAR, dtime DATETIME, username VARCHAR, lang VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS activity_image (id INT, activity INT, image VARCHAR, idx INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS activity_itinerary (id INT, activity INT, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, description_es VARCHAR, description_en VARCHAR, description_eu VARCHAR, start DATETIME, end DATETIME, place INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS activity_tag (activity INT, tag VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS album (id INT, permalink VARCHAR, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, description_es VARCHAR, description_en VARCHAR, description_eu VARCHAR, open INT, time DATETIME);");
			db.execSQL("CREATE TABLE IF NOT EXISTS festival (id INT, year INT, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, img VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS festival_day (id INT, date DATETIME, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, price INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS festival_event (id INT, gm INT, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, description_es VARCHAR, description_en VARCHAR, description_eu VARCHAR, host INT, place INT, start DATETIME, end DATETIME);");
			db.execSQL("CREATE TABLE IF NOT EXISTS festival_event_image (id INT, event INT, image VARCHAR, idx INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS festival_offer (id INT, year INT, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, description_es VARCHAR, description_en VARCHAR, description_eu VARCHAR, days INT, price INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS location (id INT, dtime DATETIME, lat FLOAT, lon FLOAT, manual INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS notification (id INT, dtime DATETIME, duration INT, action VARCHAR, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, internal INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS people (id INT, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, link VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS photo (id INT, file VARCHAR, permalink VARCHAR, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, description_es VARCHAR, description_en VARCHAR, description_eu VARCHAR, dtime DATETIME, uploaded DATETIME, place INT, width INT, height INT, size INT, username VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS photo_album (photo INT, album INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS photo_comment (id INT, photo INT, text VARCHAR, dtime DATETIME, username VARCHAR, lang VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS place (id INT, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, address_es VARCHAR, address_en VARCHAR, address_eu VARCHAR, cp VARCHAR, lat FLOAT, lon FLOAT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS post (id INT, permalink VARCHAR, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, dtime DATETIME, comments INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS post_comment (id INT, post INT, text VARCHAR, dtime DATETIME, username VARCHAR, lang VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS post_image (id INT, post INT, image VARCHAR, idx INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS post_tag (post INT, tag VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS settings (name VARCHAR, value VARCHAR);");
			db.close();
    	}
    	catch (Exception ex){
    		Log.e("Error creating database", ex.toString());
    	}
    }

	/**
	 * Performs a full sync against the remote database.
	 */
	public void sync(){
		ProgressBar pbSync = (ProgressBar) findViewById(R.id.pb_sync);
		new Sync(this, pbSync).execute();
	}
	
	/**
	 * Perform an initial sync before the app can be used. 
	 * A dialog will block the UI. 
	 * It is intended to be used only when the database is empty.
	 */
	private void initialSync(){
		//Create a dialog
		Dialog dialog = new Dialog(this);
		dialog.setCancelable(false);
		
		//Set up the window
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_sync);
		
		//Sync
		ProgressBar pbSync = (ProgressBar) findViewById(R.id.pb_sync);
		new Sync(this, pbSync, dialog, this).execute();
	}
	
	/**
	 * Overrides onBackPressed(). 
	 * Used to show the previous fragment instead of finishing the app.
	 * 
	 * @see android.support.v7.app.AppCompatActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed(){
		FragmentManager fm = getSupportFragmentManager();
		if (fm.getBackStackEntryCount() > 1){
			fm.popBackStack();
		}
		else{
			super.onBackPressed();
		}
		
	}
	
}
