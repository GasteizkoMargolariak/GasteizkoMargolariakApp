package com.ivalentin.margolariak;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Section that shows a photo of the gallery, with details and comments.
 * Contains info from almost every other section.
 *
 * @author Iñigo Valentin
 *
 * @see Fragment
 *
 */
public class PhotoLayout extends Fragment {

	//Main View
	private View view;

	private String albumName;

	private Integer photos[];
	private int position;

	private Context context;

	@SuppressLint("InflateParams") //Throws unknown error when done properly.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//Load the layout
		view = inflater.inflate(R.layout.fragment_layout_photo, null);
		context = view.getContext();

		//Get bundled id
		Bundle bundle = this.getArguments();
		albumName = bundle.getString("albumName", "");
		int id = bundle.getInt("photo", -1);
		if (id == -1) {
			Log.e("Photo error", "No such photo: " + id);
			this.getActivity().onBackPressed();
		}

		photos = loadPhotos(id);
		position = getPosition(photos, id);

		//Assign next/previous buttons listener
		Button btnPrevious = (Button) view.findViewById(R.id.bt_photo_previous);
		Button btnNext = (Button) view.findViewById(R.id.bt_photo_next);
		btnPrevious.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (position > 0){
					position --;
					populate(photos[position]);
				}
			}
		});
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (position < photos.length - 1){
					position ++;
					populate(photos[position]);
				}
			}
		});

		populate(id, view);

		return view;
	}

	/**
	 * Given a photo id array and a photo id, tells the position of the photo in the array.
	 *
	 * @param list Array withe the ids of photos.
	 * @param id Id of the reference photo.
	 *
	 * @return The position of the photo in the array.
	 */
	private int getPosition(Integer[] list, int id){
		int i;
		try{
			i = 0;
			while (list[i] != id){
				i ++;
			}

		}
		catch(Exception ex){
			i = -1;
		}

		return i;
	}

	/**
	 * Given a photo, builds an ordered array with the ids of all photos in the same album.
	 *
	 * @param id Id of the reference photo.
	 *
	 * @return A sorted Integer[] with the ids of the photos in the same album.
	 */
	private Integer[] loadPhotos(int id){

		SQLiteDatabase db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath(GM.DB_NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);

		//Get album id for the photo
		Cursor cAlbum = db.rawQuery("SELECT album FROM photo_album WHERE photo = " + id + ";", null);
		cAlbum.moveToFirst();
		int album = cAlbum.getInt(0);
		cAlbum.close();

		//Get photos of the album
		Cursor cursor = db.rawQuery("SELECT photo.id FROM photo,album, photo_album WHERE photo.id = photo AND album.id = album AND album = " + album + " ORDER BY uploaded DESC;", null);
		Integer list[] = new Integer[cursor.getCount()];
		int i = 0;
		while(cursor.moveToNext()){
			list[i] = cursor.getInt(0);
			i ++;
		}
		cursor.close();
		db.close();

		return list;
	}

	private void populate(int id){
		populate(id, view);
	}

	@SuppressLint("InflateParams") //Throws unknown error when done properly.
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void populate(int id, View v){

		final int photoId = id;

		//Get data from database
		SQLiteDatabase db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath(GM.DB_NAME).getAbsolutePath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
		final Cursor cursor;
		final String lang = GM.getLang();
		cursor = db.rawQuery("SELECT id, file, title_" + lang+ " AS title, description_" + lang + " AS description, uploaded, width, height, size FROM photo WHERE id = " + id + ";", null);
		cursor.moveToFirst();

		//Get display elements
		ImageView imageView = (ImageView) v.findViewById(R.id.iv_photo);

		TextView tvTitle = (TextView) v.findViewById(R.id.tv_photo_title);
		TextView tvDate = (TextView) v.findViewById(R.id.tv_photo_date);
		final TextView tvComments = (TextView) v.findViewById(R.id.tv_comments);
		TextView tvDescription = (TextView) v.findViewById(R.id.tv_photo_description);
		LinearLayout llCommentList = (LinearLayout) v.findViewById(R.id.ll_comment_list);

		//Set fields
		if (cursor.getString(2).length() > 0) {
			tvTitle.setVisibility(View.VISIBLE);
			tvTitle.setText(cursor.getString(2));
			((MainActivity) getActivity()).setSectionTitle(cursor.getString(2));
		}
		else{
			tvTitle.setVisibility(View.GONE);
			((MainActivity) getActivity()).setSectionTitle(albumName);
		}

		if (cursor.getString(3).length() > 0) {
			tvDescription.setVisibility(View.VISIBLE);
			tvDescription.setText(cursor.getString(3));
		}
		else{
			tvDescription.setVisibility(View.GONE);
		}
		tvDate.setText(GM.formatDate(cursor.getString(4), lang, true));

		//Get image
		String image = cursor.getString(1);

		//Check if image exists
		File f;
		f = new File(this.getActivity().getFilesDir().toString() + "/img/galeria/view/" + image);
		if (f.exists()){
			//If the image exists, set it.
			Bitmap myBitmap = BitmapFactory.decodeFile(this.getActivity().getFilesDir().toString() + "/img/galeria/view/" + image);
			imageView.setImageBitmap(myBitmap);
		}
		else {
			//If not,  set placeholder image, create directories and download asynchronously
			imageView.setImageResource(getResources().getIdentifier("com.ivalentin.margolariak:drawable/pinpoint_gm", null, null));
			File fpath;
			fpath = new File(this.getActivity().getFilesDir().toString() + "/img/galeria/view/");
			fpath.mkdirs();
			new DownloadImage(GM.SERVER + "/img/galeria/view/" + image, this.getActivity().getFilesDir().toString() + "/img/galeria/view/" + image, imageView, GM.IMG_VIEW).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}

		//Hide or show comments, as needed
		Button btNext = (Button) v.findViewById(R.id.bt_photo_next);
		Button btPrevious = (Button) v.findViewById(R.id.bt_photo_previous);
		if (position == 0){
			btPrevious.setVisibility(View.INVISIBLE);
		}
		else{
			btPrevious.setVisibility(View.VISIBLE);
		}
		if (position == photos.length - 1){
			btNext.setVisibility(View.INVISIBLE);
		}
		else{
			btNext.setVisibility(View.VISIBLE);
		}



		//Get comments
		Cursor commentCursor = db.rawQuery("SELECT text, dtime, username FROM photo_comment WHERE photo = " + id + ";", null);
		tvComments.setText(String.format(getResources().getQuantityString(R.plurals.comment_comments, commentCursor.getCount()), commentCursor.getCount()));
		//final int commentCount = commentCursor.getCount();
		cursor.moveToFirst();
		LinearLayout entry;
		LayoutInflater factory = LayoutInflater.from(getActivity());
		llCommentList.removeAllViews();
		while (commentCursor.moveToNext()) {
			entry = (LinearLayout) factory.inflate(R.layout.row_comment, null);

			//Set user
			TextView tvUser = (TextView) entry.findViewById(R.id.tv_row_comment_user);
			tvUser.setText(commentCursor.getString(2));
			TextView tvCDate = (TextView) entry.findViewById(R.id.tv_row_comment_date);
			tvCDate.setText(GM.formatDate(commentCursor.getString(1), lang, true));
			TextView tvText = (TextView) entry.findViewById(R.id.tv_row_comment_text);
			tvText.setText(commentCursor.getString(0));

			//Add to the list
			llCommentList.addView(entry);
		}
		commentCursor.close();

		//Set up comment form
		final EditText etCommentText = (EditText) view.findViewById(R.id.et_comment_text);
		final EditText etCommentName = (EditText) view.findViewById(R.id.et_comment_name);
		Button btSendComment = (Button) view.findViewById(R.id.bt_comment_send);

		btSendComment.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
			String user = etCommentName.getText().toString();
			String text = etCommentText.getText().toString();
			if (user.length() < 1){
				Toast.makeText(getActivity(), getString(R.string.comment_toast_user), Toast.LENGTH_LONG).show();
				etCommentName.requestFocus();
				return;
			}
			if (text.length() < 1){
				Toast.makeText(getActivity(), getString(R.string.comment_toast_text), Toast.LENGTH_LONG).show();
				etCommentText.requestFocus();
				return;
			}

			//Start async task
			LinearLayout form = (LinearLayout) view.findViewById(R.id.ll_new_comment);
			LinearLayout list = (LinearLayout) view.findViewById(R.id.ll_comment_list);
			new PostComment("galeria", user, text, lang, photoId, form, list, context).execute();

			}
		});

		cursor.close();
		db.close();
	}
}