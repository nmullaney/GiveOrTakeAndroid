package com.bitdance.giveortake;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by nora on 6/27/13.
 */
public class ImageFetcher {
    public static final String TAG = "ImageFetcher";

    private static final int BYTE_BUFFER_SIZE = 1024;

    private Context context;

    public ImageFetcher(Context context) {
        this.context = context;
    }

    public boolean fetchImageForItem(Item item) {
        File imageFile = item.getLocalImageFile(context);
        try {
            return fetchImageToFile(item.getImageURL(), imageFile, ".image");
        } catch (IOException e) {
            Log.e(TAG, "Failed to get image", e);
            return false;
        }
    }

    public boolean fetchThumbnailForItem(Item item) {
        File thumbnailFile = item.getLocalThumbnailFile(context);
        try {
            return fetchImageToFile(item.getThumbnailURL(), thumbnailFile, ".thumbnail");
        } catch (IOException e) {
            Log.e(TAG, "Failed to get image", e);
            return false;
        }
    }

    public boolean fetchImageToFile(String urlSpec, File file, String suffix) throws IOException {
        Log.i(TAG, "Fetching image to " + file.getName() + " for type " + suffix);
        boolean success = false;
        URL url = new URL(urlSpec);
        File tempFile = File.createTempFile("download", suffix, file.getParentFile());
        FileOutputStream out = null;

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            out = new FileOutputStream(tempFile);
            InputStream in = connection.getInputStream();

            int bytesRead = 0;
            byte[] buffer = new byte[BYTE_BUFFER_SIZE];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            out = null;
            tempFile.renameTo(file);
            success = true;
        } finally {
            connection.disconnect();
            if (out != null) {
                out.close();
            }
        }
        return success;
    }
}
