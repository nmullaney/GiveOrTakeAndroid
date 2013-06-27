package com.bitdance.giveortake;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by nora on 6/26/13.
 */
public class FreeItemDetailFragment extends Fragment {
    public static final String TAG = "FreeItemDetailFragment";

    private Item item;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Long itemID = null;

        if (getArguments() != null) {
            itemID = (Long)getArguments().getSerializable(FreeItemsFragment.EXTRA_ITEM_ID);
        } else {
            itemID = (Long)getActivity().getIntent()
                    .getSerializableExtra(FreeItemsFragment.EXTRA_ITEM_ID);
        }

        if (itemID != null) {
            item = ((GiveOrTakeApplication)getActivity().getApplication()).getItem(itemID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_free_item_detail, container, false);
        if (item != null) {

            // TODO: get image

            TextView descView = (TextView)v.findViewById(R.id.free_item_detail_desc);
            descView.setText(item.getDescription());

            // TODO: get real username data
            TextView usernameView = (TextView)v.findViewById(R.id.free_item_detail_username);
            usernameView.setText(item.getUserID().toString());
            TextView karmaView = (TextView)v.findViewById(R.id.free_item_detail_karma);
            karmaView.setText("100");

            ImageView statusIcon = (ImageView)v.findViewById(R.id.free_item_detail_status_icon);
            statusIcon.setImageDrawable(item.getDrawableForState(getActivity()));
            TextView statusText = (TextView)v.findViewById(R.id.free_item_detail_status_text);
            statusText.setText(item.getState().getName());

            TextView distanceView = (TextView)v.findViewById(R.id.free_item_detail_distance);
            distanceView.setText(distanceDescription());

            TextView updateView = (TextView)v.findViewById(R.id.free_item_detail_date_updated);
            updateView.setText(dateDescription(item.getDateUpdated()));

            TextView createdView = (TextView)v.findViewById(R.id.free_item_detail_date_created);
            createdView.setText(dateDescription(item.getDateCreated()));
        }
        return v;
    }

    private String distanceDescription() {
        if (item.getDistance() < 1) {
            return getActivity().getResources().getString(R.string.less_than_1_mile);
        } else {
            return getActivity().getResources()
                    .getString(R.string.distance_format, item.getDistance());
        }
    }

    private String dateDescription(Date date) {
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);
        Calendar nowCalendar = Calendar.getInstance();
        long secInterval = (nowCalendar.getTimeInMillis() - dateCalendar.getTimeInMillis()) / 1000;
        Resources resources = getActivity().getResources();
        if (secInterval < 60) {
            return resources.getString(R.string.less_than_1_minute);
        } else if (secInterval < 60 * 2) {
            return resources.getString(R.string.a_minute_ago);
        } else if (secInterval < 60 * 60) {
            int minutes = (int) (secInterval / 60.0);
            return resources.getString(R.string.minutes_ago_format, minutes);
        } else if (secInterval < 2 * 60 * 60) {
            return resources.getString(R.string.an_hour_ago);
        } else if (secInterval < 24 * 60 * 60) {
            int hours = (int) (secInterval / (60.0 * 60.0));
            return resources.getString(R.string.hours_ago_format, hours);
        } else if (secInterval < 2 * 24 * 60 * 60) {
            return resources.getString(R.string.a_day_ago);
        } else if (secInterval < 30 * 24 * 60 * 60) {
            int days = (int) (secInterval / (24 * 60.0 * 60.0));
            return resources.getString(R.string.days_ago_format, days);
        } else {
            return resources.getString(R.string.more_than_month);
        }
    }

}
