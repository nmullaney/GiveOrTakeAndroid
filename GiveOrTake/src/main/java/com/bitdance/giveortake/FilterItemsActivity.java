package com.bitdance.giveortake;

import android.support.v4.app.Fragment;

/**
 * Created by nora on 7/30/13.
 */
public class FilterItemsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new FilterItemsFragment();
    }
}
