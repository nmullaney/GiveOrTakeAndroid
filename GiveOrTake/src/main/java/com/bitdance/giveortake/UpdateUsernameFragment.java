package com.bitdance.giveortake;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by nora on 7/10/13.
 */
public class UpdateUsernameFragment extends Fragment {
    public static final String TAG = "UpdateUsernameFragment";

    public static final int MIN_USERNAME_LENGTH = 6;

    private EditText usernameText;
    private TextView errorTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= 14) {
            getActivity().getActionBar().setIcon(getResources()
                    .getDrawable(R.drawable.ic_profile_selected_30));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_username, container, false);
        usernameText = (EditText)view.findViewById(R.id.username);
        usernameText.setText(ActiveUser.getInstance().getUserName());
        InputFilter noSpaceFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return source;
            }
        };
        usernameText.setFilters(new InputFilter[]{noSpaceFilter});
        errorTextView = (TextView)view.findViewById(R.id.error_message);
        Button updateButton = (Button)view.findViewById(R.id.update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide the keyboard
                InputMethodManager inputManager = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                // clear any old errors
                clearError();
                String newUsername = usernameText.getText().toString();
                if (newUsername.length() < MIN_USERNAME_LENGTH) {
                    String error = getString(R.string.error_username_too_short);
                    displayError(error);
                    return;
                }
                Intent intent = new Intent(getActivity(), UserService.class);
                intent.setAction(UserService.UPDATE_USERNAME);
                intent.putExtra(UserService.EXTRA_NEW_USERNAME, usernameText.getText());
                getActivity().startService(intent);
            }
        });
        return view;
    }

    private void displayError(String error) {
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(error);
    }

    private void clearError() {
        errorTextView.setVisibility(View.INVISIBLE);
        errorTextView.setText(null);
    }
}
