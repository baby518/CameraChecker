package com.sample.camera.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Storage {
    private static final String TAG = "Storage";
    public static final String DCIM =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
    public static final String DIRECTORY = DCIM + "/Camera";
    public static final File DIRECTORY_FILE = new File(DIRECTORY);
    public static final String JPEG_POSTFIX = ".jpg";
    public static final String MIME_TYPE_JPEG = "image/jpeg";

    public static Uri addImage(ContentResolver resolver, String title, long date,
                               /*Location location, */int orientation, byte[] data, int width,
                               int height) throws IOException {
        String path = generateFilepath(title, MIME_TYPE_JPEG);
        long fileLength = writeFile(path, data);
        if (fileLength >= 0) {
            return addImageToMediaStore(resolver, title, date, null, orientation, fileLength,
                    path, width, height, MIME_TYPE_JPEG);
        }
        return null;
    }

    private static String generateFilepath(String title, String mimeType) {
        return generateFilepath(DIRECTORY, title, mimeType);
    }

    public static String generateFilepath(String directory, String title, String mimeType) {
        String extension = null;
        if (MIME_TYPE_JPEG.equals(mimeType)) {
            extension = JPEG_POSTFIX;
        } else {
            throw new IllegalArgumentException("Invalid mimeType: " + mimeType);
        }
        return (new File(directory, title + extension)).getAbsolutePath();
    }

    /**
     * Writes the data to a file.
     *
     * @param path The path to the target file.
     * @param data The data to save.
     *
     * @return The size of the file. -1 if failed.
     */
    private static long writeFile(String path, byte[] data) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(data);
            return data.length;
        } catch (Exception e) {
            Log.e(TAG, "Failed to write data", e);
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                Log.e(TAG, "Failed to close file after write", e);
            }
        }
        return -1;
    }

    public static Uri addImageToMediaStore(ContentResolver resolver, String title, long date,
            Location location, int orientation, long jpegLength, String path, int width, int height,
            String mimeType) {
        // Insert into MediaStore.
        ContentValues values = getContentValuesForData(title, date, location, orientation,
                jpegLength, path, width, height, mimeType);

        Uri uri = null;
        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Throwable th) {
            // This can happen when the external volume is already mounted, but
            // MediaScanner has not notify MediaProvider to add that volume.
            // The picture is still safe and MediaScanner will find it and
            // insert it into MediaProvider. The only problem is that the user
            // cannot click the thumbnail to review the picture.
            Log.e(TAG, "Failed to write MediaStore" + th);
        }
        return uri;
    }

    // Get a ContentValues object for the given photo data
    public static ContentValues getContentValuesForData(String title, long date, Location location,
            int orientation, long jpegLength, String path, int width, int height, String mimeType) {

        File file = new File(path);
        long dateModifiedSeconds = TimeUnit.MILLISECONDS.toSeconds(file.lastModified());

        ContentValues values = new ContentValues(10);
        values.put(MediaStore.Images.ImageColumns.TITLE, title);
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, title + JPEG_POSTFIX);
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, date);
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, dateModifiedSeconds);
        // Clockwise rotation in degrees. 0, 90, 180, or 270.
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, orientation);
        values.put(MediaStore.Images.ImageColumns.DATA, path);
        values.put(MediaStore.Images.ImageColumns.SIZE, jpegLength);

        values.put(MediaStore.MediaColumns.WIDTH, width);
        values.put(MediaStore.MediaColumns.HEIGHT, height);

        if (location != null) {
            values.put(MediaStore.Images.ImageColumns.LATITUDE, location.getLatitude());
            values.put(MediaStore.Images.ImageColumns.LONGITUDE, location.getLongitude());
        }
        return values;
    }
}
