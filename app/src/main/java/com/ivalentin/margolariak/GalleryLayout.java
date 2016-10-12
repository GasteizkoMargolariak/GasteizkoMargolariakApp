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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

/**
 * Fragment of the gallery section.
 *
 * @see Fragment
 *
 * @author Iñigo Valentin
 *
 */
public class GalleryLayout extends Fragment{

    /**
     * Run when the fragment is inflated.
     * Assigns views, gets the date and does the first call to the {@link @populate()} function.
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
        View view = inflater.inflate(R.layout.fragment_layout_gallery, null);

        //Set the title
        ((MainActivity) getActivity()).setSectionTitle(view.getContext().getString(R.string.menu_gallery));

        populate(view);

        return view;
    }

    /**
     * Populates the list of activities around.
     * It uses the default layout as parent.
     */
    @SuppressLint("InflateParams") //Throws unknown error when done properly.
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void populate(View view){

        //Assign elements
        LinearLayout llList = (LinearLayout) view.findViewById(R.id.ll_gallery_album_list);
        LinearLayout entry;

        //An inflater
        LayoutInflater factory = LayoutInflater.from(getActivity());

        SQLiteDatabase db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath(GM.DB_NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
        final Cursor cursor;
		String lang = GM.getLang();

        //Get data from the database
        cursor = db.rawQuery("SELECT DISTINCT album.id AS id, album.title_" + lang + " AS name FROM photo, album, photo_album WHERE photo.id = photo AND album.id = album ORDER BY uploaded DESC;", null);

        //Loop
        while (cursor.moveToNext()){

            //Create a new row
            entry = (LinearLayout) factory.inflate(R.layout.row_gallery, null);

            //Set margins
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 10, 10, 25);
            entry.setLayoutParams(layoutParams);

            //Set title
            TextView tvTitle = (TextView) entry.findViewById(R.id.tv_row_gallery_title);
            tvTitle.setText(cursor.getString(1));

            //Set hidden id
            TextView tvId = (TextView) entry.findViewById(R.id.tv_row_gallery_hidden);
            tvId.setText(cursor.getString(0));

            //Get images
            ImageView preview[] = new ImageView[4];
            preview[0] = (ImageView) entry.findViewById(R.id.iv_row_gallery_0);
            preview[1] = (ImageView) entry.findViewById(R.id.iv_row_gallery_1);
            preview[2] = (ImageView) entry.findViewById(R.id.iv_row_gallery_2);
            preview[3] = (ImageView) entry.findViewById(R.id.iv_row_gallery_3);

            //Get db rows
            Cursor cursorImage = db.rawQuery("SELECT id, file, width, height FROM photo, photo_album WHERE photo = id AND album = " + cursor.getString(0) + " ORDER BY random() LIMIT 4;", null);

            //Loop
            int i = 0;
            while (cursorImage.moveToNext()){
                String image = cursorImage.getString(1);

                //Check if image exists
                File f;
                f = new File(this.getActivity().getFilesDir().toString() + "/img/galeria/miniature/" + image);
                if (f.exists()){
                    //If the image exists, set it.
                    Bitmap myBitmap = BitmapFactory.decodeFile(this.getActivity().getFilesDir().toString() + "/img/galeria/miniature/" + image);
                    preview[i].setImageBitmap(myBitmap);
                }
                else {
                    //If not, create directories and download asynchronously
                    File fpath;
                    fpath = new File(this.getActivity().getFilesDir().toString() + "/img/galeria/miniature/");
                    fpath.mkdirs();
                    new DownloadImage(GM.SERVER + "/img/galeria/miniature/" + image, this.getActivity().getFilesDir().toString() + "/img/galeria/miniature/" + image, preview[i], GM.IMG_MINIATURE).execute();
                }
                i ++;
            }

            //Set photo counter
			Cursor cursorCounter = db.rawQuery("SELECT id FROM photo, photo_album WHERE photo = id AND album = " + cursor.getString(0) + ";", null);
            TextView tvPhotoCounter = (TextView) entry.findViewById(R.id.tv_row_gallery_counter);
            tvPhotoCounter.setText(getResources().getQuantityString(R.plurals.gallery_photo_count, cursorCounter.getCount(), cursorCounter.getCount()));

			//Close cursors
			cursorCounter.close();
            cursorImage.close();

            //Set onCLickListener
            entry.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                Fragment fragment = new AlbumLayout();
                Bundle bundle = new Bundle();

                //Pass post id
                int id = Integer.parseInt(((TextView) v.findViewById(R.id.tv_row_gallery_hidden)).getText().toString());
                bundle.putInt("album", id);
                fragment.setArguments(bundle);

                FragmentManager fm = GalleryLayout.this.getActivity().getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                ft.replace(R.id.activity_main_content_fragment, fragment);
                ft.addToBackStack("album_" + id);
                ft.commit();
                }
            });

            //Add to the list
            llList.addView(entry);
        }

        //Close database
        cursor.close();
        db.close();

    }
}
