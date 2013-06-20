package com.bitdance.giveortake;

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
                items = (ArrayList<Item>) intent.getSerializableExtra(ItemService.ITEMS_DATA);
                setListAdapter(new ItemArrayAdapter(getActivity(), items));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        items = new ArrayList<Item>();
        setListAdapter(new ItemArrayAdapter(getActivity(), items));
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        IntentFilter intentFilter = new IntentFilter(ItemService.FREE_ITEMS_UPDATED);
        localBroadcastManager.registerReceiver(newItemsBroadcastReceiver, intentFilter);

        Intent intent = new Intent(getActivity(), ItemService.class);
        intent.setAction(ItemService.UPDATE_FREE_ITEMS);
        getActivity().startService(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_free_items, container, false);
        TabWidget tabWidget = (TabWidget)v.findViewById(android.R.id.tabs);
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        localBroadcastManager.unregisterReceiver(newItemsBroadcastReceiver);
    }
}
