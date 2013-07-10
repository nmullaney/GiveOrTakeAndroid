package com.bitdance.giveortake;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActiveUser activeUser = ActiveUser.getInstance();
        ArrayList<StaticListItem> items = new ArrayList<StaticListItem>();

        items.add(new HeaderStaticListItem(getString(R.string.account)));
        LabelFieldStaticListItem username = new LabelFieldStaticListItem();
        username.setLabel(getString(R.string.username));
        username.setField(activeUser.getUserName());
        items.add(username);
        LabelFieldStaticListItem email = new LabelFieldStaticListItem();
        email.setLabel(getString(R.string.email));
        email.setField(activeUser.getEmail());
        items.add(email);

        items.add(new HeaderStaticListItem(getString(R.string.location)));
        items.add(new MapStaticListItem());

        items.add(new HeaderStaticListItem(getString(R.string.karma)));
        items.add(new HeaderStaticListItem(getString(R.string.logout)));
        items.add(new HeaderStaticListItem(getString(R.string.more_information)));

        StaticListAdapter adapter = new StaticListAdapter(getActivity(), items);
        setListAdapter(adapter);
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

        public void setLabel(String label) {
            this.label = label;
        }

        public void setField(String field) {
            this.field = field;
        }

        public TextView getFieldView() {
            return fieldView;
        }

        @Override
        public boolean isEnabled() {
            return true;
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

}
