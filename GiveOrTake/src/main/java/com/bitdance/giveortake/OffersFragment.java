package com.bitdance.giveortake;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by nora on 6/19/13.
 */
public class OffersFragment extends ListFragment {
    private static final String TAG = "OffersFragment";

    private static final int ADD_OFFER_REQUEST_CODE = 1;
    private static final int EDIT_OFFER_REQUEST_CODE = 2;

    public static final String EXTRA_ITEM = "item";

    private ArrayList<Item> items;

    private MenuItem refreshMenuItem;
    private boolean isRefreshing = false;

    private BroadcastReceiver newItemsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received a new items broadcast");
            if (intent.getAction().equals(ItemService.MY_ITEMS_UPDATED)) {
                setRefreshing(false);
                if (intent.hasExtra(ItemService.EXTRA_ERROR)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.error)
                            .setMessage(intent.getStringExtra(ItemService.EXTRA_ERROR))
                            .setPositiveButton(R.string.ok, null)
                            .show();
                } else {
                    items = ((GiveOrTakeApplication) getActivity().getApplication()).getOffers();

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
                Log.d(TAG, "Updating item with thumbnail: " + item.toString() + ", id: " + item.getId());
                ItemArrayAdapter adapter = (ItemArrayAdapter) getListAdapter();
                int index = adapter.getPositionForItemID(item.getId());
                if (isPositionVisible(index)) {
                    ListView listView = getListView();
                    View view = listView.getChildAt(index - listView.getFirstVisiblePosition());
                    adapter.setThumbnailForView(view, item);
                }
            }
        }
    };

    private BroadcastReceiver itemsDeletedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received a new items broadcast");
            if (intent.getAction().equals(ItemService.ITEMS_DELETED)) {
                if (intent.hasExtra(ItemService.EXTRA_ERROR)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.error)
                            .setMessage(intent.getStringExtra(ItemService.EXTRA_ERROR))
                            .setPositiveButton(R.string.ok, null)
                            .show();
                }

                // We can have partial failure errors, so we should always reload the items
                items = ((GiveOrTakeApplication) getActivity().getApplication()).getOffers();
                setListAdapter(new ItemArrayAdapter(context, items));

            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true );
        setHasOptionsMenu(true);

        items = ((GiveOrTakeApplication) getActivity().getApplication()).getOffers();
        setListAdapter(new ItemArrayAdapter(getActivity(), items));
    }

    private void setupBroadcastReceivers() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        IntentFilter intentFilter = new IntentFilter(ItemService.MY_ITEMS_UPDATED);
        localBroadcastManager.registerReceiver(newItemsBroadcastReceiver, intentFilter);
        localBroadcastManager.registerReceiver(itemThumbnailBroadcastReceiver,
                new IntentFilter(ItemService.ITEM_THUMBNAIL_FETCHED));
        localBroadcastManager.registerReceiver(itemsDeletedBroadcastReceiver,
                new IntentFilter(ItemService.ITEMS_DELETED));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView listView = getListView();
        setupBroadcastReceivers();
        refreshOffers(0);

        // Load more items when the user scrolls to the end of the existing list
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0) {
                    return;
                }
                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    refreshOffers(totalItemCount);
                }
            }
        });

        // Display a contextual menu on long click
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            private HashSet<Integer> selectedPositions;

            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
                if (checked) {
                    selectedPositions.add(position);
                } else {
                    selectedPositions.remove(position);
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.offers_list_context, menu);
                selectedPositions = new HashSet<Integer>();
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.menu_delete:
                        deleteSelectedItems(selectedPositions);
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });
    }



    private void deleteSelectedItems(HashSet<Integer> positions) {
        Log.i(TAG, "Delete items for positions: " + positions);
        if (!positions.isEmpty()) {
            ArrayList<Long> itemIDs = new ArrayList<Long>();
            for (Integer index : positions) {
                Item item = (Item) getListAdapter().getItem(index);
                itemIDs.add(item.getId());
            }
            Intent intent = new Intent(getActivity(), ItemService.class);
            intent.setAction(ItemService.DELETE_ITEMS);
            intent.putExtra(ItemService.EXTRA_ITEM_IDS, itemIDs);
            getActivity().startService(intent);
        }
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

    private void refreshOffers(Integer offset) {
        if (offset > 0 && !getGOTApplication().haveMoreOffers()) {
            return;
        }
        setRefreshing(true);
        Intent intent = new Intent(getActivity(), ItemService.class);
        intent.setAction(ItemService.UPDATE_MY_ITEMS);
        intent.putExtra(ItemService.EXTRA_OFFSET, offset);
        getActivity().startService(intent);
    }

    private void reloadItems() {
        items = ((GiveOrTakeApplication) getActivity().getApplication()).getOffers();
        getView().requestLayout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_offers, container, false);
        Button giveAwayButton = (Button)v.findViewById(R.id.new_item_button);
        giveAwayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewItem();
            }
        });
        return v;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.offers_list_options, menu);
        refreshMenuItem = menu.findItem(R.id.menu_item_refresh);
        // ensures the refresh state appears correctly
        setRefreshing(isRefreshing);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.menu_item_new_item:
                return createNewItem();
            case R.id.menu_item_refresh:
                refreshOffers(0);
                return true;
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

    private GiveOrTakeApplication getGOTApplication() {
        return (GiveOrTakeApplication) getActivity().getApplication();
    }

    @Override
    public void onResume() {
        super.onResume();
        items = ((GiveOrTakeApplication) getActivity().getApplication()).getOffers();
        setListAdapter(new ItemArrayAdapter(getActivity(), items));
    }

    public boolean createNewItem() {
        Intent i = new Intent(getActivity(), EditOfferActivity.class);
        i.putExtra(EXTRA_ITEM, new Item(getGOTApplication().getActiveUser().getUserID()));
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
                    reloadItems();
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
        localBroadcastManager.unregisterReceiver(itemsDeletedBroadcastReceiver);
    }
}
