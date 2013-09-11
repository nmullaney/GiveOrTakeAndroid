package com.bitdance.giveortake;

import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

/**
 * MainActivity is the central Activity of the app.  It's a TabListener that switches
 * between the OffersFragment, the FreeItemsFragment, and the ProfileFragment.
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    public static final String TAG = "MainActivity";

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private static final int DEFAULT_SELECTED_TAB = 0;
    private static final int FREE_ITEMS_TAB = 1;

    private Long selectedItemID;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "setting selected id from intent");
        setSelectedItemIDFromIntent();
        ActiveUser activeUser = ((GiveOrTakeApplication) getApplication()).getActiveUser();
        if (activeUser == null || activeUser.getUserID() == null) {
            Intent logoutIntent = new Intent(this, LoginActivity.class);
            logoutIntent.putExtra(LoginFragment.EXTRA_LOGIN_ACTION, LoginFragment.LOGOUT);
            logoutIntent.putExtra(MainActivity.EXTRA_ITEM_ID, getSelectedItemID());
            startActivity(logoutIntent);
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setIcon(mSectionsPagerAdapter.getPageIcon(i))
                            .setTabListener(this));
        }

        actionBar.selectTab(actionBar.getTabAt(DEFAULT_SELECTED_TAB));

        Long itemID = getSelectedItemID();
        if (itemID != null) {
            actionBar.selectTab(actionBar.getTabAt(FREE_ITEMS_TAB));
        }
    }

    private void setSelectedItemIDFromIntent() {
        Long itemID = null;
        Log.i(TAG, "Intent = " + getIntent());
        if (getIntent().getData() != null) {
            Uri uri = getIntent().getData();
            Log.i(TAG, "uri = " + uri);
            if (uri.getHost().equals("freeItem")) {
                String itemIDstr = uri.getQueryParameter("itemID");
                if (itemIDstr != null) {
                    itemID = new Long(itemIDstr);
                }
            }
        } else if (getIntent().getLongExtra(MainActivity.EXTRA_ITEM_ID, 0) != 0) {
            itemID = getIntent().getLongExtra(MainActivity.EXTRA_ITEM_ID, 0);
        }
        selectedItemID = itemID;
        Log.i(TAG, "Selected item id = " + selectedItemID);
    }

    public Long getSelectedItemID() {
        return selectedItemID;
    }

    public void clearSelectedItemID() {
        Log.i(TAG, "Clearing selected item id");
        selectedItemID = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(TAG, "Got search query: " + query);
            ((FreeItemsFragment) mSectionsPagerAdapter.getFreeItemsFragment()).searchQuery(query);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Long itemID = getSelectedItemID();
            if (itemID != null) {
                getActionBar().selectTab(getActionBar().getTabAt(FREE_ITEMS_TAB));
                ((FreeItemsFragment) mSectionsPagerAdapter.getFreeItemsFragment()).displaySingleItem(itemID);
            }
        } else {
            Log.i(TAG, "Action = " + intent.getAction());
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private Fragment offersFragment;
        private Fragment freeItemsFragment;
        private Fragment profileFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return getOffersFragment();
                case 1:
                    return getFreeItemsFragment();
                case 2:
                    return getProfileFragment();
                default:
                    return null;
            }
        }

        private Fragment getOffersFragment() {
            if (offersFragment == null) {
                offersFragment = new OffersFragment();
            }
            return offersFragment;
        }

        private Fragment getFreeItemsFragment() {
            if (freeItemsFragment == null) {
                freeItemsFragment = new FreeItemsFragment();
            }
            return freeItemsFragment;
        }

        private Fragment getProfileFragment() {
            if (profileFragment == null) {
                profileFragment = new ProfileFragment();
            }
            return profileFragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        private String getCamelCasePageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.offers_title);
                case 1:
                    return getString(R.string.free_items_title);
                case 2:
                    return getString(R.string.profile_title);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            String title = getCamelCasePageTitle(position);
            if (title == null) return null;
            return title.toUpperCase(l);
        }

        public Drawable getPageIcon(int position) {
            Resources resources = getResources();
            switch (position) {
                case 0:
                    return resources.getDrawable(R.drawable.icon_give_config_30);
                case 1:
                    return resources.getDrawable(R.drawable.icon_take_config_30);
                case 2:
                    return resources.getDrawable(R.drawable.icon_profile_config_30);
                default:
                    return null;
            }
        }
    }
}
