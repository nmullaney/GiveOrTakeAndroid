package com.bitdance.giveortake;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nora on 6/26/13.
 */
public class GiveOrTakeApplication extends Application {
    private static final String TAG = "GiveOrTakeApplication";

    private HashMap<Long, Item> items;
    private HashMap<Long, User> users;

    private ArrayList<Long> freeItemIDs;
    private ArrayList<Long> offerIDs;

    private BroadcastReceiver freeItemBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ItemService.FREE_ITEMS_UPDATED)) {
                ArrayList<Item> freeItems = (ArrayList<Item>)
                        intent.getSerializableExtra(ItemService.ITEMS_DATA);
                for (Item item : freeItems) {
                    freeItemIDs.add(item.getId());
                    items.put(item.getId(), item);
                }
            }
        }
    };

    private BroadcastReceiver offersBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ItemService.MY_ITEMS_UPDATED)) {
                ArrayList<Item> offers = (ArrayList<Item>)
                        intent.getSerializableExtra(ItemService.ITEMS_DATA);
                for (Item item : offers) {
                    offerIDs.add(item.getId());
                    items.put(item.getId(), item);
                }
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

    public void onCreate() {
        super.onCreate();
        freeItemIDs = new ArrayList<Long>();
        offerIDs = new ArrayList<Long>();
        items = new HashMap<Long, Item>();
        users = new HashMap<Long, User>();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        localBroadcastManager.registerReceiver(freeItemBroadcastReceiver,
                new IntentFilter(ItemService.FREE_ITEMS_UPDATED));
        localBroadcastManager.registerReceiver(offersBroadcastReceiver,
                new IntentFilter(ItemService.MY_ITEMS_UPDATED));
        localBroadcastManager.registerReceiver(userBroadcastReceiver,
                new IntentFilter(UserService.USER_FETCHED));
    }

    public ArrayList<Item> getFreeItems() {
        ArrayList<Item> freeItems = new ArrayList<Item>();
        for (Long itemID : freeItemIDs) {
            Item item = items.get(itemID);
            if (item != null) {
                freeItems.add(item);
            }
        }
        Log.i(TAG, "getFreeItems returns " + freeItems.size() + " items");
        Log.i(TAG, "freeItemIDs size: " + freeItemIDs.size());
        return freeItems;
    }

    public Item getItem(Long itemID) {
        return items.get(itemID);
    }

    public int getIndexOfFreeItem(Long itemID) {
        return freeItemIDs.indexOf(itemID);
    }

    public ArrayList<Item> getOffers() {
        ArrayList<Item> offers = new ArrayList<Item>();
        for (Long itemID : offerIDs) {
            Item item = items.get(itemID);
            if (item != null) {
                offers.add(item);
            }
        }
        return offers;
    }

    public User getUser(Long userID) {
        return users.get(userID);
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
