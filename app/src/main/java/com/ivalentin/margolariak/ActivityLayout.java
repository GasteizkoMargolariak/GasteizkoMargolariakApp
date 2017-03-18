package com.ivalentin.margolariak;


import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

/**
 * Fragment to display activities.
 *
 * @author Iñigo Valentin
 *
 * @see Fragment
 *
 */
@SuppressLint("ResultOfMethodCallIgnored")
public class ActivityLayout extends Fragment{

    /**
     * Run when the fragment is inflated.
     * Assigns views, gets the date and does the first call to the {@link #populate} function.
     *
     * @param inflater A LayoutInflater to manage views
     * @param container The container View
     * @param savedInstanceState Bundle containing the state
     *
     * @return The fragment view
     *
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @SuppressLint("InflateParams") //Throws unknown error when done properly.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Load the layout
        View view = inflater.inflate(R.layout.fragment_layout_activities, null);

        //Set the title
        ((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_activities));

        populate(view);
        return view;
    }

    /**
     * Populates the list of activities.
     * It uses the default layout as parent.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("InflateParams") //Throws unknown error when done properly.
    private void populate(View view){
        LinearLayout llListFuture = (LinearLayout) view.findViewById(R.id.ll_activities_future_list);
        LinearLayout llListPast = (LinearLayout) view.findViewById(R.id.ll_activities_past_list);
        LinearLayout entry;

        //An inflater
        LayoutInflater factory = LayoutInflater.from(getActivity());

        SQLiteDatabase db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath(GM.DB.NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
        final Cursor cursor;
        String lang = GM.getLang();

        //Get data from the database of the future activities
        cursor = db.rawQuery("SELECT id, date, city, title_" + lang+ " AS title, text_" + lang + " AS text, price FROM activity WHERE date >= date('now') ORDER BY date DESC;", null);
        if (cursor.getCount() > 0) {
            TextView tvNoFuture = (TextView) view.findViewById(R.id.tv_activities_no_future);
            tvNoFuture.setVisibility(View.GONE);

            //Print rows
            while (cursor.moveToNext()) {
                //Create a new row
                entry = (LinearLayout) factory.inflate(R.layout.row_activity_future, null);

                //Set margins TODO: I dont know why it doesnt read margins from the xml
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                int margin = (int) (9 * getActivity().getResources().getDisplayMetrics().density);
                layoutParams.setMargins(margin, margin, margin, margin);
                entry.setLayoutParams(layoutParams);

                //Set title
                TextView tvTitle = (TextView) entry.findViewById(R.id.tv_row_activity_future_title);
                tvTitle.setText(cursor.getString(3));

                //Set city
                TextView tvCity = (TextView) entry.findViewById(R.id.tv_row_activity_future_city);
                tvCity.setText(cursor.getString(2));

                //Set price
                TextView tvPrice = (TextView) entry.findViewById(R.id.tv_row_activity_future_price);
                tvPrice.setText(String.format(getString(R.string.price), cursor.getInt(5)));

                //Set text
                String text = Html.fromHtml(cursor.getString(4)).toString();
                TextView tvText = (TextView) entry.findViewById(R.id.tv_row_activity_future_text);
                tvText.setText(text);

                //Set date
                TextView tvDate = (TextView) entry.findViewById(R.id.tv_row_activity_future_date);
                tvDate.setText(GM.formatDate(cursor.getString(1) + " 00:00:00", lang, false));

                //Set hidden id
                TextView tvId = (TextView) entry.findViewById(R.id.tv_row_activity_future_hidden);
                tvId.setText(cursor.getString(0));

                //Get image
                ImageView iv = (ImageView) entry.findViewById(R.id.iv_row_activity_future);
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
                        new DownloadImage(GM.API.SERVER + "/img/actividades/miniature/" + image, this.getActivity().getFilesDir().toString() + "/img/actividades/miniature/" + image, iv, GM.IMG.SIZE.MINIATURE).execute();
                    }
                }
                cursorImage.close();

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

                        FragmentManager fm = ActivityLayout.this.getActivity().getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();

                        ft.replace(R.id.activity_main_content_fragment, fragment);
                        ft.addToBackStack("activity_" + id);
                        ft.commit();
                    }
                });

                //Add to the list
                llListFuture.addView(entry);
            }
            cursor.close();

        }

        //Get data from the database of the past activities
        Cursor cursorPast = db.rawQuery("SELECT id, date, city, title_" + lang + " AS title, text_" + lang + " AS text, after_" + lang + " AS after FROM activity WHERE date < date('now') ORDER BY date DESC;", null);


        //Print rows
        while (cursorPast.moveToNext()) {
            //Create a new row
            entry = (LinearLayout) factory.inflate(R.layout.row_activity_past, null);

            //Set margins TODO: I dont know why it doesnt read margins from the xml
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int margin = (int) (9 * getActivity().getResources().getDisplayMetrics().density);
            layoutParams.setMargins(margin, margin, margin, margin);
            entry.setLayoutParams(layoutParams);

            //Set title
            TextView tvTitle = (TextView) entry.findViewById(R.id.tv_row_activity_past_title);
            tvTitle.setText(cursorPast.getString(3));

            //Set text
            String text;
            if (cursorPast.getString(5) == null){
                text = Html.fromHtml(cursorPast.getString(4)).toString();
            }
            else {
                if (cursorPast.getString(5) == null || cursorPast.getString(5).length() < 1) {
                    text = Html.fromHtml(cursorPast.getString(4)).toString();
                } else {
                    text = Html.fromHtml(cursorPast.getString(5)).toString();
                }
            }
            TextView tvText = (TextView) entry.findViewById(R.id.tv_row_activity_past_text);
            tvText.setText(text);

            //Set date
            TextView tvDate = (TextView) entry.findViewById(R.id.tv_row_activity_past_date);
            tvDate.setText(GM.formatDate(cursorPast.getString(1) + " 00:00:00", lang, false));

            //Set hidden id
            TextView tvId = (TextView) entry.findViewById(R.id.tv_row_activity_past_hidden);
            tvId.setText(cursorPast.getString(0));

            //Get image
            ImageView iv = (ImageView) entry.findViewById(R.id.iv_row_activity_past);
            Cursor cursorImage = db.rawQuery("SELECT image FROM activity_image WHERE activity = " + cursorPast.getString(0) + " ORDER BY idx LIMIT 1;", null);
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
                    new DownloadImage(GM.API.SERVER + "/img/actividades/miniature/" + image, this.getActivity().getFilesDir().toString() + "/img/actividades/miniature/" + image, iv, GM.IMG.SIZE.MINIATURE).execute();
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
                int id = Integer.parseInt(((TextView) v.findViewById(R.id.tv_row_activity_past_hidden)).getText().toString());
                bundle.putInt("activity", id);
                fragment.setArguments(bundle);

                FragmentManager fm = ActivityLayout.this.getActivity().getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                ft.replace(R.id.activity_main_content_fragment, fragment);
                ft.addToBackStack("activity_" + id);
                ft.commit();
                }
            });

            //Add to the list
            llListPast.addView(entry);
        }
        cursorPast.close();
        db.close();



    }
}
