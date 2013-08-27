package com.bitdance.giveortake;

import android.support.v4.app.Fragment;

/**
 * Wrapper Activity for the FilterItemsFragment.
 */
public class FilterItemsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new FilterItemsFragment();
    }
}
