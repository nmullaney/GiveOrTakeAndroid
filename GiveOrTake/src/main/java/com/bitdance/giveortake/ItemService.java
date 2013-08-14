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
    public static final String EXTRA_OFFSET = "extra_offset";
    public static final String EXTRA_QUERY = "extra_query";

    public static final String FREE_ITEMS_UPDATED = "free_items_updated";
    public static final String MY_ITEMS_UPDATED = "my_items_updated";

    public static final String FETCH_ITEM_THUMBNAIL = "fetch_item_thumbnail";
    public static final String ITEM_THUMBNAIL_FETCHED = "item_thumbnail_fetched";

    public static final String FETCH_ITEM_IMAGE = "fetch_item_image";
    public static final String EXTRA_ITEM = "extra_item";
    public static final String ITEM_IMAGE_FETCHED = "item_image_fetched";
    public static final String EXTRA_IMAGE_FETCH_ERROR = "image_fetch_error";

    public static final String SEND_MESSAGE = "send_message";
    public static final String EXTRA_ITEM_ID = "extra_item_id";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String MESSAGE_SENT = "message_sent";
    public static final String EXTRA_MESSAGE_SENT_ERROR = "extra_message_sent_error";
    public static final String EXTRA_NUM_MESSAGES_SENT = "extra_num_messages_sent";

    public static final String POST_ITEM = "post_item";
    public static final String ITEM_POSTED = "item_posted";
    public static final String EXTRA_ERROR = "extra_error";
    public static final String EXTRA_KARMA_CHANGE = "extra_karma_change";

    public static final String DELETE_ITEMS = "delete_items";
    public static final String EXTRA_ITEM_IDS = "extra_item_ids";
    public static final String ITEMS_DELETED = "items_deleted";

    public ItemService() {
        super(TAG);
        Log.i(TAG, "Created a new item service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.getAction() == UPDATE_FREE_ITEMS) {
            Integer offset = intent.getIntExtra(EXTRA_OFFSET, 0);
            String query = intent.getStringExtra(EXTRA_QUERY);
            fetchFreeItems(offset, query);
        } else if (intent.getAction() == UPDATE_MY_ITEMS) {
            Integer offset = intent.getIntExtra(EXTRA_OFFSET, 0);
            fetchMyItems(offset);
        } else if (intent.getAction() == FETCH_ITEM_THUMBNAIL) {
            Item item = (Item) intent.getSerializableExtra(EXTRA_ITEM);
            fetchItemThumbnail(item);
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
        } else if (intent.getAction() == DELETE_ITEMS) {
            ArrayList<Long> itemIDs = (ArrayList<Long>) intent.getSerializableExtra(EXTRA_ITEM_IDS);
            deleteItems(itemIDs);
        } else {
            Log.e(TAG, "Unexpected action: " + intent.getAction());
        }
    }

    private void broadcastIntent(Intent intent) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        localBroadcastManager.sendBroadcast(intent);
    }

    private GiveOrTakeApplication getGOTApplication() {
        return ((GiveOrTakeApplication) getApplication());
    }

    private void fetchFreeItems(Integer offset, String query) {
        ItemsFetcher fetcher = new ItemsFetcher(this, getGOTApplication().getActiveUser());
        if (offset != 0 && !getGOTApplication().haveMoreFreeItems()) {
            // don't fetch more old items if we've already got them all
            return;
        }
        ItemsFetcher.ItemsResponse itemsResponse = fetcher.fetchItems(offset, query);
        Intent i = new Intent(FREE_ITEMS_UPDATED);
        if (itemsResponse.isSuccess()) {
            getGOTApplication().mergeNewFreeItems(itemsResponse.getItems());
        } else {
            i.putExtra(EXTRA_ERROR, itemsResponse.getError());
        }

        broadcastIntent(i);
    }

    private void fetchMyItems(Integer offset) {
        ItemsFetcher fetcher = new ItemsFetcher(this, getGOTApplication().getActiveUser());
        if (offset != 0 && !getGOTApplication().haveMoreOffers()) {
            // don't fetch more old items if we've already got them all
            return;
        }
        ItemsFetcher.ItemsResponse itemsResponse = fetcher.fetchMyItems(offset);
        Intent i = new Intent(MY_ITEMS_UPDATED);
        if (itemsResponse.isSuccess()) {
            getGOTApplication().mergeNewOffers(itemsResponse.getItems());
        } else {
            i.putExtra(EXTRA_ERROR, itemsResponse.getError());
        }

        broadcastIntent(i);
    }

    private void fetchItemThumbnail(Item item) {
        String token = getGOTApplication().getActiveUser().getToken();
        ImageFetcher fetcher = new ImageFetcher(this, token);
        boolean success = fetcher.fetchThumbnailForItem(item);
        Intent intent = new Intent(ITEM_THUMBNAIL_FETCHED);
        intent.putExtra(EXTRA_ITEM, item);
        if (!success) {
            intent.putExtra(EXTRA_ERROR, getResources().getString(R.string.error));
        }
        Log.d(TAG, "Broadcasting item thumbnail fetched for " + item.toString());
        broadcastIntent(intent);
    }

    private void fetchItemImage(Item item) {
        String token = getGOTApplication().getActiveUser().getToken();
        ImageFetcher fetcher = new ImageFetcher(this, token);
        boolean success = fetcher.fetchImageForItem(item);
        Intent i = new Intent(ITEM_IMAGE_FETCHED);
        if (!success) {
            i.putExtra(EXTRA_IMAGE_FETCH_ERROR, true);
        }
        broadcastIntent(i);
    }

    private void sendMessage(Long itemID, String message) {
        MessageSender sender = new MessageSender(this);
        ActiveUser activeUser = getGOTApplication().getActiveUser();
        MessageSender.SendMessageResponse response = sender.sendMessage(itemID, message, activeUser);
        Intent i = new Intent(MESSAGE_SENT);
        i.putExtra(EXTRA_ITEM_ID, itemID);
        if (!response.isSuccess()) {
            i.putExtra(EXTRA_MESSAGE_SENT_ERROR, response.getErrorMessage());
        } else {
            getGOTApplication().updateMessagesSent(itemID, response.getNumberOfMessagesSent());
            i.putExtra(EXTRA_NUM_MESSAGES_SENT, response.getNumberOfMessagesSent());
        }
        broadcastIntent(i);
    }

    private void postItem(Item item) {
        ItemsFetcher fetcher = new ItemsFetcher(this, getGOTApplication().getActiveUser());
        Intent intent = new Intent(ITEM_POSTED);
        ItemsFetcher.ItemResponse itemResponse = fetcher.postItem(item);
        if (!itemResponse.isSuccess()) {
            intent.putExtra(EXTRA_ERROR, itemResponse.getError());
            broadcastIntent(intent);
            return;
        }

        if (itemResponse.getKarmaChange() > 0) {
            intent.putExtra(EXTRA_KARMA_CHANGE, itemResponse.getKarmaChange());
        }

        if (item.hasUnsavedImage()) {
            boolean updatedImage = postImage(item);
            if (!updatedImage) {
                intent.putExtra(EXTRA_ERROR, getResources().getString(R.string.image_upload_failed));
            } else {
                item.moveTempFile(this);
            }
            getGOTApplication().addPostedItem(item);
        }
        broadcastIntent(intent);
    }

    private boolean postImage(Item item) {
        String token = getGOTApplication().getActiveUser().getToken();
        ImageFetcher fetcher = new ImageFetcher(this, token);
        return fetcher.postImage(item);
    }

    private void deleteItems(ArrayList<Long> itemIDs) {
        ItemsFetcher fetcher = new ItemsFetcher(this, getGOTApplication().getActiveUser());
        ItemsFetcher.DeleteItemsResponse deleteItemsResponse = fetcher.deleteItems(itemIDs);
        if (deleteItemsResponse.getSuccessfulIDs() != null) {
            getGOTApplication().removeOffersByID(deleteItemsResponse.getSuccessfulIDs());
            // remove any that are also free items
            getGOTApplication().removeFreeItemsByID(deleteItemsResponse.getSuccessfulIDs());
        }
        Intent intent = new Intent(ITEMS_DELETED);
        if (!deleteItemsResponse.isSuccess()) {
            intent.putExtra(EXTRA_ERROR, deleteItemsResponse.getError());
        }
        broadcastIntent(intent);
    }
}
