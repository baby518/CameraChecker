package com.sample.camera.utils;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {
    private static final String TAG = "FileUtil";
    public static void deleteFile(String fileName) {
        if (fileName != null) {
            File f = new File(fileName);
            if (f.delete()) {
                Log.v(TAG, "file deleted: " + fileName);
            }
        }
    }

    public static String convertFormatToMimeType(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return "video/mp4";
        }
        return "video/3gpp";
    }

    public static String convertFormatToFileExt(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return ".mp4";
        }
        return ".3gp";
    }

    public static String dateFormat(long dateTaken, DateFormat format) {
        if (format == null) return null;
        Date date = new Date(dateTaken);

        return format.format(date);
    }
}
