package com.prashanth.galleryapp.util;

import java.util.Comparator;
import java.util.HashMap;

public class GalleryComparator implements Comparator<HashMap<String, String>> {

    private final String key;

    private final String order;

    public GalleryComparator(String key, String order) {
        this.key = key;
        this.order = order;
    }

    @Override
    public int compare(HashMap<String, String> first, HashMap<String, String> second) {
        String firstValue = first.get(key);
        String seccondValue = second.get(key);

        if (this.order.toLowerCase().contentEquals("asc")) {
            return firstValue.compareTo(seccondValue);
        } else {
            return seccondValue.compareTo(firstValue);
        }
    }
}
