package com.bitdance.giveortake;

import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
                items = ((GiveOrTakeApplication) getActivity().getApplication()).getOffers();
                setListAdapter(new ItemArrayAdapter(context, items));
            }
        }
    };

    private BroadcastReceiver itemThumbnailBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ItemService.ITEM_THUMBNAIL_FETCHED)) {
                Item item = (Item) intent.getSerializableExtra(ItemService.EXTRA_ITEM);
                Log.i(TAG, "Updating item with thumbnail: " + item.toString() + ", id: " + item.getId());
                ItemArrayAdapter adapter = (ItemArrayAdapter) getListAdapter();
                int index = adapter.getPositionForItemID(item.getId());
                ListView listView = getListView();
                if (isPositionVisible(index)) {
                    View view = listView.getChildAt(index - listView.getFirstVisiblePosition());
                    adapter.setThumbnailForView(view, item);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        items = ((GiveOrTakeApplication) getActivity().getApplication()).getOffers();
        setListAdapter(new ItemArrayAdapter(getActivity(), items));
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        IntentFilter intentFilter = new IntentFilter(ItemService.MY_ITEMS_UPDATED);
        localBroadcastManager.registerReceiver(newItemsBroadcastReceiver, intentFilter);
        localBroadcastManager.registerReceiver(itemThumbnailBroadcastReceiver,
                new IntentFilter(ItemService.ITEM_THUMBNAIL_FETCHED));

        Intent intent = new Intent(getActivity(), ItemService.class);
        intent.setAction(ItemService.UPDATE_MY_ITEMS);
        getActivity().startService(intent);
    }

    private void refreshItems() {
        items = ((GiveOrTakeApplication) getActivity().getApplication()).getOffers();
        getView().requestLayout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_offers, container, false);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.offers_list_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.menu_item_new_item:
                return createNewItem();
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

    }

    public boolean isPositionVisible(int index) {
        int start = getListView().getFirstVisiblePosition();
        int end = getListView().getLastVisiblePosition();
        Log.i(TAG, "Start: " + start + ", End: " + end + ", index: " + index);
        return index >= start && index <= end;
    }

    @Override
    public void onResume() {
        super.onResume();
        items = ((GiveOrTakeApplication) getActivity().getApplication()).getOffers();
        setListAdapter(new ItemArrayAdapter(getActivity(), items));
    }

    public boolean createNewItem() {
        Intent i = new Intent(getActivity(), EditOfferActivity.class);
        i.putExtra(EXTRA_ITEM, new Item());
        startActivityForResult(i, ADD_OFFER_REQUEST_CODE);
        return true;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Item item = (Item) getListAdapter().getItem(position);
        Intent intent = new Intent(getActivity(), EditOfferActivity.class);
        intent.putExtra(EXTRA_ITEM, item);
        startActivityForResult(intent, EDIT_OFFER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EDIT_OFFER_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    refreshItems();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.unregisterReceiver(newItemsBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(itemThumbnailBroadcastReceiver);
    }
}
