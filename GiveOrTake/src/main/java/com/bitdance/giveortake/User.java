package com.bitdance.giveortake;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by nora on 6/23/13.
 */
public class User implements Serializable, Parcelable {
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

    public User () {}

    public void updateFromJSON(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has(JSON_ID)) setUserID(jsonObject.getLong(JSON_ID));
        if (jsonObject.has(JSON_USERNAME)) setUserName(jsonObject.getString(JSON_USERNAME));
        if (jsonObject.has(JSON_LATITUDE)) {
            if (jsonObject.isNull(JSON_LATITUDE)) {
                setLatitude(null);
            } else {
                setLatitude(jsonObject.getDouble(JSON_LATITUDE));
            }
        }
        if (jsonObject.has(JSON_LONGITUDE)) {
            if (jsonObject.isNull(JSON_LONGITUDE)) {
                setLongitude(null);
            } else {
                setLongitude(jsonObject.getDouble(JSON_LONGITUDE));
            }
        }
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

    @Override
    public String toString() {
        return getUserName();
    }

    /**
     * Methods to make this Parcelable
     */

    public User(Parcel in) {
        userID = in.readLong();
        userName = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        karma = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(getUserID());
        parcel.writeString(getUserName());
        parcel.writeDouble(getLatitude());
        parcel.writeDouble(getLongitude());
        parcel.writeInt(getKarma());
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
