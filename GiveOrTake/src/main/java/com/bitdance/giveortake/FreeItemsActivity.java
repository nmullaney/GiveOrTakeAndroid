package com.bitdance.giveortake;

import android.support.v4.app.Fragment;

public class FreeItemsActivity extends SingleFragmentActivity {

    public Fragment createFragment() {
        return new FreeItemsFragment();
    }
}
