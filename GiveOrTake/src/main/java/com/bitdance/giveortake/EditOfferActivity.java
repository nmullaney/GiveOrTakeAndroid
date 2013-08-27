package com.bitdance.giveortake;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

/**
 * Wrapper Activity for the EditOfferFragment.
 */
public class EditOfferActivity extends SingleFragmentActivity {
    public static final String TAG = "EditOfferActivity";

    private EditOfferFragment fragment;

    protected Fragment createFragment() {
        fragment = new EditOfferFragment();
        return fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return fragment.onHomePressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
