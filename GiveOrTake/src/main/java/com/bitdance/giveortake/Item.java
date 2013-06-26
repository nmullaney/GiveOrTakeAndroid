package com.bitdance.giveortake;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by nora on 6/17/13.
 */
public class Item implements Serializable {
    private static final String TAG = "Item";

    private Long id;
    private String name;
    private String description;
    private Long userID;
    private ItemState state;
    private Long stateUserID;
    private String thumbnailURL;
    private transient Drawable thumbnail;
    private String imageURL;
    private Date dateCreated;
    private Date dateUpdated;

    private int distance;
    private int numMessagesSent;

    private static final String JSON_ID = "id";
    private static final String JSON_NAME = "name";
    private static final String JSON_DESC = "description";
    private static final String JSON_USER_ID = "userID";
    private static final String JSON_STATE = "state";
    private static final String JSON_STATE_USER_ID = "stateUserID";
    private static final String JSON_THUMBNAIL_URL = "thumbnailURL";
    private static final String JSON_IMAGE_URL = "imageURL";
    private static final String JSON_DATE_CREATED = "dateCreated";
    private static final String JSON_DATE_UPDATED = "dateUpdated";
    private static final String JSON_DISTANCE = "distance";
    private static final String JSON_NUM_MESSAGES_SENT = "numMessagesSent";

    public Item(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getLong(JSON_ID);
        name = jsonObject.getString(JSON_NAME);
        if (jsonObject.isNull(JSON_DESC)) {
            description = null;
        } else {
            description = jsonObject.getString(JSON_DESC);
        }
        if (jsonObject.has(JSON_USER_ID)) {
            userID = jsonObject.getLong(JSON_USER_ID);
        }
        state = ItemState.valueForName(jsonObject.getString(JSON_STATE));
        if (jsonObject.has(JSON_STATE_USER_ID)) {
            if (jsonObject.isNull(JSON_STATE_USER_ID)) {
                stateUserID = null;
            } else {
                stateUserID = jsonObject.getLong(JSON_STATE_USER_ID);
            }
        }
        thumbnailURL = jsonObject.getString(JSON_THUMBNAIL_URL);
        imageURL = jsonObject.getString(JSON_IMAGE_URL);
        String dateCreatedStr = jsonObject.getString(JSON_DATE_CREATED);
        dateCreated = dateFromJSONString(dateCreatedStr);
        String dateUpdatedStr = jsonObject.getString(JSON_DATE_UPDATED);
        dateUpdated = dateFromJSONString(dateUpdatedStr);
        if (jsonObject.has(JSON_DISTANCE)) {
            distance = jsonObject.getInt(JSON_DISTANCE);
        } else if (ActiveUser.isActiveUser(userID)) {
            distance = 0;
        }
        if (jsonObject.has(JSON_NUM_MESSAGES_SENT)) {
            numMessagesSent = jsonObject.getInt(JSON_NUM_MESSAGES_SENT);
        }
    }

    private Date dateFromJSONString(String jsonString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(jsonString);
        } catch (ParseException pe) {
            Log.e(TAG, "Failed to parse date: " + jsonString, pe);
        }
        return null;
    }

    public Long getId() {
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

    public Long getUserID() {
        return userID;
    }

    public Long getStateUserID() {
        return stateUserID;
    }

    public String getImageURL() {
        return imageURL;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public int getDistance() {
        return distance;
    }

    public int getNumMessagesSent() {
        return numMessagesSent;
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
