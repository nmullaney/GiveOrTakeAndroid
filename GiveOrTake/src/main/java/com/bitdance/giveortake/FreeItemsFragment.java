package com.bitdance.giveortake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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
        return v;
    }

    public class FreeItemAdapter extends ArrayAdapter<Item> {
        public FreeItemAdapter(ArrayList<Item> items) {
            super(getActivity(), android.R.layout.simple_list_item_1, items);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        localBroadcastManager.unregisterReceiver(newItemsBroadcastReceiver);
    }
}
