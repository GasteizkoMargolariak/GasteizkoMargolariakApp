package com.ivalentin.margolariak;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;


/**
 * Posts a comment for an entry.
 */
class PostComment extends AsyncTask<String, String, Integer> {

	//Elements passed from fragments
	private final String type, user, text, language;
	private String userCode;
	private final LinearLayout list, form;
	private final int id;
	private final Context context;

	//Elements calculated here
	private Button btSend;
	private ProgressBar pb;

	private int code = 404;

	/**
	 * Post a comment for an entry.
	 * @param type Type of the entry to comment. @see GM.COMMENT.TYPE
	 * @param user Username.
	 * @param text Comment text.
	 * @param language Comment language.
	 * @param id ID of the entry to post the comment on.
	 * @param form Layout containing the comment form.
	 * @param list Layout containing the list of comments for the entry.
	 * @param context App context.
	 */
	PostComment(String type, String user, String text, String language, int id, LinearLayout form, LinearLayout list, Context context) {
		super();
		this.type = type;
		this.user = user;
		this.text = text;
		this.form = form;
		this.list = list;
		this.language = language;
		this.id = id;
		this.context = context;

	}


	/**
	 * Before starting background thread
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		btSend = (Button) form.findViewById(R.id.bt_comment_send);
		pb = (ProgressBar) form.findViewById(R.id.pb_comment);
		btSend.setVisibility(View.GONE);
		pb.setVisibility(View.VISIBLE);

		//Get user code
		SharedPreferences sharedData = context.getSharedPreferences(GM.DATA.DATA, Context.MODE_PRIVATE);
		userCode = sharedData.getString(GM.DATA.KEY.USER, GM.DATA.DEFAULT.USER);
	}


	/**
	 * Send request to the server
	 * @param f_url API URL.
	 * @return 0 for success, something else for error.
	 */
	@Override
	protected Integer doInBackground(String... f_url) {
		URL url;
		String urlParams;
		try {

			urlParams = GM.API.COMMENT.KEY.USERNAME + "=" + URLEncoder.encode(user,GM.ENCODING) + "&" + GM.API.COMMENT.KEY.TEXT + "=" + URLEncoder.encode(text, GM.ENCODING);
			urlParams = urlParams + "&id=" + id;
			switch (type) {
				case GM.API.COMMENT.TYPE.BLOG:
					urlParams = urlParams + "&" + GM.API.COMMENT.KEY.TYPE + "=" + GM.API.COMMENT.TYPE.BLOG;
					break;
				case GM.API.COMMENT.TYPE.GALLERY:
					urlParams = urlParams + "&" + GM.API.COMMENT.KEY.TYPE + "=" + GM.API.COMMENT.TYPE.GALLERY;
					break;
				case GM.API.COMMENT.TYPE.ACTIVITIES:
					urlParams = urlParams + "&" + GM.API.COMMENT.KEY.TYPE + "=" + GM.API.COMMENT.TYPE.ACTIVITIES;
					break;
				default:
					Log.e("POST_COMMENT", "Unknown section: " + type);
					return -1;
			}
			switch (language) {
				case GM.LANGUAGE.EN:
					urlParams = urlParams + "&" + GM.API.COMMENT.KEY.LANGUAGE + "=" + GM.LANGUAGE.EN;
					break;
				case GM.LANGUAGE.EU:
					urlParams = urlParams + "&" + GM.API.COMMENT.KEY.LANGUAGE + "=" + GM.LANGUAGE.EU;
					break;
				case GM.LANGUAGE.ES:
					urlParams = urlParams + "&" + GM.API.COMMENT.KEY.LANGUAGE + "=" + GM.LANGUAGE.ES;
					break;
				default:
					urlParams = urlParams + "&" + GM.API.COMMENT.KEY.LANGUAGE + "=" + GM.LANGUAGE.ES;
			}
			urlParams = urlParams + "&" + GM.API.COMMENT.KEY.CLIENT + "=" + GM.API.CLIENT + "&" + GM.API.COMMENT.KEY.USER + "=" + userCode;

			String uri = GM.API.SERVER + GM.API.COMMENT.PATH + "?" + urlParams;
			url = new URL(uri);
			Log.e("URI", uri);

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

			code = urlConnection.getResponseCode();

			switch (code) {
				case 400: //Client error: Bad request
					Log.e("POST_COMMENT", "The server returned a 400 code (Client Error: Bad request) for the url \"" + uri + "\"");
					return(-400);
				case 403: //Client error: Forbidden
					Log.e("POST_COMMENT", "The server returned a 403 code (Client Error: Forbidden) for the url \"" + uri + "\"");
					return(-403);
				case 200: //Success: OK
					Log.d("POST_COMMENT", "The server returned a 200 code (Success: OK) for the url \"" + uri + "\".");
					break;
				default:
					Log.e("POST_COMMENT", "The server returned a " + code + " code for the url \"" + uri + "\".");
					return(-6);
			}

		}
		catch (UnsupportedEncodingException e) {
			Log.e("POST_COMMENT", "Unable to post comment (UnsupportedEncodingException): " + e.toString());
			return -2;
		}
		catch (ProtocolException e) {
			Log.e("POST_COMMENT", "Unable to post comment (ProtocolException): " + e.toString());
			return -3;
		}
		catch (MalformedURLException e) {
			Log.e("POST_COMMENT", "Unable to post comment (MalformedURLException): " + e.toString());
			return -4;
		}
		catch (IOException e) {
			Log.e("POST_COMMENT", "Unable to post comment (IOException): " + e.toString());
			return -5;
		}
		catch (Exception e) {
			Log.e("POST_COMMENT", "Unable to post comment: " + e.toString());
			return -1;
		}

		return 0;
	}


	/**
	 * When the comment is posted, update the comment list and clean the comment form.
	 * @param a Unused.
	 */
	@Override
	protected void onPostExecute(Integer a) {

		if (code == 200) {
			//Clear form
			EditText formText = (EditText) form.findViewById(R.id.et_comment_text);
			EditText formUser = (EditText) form.findViewById(R.id.et_comment_name);
			formUser.setText("");
			formText.setText("");

			//Perform a sync
			((MainActivity) context).bgSync();

			//Insert comment in list
			LayoutInflater factory = LayoutInflater.from(context);
			LinearLayout entry = (LinearLayout) factory.inflate(R.layout.row_comment, list, false);

			//Set user
			TextView tvUser = (TextView) entry.findViewById(R.id.tv_row_comment_user);
			tvUser.setText(user);
			Date d = new Date();
			String date = DateFormat.format("yyyy-MM-dd hh:mm:ss", d.getTime()).toString();
			TextView tvCDate = (TextView) entry.findViewById(R.id.tv_row_comment_date);
			tvCDate.setText(GM.formatDate(date, language, true, true, true));
			TextView tvText = (TextView) entry.findViewById(R.id.tv_row_comment_text);
			tvText.setText(text);

			//Add to the list
			list.addView(entry);
		}

		//Show button again
		btSend.setVisibility(View.VISIBLE);
		pb.setVisibility(View.GONE);
	}
}