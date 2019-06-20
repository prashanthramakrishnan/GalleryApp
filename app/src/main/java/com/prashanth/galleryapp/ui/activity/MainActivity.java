package com.prashanth.galleryapp.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.prashanth.galleryapp.R;
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
import java.util.List;
import java.util.Locale;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.gallery_grid_view)
    GridView galleryGridView;

    private ArrayList<HashMap<String, String>> albumList = new ArrayList<>();

    private String imageFilePath;

    private static final int REQUEST_CAPTURE_IMAGE = 200;

    GalleryAdapter adapter;

    private FirebaseStorage firebaseStorage;

    private StorageReference storageReference;

    private GalleryAppSharedPref galleryAppSharedPref;

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

        galleryAppSharedPref = new GalleryAppSharedPref(this);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        if (hasExternalStoragePermission()) {
            albumList.clear();

            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + getResources().getString(R.string.app_name));

            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    Timber.d("Directory created successfully");
                } else {
                    Timber.d("Directory creation failed");
                }
            }
        }
        adapter = new GalleryAdapter(MainActivity.this, albumList);
        galleryGridView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasExternalStoragePermission()) {
            downloadImagesFromFirebase();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        File storageDir = new File(Environment.getExternalStorageDirectory() +
                File.separator + getResources().getString(R.string.app_name));
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
            downloadImagesFromFirebase();
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

                albumList.clear();

                File folder = new File(Environment.getExternalStorageDirectory() +
                        File.separator + getResources().getString(R.string.app_name));

                if (!folder.exists()) {
                    if (folder.mkdirs()) {
                        Timber.d("Directory created successfully");
                    } else {
                        Timber.d("Directory creation failed");
                    }
                }

                downloadImagesFromFirebase();
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
            albumList.clear();
            String path;
            String album;
            String timestamp;
            String countPhoto;

            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri internal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] imgDisp = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};
            Cursor internalCursor = getContentResolver().query(external,
                    imgDisp,
                    MediaStore.Images.Media.DATA + " like ? ",
                    new String[]{"%/GalleryApp/%"},
                    null);

            Cursor externalCursor = getContentResolver().query(internal,
                    imgDisp,
                    MediaStore.Images.Media.DATA + " like ? ",
                    new String[]{"%/GalleryApp/%"},
                    null);

            Cursor cursor = new MergeCursor(new Cursor[]{internalCursor, externalCursor});

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
            adapter.notifyDataSetChanged();
            galleryGridView.setOnItemClickListener((parent, view, position, id) -> {
                //launch new screen to open the image
                Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
                intent.putExtra(Utility.ALBUM_NAME, albumList.get(+position).get(Utility.ALBUM_KEY));
                startActivity(intent);
                finish();
            });

        }
    }

    private void downloadImagesFromFirebase() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.downloading));
        progressDialog.show();
        List<GalleryAppSharedPref.ImageInfo> imageInfos = galleryAppSharedPref.getImageInfoList();
        if (galleryAppSharedPref.getImageInfoList() != null && !galleryAppSharedPref.getImageInfoList().isEmpty()) {
            GalleryAppSharedPref.ImageInfo imageInfo = galleryAppSharedPref.getImageInfoList().get(0);
            File storageDir = new File(Environment.getExternalStorageDirectory() +
                    File.separator + getResources().getString(R.string.app_name));
            try {
                File checkFile = File.createTempFile(imageInfo.imageName, ".jpg", storageDir);
                if (checkFile.exists()) {
                    Timber.d("File already exists, don't download");
                    progressDialog.dismiss();
                    new LoadImagesFromStorage().execute();
                } else {
                    Timber.d("File doesn't exist downloading it");
                    StorageReference imageToDownload = storageReference.child(getString(R.string.firebase_folder) + imageInfo.imageName);
                    try {
                        File localFile =
                                File.createTempFile(imageInfo.imageName, ".jpg",
                                        new File(Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.app_name)));
                        imageToDownload.getFile(localFile)
                                .addOnSuccessListener(taskSnapshot -> {
                                    Timber.d("Image successfully downloaded");
                                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    Uri contentUri = Uri.fromFile(localFile);
                                    mediaScanIntent.setData(contentUri);
                                    this.sendBroadcast(mediaScanIntent);
                                    new LoadImagesFromStorage().execute();
                                    progressDialog.dismiss();
                                }).addOnFailureListener(e -> {
                            Timber.e(e, "Failed to download the image");
                            new LoadImagesFromStorage().execute();
                            progressDialog.dismiss();
                        }).addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage(getResources().getString(R.string.downloaded) + (int) progress + "%");
                            new LoadImagesFromStorage().execute();
                        }).addOnFailureListener(e -> {
                            Timber.e(e, "Can't download file from Firebase");
                            new LoadImagesFromStorage().execute();
                            progressDialog.dismiss();
                        });
                    } catch (IOException e) {
                        progressDialog.dismiss();
                        new LoadImagesFromStorage().execute();
                        Timber.e("Couldn't create file to download");
                    }

                }
            } catch (IOException e) {
                Timber.e(e, "Exception checking file");
                new LoadImagesFromStorage().execute();
                progressDialog.dismiss();
            }

        } else {
            new LoadImagesFromStorage().execute();
            progressDialog.dismiss();
        }

    }
}