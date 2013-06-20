package com.bitdance.giveortake;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nora on 6/20/13.
 */
public class ItemArrayAdapter  extends ArrayAdapter<Item> {
    public static final String TAG = "ItemArrayAdapter";

    public ItemArrayAdapter(Context context, ArrayList<Item> items) {
        super(context, R.layout.list_item_item, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.list_item_item, null);
        }

        Item i = getItem(position);

        ImageView thumbnailView = (ImageView) convertView.findViewById(R.id.list_item_thumbnail);
        if (i.getThumbnail() != null) {
            Log.i(TAG, "Setting image drawable for item: " + i);
            thumbnailView.setImageDrawable(i.getThumbnail());
        } else {
            Log.i(TAG, "Item image is null for item: " + i);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.list_item_name);
        textView.setText(i.getName());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_state);
        Drawable imageState = i.getDrawableForState(getContext());
        imageView.setImageDrawable(imageState);

        return convertView;
    }
}
