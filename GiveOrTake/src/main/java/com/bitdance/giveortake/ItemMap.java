package com.bitdance.giveortake;

/**
 * An orderedMap of Items, with some extra data to keep track of whether we need more data.
 */
public class ItemMap extends OrderedMap<Item> {

    private boolean hasMoreData = true;

    public boolean hasMoreData() {
        return hasMoreData;
    }

    public void setHasMoreData(boolean hasMoreData) {
        this.hasMoreData = hasMoreData;
    }
}
