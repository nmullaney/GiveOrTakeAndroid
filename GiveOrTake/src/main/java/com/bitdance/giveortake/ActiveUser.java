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
        ourInstance.getUser().updateFromJSON(jsonObject);
        if (jsonObject.has(JSON_TOKEN)) ourInstance.token = jsonObject.getString(JSON_TOKEN);
        if (jsonObject.has(JSON_EMAIL)) ourInstance.email = jsonObject.getString(JSON_EMAIL);
        if (jsonObject.has(JSON_PENDING_EMAIL))
            ourInstance.pendingEmail = jsonObject.getString(JSON_PENDING_EMAIL);
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

    public long getUserID() {
        return getUser().getUserID();
    }

    public String getUserName() {
        return getUser().getUserName();
    }

    public double getLatitude() {
        return getUser().getLatitude();
    }

    public int getMicroLatitude() {
        return (int) (getLatitude() * 1E6);
    }

    public double getLongitude() {
        return getUser().getLongitude();
    }

    public int getMicroLongitude() {
        return (int) (getLongitude() * 1E6);
    }

    public int getKarma() {
        return getUser().getKarma();
    }
}
