package com.sample.camera.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MediaSaver {
    private static final String TAG = "CAM_MediaSaver";
    private static final String VIDEO_BASE_URI = "content://media/external/video/media";
    /**
     * An interface defining the callback when a media is saved.
     */
    public interface OnMediaSavedListener {
        /**
         * The callback when the saving is done in the background.
         * @param uri The final content Uri of the saved media.
         */
        public void onMediaSaved(Uri uri);
    }

    protected final ContentResolver mContentResolver;
    private final ImageFileNamer mImageFileNamer;

    public MediaSaver(ContentResolver contentResolver, String nameFormat) {
        mContentResolver = contentResolver;
        mImageFileNamer = new ImageFileNamer(nameFormat);
    }

    public void addImage(final byte[] data/*, Location loc*/, int width,
                         int height, int orientation, OnMediaSavedListener l) {
        long date = System.currentTimeMillis();
        String title = mImageFileNamer.generateName(date);
        ImageSaveTask t = new ImageSaveTask(data, title, date,
                /*(loc == null) ? null : new Location(loc),*/
                width, height, orientation, mContentResolver, l);
        t.execute();
    }

    public void addVideo(String path, ContentValues values, OnMediaSavedListener listener) {
        // We don't set a queue limit for video saving because the file
        // is already in the storage. Only updating the database.
        new VideoSaveTask(path, values, listener, mContentResolver).execute();
    }

    private static class ImageFileNamer {
        private final SimpleDateFormat mFormat;

        // The date (in milliseconds) used to generate the last name.
        private long mLastDate;

        // Number of names generated for the same second.
        private int mSameSecondCount;

        public ImageFileNamer(String format) {
            mFormat = new SimpleDateFormat(format);
        }

        public String generateName(long dateTaken) {
            Date date = new Date(dateTaken);
            String result = mFormat.format(date);

            // If the last name was generated for the same second,
            // we append _1, _2, etc to the name.
            if (dateTaken / 1000 == mLastDate / 1000) {
                mSameSecondCount++;
                result += "_" + mSameSecondCount;
            } else {
                mLastDate = dateTaken;
                mSameSecondCount = 0;
            }

            return result;
        }
    }

    protected class ImageSaveTask extends AsyncTask<Void, Void, Uri> {
        protected final byte[] data;
        protected final String title;
        protected final long date;
//        protected final Location loc;
        protected int width, height;
        protected final int orientation;
        protected final ContentResolver resolver;
        protected final OnMediaSavedListener listener;

        public ImageSaveTask(byte[] data, String title, long date, /*Location loc,*/
                             int width, int height, int orientation,
                             ContentResolver resolver,
                             OnMediaSavedListener listener) {
            this.data = data;
            this.title = title;
            this.date = date;
//            this.loc = loc;
            this.width = width;
            this.height = height;
            this.orientation = orientation;
            this.resolver = resolver;
            this.listener = listener;
        }

        @Override
        protected Uri doInBackground(Void... v) {
            if (width == 0 || height == 0) {
                // Decode bounds
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data, 0, data.length, options);
                width = options.outWidth;
                height = options.outHeight;
            }
            try {
                return Storage.addImage(
                        resolver, title, date,/* loc,*/ orientation, data, width, height);
            } catch (IOException e) {
                Log.e(TAG, "Failed to write data", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Uri uri) {
            if (listener != null) {
                listener.onMediaSaved(uri);
            }
        }
    }

    private class VideoSaveTask extends AsyncTask <Void, Void, Uri> {
        private String path;
        private final ContentValues values;
        private final OnMediaSavedListener listener;
        private final ContentResolver resolver;

        public VideoSaveTask(String path, ContentValues values, OnMediaSavedListener l,
                             ContentResolver r) {
            this.path = path;
            this.values = new ContentValues(values);
            this.listener = l;
            this.resolver = r;
        }

        @Override
        protected Uri doInBackground(Void... v) {
            Uri uri = null;
            try {
                String finalName = values.getAsString(MediaStore.Video.Media.DATA);
                File finalFile = new File(finalName);
                if (new File(path).renameTo(finalFile)) {
                    path = finalName;
                }
                values.put(MediaStore.Video.Media.SIZE, finalFile.length());

                if (!values.containsKey(MediaStore.Video.Media.DURATION)) {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(finalName);
                    String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    values.put(MediaStore.Video.Media.DURATION, duration);
                }

                Log.i(TAG, String.format("insert video path = %s, finalName = %s", path, finalName));
                uri = resolver.insert(Uri.parse(VIDEO_BASE_URI), values);
            } catch (Exception e) {
                // We failed to insert into the database. This can happen if
                // the SD card is unmounted.
                Log.e(TAG, "failed to add video to media store", e);
                uri = null;
            } finally {
                Log.v(TAG, "Current video URI: " + uri);
            }
            return uri;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            if (listener != null) {
                listener.onMediaSaved(uri);
            }
        }
    }

}
