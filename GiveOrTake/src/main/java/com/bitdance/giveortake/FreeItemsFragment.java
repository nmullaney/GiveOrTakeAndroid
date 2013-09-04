package com.bitdance.giveortake;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

/**
 * FreeItemsFragment lists all the Freely available items.
 */
public class FreeItemsFragment extends ListFragment {
    public static final String TAG = "FreeItemsFragment";

    public static final String EXTRA_ITEM_ID = "item_id";

    public static final int REQUEST_FILTER_RESULT = 1;

    private ArrayList<Item> items;
    private String query;

    private MenuItem refreshMenuItem;
    private boolean isRefreshing = false;

    private BroadcastReceiver newItemsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received a new items broadcast");
            if (intent.getAction().equals(ItemService.FREE_ITEMS_UPDATED)) {
                setRefreshing(false);
                if (intent.hasExtra(ItemService.EXTRA_ERROR)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.error)
                            .setMessage(intent.getStringExtra(ItemService.EXTRA_ERROR))
                            .setPositiveButton(R.string.ok, null)
                            .show();
                } else {
                    items = getApplication().getFreeItems();
                    int positionToScrollTo = 0;
                    int offset = intent.getIntExtra(ItemService.EXTRA_OFFSET, 0);
                    if (offset > 0) {
                        positionToScrollTo = items.size() - offset + 1;
                    }
                    // This is a little jumpy, since the current position may be a half position
                    // but it's pretty close
                    final int position = positionToScrollTo;
                    Log.d(TAG, "Position to scroll to: " + position);

                    ItemArrayAdapter adapter = (ItemArrayAdapter) getListAdapter();
                    adapter.clear();
                    adapter.addAll(items);
                    adapter.notifyDataSetChanged();

                    getListView().post(new Runnable() {
                        @Override
                        public void run() {
                            getListView().setSelection(position);
                        }
                    });
                }
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

    private BroadcastReceiver itemsDeletedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Errors will be handled by the offers fragment
            items = getApplication().getFreeItems();
            setListAdapter(new ItemArrayAdapter(context, items));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        IntentFilter intentFilter = new IntentFilter(ItemService.FREE_ITEMS_UPDATED);
        localBroadcastManager.registerReceiver(newItemsBroadcastReceiver, intentFilter);
        localBroadcastManager.registerReceiver(itemThumbnailBroadcastReceiver,
                new IntentFilter(ItemService.ITEM_THUMBNAIL_FETCHED));
        localBroadcastManager.registerReceiver(itemsDeletedBroadcastReceiver,
                new IntentFilter(ItemService.ITEMS_DELETED));

        if (getSelectedItemID() != null) {
            displaySingleItem(getSelectedItemID());
        } else {
            items = getApplication().getFreeItems();
            setListAdapter(new ItemArrayAdapter(getActivity(), items));
            Log.i(TAG, "Refreshing onCreate");
            refreshItems(0);
        }
    }

    private Long getSelectedItemID() {
        MainActivity mainActivity = (MainActivity) getActivity();
        return mainActivity.getSelectedItemID();
    }

    public void displaySingleItem(Long itemID) {
        items = new ArrayList<Item>();
        Item singleItem = getApplication().getItem(itemID);
        if (singleItem != null) {
            Log.i(TAG, "Found single item among existing items");
            items.add(singleItem);
        }
        getApplication().replaceFreeItems(items);
        setListAdapter(new ItemArrayAdapter(getActivity(), items));
        if (singleItem == null) {
            Log.i(TAG, "Fetching single item for " + itemID);
            fetchSingleItem(itemID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_free_items, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.free_items_list_options, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        Log.i(TAG, "Setting up searchView");
        if (query == null) {
        searchView.setIconifiedByDefault(true);
        } else {
            // if a query already exists, make sure it's displayed
            if (Build.VERSION.SDK_INT >= 14) {
            searchMenuItem.expandActionView();
            }
            searchView.setIconifiedByDefault(false);
            searchView.setQuery(query, false);
        }
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                query = null;
                return false;
            }
        });
        if (Build.VERSION.SDK_INT >= 14) {
            searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem menuItem) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                    Log.i(TAG, "Unsetting query");
                    query = null;
                    if (items.size() < Constants.MAX_ITEMS_TO_REQUEST) {
                        Log.i(TAG, "Refreshing onMenuItemActonCollapse");
                        refreshItems(0);
                    }
                    return true;
                }
            });
        }

        // Store the refresh icon so that we can change it's state
        refreshMenuItem = menu.findItem(R.id.menu_item_refresh);
        // ensures that the state shows up as soon as we have a handle on the menu item
        setRefreshing(isRefreshing);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.menu_item_refresh:
                Log.i(TAG, "Refreshing due to menu");
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
                Log.d(TAG, "number of items before filter: " + items.size());
                getApplication().filterFreeItems();
                items = getApplication().getFreeItems();
                Log.d(TAG, "number of items after filter: " + items.size());
                setListAdapter(new ItemArrayAdapter(getActivity(), items));
                getListView().requestLayout();
                if (items.size() < Constants.MAX_ITEMS_TO_REQUEST) {
                    Log.i(TAG, "Refreshing due to filter change");
                    refreshItems(0);
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void searchQuery(String searchQuery) {
        Log.i(TAG, "Got search query: " + searchQuery);
        query = searchQuery;
        getApplication().filterFreeItemsForQuery(query);
        items = getApplication().getFreeItems();
        setListAdapter(new ItemArrayAdapter(getActivity(), items));
        getListView().requestLayout();
        if (items.size() < Constants.MAX_ITEMS_TO_REQUEST) {
            Log.i(TAG, "Refresh for new search query");
            refreshItems(0);
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
                    Log.d(TAG, "Getting more items, query =" + query);
                    refreshItems(totalItemCount);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getSelectedItemID() == null) {
            items = getApplication().getFreeItems();
            setListAdapter(new ItemArrayAdapter(getActivity(), items));
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Item item = ((ItemArrayAdapter) getListAdapter()).getItem(position);
        Intent i = new Intent(getActivity(), FreeItemPagerActivity.class);
        i.putExtra(FreeItemsFragment.EXTRA_ITEM_ID, item.getId());
        i.putExtra(FreeItemPagerActivity.EXTRA_QUERY, query);
        startActivityForResult(i, 0);
    }

    private void setRefreshing(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;
        if (refreshMenuItem == null) {
            return;
        }
        if (isRefreshing) {
            refreshMenuItem.setActionView(R.layout.actionbar_refresh_progress);
        } else {
            refreshMenuItem.setActionView(null);
        }
    }

    public void refreshItems(Integer offset) {
        if (offset > 0 && !getApplication().haveMoreFreeItems()) {
            return;
        }
        setRefreshing(true);
        Intent refreshIntent = new Intent(getActivity(), ItemService.class);
        refreshIntent.setAction(ItemService.UPDATE_FREE_ITEMS);
        refreshIntent.putExtra(ItemService.EXTRA_OFFSET, offset);
        refreshIntent.putExtra(ItemService.EXTRA_QUERY, query);
        getActivity().startService(refreshIntent);
    }

    public void fetchSingleItem(Long itemID) {
        setRefreshing(true);
        Intent refreshIntent = new Intent(getActivity(), ItemService.class);
        refreshIntent.setAction(ItemService.FETCH_SINGLE_ITEM);
        refreshIntent.putExtra(ItemService.EXTRA_ITEM_ID, itemID);
        getActivity().startService(refreshIntent);
    }

    private GiveOrTakeApplication getApplication() {
        return (GiveOrTakeApplication)getActivity().getApplication();
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
        localBroadcastManager.unregisterReceiver(itemsDeletedBroadcastReceiver);
    }
}
