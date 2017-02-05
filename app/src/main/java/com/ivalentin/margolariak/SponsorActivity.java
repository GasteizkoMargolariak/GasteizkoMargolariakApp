package com.ivalentin.margolariak;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

/**
 * Created by seavenois on 26/09/16.
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
			entry = (LinearLayout) factory.inflate(R.layout.row_sponsor, null);

			//Set margins TODO: I dont know why it doesnt read margins from the xml
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			int margin = (int) (9 * getResources().getDisplayMetrics().density);
			layoutParams.setMargins(margin, margin, margin, margin);
			entry.setLayoutParams(layoutParams);

			//Set title
			TextView tvTitle = (TextView) entry.findViewById(R.id.tv_row_sponsor_name);
			tvTitle.setText(cursor.getString(1));

			//Set text
			TextView tvText = (TextView) entry.findViewById(R.id.tv_row_sponsor_text);
			tvText.setText(cursor.getString(2));

			//Set image
			ImageView ivImage = (ImageView) entry.findViewById(R.id.iv_row_sponsor_image);
			//Check if image exists
			if (cursor.getString(3) != null && "".equals(cursor.getString(3)) == false) {
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


			//TODO: Set onClick listener



			llList.addView(entry);
		}
		cursor.close();
		db.close();

	}


	/**
	 * Not called from code, but referenced from activity_sponsor.xml.
	 * Finishes the activity.
	 *
	 * @param v Ignored
	 *
	 * @see Activity#finish()
	 */
	public void finish(View v){
		finish();
	}

}
