package com.bitdance.giveortake;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by nora on 6/17/13.
 */
public class Item implements Serializable {
    private long id;
    private String name;
    private String description;
    private ItemState state;
    private String thumbnailURL;
    private transient Drawable thumbnail;

    private static final String JSON_ID = "id";
    private static final String JSON_NAME = "name";
    private static final String JSON_DESC = "description";
    private static final String JSON_STATE = "state";
    private static final String JSON_THUMBNAIL_URL = "thumbnailURL";

    // TODO: remove this -- for testing
    public Item(String name) {
        this.name = name;
    }

    public Item(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getLong(JSON_ID);
        name = jsonObject.getString(JSON_NAME);
        description = jsonObject.getString(JSON_DESC);
        state = ItemState.valueForName(jsonObject.getString(JSON_STATE));
        thumbnailURL = jsonObject.getString(JSON_THUMBNAIL_URL);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemState getState() {
        return state;
    }

    public void setState(ItemState state) {
        this.state = state;
    }

    public void setState(String state) {
        this.state = ItemState.valueOf(state);
    }

    public Drawable getDrawableForState(Context context) {
        if (state != null) {
            return state.getDrawable(context);
        } else {
            return null;
        }
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public Drawable getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Drawable thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String toString() {
        return getName();
    }

    public enum ItemState {
        DRAFT("Draft", R.drawable.draft),
        AVAILABLE("Available", R.drawable.available),
        PROMISED("Promised", R.drawable.promised),
        TAKEN("Taken", R.drawable.taken),
        DELETED("Deleted", 0);

        private final String name;
        private final int resID;

        ItemState(String name, int resID) {
            this.name = name;
            this.resID = resID;
        }

        public static ItemState valueForName(String name) {
            for (ItemState state : values()) {
                if (state.name.equals(name)) {
                    return state;
                }
            }
            return null;
        }

        public Drawable getDrawable(Context context) {
            if (resID == 0) return null;
            return context.getResources().getDrawable(resID);
        }
    }
}
