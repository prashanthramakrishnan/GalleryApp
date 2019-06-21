package com.prashanth.galleryapp.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jsibbold.zoomage.ZoomageView;
import com.prashanth.galleryapp.R;
import com.prashanth.galleryapp.util.Utility;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import timber.log.Timber;

public class ViewImageActivity extends AppCompatActivity {

    @BindView(R.id.view_image)
    ZoomageView imageView;

    private FirebaseStorage firebaseStorage;

    private StorageReference storageReference;

    private File filePath;

    private String filePathString;

    private GalleryAppSharedPref galleryAppSharedPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        ButterKnife.bind(this);

        filePathString = getIntent().getStringExtra(Utility.IMAGE_PATH);
        filePath = new File(filePathString);
        Glide.with(this)
                .load(filePath)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(imageView);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        galleryAppSharedPref = new GalleryAppSharedPref(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_image_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                uploadPhotoToFirebase();
                return true;

            case R.id.action_crop:
                cropImage();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                saveFile(resultUri);
                imageView.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Timber.e(result.getError(), "Error in cropping or rotating the photo");
            }
        }
    }

    private void uploadPhotoToFirebase() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getResources().getString(R.string.uploading));
            progressDialog.show();
            progressDialog.setCancelable(false);

            Uri file = Uri.fromFile(filePath);
            StorageReference reference = storageReference.child(getString(R.string.firebase_folder) + file.getLastPathSegment());
            reference.putFile(file)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            galleryAppSharedPref.addImageInfo(filePathString.substring(filePathString.lastIndexOf("/") + 1), uri.toString());
                        });
                        Toast.makeText(ViewImageActivity.this, getResources().getString(R.string.uploading_finished), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> {

                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(ViewImageActivity.this, getResources().getString(R.string.failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        progressDialog
                                .setMessage(getResources().getString(R.string.uploaded) + " " + (int) progress + getResources().getString(R.string.percentage));
                    });
        }
    }

    private void cropImage() {
        CropImage.activity(Uri.fromFile(filePath))
                .start(this);
    }

    void saveFile(Uri sourceUri) {
        String sourceFilename = sourceUri.getPath();
        File localFile =
                null;
        try {
            localFile = File.createTempFile("EDITED_" + filePathString.substring(filePathString.lastIndexOf("/") + 1),
                    getResources().getString(R.string.image_extension),
                    new File(Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.app_name)));
        } catch (IOException e) {
            Timber.e(e, "Exception creating file");
        }

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(localFile, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);
        } catch (IOException e) {
            Timber.e("Exception writing file");
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                Timber.e(e,"Exception writing file");
            }
        }
        Utility.sendBroadcast(this, localFile);
    }
}