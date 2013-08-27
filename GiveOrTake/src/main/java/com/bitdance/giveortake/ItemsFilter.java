package com.bitdance.giveortake;

import java.util.ArrayList;

/**
 * An ItemsFilter stores the details of how to query for a set of Items.
 */
public class ItemsFilter {
    private Long ownedBy;
    private Long userID;
    private Integer distance;
    private Boolean showMyItems;
    private String query;
    private Integer limit;
    private Integer offset;
    private Long itemID;

    public String buildQueryString() {
        ArrayList<String> args = new ArrayList<String>();
        if (getOwnedBy() != null) {
            args.add("ownedBy=" + getOwnedBy());
        }
        if (getUserID() != null) {
            args.add("userID=" + getUserID());
        }
        if (getDistance() != null) {
            args.add("distance=" + getDistance());
        }
        if (getShowMyItems() != null && getShowMyItems()) {
            args.add("showMyItems=1");
        }
        if (getQuery() != null) {
            args.add("q=" + getQuery());
        }
        if (getLimit() != null) {
            args.add("limit=" + getLimit());
        }
        if (getOffset() != null) {
            args.add("offset=" + getOffset());
        }
        if (getItemID() != null) {
            args.add("itemID=" + getItemID());
        }
        return join(args, "&");
    }

    private String join(ArrayList<String> strings, String separator) {
        if (strings.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append(strings.get(0));
        for (int i = 1; i < strings.size(); i++) {
            sb.append(separator).append(strings.get(i));
        }
        return sb.toString();
    }

    public Long getOwnedBy() {
        return ownedBy;
    }

    public ItemsFilter setOwnedBy(Long ownedBy) {
        this.ownedBy = ownedBy;
        return this;
    }

    public Long getUserID() {
        return userID;
    }

    public ItemsFilter setUserID(Long userID) {
        this.userID = userID;
        return this;
    }

    public Integer getDistance() {
        return distance;
    }

    public ItemsFilter setDistance(Integer distance) {
        this.distance = distance;
        return this;
    }

    public Boolean getShowMyItems() {
        return showMyItems;
    }

    public ItemsFilter setShowMyItems(Boolean showMyItems) {
        this.showMyItems = showMyItems;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public ItemsFilter setQuery(String query) {
        this.query = query;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ItemsFilter setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public ItemsFilter setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Long getItemID() {
        return itemID;
    }

    public ItemsFilter setItemID(Long itemID) {
        this.itemID = itemID;
        return this;
    }
}
