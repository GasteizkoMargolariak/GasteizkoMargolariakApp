package com.ivalentin.margolariak;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Calendar;

/**
 * Fragment opened for La Blanca sections while the festivals are not close.
 *
 * @see Fragment
 *
 * @author Iñigo Valentin
 *
 */
public class LablancaLayout extends Fragment {

	/**
	 * Run when the fragment is inflated.
	 *
	 * @param inflater A LayoutInflater to manage views
	 * @param container The container View
	 * @param savedInstanceState Bundle containing the state
	 * @return The fragment view
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//Load the layout
		View view = inflater.inflate(R.layout.fragment_layout_lablanca, container, false);

		//Set the title
		((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_lablanca));

		//Get database info from the database
		String lang = GM.getLang();
		int year = Calendar.getInstance().get(Calendar.YEAR);
		SQLiteDatabase db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath(GM.DB.NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("SELECT text_" + lang + ", img FROM festival WHERE year = " + year + ";", null);

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();

			//Set text
			WebView headerText = (WebView) view.findViewById(R.id.wv_lablanca_header);

			headerText.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			headerText.loadDataWithBaseURL(null, cursor.getString(0), "text/html", "utf-8", null);

			//Set image
			ImageView headerImage = (ImageView) view.findViewById(R.id.iv_lablanca_header);
			String image = cursor.getString(1);
			if (image != null && image.length() > 0) {

				//Check if image exists
				File f;
				f = new File(this.getActivity().getFilesDir().toString() + "/img/fiestas/preview/" + image);
				if (f.exists()) {
					//If the image exists, set it.
					Bitmap myBitmap = BitmapFactory.decodeFile(this.getActivity().getFilesDir().toString() + "/img/fiestas/preview/" + image);
					headerImage.setImageBitmap(myBitmap);
				} else {
					//If not, create directories and download asynchronously
					File fpath;
					fpath = new File(this.getActivity().getFilesDir().toString() + "/img/fiestas/preview/");
					if (fpath.mkdirs()) {
						new DownloadImage(GM.API.SERVER + "/img/fiestas/preview/" + image, this.getActivity().getFilesDir().toString() + "/img/fiestas/preview/" + image, headerImage, GM.IMG.SIZE.PREVIEW).execute();
					}
				}
			} else {
				headerImage.setVisibility(View.GONE);
			}

			//Get prices for single days
			Cursor cursorDays = db.rawQuery("SELECT date, name_" + lang + ", price FROM festival_day WHERE strftime('%Y', date) = '" + year + "' ORDER BY date;", null);
			LinearLayout list = (LinearLayout) view.findViewById(R.id.ll_lablanca_prices_days_list);
			LinearLayout entry;
			LayoutInflater factory = LayoutInflater.from(getActivity());
			while (cursorDays.moveToNext()) {

				//Create a new row
				entry = (LinearLayout) factory.inflate(R.layout.row_festival_price_day, list, false);

				//Set margins
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(10, 10, 10, 25);
				entry.setLayoutParams(layoutParams);

				//Set title
				TextView tvName = (TextView) entry.findViewById(R.id.tv_row_festival_price_day_name);
				tvName.setText(cursorDays.getString(1));

				//Set price
				TextView tvPrice = (TextView) entry.findViewById(R.id.tv_row_festival_price_day_price);
				String price = cursorDays.getString(2) + " " + getString(R.string.eur);
				tvPrice.setText(price);

				//Set date
				TextView tvDate = (TextView) entry.findViewById(R.id.tv_row_festival_price_day_date);
				tvDate.setText(GM.formatDate(cursorDays.getString(0) + " 00:00:00", lang, false, false, false));
				/*DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				Date date;
				try {
					date = format.parse(cursorDays.getString(0));
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					int day = calendar.get(Calendar.DAY_OF_MONTH);
					int month = calendar.get(Calendar.MONTH);
					switch (month){
						case 6:
							tvDate.setText(String.format(getString(R.string.lablanca_date_july), String.valueOf(day)));
							break;
						case 7:
							tvDate.setText(String.format(getString(R.string.lablanca_date_august), String.valueOf(day)));
							break;
					}
				}
				catch(Exception ex){
					Log.e("Date format error", ex.toString());
				}*/

				list.addView(entry);
			}

			//Get prices for single days
			Cursor cursorPacks = db.rawQuery("SELECT name_" + lang + ", description_" + lang + ", price FROM festival_offer WHERE year = " + year + " ORDER BY days;", null);
			list = (LinearLayout) view.findViewById(R.id.ll_lablanca_prices_packs_list);
			while (cursorPacks.moveToNext()) {

				//Create a new row
				entry = (LinearLayout) factory.inflate(R.layout.row_festival_price_pack, list, false);

				//Set margins
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(10, 10, 10, 25);
				entry.setLayoutParams(layoutParams);

				//Set title
				TextView tvName = (TextView) entry.findViewById(R.id.tv_row_festival_price_pack_name);
				tvName.setText(cursorPacks.getString(0));

				//Set text
				TextView tvText = (TextView) entry.findViewById(R.id.tv_row_festival_price_pack_text);
				tvText.setText(cursorPacks.getString(1));

				//Set price
				TextView tvPrice = (TextView) entry.findViewById(R.id.tv_row_festival_price_pack_price);
				String price = cursorPacks.getString(2) + " " + getString(R.string.eur);
				tvPrice.setText(price);

				list.addView(entry);
			}

			cursorDays.close();
			cursorPacks.close();
		}

		cursor.close();
		db.close();

		//Set listeners for the schedule buttons.
		Button gmSchedule = (Button) view.findViewById(R.id.bt_lablanca_schedule_gm);
		Button citySchedule = (Button) view.findViewById(R.id.bt_lablanca_schedule_city);

		gmSchedule.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).loadSection(GM.SECTION.GM_SCHEDULE);
			}
		});

		citySchedule.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity) getActivity()).loadSection(GM.SECTION.SCHEDULE);
			}
		});

		return view;
	}
}