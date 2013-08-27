package com.bitdance.giveortake;

import android.support.v4.app.Fragment;

/**
 * Wrapper Activity for the UpdateLocationFragment.
 */
public class UpdateLocationActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new UpdateLocationFragment();
    }
}
