package com.bitdance.giveortake;

import android.support.v4.app.Fragment;

/**
 * Created by nora on 7/11/13.
 */
public class UpdateLocationActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new UpdateLocationFragment();
    }
}
