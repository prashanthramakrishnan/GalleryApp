package com.prashanth.galleryapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.jsibbold.zoomage.ZoomageView;
import com.prashanth.galleryapp.R;
import com.prashanth.galleryapp.util.Utility;
import java.io.File;

public class ViewImageActivity extends AppCompatActivity {

    @BindView(R.id.back_button)
    ImageView backButton;

    @BindView(R.id.title_image)
    TextView title;

    @BindView(R.id.view_image)
    ZoomageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        ButterKnife.bind(this);
        title.setText(getIntent().getStringExtra(Utility.IMAGE_NAME));

        backButton.setOnClickListener(v -> onBackPressed());

        Glide.with(this)
                .load(new File(getIntent().getStringExtra(Utility.IMAGE_PATH)))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(imageView);
    }
}