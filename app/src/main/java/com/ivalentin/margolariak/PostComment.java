package com.ivalentin.margolariak;

import android.annotation.SuppressLint;
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

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
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

    public PostComment(String type, String user, String text, String language, int id, LinearLayout form, LinearLayout list, Context context) {
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
        SharedPreferences preferences = context.getSharedPreferences(GM.PREF, Context.MODE_PRIVATE);
        userCode = preferences.getString(GM.USER_CODE, "");
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

            urlParams = "user=" + URLEncoder.encode(user) + "&text=" + URLEncoder.encode(text);
            switch (type){
                case "blog":
                    urlParams = urlParams + "&post=" + id;
                    break;
                case "galeria":
                    urlParams = urlParams + "&photo=" + id;
                    break;
                case "actividades":
                    urlParams = urlParams + "&activity=" + id;
                    break;
                default:
                    Log.e("Comment error", "Unknown section: " + type);
                    return -1;
            }
            switch (language){
                case "es":
                    urlParams = urlParams + "&lang=es";
                    break;
                case "eu":
                    urlParams = urlParams + "&lang=eu";
                    break;
                default:
                    urlParams = urlParams + "&lang=en";
            }
            urlParams = urlParams + "&from=app&code=" + userCode;
            byte[] postData       = urlParams.getBytes("UTF-8");
            int    postDataLength = postData.length;
            String request        = GM.SERVER + "/" + type + "/comment.php";
            url            = new URL( request );
            HttpURLConnection conn= (HttpURLConnection) url.openConnection();
            conn.setDoOutput( true );
            conn.setInstanceFollowRedirects( false );
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "charset", "utf-8");
            conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
            conn.setUseCaches( false );
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.write(postData);
            conn.connect();

            code = conn.getResponseCode();
            Log.d("Comment status", "" + code);

            if (code == 200){
                //Insert into local db
                SQLiteDatabase db = context.openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
                String table = "";
                String item = "";
                switch (type){
                    case "blog":
                        table = "post_comment";
                        item = "post";
                        break;
                    case "galeria":
                        table = "photo_comment";
                        item = "photo";
                        break;
                    case "actividades":
                        table = "activity_comment";
                        item = "activity";
                        break;
                }
                db.execSQL("INSERT INTO " + table + " (" + item + ", text, username, lang, dtime) VALUES (" + id + ", '" + text + "', '" + user + "', '" + language + "', datetime('NOW'));");
                db.close();
            }

        } catch (Exception e) {
            Log.e("Error posting: ", e.getMessage());
        }
        return code;
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

            //Insert comment in list
            LayoutInflater factory = LayoutInflater.from(context);
            LinearLayout entry = (LinearLayout) factory.inflate(R.layout.row_comment, null);

            //Set user
            TextView tvUser = (TextView) entry.findViewById(R.id.tv_row_comment_user);
            tvUser.setText(user);
            Date d = new Date();
            String date = DateFormat.format("yyyy-MM-dd hh:mm:ss", d.getTime()).toString();
            TextView tvCDate = (TextView) entry.findViewById(R.id.tv_row_comment_date);
            tvCDate.setText(GM.formatDate(date, language, true));
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