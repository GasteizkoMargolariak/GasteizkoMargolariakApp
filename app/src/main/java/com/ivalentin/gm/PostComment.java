package com.ivalentin.gm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

class PostComment extends AsyncTask<String, String, Integer> {

    //Elements passed from fragments
    String type, user, text, language;
    LinearLayout list, form;
    TextView counter;
    int id, currentCounter;
    Context context;

    //Elements calculated here
    Button btSend;
    ProgressBar pb;

    int code = 404;

    public PostComment(String type, String user, String text, String language, int id, LinearLayout form, LinearLayout list, int currentCounter, TextView counter, Context context) {
        super();
        this.type = type;
        this.user = user;
        this.text = text;
        this.form = form;
        this.list = list;
        this.language = language;
        this.counter = counter;
        this.id = id;
        this.currentCounter = currentCounter;
        this.context = context;
    }

    /**
     * Before starting background thread
     * */
    @Override
    protected void onPreExecute() {
        Log.e("PRE", "START");
        super.onPreExecute();
        btSend = (Button) form.findViewById(R.id.bt_post_comment_send);
        pb = (ProgressBar) form.findViewById(R.id.pb_comment);
        btSend.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);
        Log.e("PRE", "END");
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected Integer doInBackground(String... f_url) {
        Log.e("DIB", "START");
        int count;
        URL url;
        String urlString, urlParams;
        try {

            //url = new URL(GM.SERVER + "/" + type + "/comment.php?user=" + user + "&text=" + text);
            urlString = GM.SERVER + "/" + type + "/comment.php?user=" + URLEncoder.encode(user) + "&text=" + URLEncoder.encode(text);
            urlParams = "user=" + URLEncoder.encode(user) + "&text=" + URLEncoder.encode(text);
            Log.e("TYPE", type);
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
                    //TODO, error, stop
            }
            switch (language){
                case "es":
                    urlParams = urlParams + "&lang=es";
                    break;
                case "eu":
                    urlParams = urlParams + "&lang=eu";
                    break;
                default:
                    this.language = "en";
                    urlParams = urlParams + "&lang=en";
            }

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


            Log.e("URL", urlString);
            //url = new URL(urlString);
            //HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            //connection.setRequestMethod("GET");
            //connection.connect();

            code = conn.getResponseCode();
            Log.d("Comment status", "" + code);

            if (code == 200){
                //TODO: Insert into local db
                SQLiteDatabase db = context.openOrCreateDatabase(GM.DB_NAME, Context.MODE_PRIVATE, null);
                String table = "";
                switch (type){
                    case "blog":
                        table = "post_comment";
                        break;
                    case "galeria":
                        table = "photo_comment";
                        break;
                    case "actividades":
                        table = "activity_comment";
                        break;
                }
                db.rawQuery("INSERT INTO " + table + " (post, text, username, lang) VALUES (" + id + ", '" + text + "', '" + user + "', '" + language + "');", null);
                db.close();
            }

        } catch (Exception e) {
            Log.e("Error posting: ", e.getMessage());
        }
        Log.e("DIB", "END");
        return code;
    }



    /**
     * After completing background task
     * **/
    @Override
    protected void onPostExecute(Integer a) {
        Log.e("POS", "START");
        //Log.d("Comment posted", type);

        if (code == 200) {
            //Clear form
            EditText formText = (EditText) form.findViewById(R.id.et_post_comment_text);
            EditText formUser = (EditText) form.findViewById(R.id.et_post_comment_name);
            formUser.setText("");
            formText.setText("");

            //TODO: Update counter

            //TODO: Insert comment in list
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

        //ImageView myImage = (ImageView) findViewById(R.id.imageviewTest);
        Log.e("POS", "END");
    }
}