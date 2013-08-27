package com.bitdance.giveortake;

import android.support.v4.app.Fragment;

/**
 * Wrapper Activity for the MessageFragment.
 */
public class MessageActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new MessageFragment();
    }
}
