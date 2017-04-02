package com.ivalentin.margolariak;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

/**
 * Activity with info about Gasteizko margolariak.
 *
 * @author Iñigo Valentin
 *
 * @see Activity
 *
 */
public class UsActivity extends Activity {

	/**
	 * Runs when the activity is created.
	 *
	 * @param savedInstanceState Saved state of the activity.
	 * @see Activity#onCreate(Bundle)
	 */
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		//Remove title bar.
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		//Set layout.
		setContentView(R.layout.activity_us);

		//Assign layout items
		LinearLayout llCuadrilla = (LinearLayout) findViewById(R.id.ll_us_cuadrilla);
		LinearLayout llAssociation = (LinearLayout) findViewById(R.id.ll_us_asociacion);
		LinearLayout llActivities = (LinearLayout) findViewById(R.id.ll_us_activities);
		LinearLayout llTransparency = (LinearLayout) findViewById(R.id.ll_us_transparency);

		//Set onClick listeners.
		llCuadrilla.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openDialog(getString(R.string.us_cuadrilla), getString(R.string.us_cuadrilla_content), getResources().getDrawable(R.drawable.us_cuadrilla));
			}
		});
		llAssociation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openDialog(getString(R.string.us_association), getString(R.string.us_association_content), getResources().getDrawable(R.drawable.us_asociacion));
			}
		});
		llActivities.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openDialog(getString(R.string.us_activities), getString(R.string.us_activities_content), getResources().getDrawable(R.drawable.us_activities));
			}
		});
		llTransparency.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openDialog(getString(R.string.us_transparency), getString(R.string.us_transparency_content), getResources().getDrawable(R.drawable.us_transparency));
			}
		});

	}

	/**
	 * Shows a dialog with expanded info.
	 *
	 * @param title The event id.
	 * @param text The dialog text.
	 * @param img The dialog image.
	 */
	@SuppressWarnings("ConstantConditions")
	private void openDialog(final String title, final String text, final Drawable img){

		//Create the dialog
		final Dialog dialog = new Dialog(this);

		//Set up dialog window
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_us);

		//Set the custom dialog components - text, image and button
		TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_dialog_us_title);
		TextView tvText = (TextView) dialog.findViewById(R.id.tv_dialog_us_text);
		ImageView ivImage = (ImageView) dialog.findViewById(R.id.iv_dialog_us_image);
		Button btClose = (Button) dialog.findViewById(R.id.bt_dialog_us_close);

		//Set text fields
		tvTitle.setText(title);
		tvText.setText(text);

		//Set image
		ivImage.setImageDrawable(img);

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
