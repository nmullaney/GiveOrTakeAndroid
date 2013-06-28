package com.bitdance.giveortake;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by nora on 6/18/13.
 */
public class ItemService extends IntentService {
    public static final String TAG = "ItemService";

    public static final String UPDATE_FREE_ITEMS = "update_free_items";
    public static final String UPDATE_MY_ITEMS = "update_my_items";

    public static final String FREE_ITEMS_UPDATED = "free_items_updated";
    public static final String MY_ITEMS_UPDATED = "my_items_updated";
    public static final String ITEMS_DATA = "items_data";

    public static final String FETCH_ITEM_IMAGE = "fetch_item_image";
    public static final String EXTRA_ITEM_DATA = "extra_item_data";
    public static final String ITEM_IMAGE_FETCHED = "item_image_fetched";
    public static final String EXTRA_IMAGE_FETCH_ERROR = "image_fetch_error";

    public ItemService() {
        super(TAG);
        Log.i(TAG, "Created a new item service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.getAction() == UPDATE_FREE_ITEMS) {
            fetchFreeItems();
        } else if (intent.getAction() == UPDATE_MY_ITEMS) {
            fetchMyItems();
        } else if (intent.getAction() == FETCH_ITEM_IMAGE) {
            Item item = (Item) intent.getSerializableExtra(EXTRA_ITEM_DATA);
            fetchItemImage(item);
        }
    }

    private void fetchFreeItems() {
        ItemsFetcher fetcher = new ItemsFetcher(this);
        ArrayList<Item> items = fetcher.fetchMostRecentItems();
        fetchThumbnails(items);
        broadcastItems(FREE_ITEMS_UPDATED, items);
    }

    private void fetchMyItems() {
        ItemsFetcher fetcher = new ItemsFetcher(this);
        ArrayList<Item> items = fetcher.fetchMyItems();
        fetchThumbnails(items);
        broadcastItems(MY_ITEMS_UPDATED, items);
    }

    private void fetchThumbnails(ArrayList<Item> items) {
        ItemsFetcher fetcher = new ItemsFetcher(this);
        for (Item item : items) {
            Drawable thumbnail = fetcher.fetchItemThumbnail(item);
            item.setThumbnail(thumbnail);
        }
    }

    private void broadcastItems(String resultAction, ArrayList<Item> items) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        Intent i = new Intent(resultAction);
        i.putExtra(ITEMS_DATA, items);
        localBroadcastManager.sendBroadcast(i);
    }

    private void fetchItemImage(Item item) {
        ImageFetcher fetcher = new ImageFetcher(this);
        boolean success = fetcher.fetchImageForItem(item);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        Intent i = new Intent(ITEM_IMAGE_FETCHED);
        if (!success) {
            i.putExtra(EXTRA_IMAGE_FETCH_ERROR, true);
        }
        localBroadcastManager.sendBroadcast(i);
    }
}
