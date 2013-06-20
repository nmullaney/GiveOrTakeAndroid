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

    public ItemService() {
        super(TAG);
        Log.i(TAG, "Created a new item service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ItemsFetcher fetcher = new ItemsFetcher(this);
        ArrayList<Item> items = new ArrayList<Item>();
        String resultAction = null;
        if (intent.getAction() == UPDATE_FREE_ITEMS) {
            items = fetcher.fetchMostRecentItems();
            resultAction = FREE_ITEMS_UPDATED;
        } else if (intent.getAction() == UPDATE_MY_ITEMS) {
            items = fetcher.fetchMyItems();
            resultAction = MY_ITEMS_UPDATED;
        }

        for (Item item : items) {
            Drawable thumbnail = fetcher.fetchItemThumbnail(item);
            item.setThumbnail(thumbnail);
        }

        if (resultAction != null) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
            Intent i = new Intent(resultAction);
            i.putExtra(ITEMS_DATA, items);
            localBroadcastManager.sendBroadcast(i);
        }
    }
}
