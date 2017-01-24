package com.rutgerssustainability.android.rutgerssustainability.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rutgerssustainability.android.rutgerssustainability.R;
import com.rutgerssustainability.android.rutgerssustainability.pojos.Trash;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shreyashirday on 1/23/17.
 */
public class TrashListAdapter extends BaseAdapter {
    private Trash[] trash;
    private Context ctx;

    public TrashListAdapter(final Trash[] trash, final Context ctx) {
        super();
        this.trash = trash;
        this.ctx = ctx;
    }

    static class ViewHolder {
        ImageView trashImageView;
        TextView trashDateText;
    }

    @Override
    public int getCount() {
        return this.trash.length;
    }

    @Override
    public Trash getItem(final int position) {
        return this.trash[position];
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.ctx).inflate(R.layout.trash_list_item,null,false);
            viewHolder = new ViewHolder();
            final ImageView trashImageView = (ImageView)convertView.findViewById(R.id.trash_image_view);
            final TextView trashDateText = (TextView)convertView.findViewById(R.id.date_text_view);
            viewHolder.trashImageView = trashImageView;
            viewHolder.trashDateText = trashDateText;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        convertView.setBackgroundColor(Color.BLACK);
        final Trash currentTrash = getItem(position);
        final String pictureUrl = currentTrash.getPictureUrl();
        Picasso.with(this.ctx).load(pictureUrl).rotate(90).into(viewHolder.trashImageView);
        final long epoch = currentTrash.getEpoch();
        final Date trashDate = new Date(epoch);
        final String dateString = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(trashDate);
        viewHolder.trashDateText.setText(dateString);
        return convertView;
    }
}
