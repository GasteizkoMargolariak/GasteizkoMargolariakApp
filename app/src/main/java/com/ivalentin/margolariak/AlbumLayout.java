package com.ivalentin.margolariak;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

/**
 * Fragment to display photos of a selected album.
 * Album id is passed in a bundle.
 * Album name is also passed, to be displayed on photos with no title.
 * Everything is done when the view is inflated.
 *
 * @author Iñigo Valentin
 *
 * @see Fragment
 *
 */
public class AlbumLayout extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//Load the layout
		View view = inflater.inflate(R.layout.fragment_layout_album, container, false);

		//Get bundled id
		Bundle bundle = this.getArguments();
		int id = bundle.getInt("album", -1);
		if (id == -1){
			Log.e("ALBUM_LAYOUT", "No such album: " + id);
			this.getActivity().onBackPressed();
		}

		//Get data from database
		SQLiteDatabase db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath(GM.DB.NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
		final Cursor cursor;
		String lang = GM.getLang();
		cursor = db.rawQuery("SELECT id, title_" + lang + " AS title, description_" + lang + " AS description, permalink FROM album WHERE id = " + id + ";", null);
		cursor.moveToFirst();

		//Set album elements
		TextView tvTitle = (TextView) view.findViewById(R.id.tv_album_title);
		WebView tvDescription = (WebView) view.findViewById(R.id.wv_album_description);

		tvDescription.setLayerType(View.LAYER_TYPE_HARDWARE, null);

		tvTitle.setText(cursor.getString(1));
		((MainActivity) getActivity()).setSectionTitle(cursor.getString(1));
		((MainActivity) getActivity()).setShareLink(String.format(getString(R.string.share_with_title), cursor.getString(1)), GM.SHARE.GALLERY + cursor.getString(3));

		final String albumName = cursor.getString(1);
		final String albumPerm = cursor.getString(3);
		if (cursor.getString(2) == null || cursor.getString(2).length() < 1){
			tvDescription.setVisibility(View.GONE);
		}
		else {
			tvDescription.loadDataWithBaseURL(null, cursor.getString(2), "text/html", "utf-8", null);
		}
		cursor.close();


		//Assign elements
		LinearLayout llList = (LinearLayout) view.findViewById(R.id.ll_album_list);
		LinearLayout entry;

		//An inflater
		LayoutInflater factory = LayoutInflater.from(getActivity());

		String image;
		Cursor imageCursor = db.rawQuery("SELECT id, title_" + lang + " AS title, file, width, height FROM photo, photo_album WHERE photo = id AND album = " + id + " ORDER BY uploaded DESC;", null);
		while (imageCursor.moveToNext()){
			//Create a new row
			entry = (LinearLayout) factory.inflate(R.layout.row_album, llList, false);

			//Set left and right layouts
			LinearLayout left = (LinearLayout) entry.findViewById(R.id.ll_row_album_left);

			//Set title
			TextView tvTitleLeft = (TextView) entry.findViewById(R.id.tv_row_album_title_left);

			if (imageCursor.getString(1) != null && imageCursor.getString(1).length() > 0) {
				tvTitleLeft.setText(imageCursor.getString(1));
			}
			else{
				tvTitleLeft.setVisibility(View.GONE);
			}

			//Set id
			TextView tvHiddenLeft = (TextView) entry.findViewById(R.id.tv_row_album_hidden_left);
			tvHiddenLeft.setText(imageCursor.getString(0));

			//Set image
			ImageView ivLeft = (ImageView) entry.findViewById(R.id.iv_row_album_left);
			image = imageCursor.getString(2);

			//Check if image exists
			File f;
			f = new File(this.getActivity().getFilesDir().toString() + "/img/galeria/preview/" + image);
			if (f.exists()){
				//If the image exists, set it.
				try{
					File file = new File(this.getActivity().getFilesDir().toString() + "/img/galeria/preview/" + image);
					ivLeft.setImageBitmap(GM.decodeSampledBitmapFromFile(file.getAbsolutePath(), GM.IMG.SIZE.PREVIEW));
				}
				catch (Exception ex){
					Log.e("Bitmap error", "Not loading image " + image + ": " + ex.toString());
				}
			}
			else {
				//If not, create directories and download asynchronously
				File fpath;
				fpath = new File(this.getActivity().getFilesDir().toString() + "/img/galeria/preview/");
				if (fpath.mkdirs()) {
					new DownloadImage(GM.API.SERVER + "/img/galeria/preview/" + image, this.getActivity().getFilesDir().toString() + "/img/galeria/preview/" + image, ivLeft, GM.IMG.SIZE.PREVIEW).execute();
				}
			}

			//Count comments
			Cursor cursorComments = db.rawQuery("SELECT id FROM photo_comment WHERE photo = " + imageCursor.getString(0) + ";", null);
			TextView tvCommentsLeft = (TextView) entry.findViewById(R.id.tv_row_album_comments_left);
			tvCommentsLeft.setText(String.valueOf(cursorComments.getCount()));
			cursorComments.close();

			//Set onClickListener
			left.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
				//Toast.makeText(getActivity(), "OPENING POST", Toast.LENGTH_LONG).show();
				Fragment fragment = new PhotoLayout();
				Bundle bundle = new Bundle();
				//Pass post id
				int id = Integer.parseInt(((TextView) v.findViewById(R.id.tv_row_album_hidden_left)).getText().toString());
				bundle.putInt("photo", id);
				bundle.putString("albumName", albumName);
				bundle.putString("albumPerm", albumPerm);
				fragment.setArguments(bundle);

				FragmentManager fm = AlbumLayout.this.getActivity().getFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();

				ft.replace(R.id.activity_main_content_fragment, fragment);
				ft.addToBackStack("photo_" + id);
				ft.commit();
				}
			});
			
			//If odd pass, to the right line
			if (imageCursor.moveToNext()){

				LinearLayout right = (LinearLayout) entry.findViewById(R.id.ll_row_album_right);
				right.setVisibility(View.VISIBLE);

				//Set title
				TextView tvTitleRight = (TextView) entry.findViewById(R.id.tv_row_album_title_right);
				if (imageCursor.getString(1) != null && imageCursor.getString(1).length() > 0) {
					tvTitleRight.setText(imageCursor.getString(1));
				}
				else{
					tvTitleRight.setVisibility(View.GONE);
				}

				//Set id
				TextView tvHiddenRight = (TextView) entry.findViewById(R.id.tv_row_album_hidden_right);
				tvHiddenRight.setText(imageCursor.getString(0));

				//Set image
				ImageView ivRight = (ImageView) entry.findViewById(R.id.iv_row_album_right);
				image = imageCursor.getString(2);
				//Check if image exists
				f = new File(this.getActivity().getFilesDir().toString() + "/img/galeria/preview/" + image);
				if (f.exists()){
					//If the image exists, set it.
					try {
						File file = new File(this.getActivity().getFilesDir().toString() + "/img/galeria/preview/" + image);
						ivRight.setImageBitmap(GM.decodeSampledBitmapFromFile(file.getAbsolutePath(), GM.IMG.SIZE.PREVIEW));
					}
					catch (Exception ex){
						Log.e("Bitmap error", "Not loading image " + image + ": " + ex.toString());
					}
				}
				else {
					//If not, create directories and download asynchronously
					File fpath;
					fpath = new File(this.getActivity().getFilesDir().toString() + "/img/galeria/preview/");
					if (fpath.mkdirs()) {
						new DownloadImage(GM.API.SERVER + "/img/galeria/preview/" + image, this.getActivity().getFilesDir().toString() + "/img/galeria/preview/" + image, ivRight, GM.IMG.SIZE.PREVIEW).execute();
					}
				}

				//Count comments
				cursorComments = db.rawQuery("SELECT id FROM photo_comment WHERE photo = " + imageCursor.getString(0) + ";", null);
				TextView tvCommentsRight = (TextView) entry.findViewById(R.id.tv_row_album_comments_right);
				tvCommentsRight.setText(String.valueOf(cursorComments.getCount()));
				cursorComments.close();

				//Set onClickListener
				right.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
					Fragment fragment = new PhotoLayout();
					Bundle bundle = new Bundle();
					//Pass post id
					int id = Integer.parseInt(((TextView) v.findViewById(R.id.tv_row_album_hidden_right)).getText().toString());
					bundle.putInt("photo", id);
					bundle.putString("albumName", albumName);
					fragment.setArguments(bundle);

					FragmentManager fm = AlbumLayout.this.getActivity().getFragmentManager();
					FragmentTransaction ft = fm.beginTransaction();

					ft.replace(R.id.activity_main_content_fragment, fragment);
					ft.addToBackStack("photo_" + id);
					ft.commit();
					}
				});
			}

			llList.addView(entry);
		}

		imageCursor.close();
		db.close();
		return view;
	}
}