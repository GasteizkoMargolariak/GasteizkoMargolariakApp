package com.ivalentin.margolariak;

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

/**
 * Async task to download images on the fly.
 * The remote image file, the local path, and the ImageView where the image will be loaded are set on the constructor.
 *
 * @author Iñigo Valentin
 *
 */
class DownloadImage extends AsyncTask<Void, Void, Void> {

    private String file;
    private String path;
    private ImageView iv;

	/**
	 * Constructor.
	 *
	 * @param file URL of the remote file.
	 * @param path Path, including file name, where the image will be saved.
	 * @param iv ImageView that will hold the image.
	 *
	 * @see android.widget.ImageView
	 */
    public DownloadImage(String file, String path, ImageView iv) {
        super();
        this.file = file;
        this.path = path;
        this.iv = iv;
    }

    /**
     * Before starting background thread do nothing.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * Downloading file in background thread.
	 */
    @Override
    protected Void doInBackground(Void... v) {
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

            while ((count = input.read(data)) != -1) {

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
     * After completing background task, set the image on the ImageView.
     */
    @Override
    protected void onPostExecute(Void v) {
        Log.d("File downloaded", path);
        Bitmap myBitmap = BitmapFactory.decodeFile(path);
        iv.setImageBitmap(myBitmap);
    }

}