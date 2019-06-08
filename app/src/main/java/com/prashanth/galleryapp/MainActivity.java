package com.prashanth.galleryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.icu.util.UniversalTimeScale;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.prashanth.galleryapp.ui.adapter.GalleryAdapter;
import com.prashanth.galleryapp.util.GalleryComparator;
import com.prashanth.galleryapp.util.Utility;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.gallery_grid_view)
    GridView galleryGridView;

    private ArrayList<HashMap<String, String>> albumList = new ArrayList<>();

    private String imageFilePath;

    private static final int REQUEST_CAPTURE_IMAGE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        float dp = getResources().getDisplayMetrics().widthPixels / (getApplicationContext().getResources().getDisplayMetrics().densityDpi / 160f);
        if (dp < 360) {
            dp = (dp - 17) / 2;
            float px = Utility.convertDptoPixels(dp, this);
            galleryGridView.setColumnWidth(Math.round(px));
        }

        if (hasExternalStoragePermission()) {
            albumList.clear();
            new LoadImagesFromStorage().execute();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_camera:
                checkCameraPermissionAndOpenCamera();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkCameraPermissionAndOpenCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 110);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            Intent pictureIntent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            if (pictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    Timber.e(e, "Error creating file");
                }
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this, "com.prashanth.galleryapp.provider", photoFile);
                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            photoURI);
                    try {
                        startActivityForResult(pictureIntent,
                                REQUEST_CAPTURE_IMAGE);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            Toast.makeText(this, "No camera support", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE_IMAGE) {
            if (resultCode == RESULT_CANCELED) {
                deleteFile();
            } else {
                addPhotoToGallery();
            }
        }
    }

    private void addPhotoToGallery() {
        try {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(imageFilePath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteFile() {
        try {
            File file = new File(imageFilePath);
            boolean deleted = file.delete();
        } catch (Exception e) {
            Timber.e(e, "Exception deleting file");
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 12) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.d("write storage permission granted");
                new LoadImagesFromStorage().execute();
            }
        }

        if (requestCode == 110) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.d("Camera permission granted");
                openCamera();
            }
        }
    }

    private boolean hasExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
                return false;
            }
        }
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    class LoadImagesFromStorage extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            albumList.clear();
        }

        @Override
        protected String doInBackground(String... strings) {
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
                    //launch new screen to open the image
                }
            });

        }
    }
}
