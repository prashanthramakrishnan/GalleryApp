package com.prashanth.galleryapp.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.prashanth.galleryapp.R;
import com.prashanth.galleryapp.util.Utility;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import timber.log.Timber;

public class AlbumAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<HashMap<String, String>> data;

    public AlbumAdapter(Context context, ArrayList<HashMap<String, String>> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlbumImageViewHolder holder = null;
        if (convertView == null) {
            holder = new AlbumImageViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.image_layout, parent, false);

            holder.imageAlbum = (ImageView) convertView.findViewById(R.id.image_album);
            convertView.setTag(holder);
        } else {
            holder = (AlbumImageViewHolder) convertView.getTag();
        }

        holder.imageAlbum.setId(position);

        HashMap<String, String> temp = data.get(position);

        try {

            Glide.with(context)
                    .load(new File(temp.get(Utility.PATH_KEY)))
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imageAlbum);

        } catch (Exception e) {
            Timber.e(e, "Exception loading image");
        }
        return convertView;
    }

    class AlbumImageViewHolder {

        ImageView imageAlbum;
    }
}