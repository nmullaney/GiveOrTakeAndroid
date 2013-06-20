package com.bitdance.giveortake;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TabWidget;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nora on 6/17/13.
 */
public class FreeItemsFragment extends ListFragment {
    public static final String TAG = "FreeItemsFragment";

    private ArrayList<Item> items;

    private BroadcastReceiver newItemsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received a new items broadcast");
            if (intent.getAction().equals(ItemService.FREE_ITEMS_UPDATED)) {
                items = (ArrayList<Item>) intent.getSerializableExtra(ItemService.FREE_ITEMS_DATA);
                setListAdapter(new FreeItemAdapter(items));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        items = new ArrayList<Item>();
        items.add(new Item("mouse"));
        items.add(new Item("bird"));
        items.add(new Item("whale"));
        setListAdapter(new FreeItemAdapter(items));
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        IntentFilter intentFilter = new IntentFilter(ItemService.FREE_ITEMS_UPDATED);
        localBroadcastManager.registerReceiver(newItemsBroadcastReceiver, intentFilter);

        Intent intent = new Intent(getActivity(), ItemService.class);
        intent.setAction(ItemService.FREE_ITEMS_UPDATED);
        getActivity().startService(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_free_items, container, false);
        TabWidget tabWidget = (TabWidget)v.findViewById(android.R.id.tabs);
        return v;
    }

    public class FreeItemAdapter extends ArrayAdapter<Item> {
        public FreeItemAdapter(ArrayList<Item> items) {
            super(getActivity(), R.layout.list_item_item, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_item, null);
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
            Drawable imageState = i.getDrawableForState(getActivity());
            imageView.setImageDrawable(imageState);

            return convertView;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        localBroadcastManager.unregisterReceiver(newItemsBroadcastReceiver);
    }
}
