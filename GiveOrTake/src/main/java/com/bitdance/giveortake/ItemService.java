package com.bitdance.giveortake;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by nora on 6/18/13.
 */
public class ItemService extends IntentService {
    public static final String TAG = "ItemService";

    public static final String FREE_ITEMS_UPDATED = "free_items_updated";
    public static final String FREE_ITEMS_DATA = "free_items_data";

    public ItemService() {
        super(TAG);
        Log.i(TAG, "Created a new item service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Handling a new intent");
        // TODO: meaningful intent data
        ItemsFetcher fetcher = new ItemsFetcher(this);
        ArrayList<Item> items = fetcher.fetchMostRecentItems();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        Intent i = new Intent(FREE_ITEMS_UPDATED);
        i.putExtra(FREE_ITEMS_DATA, items);
        localBroadcastManager.sendBroadcast(i);
    }
}
