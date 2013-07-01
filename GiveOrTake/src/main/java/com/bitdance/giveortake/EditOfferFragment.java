package com.bitdance.giveortake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by nora on 6/30/13.
 */
public class EditOfferFragment extends Fragment {
    public static final String TAG = "EditOfferFragment";

    private Item item;

    private EditText nameText;
    private EditText descText;
    private ImageView stateIcon;
    private TextView stateText;
    private ImageView itemImage;

    private BroadcastReceiver imageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ItemService.ITEM_IMAGE_FETCHED)) {
                boolean failure = intent.getBooleanExtra(ItemService.EXTRA_IMAGE_FETCH_ERROR, false);
                if (!failure) {
                    updateUI();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = (Item) getActivity().getIntent().getSerializableExtra(OffersFragment.EXTRA_ITEM);
        if (item.getId() != null && item.getImage(getActivity()) == null) {
            Intent intent = new Intent(getActivity(), ItemService.class);
            intent.setAction(ItemService.FETCH_ITEM_IMAGE);
            intent.putExtra(ItemService.EXTRA_ITEM_DATA, item);
            getActivity().startService(intent);
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.registerReceiver(imageBroadcastReceiver,
                new IntentFilter(ItemService.ITEM_IMAGE_FETCHED));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_offer, container, false);
        nameText = (EditText)view.findViewById(R.id.edit_offer_name);
        nameText.setText(item.getName());
        descText = (EditText)view.findViewById(R.id.edit_offer_description);
        descText.setText(item.getDescription());
        stateIcon = (ImageView)view.findViewById(R.id.edit_offer_state_icon);
        stateIcon.setImageDrawable(item.getDrawableForState(getActivity()));
        stateText = (TextView)view.findViewById(R.id.edit_offer_state);
        stateText.setText(item.getState().getName());
        Button changeStateButton = (Button)view.findViewById(R.id.edit_offer_change_state_button);
        if (item.getState().equals(Item.ItemState.DRAFT)) {
            changeStateButton.setEnabled(false);
            changeStateButton.setVisibility(View.INVISIBLE);
        }
        Button addPhotoButton = (Button)view.findViewById(R.id.edit_offer_photo_button);
        itemImage = (ImageView)view.findViewById(R.id.edit_offer_photo);
        itemImage.setImageDrawable(item.getImage(getActivity()));
        Button postButton = (Button)view.findViewById(R.id.edit_offer_post_button);
        return view;
    }

    public void updateUI() {
        if (item.getImage(getActivity()) != null && itemImage != null) {
            itemImage.setImageDrawable(item.getImage(getActivity()));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.unregisterReceiver(imageBroadcastReceiver);
    }
}
