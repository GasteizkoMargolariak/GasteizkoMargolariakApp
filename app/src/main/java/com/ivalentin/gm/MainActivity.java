//TODO: implement onResume, to try and fetch location

package com.ivalentin.gm;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
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
public class MainActivity extends Activity{//} implements LocationListener{

	//The main layout of the app
	private MainLayout mLayout;

	//Alarm to get location and notifications.
	AlarmReceiver alarm;

	/**
	 * Loads a section in the main screen.
	 * 
	 * @param section Section identifier {@see GM}
	 * @param fromSliderMenu Indicates if the call has been made from the slider menu, so it can be closed.
	 */
	public void loadSection (byte section, boolean fromSliderMenu){
		FragmentManager fm = MainActivity.this.getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment = null;
		String title = "";
		Bundle bundle = new Bundle();
		switch (section){
			case GM.SECTION_HOME:
				fragment = new HomeLayout();
				title = getString(R.string.menu_home);
				break;

			case GM.SECTION_LOCATION:
				fragment = new LocationLayout();
				title = getString(R.string.menu_location);
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
	 * Runs when the activity is created. 
	 * 
	 * @param savedInstanceState Saved state of the activity.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@SuppressWarnings("deprecation")
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
							alarm = new AlarmReceiver();
							alarm.setAlarm(context);
							alarm.onReceive(context, intent);
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
		mLayout = (MainLayout) findViewById(R.id.main_layout);
		
		//Assign menu button
		ImageButton btMenu = (ImageButton) findViewById(R.id.bt_menu); 
		btMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { mLayout.toggleMenu(); }
		});
		
		//Assign menu items
		TextView menuItem[] = new TextView[7];
		menuItem[0] = (TextView) findViewById(R.id.menu_home);
		menuItem[1] = (TextView) findViewById(R.id.menu_location);
		menuItem[2] = (TextView) findViewById(R.id.menu_lablanca);
		menuItem[3] = (TextView) findViewById(R.id.menu_activities);
		menuItem[4] = (TextView) findViewById(R.id.menu_blog);
		menuItem[5] = (TextView) findViewById(R.id.menu_gallery);
		menuItem[6] = (TextView) findViewById(R.id.menu_settings);

		//Assign buttons for la blanca submenu
		TextView menuLablancaItem[] = new TextView[2];
		menuLablancaItem[0] = (TextView) findViewById(R.id.menu_lablanca_schedule);
		menuLablancaItem[1] = (TextView) findViewById(R.id.menu_lablanca_gm_schedule);

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
			public void onClick(View v) { loadSection(GM.SECTION_SETTINGS, true); }
		});


		SharedPreferences preferences = getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
		if (preferences.getInt(GM.PREF_DB_FESTIVALS, 0) == 1) {

			menuLablancaItem[0].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					loadSection(GM.SECTION_LABLANCA_SCHEDULE, true);
				}
			});

			menuLablancaItem[1].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					loadSection(GM.SECTION_LABLANCA_GM_SCHEDULE, true);
				}
			});

			//Show the entries
			for (int i = 0; i < 2; i ++){
				menuLablancaItem[i].setVisibility(View.VISIBLE);
			}
		}

		//If the user code is not set, generate one
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
						Log.e("Acton", action);
						switch (action) {

							case GM.EXTRA_ACTION_LABLANCA:

								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_lablanca));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION_LABLANCA, false);
									}
								});
								break;

							case GM.EXTRA_ACTION_LOCATION:

								//TODO: Check for a recent location!
								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_location));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION_LOCATION, false);
									}
								});
								break;

							case GM.EXTRA_ACTION_BLOG:

								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_blog));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION_BLOG, false);
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
										loadSection(GM.SECTION_ACTIVITIES, false);
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
										loadSection(GM.SECTION_GALLERY, false);
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
											loadSection(GM.SECTION_LABLANCA_GM_SCHEDULE, false);
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
											loadSection(GM.SECTION_LABLANCA_SCHEDULE, false);
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
		FragmentManager fm = getFragmentManager();
		if (fm.getBackStackEntryCount() > 1){
			fm.popBackStack();
		}
		else{
			super.onBackPressed();
		}
		
	}
	
}
