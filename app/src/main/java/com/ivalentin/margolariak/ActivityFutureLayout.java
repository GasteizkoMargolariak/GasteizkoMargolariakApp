package com.ivalentin.margolariak;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
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
public class ActivityFutureLayout extends Fragment {

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
        SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
        final Cursor cursor;
        String currLang = Locale.getDefault().getDisplayLanguage();
        if (!currLang.equals("es") && !currLang.equals("eu")){
            currLang = "en";
        }

        cursor = db.rawQuery("SELECT id, title_" + currLang+ " AS title, text_" + currLang + " AS text, date, city, price FROM activity WHERE id = " + id + ";", null);
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
        wvText.loadDataWithBaseURL(null, cursor.getString(2), "text/html", "utf-8", null);
        tvDate.setText(GM.formatDate(cursor.getString(3) + " 00:00:00", currLang, false));
        tvPrice.setText(String.format(getString(R.string.price), cursor.getInt(5)));
        tvCity.setText(cursor.getString(4));

        //Get images
        Cursor imageCursor = db.rawQuery("SELECT image, idx FROM activity_image WHERE activity = " + id + " ORDER BY idx LIMIT 5;", null);
        int i = 0;
        while (imageCursor.moveToNext()) {
            String image = imageCursor.getString(0);

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
                new DownloadImage(GM.SERVER + "/img/actividades/preview/" + image, this.getActivity().getFilesDir().toString() + "/img/actividades/preview/" + image, images[i]).execute();
            }
            images[i].setVisibility(View.VISIBLE);
            i ++;
        }
        imageCursor.close();
        cursor.close();

        //Get itinerary
        Cursor cursorItinerary = db.rawQuery("SELECT id, activity_schedule.name_" + currLang + " AS name, description_" + currLang + " AS description, start, place.name_" + currLang + " AS placename, address_" + currLang + " AS address, lat, lon FROM activity_schedule, place WHERE activity_schedule.place = place.id AND activity = " + id + ";", null);
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
                tvSchTime.setText(cursor.getString(3).substring(11, 16));

                //Set title
                TextView tvSchTitle = (TextView) entry.findViewById(R.id.tv_row_activity_itinerary_title);
                tvSchTitle.setText(cursor.getString(1));

                //Set description
                TextView tvSchDescription = (TextView) entry.findViewById(R.id.tv_row_activity_itinerary_description);
                if (!cursor.getString(1).equals(cursor.getString(2))){
                    tvSchDescription.setVisibility(View.GONE);
                }
                else {
                    tvSchTitle.setText(cursor.getString(2));
                }

                //Set place
                TextView tvSchPlace = (TextView) entry.findViewById(R.id.tv_row_activity_itinerary_place);
                tvSchPlace.setText(cursor.getString(4));

                //Set address
                TextView tvSchAddress = (TextView) entry.findViewById(R.id.tv_row_activity_itinerary_address);
                if (!cursor.getString(4).equals(cursor.getString(5))){
                    tvSchAddress.setVisibility(View.GONE);
                }
                else {
                    tvSchAddress.setText(cursor.getString(5));
                }

                //TODO: Add an onclick listener on image that shows a map

                list.addView(entry);
            }
        }


        cursorItinerary.close();
        db.close();
        return view;
    }
}