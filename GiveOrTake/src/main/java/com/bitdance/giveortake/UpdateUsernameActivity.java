package com.bitdance.giveortake;

import android.support.v4.app.Fragment;

/**
 * Created by nora on 7/10/13.
 */
public class UpdateUsernameActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new UpdateUsernameFragment();
    }
}
