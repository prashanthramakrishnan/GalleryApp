package com.prashanth.galleryapp.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.prashanth.galleryapp.R;
import com.prashanth.galleryapp.ui.adapter.AlbumAdapter;
import com.prashanth.galleryapp.util.GalleryComparator;
import com.prashanth.galleryapp.util.Utility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class AlbumActivity extends AppCompatActivity {

    @BindView(R.id.album_grid_view)
    GridView gridView;

    @BindView(R.id.album_tittle)
    TextView title;

    @BindView(R.id.back_button)
    ImageView backButton;

    String albumName;

    ArrayList<HashMap<String, String>> imageList = new ArrayList<>();

    AlbumAdapter albumAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        ButterKnife.bind(this);

        albumName = getIntent().getStringExtra(Utility.ALBUM_NAME);
        title.setText(albumName);
        setGridView();
        backButton.setOnClickListener(v -> onBackPressed());
        albumAdapter = new AlbumAdapter(AlbumActivity.this, imageList);
        gridView.setAdapter(albumAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadAlbumImages().execute();
    }

    private void setGridView() {
        float dp = getResources().getDisplayMetrics().widthPixels / (getApplicationContext().getResources().getDisplayMetrics().densityDpi / 160f);

        if (dp < 360) {
            dp = (dp - 17) / 2;
            float pixels = Utility.convertDptoPixels(dp, this);
            gridView.setColumnWidth(Math.round(pixels));
        }
    }

    @SuppressLint("StaticFieldLeak")
    class LoadAlbumImages extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageList.clear();
        }

        @Override
        protected String doInBackground(String... strings) {
            imageList.clear();
            String path;
            String album;
            String timestamp;

            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri internal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] images = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};

            Cursor cursorExternal = getContentResolver().query(external, images, "bucket_display_name =\"" + albumName + "\"", null, null);
            Cursor cursorInternal = getContentResolver().query(internal, images, "bucket_display_name =\"" + albumName + "\"", null, null);

            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});
            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));

                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

                imageList.add(Utility.mapDetails(album, path, timestamp, Utility.converTimestampToDateString(timestamp), null));

            }
            cursor.close();

            Collections.sort(imageList, new GalleryComparator(Utility.TIMESTAMP_KEY, "dsc"));
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            albumAdapter.notifyDataSetChanged();
            gridView.setOnItemClickListener((parent, view, position, id) -> {

                Intent intent = new Intent(AlbumActivity.this, ViewImageActivity.class);
                intent.putExtra(Utility.IMAGE_PATH, imageList.get(+position).get(Utility.PATH_KEY));
                intent.putExtra(Utility.IMAGE_NAME, imageList.get(+position).get(Utility.ALBUM_KEY));
                intent.putExtra(Utility.IMAGE_LIST, imageList);
                intent.putExtra(Utility.IMAGE_POSITION, position);
                startActivity(intent);
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AlbumActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}