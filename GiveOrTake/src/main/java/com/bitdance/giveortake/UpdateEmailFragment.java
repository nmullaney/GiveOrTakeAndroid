package com.bitdance.giveortake;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by nora on 7/11/13.
 */
public class UpdateEmailFragment extends Fragment {
    public static final String TAG = "UpdateEmailFragment";

    private TextView updateEmailTaskDesc;
    private EditText emailText;
    private TextView errorMessageView;
    private EditText emailCodeView;
    private Button sendCodeButton;
    private Button updateEmailButton;

    BroadcastReceiver pendingEmailAdded = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UserService.PENDING_EMAIL_ADDED)) {
                String errorMessage = intent.getStringExtra(UserService.EXTRA_UPDATE_ERROR);
                if (errorMessage != null) {
                    displayErrorMessage(errorMessage);
                } else {
                    updateUIForSendingCode();
                }
            }
        }
    };

    BroadcastReceiver emailUpdated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UserService.EMAIL_CODE_SENT)) {
                String errorMessage = intent.getStringExtra(UserService.EXTRA_UPDATE_ERROR);
                if (errorMessage != null) {
                    displayErrorMessage(errorMessage);
                } else {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }
            }
        }
    };

    BroadcastReceiver pendingEmailCancelled = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UserService.PENDING_EMAIL_CANCELLED)) {
                String errorMessage = intent.getStringExtra(UserService.EXTRA_UPDATE_ERROR);
                if (errorMessage != null) {
                    displayErrorMessage(errorMessage);
                } else {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
            }
        }
    };

    View.OnClickListener sendPendingEmailListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideErrorMessage();
            String newEmail = emailText.getText().toString();
            if (newEmail.equals(ActiveUser.getInstance().getEmail())) {
                // no change
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
            Intent intent = new Intent(getActivity(), UserService.class);
            intent.setAction(UserService.ADD_PENDING_EMAIL);
            intent.putExtra(UserService.EXTRA_NEW_EMAIL, newEmail);
            getActivity().startService(intent);
        }
    };

    View.OnClickListener cancelPendingEmailListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "Cancelling pending email");
            Intent intent = new Intent(getActivity(), UserService.class);
            intent.setAction(UserService.CANCEL_PENDING_EMAIL);
            getActivity().startService(intent);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= 14) {
            getActivity().getActionBar().setIcon(getResources()
                    .getDrawable(R.drawable.ic_profile_selected_30));
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.registerReceiver(pendingEmailAdded,
                new IntentFilter(UserService.PENDING_EMAIL_ADDED));
        localBroadcastManager.registerReceiver(emailUpdated,
                new IntentFilter(UserService.EMAIL_CODE_SENT));
        localBroadcastManager.registerReceiver(pendingEmailCancelled,
                new IntentFilter(UserService.PENDING_EMAIL_CANCELLED));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_email, container, false);
        updateEmailTaskDesc = (TextView)view.findViewById(R.id.update_email_task_desc);
        emailText = (EditText)view.findViewById(R.id.update_email);
        errorMessageView = (TextView)view.findViewById(R.id.error_message);
        emailCodeView = (EditText)view.findViewById(R.id.email_code);
        sendCodeButton = (Button)view.findViewById(R.id.send_code_button);
        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = emailCodeView.getText().toString();
                if (code.length() != 4) {
                    displayErrorMessage(getString(R.string.error_code_four_digits));
                } else {
                    Intent intent = new Intent(getActivity(), UserService.class);
                    intent.setAction(UserService.SEND_EMAIL_CODE);
                    intent.putExtra(UserService.EXTRA_EMAIL_CODE, code);
                    getActivity().startService(intent);
                }
            }
        });
        updateEmailButton = (Button)view.findViewById(R.id.update_email_action_button);
        updateEmailButton.setOnClickListener(sendPendingEmailListener);

        if (ActiveUser.getInstance().getPendingEmail() != null) {
            updateUIForSendingCode();
            emailText.setText(ActiveUser.getInstance().getPendingEmail());
        } else {
            emailText.setText(ActiveUser.getInstance().getEmail());
        }

        return view;
    }

    private void displayErrorMessage(String errorMessage) {
        errorMessageView.setText(errorMessage);
        errorMessageView.setVisibility(View.VISIBLE);
    }

    private void hideErrorMessage() {
        errorMessageView.setText(null);
        errorMessageView.setVisibility(View.INVISIBLE);
    }

    private void updateUIForSendingCode() {
        updateEmailTaskDesc.setText(getString(R.string.code_send_hint));
        updateEmailButton.setText(getString(R.string.cancel_email_update));
        updateEmailButton.setOnClickListener(cancelPendingEmailListener);
        sendCodeButton.setVisibility(View.VISIBLE);
        emailCodeView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.unregisterReceiver(pendingEmailAdded);
        localBroadcastManager.unregisterReceiver(emailUpdated);
        localBroadcastManager.unregisterReceiver(pendingEmailCancelled);
    }
}