package com.prashanth.galleryapp.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GalleryAppSharedPref {

    public static class ImageInfo {

        String imageName;

        String imageUrl;
    }

    private static final String FILE_NAME = "galleryapppref";

    private static final String IMAGE_URL_KEY = "image_url";

    private static final String IMAGE_LIST_KEY = "image_list_key";

    private SharedPreferences sharedPreferences;

    public GalleryAppSharedPref(Context context) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public void setFirebaseImageUrl(String imageURl) {
        sharedPreferences.edit().putString(IMAGE_URL_KEY, imageURl).apply();
    }

    public String getFirebaseImageUrl() {
        return sharedPreferences.getString(IMAGE_URL_KEY, null);
    }

    public void addImageInfo(String imageName, String imageUrl) {
        Set<String> imageInfoSet = sharedPreferences.getStringSet(IMAGE_LIST_KEY, new HashSet<>());
        imageInfoSet.add(imageName + "#" + imageUrl);
        sharedPreferences.edit().putStringSet(IMAGE_LIST_KEY, imageInfoSet).apply();
    }

    public List<ImageInfo> getImageInfoList() {
        List<ImageInfo> imageInfos = new ArrayList<>();

        Set<String> knownImageInfoSet = sharedPreferences.getStringSet(IMAGE_LIST_KEY, new HashSet<>());

        for (String knownImage : knownImageInfoSet) {
            String[] info = knownImage.split("#");
            ImageInfo imageInfo = new ImageInfo();
            imageInfo.imageName = info[0];
            imageInfo.imageUrl = info[1];
            imageInfos.add(imageInfo);
        }

        return imageInfos;
    }

}