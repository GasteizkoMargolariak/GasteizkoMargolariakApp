package com.ivalentin.gm;


import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

/**
 * Created by seavenois on 09/06/16.
 */
public class BlogLayout extends Fragment{

    //Main View
    private View view;

    int offset = 0;
    int totalPost = 0;

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
        view = inflater.inflate(R.layout.fragment_layout_blog, null);

        //Set the title
        ((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_blog));

        //Get total posts
        SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT id FROM post;", null);
        totalPost = cursor.getCount();
        cursor.close();
        db.close();

        //Assign pager buttons
        final Button btnPrevious = (Button) view.findViewById(R.id.bt_blog_previous);
        final Button btnNext = (Button) view.findViewById(R.id.bt_blog_next);

        btnPrevious.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               offset -= 5;
               btnNext.setVisibility(View.VISIBLE);
               populate(offset);
               if (offset == 0){
                   v.setVisibility(View.GONE);
               }
           }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               offset += 5;
               btnPrevious.setVisibility(View.VISIBLE);
               populate(offset);
               if (offset >= totalPost - 5){
                   v.setVisibility(View.GONE);
               }
           }
        });

        populate(offset);

        return view;
    }

    /**
     * Called when the fragment is brought back into the foreground.
     * Resumes the map and the location manager.
     *
     * @see android.support.v4.app.Fragment#onResume()
     */
    @Override
    public void onResume(){
        Log.e("offset", "" + offset);
        Log.e("total", "" + totalPost);
        //Assign pager buttons
        Button btnPrevious = (Button) view.findViewById(R.id.bt_blog_previous);
        Button btnNext = (Button) view.findViewById(R.id.bt_blog_next);
        if (offset == 0){
            btnPrevious.setVisibility(View.GONE);
        }
        else{
            btnPrevious.setVisibility(View.VISIBLE);
        }
        if (offset >= totalPost - 5){
            btnNext.setVisibility(View.GONE);
        }
        else{
            btnNext.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    public void populate(){
        populate(0);
    }

    /**
     * Populates the list of activities around.
     * It uses the default layout as parent.
     */
    public void populate(int offset){

        //Assign elements
        LinearLayout llList = (LinearLayout) view.findViewById(R.id.ll_blog_list);
        LinearLayout entry;

        //An inflater
        LayoutInflater factory = LayoutInflater.from(getActivity());

        SQLiteDatabase db = getActivity().openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
        final Cursor cursor;
        String currLang = Locale.getDefault().getDisplayLanguage();
        if (!currLang.equals("es") && !currLang.equals("eu")){
            currLang = "en";
        }

        //Get data from the database
        cursor = db.rawQuery("SELECT id, title_" + currLang+ " AS title, text_" + currLang + " AS text, dtime FROM post ORDER BY dtime DESC LIMIT 5 OFFSET " + offset + ";", null);

        //Clear the list
        llList.removeAllViews();

        //Loop
        while (cursor.moveToNext()){

            //Create a new row
            entry = (LinearLayout) factory.inflate(R.layout.row_blog, null);

            //Set margins
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 10, 10, 25);
            entry.setLayoutParams(layoutParams);

            //Set title
            TextView tvTitle = (TextView) entry.findViewById(R.id.tv_row_blog_title);
            tvTitle.setText(cursor.getString(1));

            //Set text
            String text = Html.fromHtml(cursor.getString(2)).toString();
            if (text.length() > 180){
                text = text.substring(0, 180) + "...";
            }
            TextView tvText = (TextView) entry.findViewById(R.id.tv_row_blog_text);
            tvText.setText(text);

            //Set date
            TextView tvDate = (TextView) entry.findViewById(R.id.tv_row_blog_date);
            tvDate.setText(GM.formatDate(cursor.getString(3), currLang, false));

            //Set hidden id
            TextView tvId = (TextView) entry.findViewById(R.id.tv_row_blog_hidden);
            tvId.setText(cursor.getString(0));

            //Get image
            ImageView iv = (ImageView) entry.findViewById(R.id.iv_row_blog_image);
            Cursor cursorImage = db.rawQuery("SELECT image FROM post_image WHERE post = " + cursor.getString(0) +" ORDER BY idx LIMIT 1;", null);
            if (cursorImage.getCount() > 0){
                cursorImage.moveToFirst();
                String image = cursorImage.getString(0);

                //Check if image exists
                File f;
                f = new File(this.getContext().getFilesDir().toString() + "/img/blog/miniature/" + image);
                if (f.exists()){
                    //If the image exists, set it.
                    Bitmap myBitmap = BitmapFactory.decodeFile(this.getContext().getFilesDir().toString() + "/img/blog/miniature/" + image);
                    iv.setImageBitmap(myBitmap);
                }
                else {
                    //If not, create directories and download asynchronously
                    File fpath;
                    fpath = new File(this.getContext().getFilesDir().toString() + "/img/blog/miniature/");
                    fpath.mkdirs();
                    new DownloadImage(GM.SERVER + "/img/blog/miniature/" + image, this.getContext().getFilesDir().toString() + "/img/blog/miniature/" + image, iv).execute();
                }
            }

            //Count comments
            Cursor cursorComment = db.rawQuery("SELECT * FROM post_comment WHERE post = " + cursor.getString(0) +";", null);
            TextView tvDetails = (TextView) entry.findViewById(R.id.tv_row_blog_details);
            tvDetails.setText(" " + cursorComment.getCount());

            //Set onCLickListener
            entry.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                //Toast.makeText(getActivity(), "OPENING POST", Toast.LENGTH_LONG).show();
                Fragment fragment = new PostLayout();
                Bundle bundle = new Bundle();
                //Pass post id
                int id = Integer.parseInt(((TextView) v.findViewById(R.id.tv_row_blog_hidden)).getText().toString());
                bundle.putInt("post", id);
                fragment.setArguments(bundle);

                FragmentManager fm = BlogLayout.this.getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                ft.replace(R.id.activity_main_content_fragment, fragment);
                ft.addToBackStack("post_" + id);
                ft.commit();
                }
            });

            //Add to the list
            llList.addView(entry);
        }

    }
}
