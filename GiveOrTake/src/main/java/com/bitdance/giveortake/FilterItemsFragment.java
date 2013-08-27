package com.bitdance.giveortake;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * The FilterItemsFragment is a view where the ActiveUser can limit which
 * items they'd like to see.
 */
public class FilterItemsFragment extends Fragment {
    public static final String TAG = "FilterItemsFragment";

    private TextView seekBarMiles;
    private CheckBox showMyItemsCheckbox;

    private Integer distance;
    private boolean showMyItems;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        SharedPreferences preferences = getActivity()
                .getSharedPreferences(Constants.FILTER_PREFERENCES, Context.MODE_PRIVATE);
        distance = preferences.getInt(Constants.DISTANCE_PREFERENCE, Constants.DEFAULT_DISTANCE);
        showMyItems = preferences.getBoolean(Constants.SHOW_MY_ITEMS_PREFERENCE,
                Constants.DEFAULT_SHOW_MY_ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_items, container, false);
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekbar);
        // The min is always "0", so to have a larger min, we have to do some math
        seekBar.setMax(Constants.MAX_DISTANCE - Constants.MIN_DISTANCE);
        seekBar.setProgress(distance - Constants.MIN_DISTANCE);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, "Progress changed: " + progress);
                distance = progress + Constants.MIN_DISTANCE;
                setSeekBarMilesLabel();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBarMiles = (TextView) view.findViewById(R.id.seekbar_miles);
        setSeekBarMilesLabel();
        showMyItemsCheckbox = (CheckBox) view.findViewById(R.id.show_my_items_checkbox);
        showMyItemsCheckbox.setChecked(showMyItems);
        Button applyFiltersButton = (Button) view.findViewById(R.id.apply_filters_button);
        applyFiltersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        return view;
    }

    private void setSeekBarMilesLabel() {
        String milesFormat = getResources().getString(R.string.number_of_miles);
        String milesStr = String.format(milesFormat, distance);
        seekBarMiles.setText(milesStr);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor preferencesEditor = getActivity()
                .getSharedPreferences(Constants.FILTER_PREFERENCES, Context.MODE_PRIVATE).edit();
        preferencesEditor.putInt(Constants.DISTANCE_PREFERENCE, distance);
        preferencesEditor.putBoolean(Constants.SHOW_MY_ITEMS_PREFERENCE, showMyItemsCheckbox.isChecked());
        preferencesEditor.commit();
        Log.d(TAG, "Setting filter values");
    }
}
