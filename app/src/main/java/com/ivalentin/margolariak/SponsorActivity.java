package com.ivalentin.margolariak;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.io.File;

/**
 * Activity that lists sponsors.
 *
 * @author Iñigo Valentin
 *
 * @see Activity
 *
 */
public class SponsorActivity extends Activity {

	/**
	 * Runs when the activity is created.
	 *
	 * @param savedInstanceState Saved state of the activity.
	 * @see Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		//Remove title bar.
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		//Set layout.
		setContentView(R.layout.activity_sponsor);

		//Assign menu items
		LinearLayout llList = (LinearLayout) findViewById(R.id.ll_sponsor_list);
		LinearLayout entry;
		LayoutInflater factory = LayoutInflater.from(this);

		//Get data from database
		SQLiteDatabase db = SQLiteDatabase.openDatabase(getDatabasePath(GM.DB.NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
		final Cursor cursor;
		String lang = GM.getLang();
		cursor = db.rawQuery("SELECT id, name_" + lang + " AS name, text_" + lang + " AS text, image, address_" + lang + " AS address, link, lat, lon FROM sponsor ORDER BY RANDOM();", null);
		cursor.moveToFirst();

		while (cursor.moveToNext()) {
			//Create a new row
			entry = (LinearLayout) factory.inflate(R.layout.row_sponsor, llList, false);

			//Set margins
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			int margin = (int) (9 * getResources().getDisplayMetrics().density);
			layoutParams.setMargins(margin, margin, margin, margin);
			entry.setLayoutParams(layoutParams);

			//Set id (hidden)
			TextView tvId = (TextView) entry.findViewById(R.id.tv_row_sponsor_id);
			tvId.setText(cursor.getString(0));

			//Set title
			TextView tvTitle = (TextView) entry.findViewById(R.id.tv_row_sponsor_name);
			tvTitle.setText(cursor.getString(1));

			//Set text
			TextView tvText = (TextView) entry.findViewById(R.id.tv_row_sponsor_text);
			tvText.setText(cursor.getString(2));

			//Set image
			ImageView ivImage = (ImageView) entry.findViewById(R.id.iv_row_sponsor_image);
			//Check if image exists
			if (cursor.getString(3) != null && !"".equals(cursor.getString(3))) {
				String image = cursor.getString(3);
				File f;
				f = new File(getFilesDir().toString() + "/img/spo/preview/" + image);
				if (f.exists()) {
					//If the image exists, set it.
					Bitmap myBitmap = BitmapFactory.decodeFile(getFilesDir().toString() + "/img/spo/preview/" + image);
					ivImage.setImageBitmap(myBitmap);
				} else {
					//If not, create directories and download asynchronously
					File fpath;
					fpath = new File(getFilesDir().toString() + "/img/spo/preview/");
					//noinspection ResultOfMethodCallIgnored
					fpath.mkdirs();
					new DownloadImage(GM.API.SERVER + "/img/spo/preview/" + image, getFilesDir().toString() + "/img/spo/preview/" + image, ivImage, GM.IMG.SIZE.PREVIEW).execute();
				}
			}
			else{
				ivImage.setVisibility(View.GONE);
			}

			//Set onClick listener
			entry.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TextView tvId = (TextView) v.findViewById(R.id.tv_row_sponsor_id);
					int id = Integer.parseInt(tvId.getText().toString());
					openDialog(id);
				}
			});


			llList.addView(entry);
		}
		cursor.close();
		db.close();

	}

	/**
	 * Shows a dialog with info about the selected event.
	 *
	 * @param id The event id
	 */
	@SuppressWarnings("ConstantConditions")
	private void openDialog(final int id){

		//Create the dialog
		final Dialog dialog = new Dialog(this);

		//Set up dialog window
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_sponsor);

		//Set the custom dialog components - text, image and button
		TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_dialog_sponsor_title);
		TextView tvText = (TextView) dialog.findViewById(R.id.tv_dialog_sponsor_text);
		ImageView ivImage = (ImageView) dialog.findViewById(R.id.iv_dialog_sponsor_image);
		TextView tvUrl = (TextView) dialog.findViewById(R.id.tv_dialog_sponsor_url);
		TextView tvAddress = (TextView) dialog.findViewById(R.id.tv_dialog_sponsor_address);
		Button btClose = (Button) dialog.findViewById(R.id.bt_dialog_sponsor_close);
		LinearLayout llUrl = (LinearLayout) dialog.findViewById(R.id.ll_dialog_sponsor_url);
		LinearLayout llAddress = (LinearLayout) dialog.findViewById(R.id.ll_dialog_sponsor_address);

		//Get info about the event
		SQLiteDatabase db = SQLiteDatabase.openDatabase(getDatabasePath(GM.DB.NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
		String lang = GM.getLang();
		Cursor cursor = db.rawQuery("SELECT id, name_" + lang + " AS name, text_" + lang + " AS text, image, address_" + lang + " AS address, link, lat, lon FROM sponsor WHERE id = " + id + ";", null);
		if (cursor.getCount() > 0){
			cursor.moveToNext();

			//Set title
			tvTitle.setText(cursor.getString(1));

			//Set description
			tvText.setText(cursor.getString(2));

			//Set image (if any)
			if (cursor.getString(3) != null && !"".equals(cursor.getString(3))){
				File f;
				f = new File(getFilesDir().toString() + "/img/spo/preview/" + cursor.getString(3));
				if (f.exists()){
					//If the image exists, set it.
					Bitmap myBitmap = BitmapFactory.decodeFile(getFilesDir().toString() + "/img/spo/preview/" + cursor.getString(3));
					ivImage.setImageBitmap(myBitmap);
				}
				else {
					//If not, create directories and download asynchronously
					File fpath;
					fpath = new File(getFilesDir().toString() + "/img/spo/preview/");
					//noinspection ResultOfMethodCallIgnored
					fpath.mkdirs();
					new DownloadImage(GM.API.SERVER + "/img/spo/preview/" + cursor.getString(3), getFilesDir().toString() + "/img/spo/preview/" + cursor.getString(3), ivImage, GM.IMG.SIZE.PREVIEW).execute();
				}
				ivImage.setVisibility(View.VISIBLE);
			}
			else{
				ivImage.setVisibility(View.GONE);
			}

			//Set address (if any)
			if (cursor.getString(4) != null && !"".equals(cursor.getString(4))){
				final String lat = cursor.getString(6);
				final String lon = cursor.getString(7);
				tvAddress.setText(cursor.getString(4));
				tvAddress.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String url = "https://www.google.com/maps/preview/@" + lat + "," + lon + ",8z";
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
					}
				});
			}
			else{
				llAddress.setVisibility(View.GONE);
			}

			//Set url (if any)
			if (cursor.getString(5) != null && !"".equals(cursor.getString(3))){
				final String url = cursor.getString(5);
				tvUrl.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
					}
				});
			}
			else{
				llUrl.setVisibility(View.GONE);
			}

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
			dialog.getWindow().setAttributes(lp);

			//Show dialog
			dialog.show();


		}
	}


	/**
	 * Not called from code, but referenced from activity_sponsor.xml.
	 * Finishes the activity.
	 *
	 * @param v Ignored
	 *
	 * @see Activity#finish()
	 */
	public void finish(@SuppressWarnings("unused") View v){
		finish();
	}

}
