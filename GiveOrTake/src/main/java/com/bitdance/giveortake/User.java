package com.bitdance.giveortake;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nora on 6/23/13.
 */
public class User {
    private long userID;
    private String userName;
    private double latitude;
    private double longitude;
    private int karma;

    private static final String JSON_ID = "id";
    private static final String JSON_USERNAME = "username";
    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_LONGITUDE = "longitude";
    private static final String JSON_KARMA = "karma";

    public void updateFromJSON(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has(JSON_ID)) setUserID(jsonObject.getLong(JSON_ID));
        if (jsonObject.has(JSON_USERNAME)) setUserName(jsonObject.getString(JSON_USERNAME));
        if (jsonObject.has(JSON_LATITUDE)) setLatitude(jsonObject.getDouble(JSON_LATITUDE));
        if (jsonObject.has(JSON_LONGITUDE)) setLongitude(jsonObject.getDouble(JSON_LONGITUDE));
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }
}
