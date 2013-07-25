package com.bitdance.giveortake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Model class for an Item.
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
    private transient BitmapDrawable thumbnail;
    private String imageURL;
    private Date dateCreated;
    private Date dateUpdated;

    private int distance;
    private int numMessagesSent;

    private transient BitmapDrawable image;
    private String tempImageFile;

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

    public Item() {
        userID = ActiveUser.getInstance().getUserID();
        state = ItemState.DRAFT;
    }

    public Item(JSONObject jsonObject) throws JSONException {
        updateFromJSON(jsonObject);
    }

    public void updateFromJSON(JSONObject jsonObject) throws JSONException {
        Log.i(TAG, "jsonObject to parse: " + jsonObject);
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

    public void loadThumbnailFromFile(Context context, String filename) {
        File thumbnailFile = context.getFileStreamPath(filename);
        if (!thumbnailFile.renameTo(getLocalThumbnailFile(context))) {
            throw new RuntimeException("Failed to move thumbnail");
        }
        // reload the image from the file
        thumbnail = null;
        getThumbnail(context);
    }

    public void loadImageFromFile(Context context, String filename) {
        File imageFile = context.getFileStreamPath(filename);
        if (!imageFile.renameTo(getLocalImageFile(context))) {
            throw new RuntimeException("Failed to move file to fix it's name");
        }
        // reload the image from the file
        image = null;
        getImage(context);
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

    public void setStateUser(User stateUser) {
        if (stateUser != null)
            this.stateUserID = stateUser.getUserID();
        else
            this.stateUserID = null;
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

    public Drawable getThumbnail(Context context) {
        if (thumbnail == null) {
            Log.i(TAG, "Thumbnail is null, loading from file");
            File file = getLocalThumbnailFile(context);
            Log.i(TAG, "File to load is " + file.getName());
            if (file != null && file.exists()) {
                Log.i(TAG, "Pulling thumbnail from file");
                thumbnail = new BitmapDrawable(context.getResources(), file.getPath());
                DisplayMetrics dm = context.getResources().getDisplayMetrics();
                thumbnail.setTargetDensity(dm);
            }
        }
        if (thumbnail != null) {
            Log.i(TAG, "Thumbnail height/width = " + thumbnail.getIntrinsicHeight() + "/" +
                    thumbnail.getIntrinsicWidth());
        }
        return thumbnail;
    }

    private byte[] getDataFromDrawable(Drawable drawable, Bitmap.CompressFormat format) {
        if (drawable == null) {
            throw new RuntimeException("Drawable is null");
        }
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(format, 100, stream);
        return stream.toByteArray();
    }

    public byte[] getImageData(Context context) {
        Drawable image = getImage(context);
        return getDataFromDrawable(image, Bitmap.CompressFormat.JPEG);
    }

    public byte[] getThumbnailData(Context context) {
        Drawable thumbnailDrawable = getThumbnail(context);
        return getDataFromDrawable(thumbnailDrawable, Bitmap.CompressFormat.PNG);
    }

    public void setThumbnail(BitmapDrawable thumbnail) {
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

    public File getLocalImageFile(Context context) {
        String filename = null;
        if (getId() != null) {
            filename = getId() + "_image.jpg";
        } else if (tempImageFile != null) {
            filename = tempImageFile;
        }

        if (filename != null)
            return context.getFileStreamPath(filename);
        else
            try {
                File tempFile = File.createTempFile("image", "jpg", context.getFilesDir());
                tempImageFile = tempFile.getName();
                return tempFile;
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to open temp file for image.", ioe);
            }
        return null;
    }

    public boolean moveTempFile(Context context) {
        if (getId() == null) {
            throw new Error("The temp file should only be moved once we have an id");
        }
        if (tempImageFile == null) {
            // no need to move non-existent file
            return true;
        }
        File newFile = getLocalImageFile(context);
        File tempFile = context.getFileStreamPath(tempImageFile);
        Log.i(TAG, "Moving " + tempFile.getName() + " to " + newFile.getName());
        return tempFile.renameTo(newFile);
    }

    public File getLocalThumbnailFile(Context context) {
        String filename = getId() + "_thumbnail.png";
        return context.getFileStreamPath(filename);
    }

    public Drawable getImage(Context context) {
        if (image != null) {
            return image;
        }
        File file = getLocalImageFile(context);
        if (file != null && !file.exists() && tempImageFile != null) {
            file = context.getFileStreamPath(tempImageFile);
        }
        if (file != null && file.exists()) {
            BitmapDrawable fullImage = new BitmapDrawable(context.getResources(), file.getPath());
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            fullImage.setTargetDensity(dm);
            Display display = ((WindowManager) context.getSystemService(context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            Rect size = new Rect();
            display.getRectSize(size);
            int maxDimen = Math.min(size.width(), size.height());
            if (fullImage.getIntrinsicHeight() > maxDimen ||
                    fullImage.getIntrinsicWidth() > maxDimen) {
                image = new BitmapDrawable(context.getResources(),
                        Bitmap.createScaledBitmap(fullImage.getBitmap(), maxDimen, maxDimen, false));
            } else {
                image = fullImage;
            }

            return image;
        }
        return null;
    }

    public void clearImage() {
        if (image != null) {
            image.getBitmap().recycle();
        }
        image = null;
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

        public static ArrayList<ItemState> getSelectableStates() {
            ArrayList<ItemState> selectableStates = new ArrayList<ItemState>();
            selectableStates.add(AVAILABLE);
            selectableStates.add(PROMISED);
            selectableStates.add(TAKEN);
            return selectableStates;
        }

        public Drawable getDrawable(Context context) {
            if (resID == 0) return null;
            return context.getResources().getDrawable(resID);
        }

        public String getName() {
            return name;
        }
    }
}
