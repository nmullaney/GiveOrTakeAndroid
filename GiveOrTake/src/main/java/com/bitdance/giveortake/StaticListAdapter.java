package com.bitdance.giveortake;

import android.content.Context;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * StaticListAdapter allows the ProfileFragment to use a ListView, while containing
 * various different forms of data.
 */
public class StaticListAdapter extends ArrayAdapter<StaticListItem> {
    public static final String TAG = "StaticListAdapter";

    public StaticListAdapter(Context context, ArrayList<StaticListItem> items) {
        super(context, android.R.layout.simple_list_item_1, items);
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
