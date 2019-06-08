package com.prashanth.galleryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.icu.util.UniversalTimeScale;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.prashanth.galleryapp.ui.adapter.GalleryAdapter;
import com.prashanth.galleryapp.util.GalleryComparator;
import com.prashanth.galleryapp.util.Utility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.gallery_grid_view)
    GridView galleryGridView;

    private ArrayList<HashMap<String, String>> albumList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        hasExternalStoragePermission();

        float dp = getResources().getDisplayMetrics().widthPixels / (getApplicationContext().getResources().getDisplayMetrics().densityDpi / 160f);
        if (dp < 360) {
            dp = (dp - 17) / 2;
            float px = Utility.convertDptoPixels(dp, this);
            galleryGridView.setColumnWidth(Math.round(px));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadImagesFromStorage().execute();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 12) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.d("write storage permission granted");
            }
        }
    }

    private void hasExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
            }
        }
    }

    class LoadImagesFromStorage extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            albumList.clear();
        }

        @Override
        protected String doInBackground(String... strings) {
            String xml = "";
            String path = null;
            String album = null;
            String timestamp = null;
            String countPhoto = null;
            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri internal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] imgdisp = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};
            Cursor cursorExt = getContentResolver().query(external, imgdisp, "_data IS NOT NULL ) GROUP BY " +
                    "(bucket_display_name", null, null);
            Cursor cursorInt = getContentResolver().query(internal, imgdisp, "_data IS NOT NULL ) GROUP BY " +
                    "(bucket_display_name", null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExt, cursorInt});

            while (cursor.moveToNext()) {
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                countPhoto = Utility.getCount(MainActivity.this, album);
                albumList.add(Utility.mapDetails(album, path, timestamp, Utility.converTimestampToDateString(timestamp), countPhoto));
            }
            cursor.close();

            Collections.sort(albumList, new GalleryComparator(Utility.TIMESTAMP_KEY, "dsc"));

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            GalleryAdapter adapter = new GalleryAdapter(MainActivity.this, albumList);
            galleryGridView.setAdapter(adapter);
            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                    Intent intent = new Intent(Home.this, AlbumActivity.class);
//                    intent.putExtra("name", albumlist.get(+position).get(Funtion.KEY_ALBUM));
//                    Toast.makeText(Home.this, "Clicked", Toast.LENGTH_SHORT).show();
//                    startActivity(intent);
//                    finish();
                }
            });

        }
    }
}
