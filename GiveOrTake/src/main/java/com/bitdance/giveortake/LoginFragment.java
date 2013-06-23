package com.bitdance.giveortake;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

    private UiLifecycleHelper uiHelper;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        LoginButton loginButton = (LoginButton)v.findViewById(R.id.login_button);
        loginButton.setFragment(this);
        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                Log.i(TAG, "User info fetched: " + user);
                updateUI();
                if (user != null) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }

            }
        });
        return v;
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
        uiHelper.onDestroy();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if ((exception instanceof FacebookOperationCanceledException ||
                        exception instanceof FacebookAuthorizationException)) {
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
