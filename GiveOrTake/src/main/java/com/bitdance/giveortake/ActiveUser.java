package com.bitdance.giveortake;

import android.util.Log;

import com.facebook.model.GraphUser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nora on 6/23/13.
 */
public class ActiveUser {
    public static final String TAG = "ActiveUser";

    private static final String JSON_TOKEN = "token";
    private static final String JSON_EMAIL = "email";
    private static final String JSON_PENDING_EMAIL = "pending_email";
    private static final String JSON_IS_NEW_USER = "isNewUser";

    private static ActiveUser ourInstance;

    private User user;
    private String facebookID;
    private String token;
    private String email;
    private String pendingEmail;
    private boolean isNewUser;

    public static ActiveUser getInstance() {
        return ourInstance;
    }

    public static Boolean isActiveUser(long userID) {
        ActiveUser activeUser = getInstance();
        if (activeUser != null) {
            if (activeUser.getUserID() == userID) {
                return true;
            }
        }
        return false;
    }

    public static void loadActiveUser(GraphUser graphUser) {
        ourInstance = new ActiveUser();
        ourInstance.facebookID = graphUser.getId();
        ourInstance.getUser().setUserName(graphUser.getUsername());
        ourInstance.email = (String) graphUser.getProperty("email");
    }

    public static void updateFromJSON(JSONObject jsonObject) throws JSONException {
        Log.i(TAG, "updating from json: " + jsonObject.toString());
        ourInstance.getUser().updateFromJSON(jsonObject);
        if (jsonObject.has(JSON_TOKEN)) ourInstance.token = jsonObject.getString(JSON_TOKEN);
        if (jsonObject.has(JSON_EMAIL)) ourInstance.email = jsonObject.getString(JSON_EMAIL);
        if (jsonObject.has(JSON_PENDING_EMAIL))
            if (jsonObject.isNull(JSON_PENDING_EMAIL)) {
                ourInstance.pendingEmail = null;
            } else {
              ourInstance.pendingEmail = jsonObject.getString(JSON_PENDING_EMAIL);
            }
        if (jsonObject.has(JSON_IS_NEW_USER)) ourInstance.isNewUser =
                jsonObject.getBoolean(JSON_IS_NEW_USER);
    }

    public static void logout() {
        ourInstance = null;
    }

    private ActiveUser() {
        user = new User();
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
