package com.bitdance.giveortake;

import android.support.v4.app.Fragment;

/**
 * Wrapper Activity for UpdateUsernameFragment.
 */
public class UpdateUsernameActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new UpdateUsernameFragment();
    }
}
