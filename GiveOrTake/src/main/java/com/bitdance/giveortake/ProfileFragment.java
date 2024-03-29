package com.bitdance.giveortake;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


/**
 * The ProfileFragment displays information about the ActiveUser, as
 * well as general data about the app.
 */
public class ProfileFragment extends ListFragment {
    public static final String TAG = "ProfileFragment";

    public static final int UPDATE_USERNAME_RESULT = 1;
    public static final int UPDATE_EMAIL_RESULT = 2;
    public static final int UPDATE_LOCATION_RESULT = 3;

    private LabelFieldStaticListItem usernameItem;
    private LabelFieldStaticListItem emailItem;
    private MapStaticListItem mapItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        ActiveUser activeUser = getActiveUser();
        if (activeUser == null) {
            return;
        }
        ArrayList<StaticListItem> items = new ArrayList<StaticListItem>();

        items.add(new HeaderStaticListItem(getString(R.string.account)));
        usernameItem = new LabelFieldStaticListItem();
        usernameItem.setLabel(getString(R.string.username));
        usernameItem.setField(activeUser.getUserName());
        Intent usernameIntent = new Intent(getActivity(), UpdateUsernameActivity.class);
        usernameItem.setIntentAndResult(usernameIntent, UPDATE_USERNAME_RESULT);
        items.add(usernameItem);

        emailItem = new LabelFieldStaticListItem();
        emailItem.setLabel(getString(R.string.email));
        emailItem.setField(activeUser.getEmail());
        Intent emailIntent = new Intent(getActivity(), UpdateEmailActivity.class);
        emailItem.setIntentAndResult(emailIntent, UPDATE_EMAIL_RESULT);
        items.add(emailItem);

        items.add(new HeaderStaticListItem(getString(R.string.location)));
        mapItem = new MapStaticListItem();
        Intent locationIntent = new Intent(getActivity(), UpdateLocationActivity.class);
        mapItem.setIntentAndResult(locationIntent, UPDATE_LOCATION_RESULT);
        items.add(mapItem);

        items.add(new HeaderStaticListItem(getString(R.string.karma)));
        items.add(new KarmaStaticListItem());

        items.add(new HeaderStaticListItem(getString(R.string.logout)));
        ButtonListItem logoutButtonListItem = new ButtonListItem();
        logoutButtonListItem.setText(getString(R.string.logout));
        logoutButtonListItem.setBackgroundColor(Color.RED);
        logoutButtonListItem.setTextColor(Color.WHITE);
        logoutButtonListItem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent logoutIntent = new Intent(getActivity(), LoginActivity.class);
                logoutIntent.putExtra(LoginFragment.EXTRA_LOGIN_ACTION, LoginFragment.LOGOUT);
                startActivity(logoutIntent);
                getActivity().finish();
            }
        });
        items.add(logoutButtonListItem);

        items.add(new HeaderStaticListItem(getString(R.string.more_information)));
        ButtonListItem aboutButtonListItem = new ButtonListItem();
        aboutButtonListItem.setText(getString(R.string.about));
        aboutButtonListItem.setTextColor(Color.BLUE);
        aboutButtonListItem.setBackgroundColor(Color.LTGRAY);
        aboutButtonListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openAboutPage = new Intent(Intent.ACTION_VIEW);
                openAboutPage.setData(Uri.parse(Constants.ABOUT_URL));
                startActivity(openAboutPage);
            }
        });
        items.add(aboutButtonListItem);

        ButtonListItem legalNoticesButtonListItem = new ButtonListItem();
        legalNoticesButtonListItem.setText(getString(R.string.legal_notices));
        legalNoticesButtonListItem.setTextColor(Color.BLUE);
        legalNoticesButtonListItem.setBackgroundColor(Color.LTGRAY);
        legalNoticesButtonListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Showing legal notices");
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.legal_notices))
                        .setMessage(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getActivity()))
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
        });
        items.add(legalNoticesButtonListItem);

        StaticListAdapter adapter = new StaticListAdapter(getActivity(), items);
        setListAdapter(adapter);
    }

    private ActiveUser getActiveUser() {
        return ((GiveOrTakeApplication) getActivity().getApplication()).getActiveUser();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        StaticListItem item = ((StaticListAdapter) getListAdapter()).getItem(position);
        item.handleOnClick();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case UPDATE_USERNAME_RESULT:
                String username = getActiveUser().getUserName();
                usernameItem.getFieldView().setText(username);
                break;
            case UPDATE_EMAIL_RESULT:
                String email = getActiveUser().getEmail();
                emailItem.getFieldView().setText(email);
                break;
            case UPDATE_LOCATION_RESULT:
                mapItem.updateLocation();
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        return v;
    }

    public class HeaderStaticListItem extends StaticListItem {

        private String title;

        public HeaderStaticListItem(String title) {
            this.title = title;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public View getView(Context context, View convertView) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_header_item, null);
            TextView titleView = (TextView)convertView.findViewById(R.id.header_title);
            titleView.setText(title);
            return convertView;
        }
    }

    public class LabelFieldStaticListItem extends StaticListItem {

        private String label;
        private String field;

        private TextView fieldView;
        private Intent intent;
        private int intentResult;

        public void setLabel(String label) {
            this.label = label;
        }

        public void setField(String field) {
            this.field = field;
        }

        public void setIntentAndResult(Intent intent, int intentResult) {
            this.intent = intent;
            this.intentResult = intentResult;
        }

        public TextView getFieldView() {
            return fieldView;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void handleOnClick() {
            startActivityForResult(intent, intentResult);
        }

        @Override
        public View getView(Context context, View convertView) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_label_field, null);
            TextView labelView = (TextView) convertView.findViewById(R.id.label_field_label);
            labelView.setText(label);
            fieldView = (TextView)convertView.findViewById(R.id.label_field_field);
            fieldView.setText(field);
            return convertView;
        }
    }

    public class MapStaticListItem extends  StaticListItem {
        public static final String TAG = "MapStaticListItem";

        private View fullView;
        private Intent intent;
        private int intentResult;
        private GoogleMap map;
        private Marker marker;

        public void setIntentAndResult(Intent intent, int intentResult) {
            this.intent = intent;
            this.intentResult = intentResult;
        }

        public void updateLocation() {
            ActiveUser activeUser = getActiveUser();
            if (activeUser != null && activeUser.getLatitude() != null &&
                    activeUser.getLongitude() != null) {
                LatLng latLng = new LatLng(activeUser.getLatitude(), activeUser.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
                map.animateCamera(cameraUpdate);
                if (marker == null) {
                    marker = createMarker(latLng);
                } else {
                    marker.setPosition(latLng);
                }
            }
        }

        public boolean isEnabled() {
            return true;
        }

        @Override
        public void handleOnClick() {
            startActivityForResult(intent, intentResult);
        }

        private Marker createMarker(LatLng position) {
            return map.addMarker(new MarkerOptions()
                    .position(position)
                    .title(getString(R.string.my_location)));
        }

        @Override
        public View getView(Context context, View convertView) {
            if (fullView != null) {
                return fullView;
            }
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            fullView = layoutInflater.inflate(R.layout.list_map, null);
            FragmentManager fm = getActivity().getSupportFragmentManager();
            SupportMapFragment mapFragment = ((SupportMapFragment) fm.findFragmentById(R.id.map));
            mapFragment.setRetainInstance(true);

            map = mapFragment.getMap();
            if (map == null) {
                return fullView;
            }
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    handleOnClick();
                }
            });

            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setAllGesturesEnabled(false);

            updateLocation();

            return fullView;
        }
    }

    public class KarmaStaticListItem extends StaticListItem {

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public View getView(Context context, View convertView) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_karma, null);
            TextView karmaView = (TextView)convertView.findViewById(R.id.karma);
            ActiveUser activeUser = getActiveUser();
            karmaView.setText(String.valueOf(activeUser.getKarma()));
            return convertView;
        }
    }

    public class ButtonListItem extends StaticListItem {

        private String text;
        private int textColor;
        private int backgroundColor;
        private View.OnClickListener onClickListener;

        public void setText(String text) {
            this.text = text;
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        public void setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public View getView(Context context, View convertView) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_button, null);
            Button button = (Button)convertView.findViewById(R.id.list_button);
            button.setText(text);
            button.setTextColor(textColor);
            button.setBackgroundColor(backgroundColor);
            button.setOnClickListener(onClickListener);
            return convertView;
        }
    }

}
