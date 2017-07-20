package com.ivalentin.margolariak;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
     * */
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
     * Downloading file in background thread
     * */
    @Override
    @SuppressWarnings("deprecation")
    protected Integer doInBackground(String... f_url) {
        URL url;
        String urlParams;
        try {

            urlParams = "username=" + URLEncoder.encode(user) + "&text=" + URLEncoder.encode(text);
            urlParams = urlParams + "&id=" + id;
            switch (type) {
                case "blog":
                    urlParams = urlParams + "&target=post";
                    break;
                case "galeria":
                    urlParams = urlParams + "&target=photo";
                    break;
                case "actividades":
                    urlParams = urlParams + "&target=activity";
                    break;
                default:
                    Log.e("Comment error", "Unknown section: " + type);
                    return -1;
            }
            switch (language) {
                case "es":
                    urlParams = urlParams + "&lang=es";
                    break;
                case "eu":
                    urlParams = urlParams + "&lang=eu";
                    break;
                default:
                    urlParams = urlParams + "&lang=en";
            }
            urlParams = urlParams + "&client=" + GM.API.CLIENT + "&user=" + userCode;

            String uri = GM.API.SERVER + GM.API.COMMENT.PATH + "?" + urlParams;
            url = new URL(uri);
			Log.e("URI", uri);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            code = urlConnection.getResponseCode();

            switch (code) {
                case 400:    //Client error: Bad request
                    Log.e("COMMENT", "The server returned a 400 code (Client Error: Bad request) for the url \"" + uri + "\"");
					return(-400);
                case 403:    //Client error: Forbidden
                    Log.e("COMMENT", "The server returned a 403 code (Client Error: Forbidden) for the url \"" + uri + "\"");
					return(-403);
                case 200:    //Success: OK
                    Log.d("COMMENT", "The server returned a 200 code (Success: OK) for the url \"" + uri + "\".");
					break;
				default:
					Log.e("COMMENT", "The server returned a " + code + " code for the url \"" + uri + "\".");
					return(-6);
            }

        }
		catch (UnsupportedEncodingException e) {
            Log.e("COMMENT", "Unable to post comment (UnsupportedEncodingException): " + e.toString());
			return -2;
        }
		catch (ProtocolException e) {
            Log.e("COMMENT", "Unable to post comment (ProtocolException): " + e.toString());
			return -3;
        }
		catch (MalformedURLException e) {
            Log.e("COMMENT", "Unable to post comment (MalformedURLException): " + e.toString());
			return -4;
        }
		catch (IOException e) {
            Log.e("COMMENT", "Unable to post comment (IOException): " + e.toString());
			return -5;
        }
		catch (Exception e) {
            Log.e("COMMENT", "Unable to post comment: " + e.toString());
			return -1;
        }

		return 0;
    }



    /**
     * After completing background task
     */
    @SuppressLint("InflateParams") //Throws unknown error when done properly.
    @Override
    protected void onPostExecute(Integer a) {

        if (code == 200) {
            //Clear form
            EditText formText = (EditText) form.findViewById(R.id.et_comment_text);
            EditText formUser = (EditText) form.findViewById(R.id.et_comment_name);
            formUser.setText("");
            formText.setText("");

            //TODO: Update counter

			//Perform a sync
			((MainActivity) context).bgSync();


            //Insert comment in list
            LayoutInflater factory = LayoutInflater.from(context);
            LinearLayout entry = (LinearLayout) factory.inflate(R.layout.row_comment, null);

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