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

    public UserService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(LOGIN)) {
            login();
        }
    }

    private void login() {
        ActiveUser activeUser = ActiveUser.getInstance();
        assert(activeUser != null);
        UserFetcher fetcher = new UserFetcher(this);
        boolean loginSucceeded = fetcher.loginUser();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        Intent i = new Intent(LOGIN_RESULT);
        if (!loginSucceeded) {
            i.putExtra(EXTRA_LOGIN_ERROR,
                    getApplicationContext().getResources().getString(R.string.login_failure));
        }
        Log.i(TAG, "Sending login result broadcast");
        localBroadcastManager.sendBroadcast(i);
    }
}
