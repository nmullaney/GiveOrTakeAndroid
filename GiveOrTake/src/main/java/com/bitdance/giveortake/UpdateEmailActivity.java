package com.bitdance.giveortake;

import android.support.v4.app.Fragment;

/**
 * Wrapper Activity for the UpdateEmailFragment.
 */
public class UpdateEmailActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new UpdateEmailFragment();
    }
}
