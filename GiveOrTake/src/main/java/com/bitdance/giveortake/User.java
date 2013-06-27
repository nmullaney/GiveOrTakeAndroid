package com.bitdance.giveortake;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by nora on 6/23/13.
 */
public class User implements Serializable {
    private Long userID;
    private String userName;
    private Double latitude;
    private Double longitude;
    private Integer karma;

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
        if (jsonObject.has(JSON_KARMA)) setKarma(jsonObject.getInt(JSON_KARMA));
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getKarma() {
        return karma;
    }

    public void setKarma(Integer karma) {
        this.karma = karma;
    }
}
