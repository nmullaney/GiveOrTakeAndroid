package com.bitdance.giveortake;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nora on 6/20/13.
 */
public class ItemArrayAdapter  extends ArrayAdapter<Item> {
    public static final String TAG = "ItemArrayAdapter";

    public ItemArrayAdapter(Context context, ArrayList<Item> items) {
        super(context, R.layout.list_item_item, items);
        Log.d(TAG, "The context = " + context);
        Log.d(TAG, "The items = " + items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.list_item_item, null);
        }

        Item i = getItem(position);

        ImageView thumbnailView = (ImageView) convertView.findViewById(R.id.list_item_thumbnail);
        Drawable thumbnail = i.getThumbnail(getContext());
        if (thumbnail != null) {
            Log.d(TAG, "Setting image drawable for item: " + i);
            thumbnailView.setImageDrawable(thumbnail);
        } else {
            Log.d(TAG, "Loading thumbnail for item: " + i);
            Intent intent = new Intent(getContext(), ItemService.class);
            intent.setAction(ItemService.FETCH_ITEM_THUMBNAIL);
            intent.putExtra(ItemService.EXTRA_ITEM, i);
            getContext().startService(intent);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.list_item_name);
        textView.setText(i.getName());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_state);
        Drawable imageState = i.getDrawableForState(getContext());
        imageView.setImageDrawable(imageState);

        return convertView;
    }

    // This is a bit lame, but probably good enough
    public int getPositionForItemID(Long itemID) {
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).getId().equals(itemID)) {
                return i;
            }
        }
        return -1;
    }

    public void setThumbnailForView(View view, Item item) {
        ImageView thumbnailView = (ImageView) view.findViewById(R.id.list_item_thumbnail);
        Drawable thumbnail = item.getThumbnail(getContext());
        thumbnailView.setImageDrawable(thumbnail);
    }
}
