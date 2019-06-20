package com.prashanth.galleryapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.prashanth.galleryapp.R;
import com.prashanth.galleryapp.util.Utility;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import timber.log.Timber;

public class GalleryAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<HashMap<String, String>> data;

    public GalleryAdapter(Context context, ArrayList<HashMap<String, String>> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        if (data.size() > 0) {
            return 1;
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlbumViewHolder holder;
        if (convertView == null) {
            holder = new AlbumViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_layout, parent, false);

            holder.galleryImage = convertView.findViewById(R.id.galleryImage);
            holder.count = convertView.findViewById(R.id.galleryCount);
            holder.tittle = convertView.findViewById(R.id.galleryTittle);

            convertView.setTag(holder);
        } else {
            holder = (AlbumViewHolder) convertView.getTag();
        }

        holder.galleryImage.setId(position);
        holder.count.setId(position);
        holder.tittle.setId(position);

        HashMap<String, String> temp = data.get(position);

        try {
            holder.tittle.setText(temp.get(Utility.ALBUM_KEY));
            holder.count.setText(temp.get(Utility.COUNT_KEY));

            Glide.with(context)
                    .load(new File(temp.get(Utility.PATH_KEY)))
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.galleryImage);

        } catch (Exception e) {
            Timber.e(e, "Exception loading album");

        }
        return convertView;
    }

    class AlbumViewHolder {

        ImageView galleryImage;

        TextView count, tittle;
    }

}
