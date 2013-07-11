package com.bitdance.giveortake;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


/**
 * Created by nora on 6/19/13.
 */
public class ProfileFragment extends ListFragment {
    public static final String TAG = "ProfileFragment";

    public static final int UPDATE_USERNAME_RESULT = 1;
    public static final int UPDATE_EMAIL_RESULT = 2;

    private LabelFieldStaticListItem usernameItem;
    private LabelFieldStaticListItem emailItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActiveUser activeUser = ActiveUser.getInstance();
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
        items.add(new MapStaticListItem());

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

        StaticListAdapter adapter = new StaticListAdapter(getActivity(), items);
        setListAdapter(adapter);
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
                String username = ActiveUser.getInstance().getUserName();
                usernameItem.getFieldView().setText(username);
                break;
            case UPDATE_EMAIL_RESULT:
                String email = ActiveUser.getInstance().getEmail();
                emailItem.getFieldView().setText(email);
                break;
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

        public boolean isEnabled() {
            return true;
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

            GoogleMap map = mapFragment.getMap();

            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setZoomGesturesEnabled(false);

            ActiveUser activeUser = ActiveUser.getInstance();
            LatLng latLng = new LatLng(0, 0);
            if (activeUser != null) {
                latLng = new LatLng(activeUser.getLatitude(), activeUser.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
                map.animateCamera(cameraUpdate);
            }

            map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("My location"));

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
            ActiveUser activeUser = ActiveUser.getInstance();
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
