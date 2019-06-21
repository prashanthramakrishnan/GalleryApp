package com.prashanth.galleryapp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import timber.log.Timber;

public class Utility {

    public static final String ALBUM_KEY = "ALBUM_NAME";

    public static final String PATH_KEY = "PATH";

    public static final String TIMESTAMP_KEY = "TIMESTAMP";

    public static final String TIME_KEY = "DATE";

    public static final String COUNT_KEY = "COUNT";

    public static final String ALBUM_NAME = "ALBUM_NAME";

    public static final String IMAGE_PATH = "IMAGE_PATH";

    public static final String IMAGE_NAME = "IMAGE_NAME";

    public static final String IMAGE_LIST = "IMAGE_LIST";

    public static final String IMAGE_POSITION = "IMAGE_POSITION";

    public static float convertDptoPixels(float dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().densityDpi / 160f);
    }

    public static String getCount(Context context, String album_name) {
        Uri Ext = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri Inter = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        String[] arrayImagesDisplay = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_MODIFIED};
        Cursor cursorExt = context.getContentResolver().query(Ext, arrayImagesDisplay, "bucket_display_name =\"" + album_name + "\"", null, null);
        Cursor cursorInt = context.getContentResolver().query(Inter, arrayImagesDisplay, "bucket_display_name =\"" + album_name + "\"", null, null);
        Cursor cursor = new MergeCursor(new Cursor[]{cursorExt, cursorInt});
        return cursor.getCount() + "Photos";
    }

    public static HashMap<String, String> mapDetails(String album, String path, String timestamp, String time, String count) {
        HashMap<String, String> map = new HashMap<>();
        map.put(ALBUM_KEY, album);
        map.put(PATH_KEY, path);
        map.put(TIMESTAMP_KEY, timestamp);
        map.put(TIME_KEY, time);
        map.put(COUNT_KEY, count);
        return map;
    }

    @SuppressLint("SimpleDateFormat")
    public static String converTimestampToDateString(String timestamp) {
        long datetime = Long.parseLong(timestamp);
        Date date = new Date(datetime);
        DateFormat format = new SimpleDateFormat("dd/MM HH:mm");
        return format.format(date);
    }

    public static void sendBroadcast(Context context, File file) {
        Timber.d("Sending broadcast intent");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

}
