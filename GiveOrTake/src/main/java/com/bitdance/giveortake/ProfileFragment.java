package com.bitdance.giveortake;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nora on 6/19/13.
 */
public class ProfileFragment extends ListFragment {

    private MapStaticListItem mapStaticListItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActiveUser activeUser = ActiveUser.getInstance();
        ArrayList<StaticListItem> items = new ArrayList<StaticListItem>();

        items.add(new HeaderStaticListItem(getString(R.string.account)));
        LabelFieldStaticListItem username = new LabelFieldStaticListItem();
        username.setLabel(getString(R.string.username));
        username.setField("Whatever");
        items.add(username);
        LabelFieldStaticListItem email = new LabelFieldStaticListItem();
        email.setLabel(getString(R.string.email));
        email.setField("whatever@whatever.com");
        items.add(email);

        items.add(new HeaderStaticListItem(getString(R.string.location)));
        mapStaticListItem = new MapStaticListItem(savedInstanceState);
        items.add(mapStaticListItem);

        items.add(new HeaderStaticListItem(getString(R.string.karma)));
        items.add(new HeaderStaticListItem(getString(R.string.logout)));
        items.add(new HeaderStaticListItem(getString(R.string.more_information)));

        StaticListAdapter adapter = new StaticListAdapter(getActivity(), items);
        setListAdapter(adapter);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        // This is a hack to make sure the mapview updates properly as it's
        // details come in
        if (menuVisible) {
            int index = 4;
            getListAdapter().getView(index, null, null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        mapStaticListItem.setBundle(savedInstanceState);
        return v;
    }

    private MapView getMapView() {
        if (mapStaticListItem == null) return null;
        return mapStaticListItem.getMapView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getMapView() != null) {
            getMapView().onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getMapView() != null) {
            getMapView().onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getMapView() != null) {
            getMapView().onDestroy();
        }
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

        private Bundle bundle;
        private MapView mapView;
        private View fullView;

        public MapStaticListItem(Bundle savedInstanceStateBundle) {
            this.bundle = savedInstanceStateBundle;
        }

        public void setBundle(Bundle savedInstanceStateBundle) {
            this.bundle = savedInstanceStateBundle;
        }

        public MapView getMapView() {
            return mapView;
        }

        public boolean isEnabled() {
            return true;
        }

        @Override
        public View getView(Context context, View convertView) {
            if (fullView != null) {
                mapView.invalidate();
                return fullView;
            }
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            fullView = layoutInflater.inflate(R.layout.list_map, null);
            mapView = (MapView) fullView.findViewById(R.id.mapview);
            mapView.onCreate(bundle);

            GoogleMap map = mapView.getMap();
            Log.i(TAG, "Map = " + map);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setZoomGesturesEnabled(false);
            mapView.buildDrawingCache(true);

            try {
                Log.i(TAG, "Intializing map");
                MapsInitializer.initialize(context);
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.e(TAG, "Failed to init map:", e);
            }
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
