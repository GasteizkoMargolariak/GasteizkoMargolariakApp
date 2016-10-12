package com.ivalentin.margolariak;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Handler;

/**
 * The main Activity of the app. It's actually the only activity, and it loads other fragments.
 * 
 * @author Iñigo Valentin
 *
 */
public class MainActivity extends Activity{

	//Alarm to get location and notifications.
	private AlarmReceiver notificationAlarm;

	//The menu entries
	private RelativeLayout menuItem[];
	private TextView menuText[];
	private ImageView menuImage[];

	//Handler to periodically check for location updates
	private final Handler locationHandler = new Handler();
	// Code to check for location updates
	private final Runnable checkForLocation = new Runnable() {
		@Override
		public void run() {

			//Get location page and parse it
			boolean result = false;
			FetchURL fu = new FetchURL();
			//TODO: This needs a simple API too
			fu.Run(GM.SERVER + "/app/location.php");
			String lat = "", lon = "";
			String o = fu.getOutput().toString();
			Log.d("Location", "Fetched location page");
			if (o.contains("<location>none</location>"))
				result = false;
			if (o.contains("<lat>") && o.contains("<lon>")){
				lat = o.substring(o.indexOf("<lat>") + 5, o.indexOf("</lat>"));
				lon = o.substring(o.indexOf("<lon>") + 5, o.indexOf("</lon>"));
				result = true;
			}

			SharedPreferences settings = getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			if (result){
				editor.putLong(GM.PREF_GM_LATITUDE, Double.doubleToLongBits(Double.parseDouble(lat)));
				editor.putLong(GM.PREF_GM_LONGITUDE, Double.doubleToLongBits(Double.parseDouble(lon)));
				editor.putString(GM.PREF_GM_LOCATION, new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Calendar.getInstance().getTime()));

				//Show menu entry
				if (menuItem[1] != null){
					menuItem[1].setVisibility(View.VISIBLE);
				}
			}
			else{
				editor.putString(GM.PREF_GM_LOCATION, GM.DEFAULT_PREF_GM_LOCATION);
				if (menuItem[1] != null){
					menuItem[1].setVisibility(View.GONE);
				}
			}
			editor.apply();

			//Repeat this the same runnable code block again another th specified interval
			locationHandler.postDelayed(checkForLocation, GM.INTERVAL_LOCATION);
		}
	};

	//Not referenced in code.
	public void showMenu(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu, popup.getMenu());

		popup.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(MainActivity.this, AboutActivity.class);
				startActivity(intent);
				return true;
			}
		});

		popup.getMenu().getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(MainActivity.this, SponsorActivity.class);
				startActivity(intent);
				return true;
			}
		});

		popup.getMenu().getItem(2).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
				return true;
			}
		});

		popup.show();
	}



	/**
	 * Loads a section in the main screen.
	 * 
	 * @param section Section identifier {@see GM}
	 */
	public void loadSection (byte section){
		FragmentManager fm = MainActivity.this.getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment = null;
		String title = "";
		Bundle bundle = new Bundle();

		//Reset all tabs to it's original state
		for (int i = 0; i < 6; i ++){
			menuText[i].setTypeface(null, Typeface.NORMAL);
			menuImage[i].requestLayout();
			menuImage[i].getLayoutParams().height = 2;
		}

		switch (section){
			case GM.SECTION_HOME:
				fragment = new HomeLayout();
				title = getString(R.string.menu_home);
				menuText[0].setTypeface(null, Typeface.BOLD);
				menuImage[0].getLayoutParams().height = 8;
				break;

			case GM.SECTION_LOCATION:
				fragment = new LocationLayout();
				title = getString(R.string.menu_location);
				menuText[1].setTypeface(null, Typeface.BOLD);
				menuImage[1].getLayoutParams().height = 8;
				break;

			case GM.SECTION_LABLANCA:
				//Get settings
				SharedPreferences preferences = getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
				if (preferences.getInt(GM.PREF_DB_FESTIVALS, 0) == 1){
					fragment = new LablancaLayout();
				}
				else{
					fragment = new LablancaNoFestivalsLayout();
				}
				title = getString(R.string.menu_lablanca);
				menuText[2].setTypeface(null, Typeface.BOLD);
				menuImage[2].getLayoutParams().height = 8;
				break;

			case GM.SECTION_LABLANCA_SCHEDULE:
				fragment = new ScheduleLayout();
				bundle.putInt(GM.SCHEDULE, GM.SECTION_LABLANCA_SCHEDULE);
				fragment.setArguments(bundle);
				title = getString(R.string.menu_lablanca_schedule);
				menuText[2].setTypeface(null, Typeface.BOLD);
				menuImage[2].getLayoutParams().height = 8;
				break;

			case GM.SECTION_LABLANCA_GM_SCHEDULE:
				fragment = new ScheduleLayout();
				bundle.putInt(GM.SCHEDULE, GM.SECTION_LABLANCA_GM_SCHEDULE);
				fragment.setArguments(bundle);
				title = getString(R.string.menu_lablanca_gm_schedule);
				menuText[2].setTypeface(null, Typeface.BOLD);
				menuImage[2].getLayoutParams().height = 8;
				break;

			case GM.SECTION_ACTIVITIES:
				fragment = new ActivityLayout();
				title = getString(R.string.menu_activities);
				menuText[3].setTypeface(null, Typeface.BOLD);
				menuImage[3].getLayoutParams().height = 8;
				break;

			case GM.SECTION_BLOG:
				fragment = new BlogLayout();
				title = getString(R.string.menu_blog);
				menuText[4].setTypeface(null, Typeface.BOLD);
				menuImage[4].getLayoutParams().height = 8;
				break;

			case GM.SECTION_GALLERY:
				fragment = new GalleryLayout();
				title = getString(R.string.menu_blog);
				menuText[5].setTypeface(null, Typeface.BOLD);
				menuImage[5].getLayoutParams().height = 8;
				break;
		}
		
		//Replace the fragment.
		ft.replace(R.id.activity_main_content_fragment, fragment);
		//ft.addToBackStack(title);
		ft.commit();
		setSectionTitle(title);
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
	 * Runs when the activity is created. 
	 * 
	 * @param savedInstanceState Saved state of the activity.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@SuppressWarnings("deprecation, ConstantConditions")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

	    //Get intent extras
	    String action = getIntent().getStringExtra(GM.EXTRA_ACTION);
	    String actionText = getIntent().getStringExtra(GM.EXTRA_TEXT);
	    String actionTitle = getIntent().getStringExtra(GM.EXTRA_TITLE);

		//Set an alarm for notifications..
		//Wait a little
		final Intent intent = this.getIntent();
		final Context context = this;

		Thread t = new Thread() {

			@Override
			public void run() {
				try {
					Thread.sleep(20000);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							notificationAlarm = new AlarmReceiver();
							notificationAlarm.setAlarm(context);
							notificationAlarm.onReceive(context, intent);
						}
					});
				} catch (InterruptedException e) {
					Log.e("Sleep interrupted", e.toString());
				}
			}
		};

		t.start();

		//Remove title bar.
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    super.onCreate(savedInstanceState);

	    //Set layout.
		setContentView(R.layout.activity_main);

		//Assign menu items
		menuItem = new RelativeLayout[6];
		menuItem[0] = (RelativeLayout) findViewById(R.id.rl_menu_home);
		menuItem[1] = (RelativeLayout) findViewById(R.id.rl_menu_location);
		menuItem[2] = (RelativeLayout) findViewById(R.id.rl_menu_lablanca);
		menuItem[3] = (RelativeLayout) findViewById(R.id.rl_menu_activities);
		menuItem[4] = (RelativeLayout) findViewById(R.id.rl_menu_blog);
		menuItem[5] = (RelativeLayout) findViewById(R.id.rl_menu_gallery);
		menuText = new TextView[6];
		menuText[0] = (TextView) menuItem[0].findViewById(R.id.tv_menu_home);
		menuText[1] = (TextView) menuItem[1].findViewById(R.id.tv_menu_location);
		menuText[2] = (TextView) menuItem[2].findViewById(R.id.tv_menu_lablanca);
		menuText[3] = (TextView) menuItem[3].findViewById(R.id.tv_menu_activities);
		menuText[4] = (TextView) menuItem[4].findViewById(R.id.tv_menu_blog);
		menuText[5] = (TextView) menuItem[5].findViewById(R.id.tv_menu_gallery);
		menuImage = new ImageView[6];
		menuImage[0] = (ImageView) menuItem[0].findViewById(R.id.iv_menu_home);
		menuImage[1] = (ImageView) menuItem[1].findViewById(R.id.iv_menu_location);
		menuImage[2] = (ImageView) menuItem[2].findViewById(R.id.iv_menu_lablanca);
		menuImage[3] = (ImageView) menuItem[3].findViewById(R.id.iv_menu_activities);
		menuImage[4] = (ImageView) menuItem[4].findViewById(R.id.iv_menu_blog);
		menuImage[5] = (ImageView) menuItem[5].findViewById(R.id.iv_menu_gallery);

		//Remove scrollbars from the sections menu
		HorizontalScrollView svMenu = (HorizontalScrollView) findViewById(R.id.sv_menu);
		svMenu.setHorizontalScrollBarEnabled(false);

		//Set click listers for menu items
		menuItem[0].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_HOME); }
		});
		menuItem[1].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_LOCATION); }
		});
		menuItem[2].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_LABLANCA); }
		});
		menuItem[3].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_ACTIVITIES); }
		});
		menuItem[4].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_BLOG); }
		});
		menuItem[5].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { loadSection(GM.SECTION_GALLERY); }
		});

		//Get preferences
		SharedPreferences preferences = getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);

		//TODO: Read preferences. If there is a recent location, show the menu entry

		//If the user code is not set, generate one
		if (preferences.getString(GM.USER_CODE, "").length() == 0){
			SecureRandom random = new SecureRandom();
			String newCode = new BigInteger(130, random).toString(32).substring(0, 16);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(GM.USER_CODE, newCode);
			editor.apply();
		}
		
		//Cerate database if it's not already created
		createDatabase();

		
		//If its the first time
		if (preferences.getInt(GM.PREF_DB_VERSION, GM.DEFAULT_PREF_DB_VERSION) == GM.DEFAULT_PREF_DB_VERSION){
			initialSync();
		}
		//If t's not the first time
		else{
			//Sync db
			sync();
			
			//Load initial section
			loadSection(GM.SECTION_HOME);
			
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

					//Set the icon
					dialogIcon = getResources().getDrawable(R.drawable.ic_launcher);
					if (dialogIcon != null) {
						dialogIcon.setBounds(0, 0, (int) (tvDialogTitle.getTextSize() * 1.4), (int) (tvDialogTitle.getTextSize() * 1.4));
					}
					tvDialogTitle.setCompoundDrawables(dialogIcon, null, null, null);
					tvDialogTitle.setCompoundDrawablePadding(20);

					//Get preferences
					int festivals = preferences.getInt(GM.PREF_DB_FESTIVALS, 0);

					//Set the action button
					btDialogAction = (Button) dialog.findViewById(R.id.bt_dialog_notification_action);
					if (action != null){
						switch (action) {

							case GM.EXTRA_ACTION_LABLANCA:

								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_lablanca));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION_LABLANCA);
									}
								});
								break;

							case GM.EXTRA_ACTION_LOCATION:

								if (!preferences.getString(GM.PREF_GM_LOCATION, GM.DEFAULT_PREF_GM_LOCATION).equals(GM.DEFAULT_PREF_GM_LOCATION)) {
									//Set up the action button if location is reported
									btDialogAction.setVisibility(View.VISIBLE);
									btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_location));
									btDialogAction.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											dialog.dismiss();
											loadSection(GM.SECTION_LOCATION);
										}
									});

								}
								break;

							case GM.EXTRA_ACTION_BLOG:

								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_blog));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION_BLOG);
									}
								});
								break;

							case GM.EXTRA_ACTION_ACTIVITIES:

								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_activities));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION_ACTIVITIES);
									}
								});
								break;

							case GM.EXTRA_ACTION_GALLERY:

								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_gallery));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION_GALLERY);
									}
								});
								break;

							case GM.EXTRA_ACTION_GMSCHEDULE:

								if (festivals == 1) {
									//Set up the action button
									btDialogAction.setVisibility(View.VISIBLE);
									btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_gmschedule));
									btDialogAction.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											dialog.dismiss();
											loadSection(GM.SECTION_LABLANCA_GM_SCHEDULE);
										}
									});
								}
								break;

							case GM.EXTRA_ACTION_CITYSCHEDULE:
								if (festivals == 1) {
									//Set up the action button
									btDialogAction.setVisibility(View.VISIBLE);
									btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_cityschedule));
									btDialogAction.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											dialog.dismiss();
											loadSection(GM.SECTION_LABLANCA_SCHEDULE);
										}
									});
								}
								break;

							//If the notification is just text
							default:
								//Hide action button
								btDialogAction.setVisibility(View.GONE);
						}
					}
					else{
						btDialogAction.setVisibility(View.GONE);
					}

					//Set dialog parameters
					WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
					lp.copyFrom(dialog.getWindow().getAttributes());
					lp.width = WindowManager.LayoutParams.MATCH_PARENT;
					lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
					lp.gravity = Gravity.CENTER;
					lp.dimAmount = 0.4f;
					dialog.getWindow().setAttributes(lp);

					//Show dialog
					dialog.show();
				}
			}
		}
	}
    
    /**
     * Creates the app database and fills it with the hard coded, default data.
     */
    private void createDatabase(){
    	SQLiteDatabase db;
    	try {
    		//Create database
    		db = openOrCreateDatabase(GM.DB_NAME, Activity.MODE_PRIVATE, null);

			//TODO: Predefine these queries as static strings in GM.java, so they can be used also in Sync.java
			db.execSQL("CREATE TABLE IF NOT EXISTS activity (id INT, permalink VARCHAR, date DATETIME, city VARCHAR, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, after_es VARCHAR, after_en VARCHAR, after_eu VARCHAR, price INT, inscription INT, max_people INT, album INT, dtime DATETIME, comments INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS activity_comment (id INT, activity INT, text VARCHAR, dtime DATETIME, username VARCHAR, lang VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS activity_image (id INT, activity INT, image VARCHAR, idx INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS activity_itinerary (id INT, activity INT, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, description_es VARCHAR, description_en VARCHAR, description_eu VARCHAR, start DATETIME, end DATETIME, place INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS activity_tag (activity INT, tag VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS album (id INT, permalink VARCHAR, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, description_es VARCHAR, description_en VARCHAR, description_eu VARCHAR, open INT, time DATETIME);");
			db.execSQL("CREATE TABLE IF NOT EXISTS festival (id INT, year INT, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, summary_es VARCHAR, summary_en VARCHAR, summary_eu VARCHAR, img VARCHAR);");
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
			db.execSQL("CREATE TABLE IF NOT EXISTS post (id INT, permalink VARCHAR, title_es VARCHAR, title_en VARCHAR, title_eu VARCHAR, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, dtime DATETIME, comments INT, username VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS post_comment (id INT, post INT, text VARCHAR, dtime DATETIME, username VARCHAR, lang VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS post_image (id INT, post INT, image VARCHAR, idx INT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS post_tag (post INT, tag VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS settings (name VARCHAR, value VARCHAR);");
			db.execSQL("CREATE TABLE IF NOT EXISTS sponsor (id INT, name_es VARCHAR, name_en VARCHAR, name_eu VARCHAR, text_es VARCHAR, text_en VARCHAR, text_eu VARCHAR, image VARCHAR, address_es VARCHAR, address_en VARCHAR, address_eu VARCHAR, link VARCHAR, lat FLOAT, lon FLOAT)");
			db.execSQL("CREATE TABLE IF NOT EXISTS version (section VARCHAR, version INT);");
			db.close();
    	}
    	catch (Exception ex){
    		Log.e("Error creating database", ex.toString());
    	}
    }

	/**
	 * Performs a full sync against the remote database.
	 */
	private void sync(){
		ProgressBar pbSync = (ProgressBar) findViewById(R.id.pb_sync);
		ImageView ivSync = (ImageView) findViewById(R.id.iv_logo);
		new Sync(this, pbSync, ivSync).execute();
	}
	
	/**
	 * Perform an initial sync before the app can be used. 
	 * A dialog will block the UI. 
	 * It is intended to be used only when the database is empty.
	 */
	@SuppressWarnings("ConstantConditions")
	private void initialSync(){
		//Create a dialog
		Dialog dialog = new Dialog(this);
		dialog.setCancelable(false);
		
		//Set up the window
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_sync);

		//Set dialog parameters
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.gravity = Gravity.CENTER;
		lp.dimAmount = 0.4f;
		dialog.getWindow().setAttributes(lp);
		
		//Sync
		ProgressBar pbSync = (ProgressBar) findViewById(R.id.pb_sync);
		ImageView ivSync = (ImageView) findViewById(R.id.iv_logo);
		new Sync(this, pbSync, ivSync, dialog, this).execute();
	}
	
	/**
	 * Overrides onBackPressed(). 
	 * Used to show the previous fragment instead of finishing the app.
	 * 
	 * @see android.support.v7.app.AppCompatActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed(){
		try {
			FragmentManager fm = getFragmentManager();
			if (fm.getBackStackEntryCount() > 1) {
				fm.popBackStack();
			} else {
				finish();
				super.onBackPressed();
			}
		}
		catch (Exception ex){
			Log.e("Error going back", "Finishing activity: " + ex.toString());
			Log.e("RUNNABLE", "Finishing callback for location");
			locationHandler.removeCallbacks(checkForLocation);
			finish();
			//super.onBackPressed();
		}
		
	}

	/**
	 * Overrides onKeyUp().
	 * Used to toggle the menu whent the key is pressed.
	 *
	 * @see android.app.Activity#onKeyUp(int, KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			showMenu(this.findViewById(R.id.bt_menu));
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onResume(){
		locationHandler.post(checkForLocation);
		super.onResume();
	}

	@Override
	public void onPause(){
		locationHandler.removeCallbacks(checkForLocation);
		super.onPause();
	}

	@Override
	public void onDestroy(){
		locationHandler.removeCallbacks(checkForLocation);
		super.onDestroy();
	}
	
}
