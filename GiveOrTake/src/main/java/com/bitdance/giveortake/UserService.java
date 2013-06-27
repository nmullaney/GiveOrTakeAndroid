package com.bitdance.giveortake;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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
        intent.putExtra(EXTRA_USER_DATA, user);
        localBroadcastManager.sendBroadcast(intent);
    }
}
