package com.bitdance.giveortake;

import android.support.v4.app.Fragment;

/**
 * Wrapper Activity for the LoginFragment.
 */
public class LoginActivity extends SingleFragmentActivity {

    protected Fragment createFragment() {
        return new LoginFragment();
    }
}
