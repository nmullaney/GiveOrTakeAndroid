package com.bitdance.giveortake;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by nora on 6/26/13.
 */
public class FreeItemDetailFragment extends Fragment {
    public static final String TAG = "FreeItemDetailFragment";

    private Item item;
    private User owner;

    private TextView usernameView;
    private TextView karmaView;
    private ImageView imageView;
    private Button wantButton;

    private boolean userErrorShown = false;

    private BroadcastReceiver userBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UserService.USER_FETCHED)) {
                Long intentUserID = intent.getLongExtra(UserService.EXTRA_USER_ID, 0);
                if (!item.getUserID().equals(intentUserID)) {
                    // This is not for us
                    return;
                }
                if (intent.hasExtra(UserService.EXTRA_ERROR)) {
                    if (!userErrorShown) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.error)
                                .setMessage(intent.getStringExtra(UserService.EXTRA_ERROR))
                                .setPositiveButton(R.string.ok, null)
                                .show();
                        // only show a user error once, although we may get multiples
                        userErrorShown = true;
                    }
                    return;
                }
                User user = (User) intent.getSerializableExtra(UserService.EXTRA_USER_DATA);
                owner = user;
                updateUI();
            }
        }
    };

    private BroadcastReceiver imageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().endsWith(ItemService.ITEM_IMAGE_FETCHED)) {
                boolean failure = intent.getBooleanExtra(ItemService.EXTRA_IMAGE_FETCH_ERROR, false);
                if (!failure) {
                    updateUI();
                }
                // TODO: alert on failure
            }
        }
    };

    private BroadcastReceiver messageSentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ItemService.MESSAGE_SENT)) {
                Long itemID = intent.getLongExtra(ItemService.EXTRA_ITEM_ID, 0);
                // only handle my item's messages
                if (!itemID.equals(item.getId())) return;
                String error = intent.getStringExtra(ItemService.EXTRA_MESSAGE_SENT_ERROR);
                Resources resources = getResources();
                if (error != null) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.error)
                            .setMessage(error)
                            .setPositiveButton(R.string.ok, null)
                            .show();
                } else {
                    Item item = ((GiveOrTakeApplication) getActivity().getApplication()).getItem(itemID);
                    Log.i(TAG, "Number of messages sent (got app) :" + item.getNumMessagesSent() +
                      ", result: " + intent.getIntExtra(ItemService.EXTRA_NUM_MESSAGES_SENT, 0));

                    new AlertDialog.Builder(getActivity())
                            .setMessage(resources.getString(R.string.message_sent))
                            .setPositiveButton(R.string.ok, null)
                            .show();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        Long itemID = null;

        if (getArguments() != null) {
            itemID = (Long)getArguments().getSerializable(FreeItemsFragment.EXTRA_ITEM_ID);
        } else {
            itemID = (Long)getActivity().getIntent()
                    .getSerializableExtra(FreeItemsFragment.EXTRA_ITEM_ID);
        }

        if (itemID == null) return;

        item = ((GiveOrTakeApplication)getActivity().getApplication()).getItem(itemID);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.registerReceiver(userBroadcastReceiver,
                new IntentFilter(UserService.USER_FETCHED));
        localBroadcastManager.registerReceiver(imageBroadcastReceiver,
                new IntentFilter(ItemService.ITEM_IMAGE_FETCHED));
        localBroadcastManager.registerReceiver(messageSentReceiver,
                new IntentFilter(ItemService.MESSAGE_SENT));

        User user = ((GiveOrTakeApplication)getActivity().getApplication()).getUser(item.getUserID());
        if (user != null) {
            owner = user;
        } else {
            Log.i(TAG, "Requesting user " + item.getUserID() + ", owner of item " + item.getName());
            Intent userIntent = new Intent(getActivity(), UserService.class);
            userIntent.setAction(UserService.FETCH_USER);
            userIntent.putExtra(UserService.EXTRA_USER_ID, item.getUserID());
            getActivity().startService(userIntent);
        }

        if (item.getImage(getActivity(), null) == null) {
            Intent imageIntent = new Intent(getActivity(), ItemService.class);
            imageIntent.setAction(ItemService.FETCH_ITEM_IMAGE);
            imageIntent.putExtra(ItemService.EXTRA_ITEM, item);
            getActivity().startService(imageIntent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_free_item_detail, container, false);

        if (item == null) {
            TextView descView = (TextView)v.findViewById(R.id.free_item_detail_desc);
            descView.setText(getResources().getString(R.string.no_item_found));
            return v;
        }

        imageView = (ImageView)v.findViewById(R.id.free_item_detail_image);

        TextView descView = (TextView)v.findViewById(R.id.free_item_detail_desc);
        descView.setText(item.getDescription());

        usernameView = (TextView)v.findViewById(R.id.free_item_detail_username);
        karmaView = (TextView)v.findViewById(R.id.free_item_detail_karma);

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

        wantButton = (Button)v.findViewById(R.id.free_item_detail_want_button);
        wantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessageDialogFragment messageDialogFragment = new MessageDialogFragment(item, owner);
                messageDialogFragment.show(getActivity().getSupportFragmentManager(), item.getId().toString());
            }
        });
        wantButton.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.i(TAG, "Layout changed for want button");
                updateUI();
            }
        });
        if (owner == null) {
            // This should be disabled until we have the owner, so the user can see who
            // they are sending a message to.
            wantButton.setEnabled(false);
        }

        // Fill in any data we already have
        updateUI();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.free_item_detail_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_item_share:
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, item.getName());
                intent.putExtra(Intent.EXTRA_TEXT, item.getUri().toString());
                startActivity(Intent.createChooser(intent, getString(R.string.how_share)));
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void updateUI() {
        if (owner != null && usernameView != null && karmaView != null && wantButton != null) {
            usernameView.setText(owner.getUserName());
            karmaView.setText(owner.getKarma().toString());
            wantButton.setEnabled(true);
        }
        int maxImageDimension = getMaxImageDimension();
        if (maxImageDimension > 0 && item.getImage(getActivity(), maxImageDimension) != null &&
                imageView != null) {
            Log.i(TAG, "Setting height for " + item.getName() + " to " + maxImageDimension);
            imageView.setImageDrawable(item.getImage(getActivity(), maxImageDimension));
            imageView.getParent().requestLayout();
        }
    }

    private int getMaxImageDimension() {
        Display display = ((WindowManager) getActivity().getSystemService(getActivity().WINDOW_SERVICE))
                .getDefaultDisplay();
        int width;
        int height;
        if (Build.VERSION.SDK_INT >= 13) {
            Rect size = new Rect();
            display.getRectSize(size);
            width = size.width();
            height = size.height();
        } else {
            width = display.getWidth();
            height = display.getHeight();
        }
        int maxDimen = Math.min(width, height);
        if (maxDimen == height) {
            Log.i(TAG, "Height is the lesser, removing want button height: " + wantButton.getHeight());
            Log.i(TAG, "Action bar height = " + getActivity().getActionBar().getHeight());

            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            int statusBarHeight = 0;
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            Log.i(TAG, "Status bat height = " + statusBarHeight);
            if (wantButton.getHeight() <= 0 || getActivity().getActionBar().getHeight() <= 0) {
                return 0;
            }
            maxDimen = height - wantButton.getHeight() - getActivity().getActionBar().getHeight() - statusBarHeight;
        } else {
            Log.i(TAG, "Width is the lesser");
        }
        return maxDimen;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.unregisterReceiver(userBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(imageBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(messageSentReceiver);
        item.clearImage();
    }
}
