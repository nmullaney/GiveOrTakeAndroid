package com.bitdance.giveortake;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stores data that's needed long-term and/or cross-thread.
 */
public class GiveOrTakeApplication extends Application {
    private static final String TAG = "GiveOrTakeApplication";

    private OrderedMap<Item> freeItemsMap;
    private OrderedMap<Item> offersMap;

    private HashMap<Long, User> users;

    private BroadcastReceiver freeItemBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ItemService.FREE_ITEMS_UPDATED)) {
                ArrayList<Item> freeItems = (ArrayList<Item>)
                        intent.getSerializableExtra(ItemService.ITEMS_DATA);
                freeItemsMap.clear();
                freeItemsMap.addAll(freeItems);
            }
        }
    };

    private BroadcastReceiver offersBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ItemService.MY_ITEMS_UPDATED)) {
                ArrayList<Item> offers = (ArrayList<Item>)
                        intent.getSerializableExtra(ItemService.ITEMS_DATA);
                offersMap.clear();
                offersMap.addAll(offers);
            }
        }
    };

    private BroadcastReceiver userBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UserService.USER_FETCHED)) {
                User user = (User)intent.getSerializableExtra(UserService.EXTRA_USER_DATA);
                if (user != null) {
                    users.put(user.getUserID(), user);
                }
            }
        }
    };

    private BroadcastReceiver itemPostedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ItemService.ITEM_POSTED)) {
                String error = intent.getStringExtra(ItemService.EXTRA_ERROR);
                if (error == null) {
                    Item item = (Item) intent.getSerializableExtra(ItemService.EXTRA_ITEM);
                    offersMap.remove(item);
                    offersMap.add(0, item);
                    if (freeItemsMap.contains(item)) {
                        freeItemsMap.remove(item);
                        freeItemsMap.add(0, item);
                    }
                }
            }
        }
    };

    public void onCreate() {
        super.onCreate();
        freeItemsMap = new OrderedMap<Item>();
        offersMap = new OrderedMap<Item>();
        users = new HashMap<Long, User>();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        localBroadcastManager.registerReceiver(freeItemBroadcastReceiver,
                new IntentFilter(ItemService.FREE_ITEMS_UPDATED));
        localBroadcastManager.registerReceiver(offersBroadcastReceiver,
                new IntentFilter(ItemService.MY_ITEMS_UPDATED));
        localBroadcastManager.registerReceiver(userBroadcastReceiver,
                new IntentFilter(UserService.USER_FETCHED));
        localBroadcastManager.registerReceiver(itemPostedBroadcastReceiver,
                new IntentFilter(ItemService.ITEM_POSTED));
    }

    public ArrayList<Item> getFreeItems() {
        return freeItemsMap.getAll();
    }

    public Item getItem(Long itemID) {
        Item item = freeItemsMap.get(itemID);
        if (item == null) {
            item = offersMap.get(itemID);
        }
        return item;
    }

    public int getIndexOfFreeItem(Long itemID) {
        return freeItemsMap.indexOf(itemID);
    }

    public ArrayList<Item> getOffers() {
        return offersMap.getAll();
    }

    public User getUser(Long userID) {
        return users.get(userID);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        for (Item item : freeItemsMap.getAll()) {
            item.clearImage();
        }
        for (Item item : offersMap.getAll()) {
            item.clearImage();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        localBroadcastManager.unregisterReceiver(freeItemBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(offersBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(userBroadcastReceiver);
    }
}
