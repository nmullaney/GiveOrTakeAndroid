package com.bitdance.giveortake;

import android.support.v4.app.Fragment;

/**
 * Created by nora on 6/19/13.
 */
public class OffersActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return new OffersFragment();
    }
}
