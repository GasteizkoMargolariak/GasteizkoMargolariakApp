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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

/**
 * Created by seavenois on 12/06/16.
 */
public class PostLayout extends Fragment {

    //Main View
    private View view;

    String currLang;

    Context context;

    @SuppressLint("InflateParams") //Throws unknown error when done properly.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Load the layout
        view = inflater.inflate(R.layout.fragment_layout_post, null);
        context = view.getContext();

        //Get bundled id
        Bundle bundle = this.getArguments();
        int id = bundle.getInt("post", -1);
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
        cursor = db.rawQuery("SELECT id, title_" + currLang+ " AS title, text_" + currLang + " AS text, dtime FROM post WHERE id = " + id + ";", null);
        cursor.moveToFirst();

        //Get display elements
        ImageView images[] = new ImageView[5];
        images[0] = (ImageView) view.findViewById(R.id.iv_post_0);
        images[1] = (ImageView) view.findViewById(R.id.iv_post_1);
        images[2] = (ImageView) view.findViewById(R.id.iv_post_2);
        images[3] = (ImageView) view.findViewById(R.id.iv_post_3);
        images[4] = (ImageView) view.findViewById(R.id.iv_post_4);

        TextView tvTitle = (TextView) view.findViewById(R.id.tv_post_title);
        TextView tvDate = (TextView) view.findViewById(R.id.tv_post_date);
        final TextView tvComments = (TextView) view.findViewById(R.id.tv_comments);
        WebView wvText = (WebView) view.findViewById(R.id.wv_post_text);
        LinearLayout llCommentList = (LinearLayout) view.findViewById(R.id.ll_comment_list);

        //Set fields
        tvTitle.setText(cursor.getString(1));
        ((MainActivity) getActivity()).setSectionTitle(cursor.getString(1));
        wvText.loadDataWithBaseURL(null, cursor.getString(2), "text/html", "utf-8", null);
        tvDate.setText(GM.formatDate(cursor.getString(3), currLang, true));

        //Close the cursor
        //cursor.close();

        //Get images
        Cursor imageCursor = db.rawQuery("SELECT image, idx FROM post_image WHERE post = " + id + " ORDER BY idx LIMIT 5;", null);
        //imageCursor.moveToFirst();
        //Log.e("IMGCOUNT", "" + cursor.getCount());
        int i = 0;
        while (imageCursor.moveToNext()) {
            //Log.e("IMG", "" + i);
            String image = imageCursor.getString(0);

            //Check if image exists
            File f;
            f = new File(this.getActivity().getFilesDir().toString() + "/img/blog/preview/" + image);
            if (f.exists()){
                //If the image exists, set it.
                Bitmap myBitmap = BitmapFactory.decodeFile(this.getActivity().getFilesDir().toString() + "/img/blog/preview/" + image);
                images[i].setImageBitmap(myBitmap);
            }
            else {
                //If not, create directories and download asynchronously
                File fpath;
                fpath = new File(this.getActivity().getFilesDir().toString() + "/img/blog/preview/");
                fpath.mkdirs();
                new DownloadImage(GM.SERVER + "/img/blog/preview/" + image, this.getActivity().getFilesDir().toString() + "/img/blog/preview/" + image, images[i]).execute();
            }
            images[i].setVisibility(View.VISIBLE);
            i ++;
        }
        imageCursor.close();

        //Get comments
        Cursor commentCursor = db.rawQuery("SELECT text, dtime, username FROM post_comment WHERE post = " + id + ";", null);
        tvComments.setText(String.format(getResources().getQuantityString(R.plurals.comment_comments, commentCursor.getCount()), commentCursor.getCount()));
        final int commentCount = commentCursor.getCount();
        cursor.moveToFirst();
        LinearLayout entry;
        LinearLayout commentList = (LinearLayout) view.findViewById(R.id.ll_comment_list);
        LayoutInflater factory = LayoutInflater.from(getActivity());
        while (commentCursor.moveToNext()) {
            entry = (LinearLayout) factory.inflate(R.layout.row_comment, null);

            //Set user
            TextView tvUser = (TextView) entry.findViewById(R.id.tv_row_comment_user);
            tvUser.setText(commentCursor.getString(2));
            TextView tvCDate = (TextView) entry.findViewById(R.id.tv_row_comment_date);
            tvCDate.setText(GM.formatDate(commentCursor.getString(1), currLang, true));
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
                new PostComment("blog", user, text, currLang, cursor.getInt(0), form, list, commentCount, tvComments, context).execute();

            }
        });

        db.close();
        return view;
    }
}