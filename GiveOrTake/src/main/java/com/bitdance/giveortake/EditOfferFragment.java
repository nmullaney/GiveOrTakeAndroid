package com.bitdance.giveortake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nora on 6/30/13.
 */
public class EditOfferFragment extends Fragment {
    public static final String TAG = "EditOfferFragment";

    public static final int REQUEST_IMAGE_RESULT = 1;

    private Item item;

    private EditText nameText;
    private EditText descText;
    private ImageView itemImage;
    private Spinner itemStateSpinner;
    private Spinner stateUser;


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

    private BroadcastReceiver usersWhoWantItemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UserService.FETCH_USERS_WHO_WANT_ITEM)) {
                ArrayList<User> usersWhoWant =
                        intent.getParcelableArrayListExtra(UserService.EXTRA_USERS);
                stateUser.setAdapter(new ArrayAdapter<User>(getActivity(),
                        android.R.layout.simple_list_item_1, usersWhoWant));

            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= 14) {
            getActivity().getActionBar().setIcon(getResources()
                    .getDrawable(R.drawable.ic_give_selected_30));
        }

        item = (Item) getActivity().getIntent().getSerializableExtra(OffersFragment.EXTRA_ITEM);
        if (item.getId() != null && item.getImage(getActivity()) == null) {
            Intent intent = new Intent(getActivity(), ItemService.class);
            intent.setAction(ItemService.FETCH_ITEM_IMAGE);
            intent.putExtra(ItemService.EXTRA_ITEM_DATA, item);
            getActivity().startService(intent);
        }

        if (item.getId() != null) {
            Intent intent = new Intent(getActivity(), UserService.class);
            intent.setAction(UserService.FETCH_USERS_WHO_WANT_ITEM);
            intent.putExtra(UserService.EXTRA_ITEM_ID, item.getId());
            intent.putExtra(UserService.EXTRA_MIN_MESSAGES, 1);
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.registerReceiver(imageBroadcastReceiver,
                new IntentFilter(ItemService.ITEM_IMAGE_FETCHED));
        localBroadcastManager.registerReceiver(usersWhoWantItemReceiver,
                new IntentFilter(UserService.USERS_WHO_WANT_ITEM_FETCHED));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_offer, container, false);
        nameText = (EditText)view.findViewById(R.id.edit_offer_name);
        nameText.setText(item.getName());
        descText = (EditText)view.findViewById(R.id.edit_offer_description);
        descText.setText(item.getDescription());
        itemStateSpinner = (Spinner)view.findViewById(R.id.item_state_spinner);
        ItemStateSpinnerAdapter adapter;
        if (item.getState().equals(Item.ItemState.DRAFT)) {
            adapter = new ItemStateSpinnerAdapter(true);
        } else {
            adapter = new ItemStateSpinnerAdapter(false);
        }

        itemStateSpinner.setAdapter(adapter);
        itemStateSpinner.setSelection(adapter.getPosition(item.getState()));

        stateUser = (Spinner)view.findViewById(R.id.state_user);


        Button addPhotoButton = (Button)view.findViewById(R.id.edit_offer_photo_button);
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), CameraActivity.class);
                startActivityForResult(i, REQUEST_IMAGE_RESULT);
            }
        });
        itemImage = (ImageView)view.findViewById(R.id.edit_offer_photo);
        itemImage.setImageDrawable(item.getImage(getActivity()));
        Button postButton = (Button)view.findViewById(R.id.edit_offer_post_button);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE_RESULT:
                String thumbnailFile = data.getStringExtra(CameraFragment.EXTRA_THUMBNAIL_FILENAME);
                String imageFile = data.getStringExtra(CameraFragment.EXTRA_IMAGE_FILENAME);
                item.loadThumbnailFromFile(getActivity(), thumbnailFile);
                item.loadImageFromFile(getActivity(), imageFile);
                updateUI();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
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
        localBroadcastManager.unregisterReceiver(usersWhoWantItemReceiver);
    }

    private class ItemStateSpinnerAdapter extends ArrayAdapter implements SpinnerAdapter {

        public ItemStateSpinnerAdapter(boolean isDraft) {
            super(getActivity(), 0, 0, new ArrayList<Item.ItemState>());
            if (isDraft) {
                add(Item.ItemState.DRAFT);
            } else {
                addAll(Item.ItemState.getSelectableStates());
            }
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            Item.ItemState itemState = (Item.ItemState) getItem(position);
            if (convertView == null) {
                convertView = new ItemStateView(getActivity(), null);
            }
            ((ItemStateView)convertView).setItemState(itemState);
            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item.ItemState itemState = (Item.ItemState) getItem(position);
            if (convertView == null) {
                convertView = new ItemStateView(getActivity(), null);
            }
            ((ItemStateView)convertView).setItemState(itemState);
            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return getCount();
        }
    }
}
