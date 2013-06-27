package com.bitdance.giveortake;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import java.util.ArrayList;

public class FreeItemPagerActivity extends FragmentActivity {
    public static final String TAG = "FreeItemPagerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "OnCreate");
        setContentView(R.layout.activity_free_item_pager);

        final ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);
        final ArrayList<Item> freeItems = ((GiveOrTakeApplication) getApplication()).getFreeItems();

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
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageSelected(int position) {
                Long itemID = freeItems.get(position).getId();
                Item item = ((GiveOrTakeApplication) getApplication()).getItem(itemID);
                setTitle(item.getName());
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
    }
    
}
