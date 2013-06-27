package com.bitdance.giveortake;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.UUID;

/**
 * Created by nora on 6/26/13.
 */
public class FreeItemDetailFragment extends Fragment {
    public static final String TAG = "FreeItemDetailFragment";

    private TextView nameView;

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
        nameView = (TextView)v.findViewById(R.id.free_item_detail_name);
        if (item != null) {
            nameView.setText(item.getName());
        }
        return v;
    }

}
