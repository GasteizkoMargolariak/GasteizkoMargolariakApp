package com.ivalentin.gm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

class DownloadImage extends AsyncTask<String, String, String> {

    String file;
    String path;
    ImageView iv;

    public DownloadImage(String file, String path, ImageView iv) {
        super();
        this.file = file;
        this.path = path;
        this.iv = iv;
    }

    /**
     * Before starting background thread
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {

            URL url = new URL(file);

            URLConnection conection = url.openConnection();
            conection.connect();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream to write file

            OutputStream output = new FileOutputStream(path);
            byte data[] = new byte[1024];

            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;

                // writing data to file
                output.write(data, 0, count);

            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error downloading: ", e.getMessage());
        }

        return null;
    }



    /**
     * After completing background task
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        Log.d("File downloaded", path);
        Bitmap myBitmap = BitmapFactory.decodeFile(path);

        //ImageView myImage = (ImageView) findViewById(R.id.imageviewTest);

        iv.setImageBitmap(myBitmap);
    }

}