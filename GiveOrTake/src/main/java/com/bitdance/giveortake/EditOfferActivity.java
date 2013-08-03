package com.bitdance.giveortake;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

/**
 * Created by nora on 6/30/13.
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
