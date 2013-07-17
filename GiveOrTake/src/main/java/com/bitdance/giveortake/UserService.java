package com.bitdance.giveortake;

import android.app.IntentService;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by nora on 6/23/13.
 */
public class UserService extends IntentService {
    public static final String TAG = "UserService";

    public static final String LOGIN = "login";
    public static final String LOGIN_RESULT = "login_result";
    public static final String EXTRA_LOGIN_ERROR = "login_error";

    public static final String FETCH_USER = "fetch_user";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String USER_FETCHED = "user_fetched";
    public static final String EXTRA_USER_DATA = "user_data";

    public static final String UPDATE_USERNAME = "update_username";
    public static final String EXTRA_NEW_USERNAME = "extra_new_username";
    public static final String USERNAME_UPDATED = "username_updated";
    public static final String EXTRA_UPDATE_ERROR = "extra_update_error";

    public static final String ADD_PENDING_EMAIL = "add_pending_email";
    public static final String EXTRA_NEW_EMAIL = "extra_new_email";
    public static final String PENDING_EMAIL_ADDED = "pending_email_added";
    public static final String SEND_EMAIL_CODE = "send_email_code";
    public static final String EXTRA_EMAIL_CODE = "extra_email_code";
    public static final String EMAIL_CODE_SENT = "email_code_sent";
    public static final String CANCEL_PENDING_EMAIL = "cancel_pending_email";
    public static final String PENDING_EMAIL_CANCELLED = "pending_email_cancelled";

    public static final String UPDATE_LOCATION = "update_location";
    public static final String EXTRA_LATLNG = "extra_latlng";
    public static final String LOCATION_UPDATED = "location_updated";

    public static final String FETCH_USERS_WHO_WANT_ITEM = "fetch_users_who_want_item";
    public static final String EXTRA_ITEM_ID = "extra_item_id";
    public static final String EXTRA_MIN_MESSAGES = "extra_min_messages";
    public static final String USERS_WHO_WANT_ITEM_FETCHED = "users_who_want_item_fetched";
    public static final String EXTRA_USERS = "extra_users";


    public UserService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(LOGIN)) {
            login();
        } else if (intent.getAction().equals(FETCH_USER)) {
            Long userID = intent.getLongExtra(EXTRA_USER_ID, 0);
            fetchUser(userID);
        } else if (intent.getAction().equals(UPDATE_USERNAME)) {
            String username = intent.getStringExtra(EXTRA_NEW_USERNAME);
            updateUsername(username);
        } else if (intent.getAction().equals(ADD_PENDING_EMAIL)) {
            String email = intent.getStringExtra(EXTRA_NEW_EMAIL);
            addPendingEmail(email);
        } else if (intent.getAction().equals(SEND_EMAIL_CODE)) {
            String code = intent.getStringExtra(EXTRA_EMAIL_CODE);
            sendEmailCode(code);
        } else if (intent.getAction().equals(CANCEL_PENDING_EMAIL)) {
            cancelPendingEmail();
        } else if (intent.getAction().equals(UPDATE_LOCATION)) {
            LatLng latLng = intent.getParcelableExtra(EXTRA_LATLNG);
            updateLocation(latLng);
        } else if (intent.getAction().equals(FETCH_USERS_WHO_WANT_ITEM)) {
            Log.i(TAG, "Handling fetch users who want item");
            Long itemID = intent.getLongExtra(EXTRA_ITEM_ID, 0);
            int minMessages = intent.getIntExtra(EXTRA_MIN_MESSAGES, 0);
            fetchUsersWhoWantItem(itemID, minMessages);
        }
    }

    private void login() {
        ActiveUser activeUser = ActiveUser.getInstance();
        assert(activeUser != null);
        UserFetcher fetcher = new UserFetcher(this);
        boolean loginSucceeded = fetcher.loginUser();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        Intent i = new Intent(LOGIN_RESULT);
        if (!loginSucceeded) {
            i.putExtra(EXTRA_LOGIN_ERROR,
                    getApplicationContext().getResources().getString(R.string.login_failure));
        }
        Log.i(TAG, "Sending login result broadcast");
        localBroadcastManager.sendBroadcast(i);
    }

    private void fetchUser(Long userID) {
        if (userID == 0) {
            Log.e(TAG, "Cannot fetch user without userID.");
            return;
        }

        // try to fetch the local value
        User user = ((GiveOrTakeApplication) getApplication()).getUser(userID);
        if (user == null) {
            UserFetcher fetcher = new UserFetcher(this);
            user = fetcher.fetchUser(userID);
        }
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        Intent intent = new Intent(USER_FETCHED);
        intent.putExtra(EXTRA_USER_DATA, (Parcelable) user);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void updateUsername(String newUsername) {
        UserFetcher fetcher = new UserFetcher(this);
        UserFetcher.UpdateResponse response = fetcher.updateUsername(newUsername);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        Intent intent = new Intent(USERNAME_UPDATED);
        if (!response.isSuccess()) {
            intent.putExtra(EXTRA_UPDATE_ERROR, response.getErrorMessage());
        }
        localBroadcastManager.sendBroadcast(intent);
    }

    private void addPendingEmail(String newEmail) {
        UserFetcher fetcher = new UserFetcher(this);
        UserFetcher.UpdateResponse response = fetcher.addPendingEmail(newEmail);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        Intent intent = new Intent(PENDING_EMAIL_ADDED);
        if (!response.isSuccess()) {
            intent.putExtra(EXTRA_UPDATE_ERROR, response.getErrorMessage());
        }
        localBroadcastManager.sendBroadcast(intent);
    }

    private void sendEmailCode(String emailCode) {
        UserFetcher fetcher = new UserFetcher(this);
        UserFetcher.UpdateResponse response = fetcher.sendEmailCode(emailCode);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        Intent intent = new Intent(EMAIL_CODE_SENT);
        if (!response.isSuccess()) {
            intent.putExtra(EXTRA_UPDATE_ERROR, response.getErrorMessage());
        }
        localBroadcastManager.sendBroadcast(intent);
    }

    private void cancelPendingEmail() {
        UserFetcher fetcher = new UserFetcher(this);
        UserFetcher.UpdateResponse response = fetcher.cancelPendingEmail();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        Intent intent = new Intent(PENDING_EMAIL_CANCELLED);
        if (!response.isSuccess()) {
            intent.putExtra(EXTRA_UPDATE_ERROR, response.getErrorMessage());
        }
        localBroadcastManager.sendBroadcast(intent);
    }

    private void updateLocation(LatLng latLng) {
        UserFetcher fetcher = new UserFetcher(this);
        UserFetcher.UpdateResponse response = fetcher.updateLocation(latLng);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        Intent intent = new Intent(LOCATION_UPDATED);
        if (!response.isSuccess()) {
            intent.putExtra(EXTRA_UPDATE_ERROR, response.getErrorMessage());
        }
        localBroadcastManager.sendBroadcast(intent);
    }

    private void fetchUsersWhoWantItem(Long itemID, int minMessages) {
        Log.i(TAG, "Fetching users who want item");
        UserFetcher fetcher = new UserFetcher(this);
        ArrayList<User> users = fetcher.fetchUsersWhoWantItem(itemID, minMessages);
        Log.i(TAG, "Found users who want item: " + users);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        Intent intent = new Intent(USERS_WHO_WANT_ITEM_FETCHED);
        intent.putExtra(EXTRA_USERS, users);
        localBroadcastManager.sendBroadcast(intent);
    }
}
