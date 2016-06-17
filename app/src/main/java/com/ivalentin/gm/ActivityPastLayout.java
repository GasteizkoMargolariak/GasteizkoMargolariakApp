package com.ivalentin.gm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

/**
 * Created by seavenois on 14/06/16.
 */
public class ActivityPastLayout extends Fragment {

    //Main View
    private View view;

    String currLang;

    Context context;

    @SuppressLint("InflateParams") //Throws unknown error when done properly.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Load the layout
        view = inflater.inflate(R.layout.fragment_layout_activity_past, null);
        context = view.getContext();

        //Get bundled id
        Bundle bundle = this.getArguments();
        int id = bundle.getInt("activity", -1);
        if (id == -1){
            //TODO: Error, do something
        }

        //Get data from database
        SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
        final Cursor cursor;
        currLang = Locale.getDefault().getDisplayLanguage();
        if (!currLang.equals("es") && !currLang.equals("eu")){
            currLang = "en";
        }
        cursor = db.rawQuery("SELECT id, title_" + currLang+ " AS title, text_" + currLang + " AS text, date, city, after_" + currLang + " AS after FROM activity WHERE id = " + id + ";", null);
        cursor.moveToFirst();

        //Get display elements
        ImageView images[] = new ImageView[5];
        images[0] = (ImageView) view.findViewById(R.id.iv_activity_past_0);
        images[1] = (ImageView) view.findViewById(R.id.iv_activity_past_1);
        images[2] = (ImageView) view.findViewById(R.id.iv_activity_past_2);
        images[3] = (ImageView) view.findViewById(R.id.iv_activity_past_3);
        images[4] = (ImageView) view.findViewById(R.id.iv_activity_past_4);

        TextView tvTitle = (TextView) view.findViewById(R.id.tv_activity_past_title);
        TextView tvDate = (TextView) view.findViewById(R.id.tv_activity_past_date);
        TextView tvCity = (TextView) view.findViewById(R.id.tv_activity_past_city);
        WebView wvText = (WebView) view.findViewById(R.id.wv_activity_past_text);

        //Set fields
        tvTitle.setText(cursor.getString(1));
        ((MainActivity) getActivity()).setSectionTitle(cursor.getString(1));
        if (cursor.getString(5).length() < 1) {
            wvText.loadDataWithBaseURL(null, cursor.getString(2), "text/html", "utf-8", null);
        }
        else{
            wvText.loadDataWithBaseURL(null, cursor.getString(5), "text/html", "utf-8", null);
        }
        tvDate.setText(GM.formatDate(cursor.getString(3) + " 00:00:00", currLang, false));
        tvCity.setText(cursor.getString(4));

        //Get images
        Cursor imageCursor = db.rawQuery("SELECT image, idx FROM activity_image WHERE activity = " + id + " ORDER BY idx LIMIT 5;", null);
        //imageCursor.moveToFirst();
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
                fpath.mkdirs();
                new DownloadImage(GM.SERVER + "/img/actividades/preview/" + image, this.getActivity().getFilesDir().toString() + "/img/actividades/preview/" + image, images[i]).execute();
            }
            images[i].setVisibility(View.VISIBLE);
            i ++;
        }
        imageCursor.close();

        cursor.close();
        db.close();
        return view;
    }
}