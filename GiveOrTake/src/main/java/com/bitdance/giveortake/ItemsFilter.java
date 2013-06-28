package com.bitdance.giveortake;

import java.util.ArrayList;

/**
 * Created by nora on 6/20/13.
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
        if (ownedBy != null) {
            args.add("ownedBy=" + ownedBy);
        }
        if (userID != null) {
            args.add("userID=" + userID);
        }
        if (distance != null) {
            args.add("distance=" + distance);
        }
        if (showMyItems != null && showMyItems) {
            args.add("showMyItems=1");
        }
        if (query != null) {
            args.add("q=" + query);
        }
        if (limit != null) {
            args.add("limit=" + limit);
        }
        if (offset != null) {
            args.add("offset=" + offset);
        }
        if (itemID != null) {
            args.add("itemID=" + itemID);
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
