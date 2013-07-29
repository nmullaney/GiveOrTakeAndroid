package com.bitdance.giveortake;

import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

/**
 * An orderedMap of Items, with some extra data to keep track of whether we need more data.
 */
public class ItemMap extends OrderedMap<Item> {
    public static final String TAG = "ItemMap";

    private boolean hasMoreData = true;

    private Comparator<Item> itemComparator = new Comparator<Item>() {
        @Override
        /**
         * Negative return value if item is less than item2
         * 0 return value if they are the same item
         * Positive return value if item is more than item2
         *
         * We order by date updated, name, and then ID.
         */
        public int compare(Item item, Item item2) {
            // Descending
            int result = item.getDateUpdated().compareTo(item2.getDateUpdated());
            if (result == 0) {
                result = item.getName().compareTo(item2.getName());
                if (result == 0) {
                    // This ensures that any that are "equal" are actually the same item
                    result = item.getId().compareTo(item2.getId());
                }
            }
            // we want this result in descending order, so we'll return negative of the
            // result
            return -result;
        }
    };

    public boolean hasMoreData() {
        return hasMoreData;
    }

    public void setHasMoreData(boolean hasMoreData) {
        this.hasMoreData = hasMoreData;
    }

    // Add these sorted items to the list.
    public void mergeNewItems(ArrayList<Item> newItems) {
        if (newItems.isEmpty()) {
            return;
        }

        HashSet<Long> newItemIds = new HashSet<Long>();
        for (Item item : newItems) {
            newItemIds.add(item.getId());
        }

        ArrayList<Item> currentItems = getAll();
        ArrayList<Item> combinedItems = new ArrayList<Item>();
        int n = 0; // new
        int c = 0; // current
        while (n < newItems.size() && c < currentItems.size()) {
            Item currentItem = currentItems.get(c);
            if (newItemIds.contains(currentItem.getId())) {
                c++;
                continue;
            }
            Item newItem = newItems.get(n);
            int compareResult = itemComparator.compare(newItem, currentItem);
            if (compareResult < 0) {
                combinedItems.add(newItem);
                n++;
            } else {
                combinedItems.add(currentItem);
                c++;
            }
        }

        while (n < newItems.size()) {
            combinedItems.add(newItems.get(n));
            n++;
        }
        while (c < currentItems.size()) {
            combinedItems.add(currentItems.get(c));
            c++;
        }
        clear();
        addAll(combinedItems);
    }
}
