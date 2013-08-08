package com.bitdance.giveortake;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

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
            Log.d(TAG, "inside usersWhoWantItemReceiver");
            if (intent.getAction().equals(UserService.USERS_WHO_WANT_ITEM_FETCHED)) {
                if (intent.hasExtra(UserService.EXTRA_ERROR)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.error)
                            .setMessage(intent.getStringExtra(UserService.EXTRA_ERROR))
                            .setPositiveButton(R.string.ok, null)
                            .show();
                    return;
                }
                ArrayList<User> usersWhoWant =
                        intent.getParcelableArrayListExtra(UserService.EXTRA_USERS);
                Log.d(TAG, "Got usersWhoWant: " + usersWhoWant);
                ArrayAdapter<User> userArrayAdapter = new ArrayAdapter<User>(getActivity(),
                        android.R.layout.simple_spinner_dropdown_item, usersWhoWant);
                stateUser.setAdapter(userArrayAdapter);
                if (item.getStateUserID() != null) {
                    User selectedUser = null;
                    Log.d(TAG, "Looking for user with id " + item.getStateUserID());
                    for (User user : usersWhoWant) {
                        if (user.getUserID().equals(item.getStateUserID())) {
                            selectedUser = user;
                            Log.d(TAG, "Found user matching: " + user);
                            break;
                        }
                    }
                    if (selectedUser != null) {
                        int positionToSelect = userArrayAdapter.getPosition(selectedUser);
                        Log.d(TAG, "Position to select: " + positionToSelect);
                        stateUser.setSelection(positionToSelect);
                    }
                }

                updateUI();

            }
        }
    };

    private BroadcastReceiver itemPostedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received item posted message");
            if (intent.getAction().equals(ItemService.ITEM_POSTED)) {
                String error = intent.getStringExtra(ItemService.EXTRA_ERROR);
                if (error == null) {
                    getActivity().setResult(Activity.RESULT_OK);
                    item.setHasUnsavedImage(false);
                    getActivity().finish();
                } else {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.error)
                            .setMessage(error)
                            .setPositiveButton(R.string.ok, null)
                            .show();
                }
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
        if (item.getId() != null && item.getImage(getActivity(), null) == null) {
            Intent intent = new Intent(getActivity(), ItemService.class);
            intent.setAction(ItemService.FETCH_ITEM_IMAGE);
            intent.putExtra(ItemService.EXTRA_ITEM, item);
            getActivity().startService(intent);
        }

        if (item.getId() != null) {
            Log.i(TAG, "Building intent for users who want item");
            Intent intent = new Intent(getActivity(), UserService.class);
            intent.setAction(UserService.FETCH_USERS_WHO_WANT_ITEM);
            intent.putExtra(UserService.EXTRA_ITEM_ID, item.getId());
            intent.putExtra(UserService.EXTRA_MIN_MESSAGES, 1);
            getActivity().startService(intent);
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.registerReceiver(imageBroadcastReceiver,
                new IntentFilter(ItemService.ITEM_IMAGE_FETCHED));
        localBroadcastManager.registerReceiver(usersWhoWantItemReceiver,
                new IntentFilter(UserService.USERS_WHO_WANT_ITEM_FETCHED));
        localBroadcastManager.registerReceiver(itemPostedReceiver,
                new IntentFilter(ItemService.ITEM_POSTED));
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
        itemStateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                updateUI();
            }
        });

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
        itemImage.setImageDrawable(item.getImage(getActivity(), null));
        Button postButton = (Button)view.findViewById(R.id.edit_offer_post_button);
        if (item.getId() != null) {
            postButton.setText(getString(R.string.update_offer));
        }
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateItem();
            }
        });
        return view;
    }

    private void updateItem() {
        updateItemData();
        Intent intent = new Intent(getActivity(), ItemService.class);
        intent.setAction(ItemService.POST_ITEM);
        intent.putExtra(ItemService.EXTRA_ITEM, item);
        getActivity().startService(intent);
    }

    public void updateItemData() {
        item.setName(nameText.getText().toString());
        item.setDescription(descText.getText().toString());
        item.setState((Item.ItemState) itemStateSpinner.getSelectedItem());
        if (stateUser.getSelectedItem() != null) {
            item.setStateUser((User)stateUser.getSelectedItem());
        } else {
            item.setStateUser(null);
        }
    }

    private boolean itemNeedsUpdate() {
        if (item.getId() == null) {
            // No ID means this has never been uploaded
            return true;
        }
        if (!item.getName().equals(nameText.getText().toString()))
            return true;
        if (!item.getDescription().equals(descText.getText().toString()))
            return true;
        if (!item.getState().equals(itemStateSpinner.getSelectedItem()))
            return true;
        if (stateUser.getSelectedItem() != null) {
            User selectedStateUser = (User) stateUser.getSelectedItem();
            if (!item.getStateUserID().equals(selectedStateUser.getUserID())) {
                return true;
            }
        } else {
            if (item.getStateUserID() != null) {
                return true;
            }
        }
        if (item.hasUnsavedImage()) {
            return true;
        }
        return false;
    }

    // Returns false if another code path should handle this event
    // In this case, that means navigating up the stack normally
    public boolean onHomePressed() {
        Log.d(TAG, "OnHomePressed");
        if (itemNeedsUpdate()) {
            int postResource = item.getId() != null ? R.string.update_offer : R.string.post_offer;
            DialogInterface.OnClickListener postOfferListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    updateItem();
                }
            };
            DialogInterface.OnClickListener continueListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getActivity().finish();
                }
            };
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.unsaved_changes)
                    .setMessage(getString(R.string.item_not_saved))
                    .setPositiveButton(postResource, postOfferListener)
                    .setNegativeButton(R.string.continue_without_posting, continueListener)
                    .show();
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE_RESULT:
                if (data == null) {
                    break;
                }
                String thumbnailFile = data.getStringExtra(CameraFragment.EXTRA_THUMBNAIL_FILENAME);
                String imageFile = data.getStringExtra(CameraFragment.EXTRA_IMAGE_FILENAME);
                if (thumbnailFile != null && imageFile != null) {
                    item.loadThumbnailFromFile(getActivity(), thumbnailFile);
                    item.loadImageFromFile(getActivity(), imageFile);
                    item.setHasUnsavedImage(true);
                    updateUI();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void updateUI() {
        if (item.getImage(getActivity(), null) != null && itemImage != null) {
            itemImage.setImageDrawable(item.getImage(getActivity(), null));
        }
        if (itemStateSpinner.getSelectedItem().equals(Item.ItemState.PROMISED) ||
                itemStateSpinner.getSelectedItem().equals(Item.ItemState.TAKEN)) {
            stateUser.setVisibility(View.VISIBLE);
        } else {
            stateUser.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.unregisterReceiver(imageBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(usersWhoWantItemReceiver);
        localBroadcastManager.unregisterReceiver(itemPostedReceiver);
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
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                convertView = layoutInflater.inflate(R.layout.dropdown_item_state, null);
            }
            ItemStateView itemStateView = (ItemStateView)
                    convertView.findViewById(R.id.itemStateView);
            itemStateView.setItemState(itemState);
            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item.ItemState itemState = (Item.ItemState) getItem(position);
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                convertView = layoutInflater.inflate(R.layout.dropdown_item_state, null);
            }
            ItemStateView itemStateView = (ItemStateView)
                    convertView.findViewById(R.id.itemStateView);
            itemStateView.setItemState(itemState);
            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return getCount();
        }
    }
}
