package com.bitdance.giveortake;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Stores data that's needed long-term and/or cross-thread.
 */
public class GiveOrTakeApplication extends Application {
    private static final String TAG = "GiveOrTakeApplication";

    private ItemMap freeItemsMap;
    private ItemMap offersMap;

    private HashMap<Long, User> users;
    private ActiveUser activeUser;

    public void onCreate() {
        super.onCreate();
        freeItemsMap = new ItemMap();
        offersMap = new ItemMap();
        users = new HashMap<Long, User>();
        activeUser = new ActiveUser();
    }

    public ActiveUser getActiveUser() {
        if (activeUser == null) {
            activeUser = new ActiveUser();
        }
        return activeUser;
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

    public void filterFreeItems() {
        SharedPreferences preferences = this
                .getSharedPreferences(Constants.FILTER_PREFERENCES, Context.MODE_PRIVATE);
        int distance = preferences.getInt(Constants.DISTANCE_PREFERENCE, Constants.DEFAULT_DISTANCE);
        boolean showMyItems = preferences.getBoolean(Constants.SHOW_MY_ITEMS_PREFERENCE,
                Constants.DEFAULT_SHOW_MY_ITEMS);
        for (Iterator<Item> itemIterator = freeItemsMap.iterator(); itemIterator.hasNext(); ) {
            Item next = itemIterator.next();
            if (!showMyItems && activeUser.isActiveUser(next.getUserID())) {
                itemIterator.remove();
                continue;
            }
            if (next.getDistance() > Integer.valueOf(distance)) {
                itemIterator.remove();
            }
        }
    }

    public void filterFreeItemsForQuery(String query) {
        String lowerQuery = query.toLowerCase();
        for (Iterator<Item> itemIterator = freeItemsMap.iterator(); itemIterator.hasNext(); ) {
            Item next = itemIterator.next();
            if (!next.getName().toLowerCase().contains(lowerQuery) &&
                    !next.getDescription().toLowerCase().contains(lowerQuery)) {
                itemIterator.remove();
            }
        }
    }

    public ArrayList<Item> getOffers() {
        return offersMap.getAll();
    }

    public void updateMessagesSent(Long itemID, Integer numMessagesSent) {
        Item item = getItem(itemID);
        item.setNumMessagesSent(numMessagesSent);
    }

    public User getUser(Long userID) {
        return users.get(userID);
    }

    public void logout() {
        // clear all the data!
        freeItemsMap.clear();
        offersMap.clear();
        users.clear();
        activeUser = null;
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
