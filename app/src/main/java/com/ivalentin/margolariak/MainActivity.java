package com.ivalentin.margolariak;

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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * The main Activity of the app. It's actually the only content activity, and it loads other fragments.
 *
 * @author Iñigo Valentin
 */
public class MainActivity extends Activity {

	//Alarm to get location and notifications.
	private AlarmReceiver notificationAlarm;

	private TextView menuText[];
	private ImageView menuImage[];

	//Handler to periodically check for location updates
	private final Handler locationHandler = new Handler();
	// Code to check for location updates
	private MainActivity activity = this;
	private final Runnable checkForLocation = new Runnable() {
		@Override
		public void run() {

			new ReceiveLocation(activity).execute();

			//Repeat this the same runnable code block again another th specified interval
			locationHandler.postDelayed(checkForLocation, GM.LOCATION.INTERVAL);
		}
	};

	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	/**
	 * Enables o disables the location section.
	 *
	 * Shows or hiddes the menu entry and the home fragment,
	 *
	 * @param report True to show, false to hide.
	 */
	public void gotLocation(boolean report){
		if (report) {
			findViewById(R.id.rl_menu_location).setVisibility(View.VISIBLE);
			if (findViewById(R.id.ll_home_section_location) != null){
				findViewById(R.id.ll_home_section_location).setVisibility(View.VISIBLE);
			}
		}
		else {
			findViewById(R.id.rl_menu_location).setVisibility(View.GONE);
			if (findViewById(R.id.ll_home_section_location) != null) {
				findViewById(R.id.ll_home_section_location).setVisibility(View.GONE);
			}
		}
	}

	//Not referenced in code.
	public void showMenu(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu, popup.getMenu());

		popup.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(MainActivity.this, AboutActivity.class);
				startActivity(intent);
				return true;
			}
		});

		popup.getMenu().getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(MainActivity.this, SponsorActivity.class);
				startActivity(intent);
				return true;
			}
		});

		popup.getMenu().getItem(2).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
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
	public void loadSection(byte section) {
		FragmentManager fm = MainActivity.this.getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment = null;
		String title = "";
		Bundle bundle = new Bundle();

		//Get measures in dp
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int dp7 = (int) (8 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
		int dp3 = (int) (4 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));

		//Reset all tabs to it's original state
		for (int i = 0; i < 6; i++) {
			menuText[i].setTypeface(null, Typeface.NORMAL);
			menuImage[i].requestLayout();
			menuImage[i].getLayoutParams().height = dp3;
		}

		switch (section) {
			case GM.SECTION.HOME:
				fragment = new HomeLayout();
				title = getString(R.string.menu_home);
				menuText[0].setTypeface(null, Typeface.BOLD);
				menuImage[0].getLayoutParams().height = dp7;
				break;

			case GM.SECTION.LOCATION:
				fragment = new LocationLayout();
				title = getString(R.string.menu_location);
				menuText[1].setTypeface(null, Typeface.BOLD);
				menuImage[1].getLayoutParams().height = dp7;
				break;

			case GM.SECTION.LABLANCA:
				//Get settings
				SharedPreferences sharedData = getSharedPreferences(GM.DATA.DATA, Context.MODE_PRIVATE);
				if (sharedData.getBoolean(GM.DATA.KEY.LABLANCA, GM.DATA.DEFAULT.LABLANCA)) {
					fragment = new LablancaLayout();
				} else {
					fragment = new LablancaNoFestivalsLayout();
				}
				title = getString(R.string.menu_lablanca);
				menuText[2].setTypeface(null, Typeface.BOLD);
				menuImage[2].getLayoutParams().height = dp7;
				break;

			case GM.SECTION.SCHEDULE:
				fragment = new ScheduleLayout();
				bundle.putInt(GM.SCHEDULE.KEY, GM.SCHEDULE.MARGOLARIAK);
				fragment.setArguments(bundle);
				title = getString(R.string.menu_lablanca_schedule);
				menuText[2].setTypeface(null, Typeface.BOLD);
				menuImage[2].getLayoutParams().height = dp7;
				break;

			case GM.SECTION.GM_SCHEDULE:
				fragment = new ScheduleLayout();
				bundle.putInt(GM.SCHEDULE.KEY, GM.SCHEDULE.MARGOLARIAK);
				fragment.setArguments(bundle);
				title = getString(R.string.menu_lablanca_gm_schedule);
				menuText[2].setTypeface(null, Typeface.BOLD);
				menuImage[2].getLayoutParams().height = dp7;
				break;

			case GM.SECTION.ACTIVITIES:
				fragment = new ActivityLayout();
				title = getString(R.string.menu_activities);
				menuText[3].setTypeface(null, Typeface.BOLD);
				menuImage[3].getLayoutParams().height = dp7;
				break;

			case GM.SECTION.BLOG:
				fragment = new BlogLayout();
				title = getString(R.string.menu_blog);
				menuText[4].setTypeface(null, Typeface.BOLD);
				menuImage[4].getLayoutParams().height = dp7;
				break;

			case GM.SECTION.GALLERY:
				fragment = new GalleryLayout();
				title = getString(R.string.menu_blog);
				menuText[5].setTypeface(null, Typeface.BOLD);
				menuImage[5].getLayoutParams().height = dp7;
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
	public void setSectionTitle(String title) {
		TextView tvTitle = (TextView) findViewById(R.id.activity_main_content_title);
		tvTitle.setText(title);
	}

	/**
	 * Runs when the activity is created.
	 *
	 * @param savedInstanceState Saved state of the activity.
	 * @see Activity#onCreate(Bundle)
	 */
	@SuppressWarnings("deprecation, ConstantConditions")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		//Get intent extras
		String action = getIntent().getStringExtra(GM.EXTRA.ACTION);
		String actionText = getIntent().getStringExtra(GM.EXTRA.TEXT);
		String actionTitle = getIntent().getStringExtra(GM.EXTRA.TITLE);

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
		RelativeLayout[] menuItem = new RelativeLayout[6];
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
			public void onClick(View v) {
				loadSection(GM.SECTION.HOME);
			}
		});
		menuItem[1].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadSection(GM.SECTION.LOCATION);
			}
		});
		menuItem[2].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadSection(GM.SECTION.LABLANCA);
			}
		});
		menuItem[3].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadSection(GM.SECTION.ACTIVITIES);
			}
		});
		menuItem[4].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadSection(GM.SECTION.BLOG);
			}
		});
		menuItem[5].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadSection(GM.SECTION.GALLERY);
			}
		});

		//Get preferences
		SharedPreferences sharedData = getSharedPreferences(GM.PREFERENCES.PREFERNCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor dataEditor = sharedData.edit();

		//If the user code is not set, generate one
		if (sharedData.getString(GM.DATA.KEY.USER, GM.DATA.DEFAULT.USER).length() == GM.DATA.DEFAULT.USER.length()) {
			SecureRandom random = new SecureRandom();
			String newCode = new BigInteger(130, random).toString(32).substring(0, 16);
			dataEditor.putString(GM.DATA.KEY.USER, newCode);
			dataEditor.apply();
		}

		//Cerate database if it's not already created
		createDatabase();

		//If the database has just been updated, recreate the database
		if (sharedData.getInt(GM.DATA.KEY.PREVIOUS_APP_VERSION, GM.DATA.DEFAULT.PREVIOUS_APP_VERSION) < BuildConfig.VERSION_CODE) {
			Log.d("UPDATE", "App updated from version " + sharedData.getInt(GM.DATA.KEY.PREVIOUS_APP_VERSION, GM.DATA.DEFAULT.PREVIOUS_APP_VERSION) + " to " + BuildConfig.VERSION_CODE + ". Forcing a new sync...");
			deleteDatabase();
			dataEditor.putInt(GM.DATA.KEY.PREVIOUS_APP_VERSION, BuildConfig.VERSION_CODE);
			dataEditor.apply();
			createDatabase();
			initialSync();
		}
		else {
			//Sync db
			sync();

			//Load initial section
			loadSection(GM.SECTION.HOME);

			//If the intent had extras (from notifications), do something
			if (actionText != null) {
				TextView tvDialogTitle, tvDialogText;
				Button btDialogClose, btDialogAction;
				Drawable dialogIcon;
				if (actionTitle != null) {

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
					boolean festivals = sharedData.getBoolean(GM.DATA.KEY.LABLANCA, GM.DATA.DEFAULT.LABLANCA);

					//Set the action button
					btDialogAction = (Button) dialog.findViewById(R.id.bt_dialog_notification_action);
					if (action != null) {
						switch (action) {

							case GM.EXTRA.SECTION.LABLANCA:

								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_lablanca));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION.LABLANCA);
									}
								});
								break;

							case GM.EXTRA.SECTION.LOCATION:

								//TODO: Read from database
								/*if (!sharedData.getString(GM.PREF_GM_LOCATION, GM.DEFAULT_PREF_GM_LOCATION).equals(GM.DEFAULT_PREF_GM_LOCATION)) {
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

								}*/
								break;

							case GM.EXTRA.SECTION.BLOG:

								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_blog));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION.BLOG);
									}
								});
								break;

							case GM.EXTRA.SECTION.ACTIVITIES:

								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_activities));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION.ACTIVITIES);
									}
								});
								break;

							case GM.EXTRA.SECTION.GALLERY:

								//Set up the action button
								btDialogAction.setVisibility(View.VISIBLE);
								btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_gallery));
								btDialogAction.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										loadSection(GM.SECTION.GALLERY);
									}
								});
								break;

							case GM.EXTRA.SECTION.GMSCHEDULE:

								if (festivals) {
									//Set up the action button
									btDialogAction.setVisibility(View.VISIBLE);
									btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_gmschedule));
									btDialogAction.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											dialog.dismiss();
											loadSection(GM.SECTION.GM_SCHEDULE);
										}
									});
								}
								break;

							case GM.EXTRA.SECTION.SCHEDULE:
								if (festivals) {
									//Set up the action button
									btDialogAction.setVisibility(View.VISIBLE);
									btDialogAction.setText(this.getApplicationContext().getString(R.string.notification_action_cityschedule));
									btDialogAction.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											dialog.dismiss();
											loadSection(GM.SECTION.SCHEDULE);
										}
									});
								}
								break;

							//If the notification is just text
							default:
								//Hide action button
								btDialogAction.setVisibility(View.GONE);
						}
					} else {
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

					dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

					//Show dialog
					dialog.show();
				}
			}
		}

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	/**
	 * Creates the app database and fills it with the hard coded, default data.
	 */
	private void createDatabase() {
		SQLiteDatabase db;
		try {
			//Create database
			db = openOrCreateDatabase(GM.DB.NAME, Activity.MODE_PRIVATE, null);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY_COMMENT);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY_IMAGE);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY_ITINERARY);
			db.execSQL(GM.DB.QUERY.CREATE.ACTIVITY_TAG);
			db.execSQL(GM.DB.QUERY.CREATE.ALBUM);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL_DAY);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL_EVENT);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL_EVENT_IMAGE);
			db.execSQL(GM.DB.QUERY.CREATE.FESTIVAL_OFFER);
			db.execSQL(GM.DB.QUERY.CREATE.LOCATION);
			db.execSQL(GM.DB.QUERY.CREATE.NOTIFICATION);
			db.execSQL(GM.DB.QUERY.CREATE.PEOPLE);
			db.execSQL(GM.DB.QUERY.CREATE.PHOTO);
			db.execSQL(GM.DB.QUERY.CREATE.PHOTO_ALBUM);
			db.execSQL(GM.DB.QUERY.CREATE.PHOTO_COMMENT);
			db.execSQL(GM.DB.QUERY.CREATE.PLACE);
			db.execSQL(GM.DB.QUERY.CREATE.POST);
			db.execSQL(GM.DB.QUERY.CREATE.POST_COMMENT);
			db.execSQL(GM.DB.QUERY.CREATE.POST_IMAGE);
			db.execSQL(GM.DB.QUERY.CREATE.POST_TAG);
			//db.execSQL(GM.DB.QUERY.CREATE.SETTINGS);
			db.execSQL(GM.DB.QUERY.CREATE.SPONSOR);
			db.execSQL(GM.DB.QUERY.CREATE.VERSION);
			db.close();
		} catch (Exception ex) {
			Log.e("Error creating database", ex.toString());
		}
	}

	/**
	 * Creates the app database and fills it with the hard coded, default data.
	 */
	private void deleteDatabase() {
		try {
			getApplicationContext().deleteDatabase(GM.DB.NAME);
		} catch (Exception ex) {
			Log.e("Error deleting database", ex.toString());
		}
	}

	/**
	 * Performs a full sync against the remote database.
	 */
	private void sync() {
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
	private void initialSync() {
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
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
	 * @see AppCompatActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		try {
			FragmentManager fm = getFragmentManager();
			if (fm.getBackStackEntryCount() > 1) {
				fm.popBackStack();
			} else {
				finish();
				super.onBackPressed();
			}
		} catch (Exception ex) {
			Log.e("Error going back", "Finishing activity: " + ex.toString());
			locationHandler.removeCallbacks(checkForLocation);
			finish();
			//super.onBackPressed();
		}

	}

	/**
	 * Overrides onKeyUp().
	 * Used to toggle the menu whent the key is pressed.
	 *
	 * @see Activity#onKeyUp(int, KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			showMenu(this.findViewById(R.id.bt_menu));
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * Overrides onResume().
	 * Resumes the location handler to check for location regularly.
	 *
	 * @see Activity#onResume()
	 */
	@Override
	public void onResume() {
		locationHandler.post(checkForLocation);
		super.onResume();
	}

	/**
	 * Overrides onPause().
	 * Stops the location handler to check for location regularly.
	 *
	 * @see Activity#onPause()
	 */
	@Override
	public void onPause() {
		locationHandler.removeCallbacks(checkForLocation);
		super.onPause();
	}

	/**
	 * Overrides onDestroy().
	 * Stops the location handler to check for location regularly.
	 *
	 * @see Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		locationHandler.removeCallbacks(checkForLocation);
		super.onDestroy();
	}

	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	public Action getIndexApiAction() {
		Thing object = new Thing.Builder()
				.setName("Main Page") // TODO: Define a title for the content shown.
				// TODO: Make sure this auto-generated URL is correct.
				.setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
				.build();
		return new Action.Builder(Action.TYPE_VIEW)
				.setObject(object)
				.setActionStatus(Action.STATUS_TYPE_COMPLETED)
				.build();
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		AppIndex.AppIndexApi.start(client, getIndexApiAction());
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		AppIndex.AppIndexApi.end(client, getIndexApiAction());
		client.disconnect();
	}
}
