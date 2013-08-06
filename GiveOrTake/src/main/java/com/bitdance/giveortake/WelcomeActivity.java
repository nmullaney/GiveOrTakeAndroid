package com.bitdance.giveortake;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;

/**
 * Created by nora on 8/6/13.
 */
public class WelcomeActivity extends FragmentActivity {

    private ArrayList<Fragment> fragments;
    private int currentIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        loadNextFragment();
    }

    public void loadNextFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = (Fragment) fm.findFragmentById(R.id.fragmentContainer);
        Fragment nextFragment = getFragment(currentIndex + 1);
        if (currentFragment == null) {
            fm.beginTransaction().add(R.id.fragmentContainer, nextFragment).commit();
        } else if (nextFragment == null) {
            // we've finished all the fragments, launch Main
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
            finish();
        } else {
            fm.beginTransaction().replace(R.id.fragmentContainer, nextFragment)
                    .addToBackStack(null).commit();

        }
    }

    private Fragment getFragment(int index) {
        if (getFragments().size() <= index) {
            return null;
        }
        Fragment fragment = getFragments().get(index);
        currentIndex = index;
        return fragment;
    }

    private ArrayList<Fragment> getFragments() {
        if (fragments == null) {
            Bundle args = new Bundle();
            args.putBoolean(Constants.NEW_USER, true);
            fragments = new ArrayList<Fragment>();

            fragments.add(new WelcomeFragment());

            UpdateUsernameFragment usernameFragment = new UpdateUsernameFragment();
            usernameFragment.setArguments(args);
            fragments.add(usernameFragment);

            UpdateEmailFragment emailFragment = new UpdateEmailFragment();
            emailFragment.setArguments(args);
            fragments.add(emailFragment);

            UpdateLocationFragment locationFragment = new UpdateLocationFragment();
            locationFragment.setArguments(args);
            fragments.add(locationFragment);
        }
        return fragments;
    }
}
