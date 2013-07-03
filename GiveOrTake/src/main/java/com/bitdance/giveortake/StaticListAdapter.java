package com.bitdance.giveortake;

import android.R;
import android.content.Context;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by nora on 7/3/13.
 */
public class StaticListAdapter extends ArrayAdapter<StaticListItem> {
    public static final String TAG = "StaticListAdapter";

    public StaticListAdapter(Context context, ArrayList<StaticListItem> items) {
        super(context, R.layout.simple_list_item_1, items);
        Log.i(TAG, "Created with items: " + items.size());
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }

    @Override
    public int getItemViewType(int position) {
        return IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItem(position).getView(getContext(), convertView);
    }
}
