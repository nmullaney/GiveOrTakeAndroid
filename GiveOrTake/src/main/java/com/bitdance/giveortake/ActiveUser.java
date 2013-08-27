package com.bitdance.giveortake;

import android.util.Log;

import com.facebook.model.GraphUser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class represents the logged-in user.  It contains a regular User
 * object, but also has information about email, facebookID, a login token,
 * and whether the user is brand new.
 */
public class ActiveUser {
    public static final String TAG = "ActiveUser";

    private static final String JSON_TOKEN = "token";
    private static final String JSON_EMAIL = "email";
    private static final String JSON_PENDING_EMAIL = "pending_email";
    private static final String JSON_IS_NEW_USER = "isNewUser";

    private User user;
    private String facebookID;
    private String token;
    private String email;
    private String pendingEmail;
    private boolean isNewUser;

    public ActiveUser() {
        user = new User();
    }

    public Boolean isActiveUser(long userID) {
        return (getUserID() == userID);
    }

    public void loadActiveUser(GraphUser graphUser) {
        facebookID = graphUser.getId();
        getUser().setUserName(graphUser.getUsername());
        email = (String) graphUser.getProperty("email");
    }

    public void updateFromJSON(JSONObject jsonObject) throws JSONException {
        Log.d(TAG, "updating from json: " + jsonObject.toString());
        getUser().updateFromJSON(jsonObject);
        if (jsonObject.has(JSON_TOKEN)) token = jsonObject.getString(JSON_TOKEN);
        if (jsonObject.has(JSON_EMAIL)) email = jsonObject.getString(JSON_EMAIL);
        if (jsonObject.has(JSON_PENDING_EMAIL))
            if (jsonObject.isNull(JSON_PENDING_EMAIL)) {
                pendingEmail = null;
            } else {
                pendingEmail = jsonObject.getString(JSON_PENDING_EMAIL);
            }
        if (jsonObject.has(JSON_IS_NEW_USER)) isNewUser =
                jsonObject.getBoolean(JSON_IS_NEW_USER);
    }

    public void logout() {
        user = null;
    }

    public String getFacebookID() {
        return facebookID;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getPendingEmail() {
        return pendingEmail;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public User getUser() {
        return user;
    }

    public Long getUserID() {
        if (getUser() != null)
            return getUser().getUserID();
        return null;
    }

    public String getUserName() {
        if (getUser() != null)
            return getUser().getUserName();
        return null;
    }

    public Double getLatitude() {
        if (getUser() != null)
            return getUser().getLatitude();
        return null;
    }

    public Double getLongitude() {
        if (getUser() != null)
            return getUser().getLongitude();
        return null;
    }

    public Integer getKarma() {
        if (getUser() != null)
            return getUser().getKarma();
        return null;
    }
}
