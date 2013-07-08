package com.bitdance.giveortake;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.Menu;

import com.google.android.gms.maps.SupportMapFragment;

public class TestActivity extends SingleFragmentActivity {

    public Fragment createFragment() {
        return new ProfileFragment();
    }
}
