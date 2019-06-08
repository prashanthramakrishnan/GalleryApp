package com.prashanth.galleryapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.prashanth.galleryapp.R;
import com.prashanth.galleryapp.util.Utility;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import okhttp3.internal.Util;
import timber.log.Timber;

public class GalleryAdapter extends BaseAdapter {

    Context context;

    ArrayList<HashMap<String, String>> data;

    public GalleryAdapter(Context context, ArrayList<HashMap<String, String>> data) {
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
        AlbumViewHolder holder = null;
        if (convertView == null) {
            holder = new AlbumViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_layout, parent, false);

            holder.galleryImage = (ImageView) convertView.findViewById(R.id.galleryImage);
            holder.count = (TextView) convertView.findViewById(R.id.galleryCount);
            holder.tittle = (TextView) convertView.findViewById(R.id.galleryTittle);

            convertView.setTag(holder);
        } else {
            holder = (AlbumViewHolder) convertView.getTag();
        }

        holder.galleryImage.setId(position);
        holder.count.setId(position);
        holder.tittle.setId(position);

        HashMap<String, String> temp = new HashMap<String, String>();

        temp = data.get(position);

        try {
            holder.tittle.setText(temp.get(Utility.ALBUM_KEY));
            holder.count.setText(temp.get(Utility.COUNT_KEY));
            Picasso.get()
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
