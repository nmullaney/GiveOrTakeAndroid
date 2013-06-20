package com.bitdance.giveortake;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

public class MainTabActivity extends android.app.TabActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        Resources resources = getResources();
        TabHost tabHost = getTabHost();

        Intent intentOffers = new Intent().setClass(this, OffersActivity.class);
        View offersIndicator = indicatorView("Give", R.drawable.icon_give_config, tabHost);
        TabHost.TabSpec tabSpecGive = tabHost
                .newTabSpec("Give")
                .setIndicator(offersIndicator)
                .setContent(intentOffers);

        Intent intentFreeItems = new Intent().setClass(this, FreeItemsActivity.class);
        View freeItemsIndicator = indicatorView("Take", R.drawable.icon_take_config, tabHost);
        TabHost.TabSpec tabSpecTake = tabHost
                .newTabSpec("Take")
                .setIndicator(freeItemsIndicator)
                .setContent(intentFreeItems);

        Intent intentProfile = new Intent().setClass(this, ProfileActivity.class);
        View profileIndicator = indicatorView("Profile", R.drawable.icon_profile_config, tabHost);
        TabHost.TabSpec tabSpecProfile = tabHost
                .newTabSpec("Profile")
                .setIndicator(profileIndicator)
                .setContent(intentProfile);

        // add all tabs
        tabHost.addTab(tabSpecGive);
        tabHost.addTab(tabSpecTake);
        tabHost.addTab(tabSpecProfile);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int tabWidth = displayMetrics.widthPixels / tabHost.getTabWidget().getTabCount();
        for(int i = 0; i < tabHost.getTabWidget().getTabCount(); i++)
            tabHost.getTabWidget().getChildAt(i).getLayoutParams().width = tabWidth;

        tabHost.setCurrentTab(1);
    }

    private View indicatorView(String name, int iconResId, ViewGroup parent) {
        View indicator = getLayoutInflater().inflate(R.layout.tab, parent, false);
        TextView textView = (TextView)indicator.findViewById(R.id.tab_text);
        textView.setText(name);

        ImageView imageView = (ImageView)indicator.findViewById(R.id.tab_image);
        imageView.setImageDrawable(getResources().getDrawable(iconResId));

        return indicator;
    }

}
