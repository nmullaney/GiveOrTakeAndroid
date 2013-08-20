package com.bitdance.giveortake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;

public class FreeItemPagerActivity extends FragmentActivity {
    public static final String TAG = "FreeItemPagerActivity";

    public static final String EXTRA_QUERY = "query";

    private ArrayList<Item> freeItems;
    private ViewPager viewPager;
    private String query;

    private BroadcastReceiver freeItemsUpdated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Got intent with action: " + intent.getAction());
            if (intent.getAction().equals(ItemService.FREE_ITEMS_UPDATED)) {
                freeItems = getGOTApplication().getFreeItems();
                Log.i(TAG, "Notifying data set changed");
                viewPager.getAdapter().notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "OnCreate");
        setContentView(R.layout.activity_free_item_pager);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= 14) {
            getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_take_selected_30));
        }

        viewPager = (ViewPager)findViewById(R.id.viewPager);
        freeItems = getGOTApplication().getFreeItems();

        FragmentManager fm = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int i) {
                Bundle arguments = new Bundle();
                Long itemID = freeItems.get(i).getId();
                arguments.putSerializable(FreeItemsFragment.EXTRA_ITEM_ID, itemID);
                Fragment f = new FreeItemDetailFragment();
                f.setArguments(arguments);
                return f;
            }

            @Override
            public int getCount() {
                return freeItems.size();
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageSelected(int position) {
                Long itemID = freeItems.get(position).getId();
                Item item = getGOTApplication().getItem(itemID);
                setTitle(item.getName());
                if (position >= (freeItems.size() - 2) &&  getGOTApplication().haveMoreFreeItems()) {
                    loadMoreItems();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });

        Long itemID = (Long)getIntent().getSerializableExtra(FreeItemsFragment.EXTRA_ITEM_ID);
        int index = ((GiveOrTakeApplication) getApplication()).getIndexOfFreeItem(itemID);
        if (index == -1) {
            index = 0; // not found
        }
        viewPager.setCurrentItem(index);

        query = getIntent().getStringExtra(EXTRA_QUERY);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        localBroadcastManager.registerReceiver(freeItemsUpdated, new IntentFilter(ItemService.FREE_ITEMS_UPDATED));
    }

    private void loadMoreItems() {
        Log.i(TAG, "Loading more items");
        Intent loadMoreIntent = new Intent(this, ItemService.class);
        loadMoreIntent.setAction(ItemService.UPDATE_FREE_ITEMS);
        int offset = freeItems.size();
        loadMoreIntent.putExtra(ItemService.EXTRA_OFFSET, offset);
        loadMoreIntent.putExtra(ItemService.EXTRA_QUERY, query);
        startService(loadMoreIntent);
    }

    private GiveOrTakeApplication getGOTApplication() {
        return (GiveOrTakeApplication) getApplication();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getApplicationContext());
        localBroadcastManager.unregisterReceiver(freeItemsUpdated);
    }
}
