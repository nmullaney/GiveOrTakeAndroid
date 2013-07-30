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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabWidget;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nora on 6/17/13.
 */
public class FreeItemsFragment extends ListFragment {
    public static final String TAG = "FreeItemsFragment";

    public static final String EXTRA_ITEM_ID = "item_id";

    public static final int REQUEST_FILTER_RESULT = 1;

    private ArrayList<Item> items;
    private ItemsFilter itemsFilter;

    private BroadcastReceiver newItemsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received a new items broadcast");
            if (intent.getAction().equals(ItemService.FREE_ITEMS_UPDATED)) {
                items = ((GiveOrTakeApplication) getActivity().getApplication()).getFreeItems();
                setListAdapter(new ItemArrayAdapter(getActivity(), items));
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
        setRetainInstance(true);
        setHasOptionsMenu(true);

        items = ((GiveOrTakeApplication) getActivity().getApplication()).getFreeItems();
        setListAdapter(new ItemArrayAdapter(getActivity(), items));
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        IntentFilter intentFilter = new IntentFilter(ItemService.FREE_ITEMS_UPDATED);
        localBroadcastManager.registerReceiver(newItemsBroadcastReceiver, intentFilter);
        localBroadcastManager.registerReceiver(itemThumbnailBroadcastReceiver,
                new IntentFilter(ItemService.ITEM_THUMBNAIL_FETCHED));

        Intent intent = new Intent(getActivity(), ItemService.class);
        intent.setAction(ItemService.UPDATE_FREE_ITEMS);
        getActivity().startService(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_free_items, container, false);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.free_items_list_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.menu_item_refresh:
                refreshItems(0);
                return true;
            case R.id.menu_item_filter:
                updateItemFilter();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_FILTER_RESULT:
                Log.d(TAG, "Got new filter");
                GiveOrTakeApplication gotApplication =
                        ((GiveOrTakeApplication) getActivity().getApplication());
                Log.d(TAG, "number of items before filter: " + items.size());
                gotApplication.filterFreeItems();
                items = ((GiveOrTakeApplication) getActivity().getApplication()).getFreeItems();
                Log.d(TAG, "number of items after filter: " + items.size());
                setListAdapter(new ItemArrayAdapter(getActivity(), items));
                getListView().requestLayout();
                if (items.size() < Constants.MAX_ITEMS_TO_REQUEST) {
                    refreshItems(0);
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateItemFilter() {
        Intent intent = new Intent(getActivity(), FilterItemsActivity.class);
        startActivityForResult(intent, REQUEST_FILTER_RESULT);
    }

    @Override
    public void onStart() {
        super.onStart();
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.d(TAG, "On scrollStateChanged: " + scrollState);
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0) {
                    return;
                }
                Log.d(TAG, "OnScroll: first: " + firstVisibleItem + ", count: " + visibleItemCount
                    + ", totalCount: " + totalItemCount);
                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    // we are at the bottom
                    Log.i(TAG, "Getting more items");
                    refreshItems(totalItemCount);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        items = ((GiveOrTakeApplication) getActivity().getApplication()).getFreeItems();
        setListAdapter(new ItemArrayAdapter(getActivity(), items));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Item item = ((ItemArrayAdapter) getListAdapter()).getItem(position);
        Intent i = new Intent(getActivity(), FreeItemPagerActivity.class);
        i.putExtra(FreeItemsFragment.EXTRA_ITEM_ID, item.getId());
        startActivityForResult(i, 0);
    }

    public void refreshItems(Integer offset) {
        Intent refreshIntent = new Intent(getActivity(), ItemService.class);
        refreshIntent.setAction(ItemService.UPDATE_FREE_ITEMS);
        if (offset != null) {
            refreshIntent.putExtra(ItemService.EXTRA_OFFSET, offset);
        }
        getActivity().startService(refreshIntent);
    }

    public boolean isPositionVisible(int index) {
        int start = getListView().getFirstVisiblePosition();
        int end = getListView().getLastVisiblePosition();
        Log.i(TAG, "Start: " + start + ", End: " + end + ", index: " + index);
        return index >= start && index <= end;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        localBroadcastManager.unregisterReceiver(newItemsBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(itemThumbnailBroadcastReceiver);
    }
}
