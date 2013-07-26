package com.bitdance.giveortake;

import android.app.Application;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stores data that's needed long-term and/or cross-thread.
 */
public class GiveOrTakeApplication extends Application {
    private static final String TAG = "GiveOrTakeApplication";

    private ItemMap freeItemsMap;
    private ItemMap offersMap;

    private HashMap<Long, User> users;

    public void onCreate() {
        super.onCreate();
        freeItemsMap = new ItemMap();
        offersMap = new ItemMap();
        users = new HashMap<Long, User>();
    }

    public void addUser(User user) {
        if (user != null) {
            users.put(user.getUserID(), user);
        }
    }

    public void mergeNewFreeItems(ArrayList<Item> newItems) {
        if (newItems.size() < Constants.MAX_ITEMS_TO_REQUEST) {
            freeItemsMap.setHasMoreData(false);
        }
        freeItemsMap.mergeNewItems(newItems);
    }

    public void mergeNewOffers(ArrayList<Item> newOffers) {
        if (newOffers.size() < Constants.MAX_ITEMS_TO_REQUEST) {
            offersMap.setHasMoreData(false);
        }
        offersMap.mergeNewItems(newOffers);
    }

    public void addPostedItem(Item item) {
        offersMap.remove(item);
        offersMap.add(0, item);
        if (freeItemsMap.contains(item)) {
            freeItemsMap.remove(item);
            freeItemsMap.add(0, item);
        }
    }

    public ArrayList<Item> getFreeItems() {
        return freeItemsMap.getAll();
    }

    public boolean haveMoreFreeItems() {
        return freeItemsMap.hasMoreData();
    }

    public boolean haveMoreOffers() {
        return offersMap.hasMoreData();
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
}
