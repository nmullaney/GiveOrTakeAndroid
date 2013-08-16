package com.bitdance.giveortake;

import android.support.v4.app.Fragment;

/**
 * Created by nora on 8/16/13.
 */
public class MessageActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new MessageFragment();
    }
}
