package com.bitdance.giveortake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by nora on 6/19/13.
 */
public class OffersFragment extends ListFragment {
    private static final String TAG = "OffersFragment";

    private static final int ADD_OFFER_REQUEST_CODE = 1;
    private static final int EDIT_OFFER_REQUEST_CODE = 2;

    public static final String EXTRA_ITEM = "item";

    private ArrayList<Item> items;

    private BroadcastReceiver newItemsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received a new items broadcast");
            if (intent.getAction().equals(ItemService.MY_ITEMS_UPDATED)) {
                items = (ArrayList<Item>) intent.getSerializableExtra(ItemService.ITEMS_DATA);
                setListAdapter(new ItemArrayAdapter(context, items));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        items = ((GiveOrTakeApplication) getActivity().getApplication()).getOffers();
        setListAdapter(new ItemArrayAdapter(getActivity(), items));
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        IntentFilter intentFilter = new IntentFilter(ItemService.MY_ITEMS_UPDATED);
        localBroadcastManager.registerReceiver(newItemsBroadcastReceiver, intentFilter);

        Intent intent = new Intent(getActivity(), ItemService.class);
        intent.setAction(ItemService.UPDATE_MY_ITEMS);
        getActivity().startService(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_offers, container, false);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Item item = (Item) getListAdapter().getItem(position);
        Intent intent = new Intent(getActivity(), EditOfferActivity.class);
        intent.putExtra(EXTRA_ITEM, item);
        startActivityForResult(intent, EDIT_OFFER_REQUEST_CODE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.unregisterReceiver(newItemsBroadcastReceiver);
    }
}
