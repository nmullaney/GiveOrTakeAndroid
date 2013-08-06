package com.bitdance.giveortake;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

/**
 * Created with IntelliJ IDEA.
 * User: nora
 * Date: 6/21/13
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoginFragment extends Fragment {
    public static final String TAG = "LoginFragment";

    public static final String EXTRA_LOGIN_ACTION = "login_action";
    public static final String LOGOUT = "logout";

    private ProgressBar progressBar;
    private LoginButton loginButton;

    private UiLifecycleHelper uiHelper;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private BroadcastReceiver loginBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received login result");
            if (intent.getAction().equals(UserService.LOGIN_RESULT)) {
                progressBar.setVisibility(View.INVISIBLE);
                if (intent.hasExtra(UserService.EXTRA_ERROR)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.error)
                            .setMessage(intent.getStringExtra(UserService.EXTRA_ERROR))
                            .setPositiveButton(R.string.ok, null)
                            .show();
                    Session.getActiveSession().closeAndClearTokenInformation();
                } else {
                    launchMainOrWelcomeActivity();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");

        String loginAction = getActivity().getIntent().getStringExtra(EXTRA_LOGIN_ACTION);
        if (loginAction != null && loginAction.equals(LOGOUT)) {
            Log.d(TAG, "Logging out");
            ((GiveOrTakeApplication) getActivity().getApplication()).logout();
            Session.getActiveSession().closeAndClearTokenInformation();
            ActiveUser.logout();
        }

        if (ActiveUser.getInstance() != null) {
            // we are still logged in and can go directly to the main activity
            launchMainOrWelcomeActivity();
            return;
        }

        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);

        LocalBroadcastManager localBroadcastManager =
                LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        IntentFilter loginIntentFilter = new IntentFilter(UserService.LOGIN_RESULT);
        localBroadcastManager.registerReceiver(loginBroadcastReceiver, loginIntentFilter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        progressBar = (ProgressBar)v.findViewById(R.id.progressBar);
        loginButton = (LoginButton)v.findViewById(R.id.login_button);
        loginButton.setFragment(this);
        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                Log.d(TAG, "User info fetched: " + user);
                updateUI();
                if (user != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    ActiveUser.loadActiveUser(user);
                    Intent intent = new Intent(getActivity(), UserService.class);
                    intent.setAction(UserService.LOGIN);
                    getActivity().startService(intent);
                }

            }
        });
        return v;
    }

    private void launchMainOrWelcomeActivity() {
        if (ActiveUser.getInstance().isNewUser()) {
            Intent welcomeIntent = new Intent(getActivity(), WelcomeActivity.class);
            welcomeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(welcomeIntent);
        } else {
            Intent mainIntent = new Intent(getActivity(), MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
        }
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();

        updateUI();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (uiHelper != null) {
            uiHelper.onDestroy();
        }
        LocalBroadcastManager localBroadcastManager =
                LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        localBroadcastManager.unregisterReceiver(loginBroadcastReceiver);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if ((exception instanceof FacebookOperationCanceledException ||
                        exception instanceof FacebookAuthorizationException)) {
            Log.e(TAG, "Facebook exception: " + exception);
            StringBuilder error = new StringBuilder();
            for (StackTraceElement elem : exception.getStackTrace()) {
                error.append(elem.toString());
                error.append("\n");
            }
            Log.i(TAG, "Full stactrace: " + error);
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.cancelled)
                    .setMessage(R.string.permission_not_granted)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            Log.i(TAG, "opened token updated");
        }
        updateUI();
    }

    private void updateUI() {
        Session session = Session.getActiveSession();
        boolean enableButtons = (session != null && session.isOpened());
    }


}
