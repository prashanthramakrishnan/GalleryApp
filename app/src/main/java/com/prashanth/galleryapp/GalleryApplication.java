package com.prashanth.galleryapp;

import android.app.Application;
import timber.log.Timber;

public class GalleryApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
