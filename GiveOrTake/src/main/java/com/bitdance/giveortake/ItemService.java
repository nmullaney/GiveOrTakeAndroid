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

    public static final String UPDATE_FREE_ITEMS = "update_free_items";
    public static final String UPDATE_MY_ITEMS = "update_my_items";

    public static final String FREE_ITEMS_UPDATED = "free_items_updated";
    public static final String MY_ITEMS_UPDATED = "my_items_updated";
    public static final String ITEMS_DATA = "items_data";

    public static final String FETCH_ITEM_IMAGE = "fetch_item_image";
    public static final String EXTRA_ITEM = "extra_item";
    public static final String ITEM_IMAGE_FETCHED = "item_image_fetched";
    public static final String EXTRA_IMAGE_FETCH_ERROR = "image_fetch_error";

    public static final String SEND_MESSAGE = "send_message";
    public static final String EXTRA_ITEM_ID = "extra_item_id";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String MESSAGE_SENT = "message_sent";
    public static final String EXTRA_MESSAGE_SENT_ERROR = "extra_message_sent_error";

    public static final String POST_ITEM = "post_item";
    public static final String ITEM_POSTED = "item_posted";
    public static final String EXTRA_ERROR = "extra_error";

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
            Item item = (Item) intent.getSerializableExtra(EXTRA_ITEM);
            fetchItemImage(item);
        } else if (intent.getAction() == SEND_MESSAGE) {
            Log.i(TAG, "Service sending message");
            Long itemID = intent.getLongExtra(EXTRA_ITEM_ID, 0);
            String message = intent.getStringExtra(EXTRA_MESSAGE);
            sendMessage(itemID, message);
        } else if (intent.getAction() == POST_ITEM) {
            Item item = (Item) intent.getSerializableExtra(EXTRA_ITEM);
            postItem(item);
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
        ImageFetcher fetcher = new ImageFetcher(this);
        //ItemsFetcher itemFetcher = new ItemsFetcher(this);
        for (Item item : items) {
            boolean success = fetcher.fetchThumbnailForItem(item);
            //ItemsFetcher itemsFetcher = new ItemsFetcher(this);
            //itemsFetcher.fetchItemThumbnail(item);
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

    private void sendMessage(Long itemID, String message) {
        MessageSender sender = new MessageSender();
        boolean success = sender.sendMessage(itemID, message);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        Intent i = new Intent(MESSAGE_SENT);
        i.putExtra(EXTRA_ITEM_ID, itemID);
        if (!success) {
            i.putExtra(EXTRA_MESSAGE_SENT_ERROR, true);
        }
        localBroadcastManager.sendBroadcast(i);
    }

    private void postItem(Item item) {
        ItemsFetcher fetcher = new ItemsFetcher(this);
        item = fetcher.postItem(item);
        boolean updatedImage = postImage(item);
        Intent intent = new Intent(ITEM_POSTED);
        intent.putExtra(EXTRA_ITEM, item);
        if (!updatedImage) {
            intent.putExtra(EXTRA_ERROR, getResources().getString(R.string.image_upload_failed));
        } else {
            item.moveTempFile(this);
        }
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(intent);
    }

    private boolean postImage(Item item) {
        ImageFetcher fetcher = new ImageFetcher(this);
        return fetcher.postImage(item);
    }
}
