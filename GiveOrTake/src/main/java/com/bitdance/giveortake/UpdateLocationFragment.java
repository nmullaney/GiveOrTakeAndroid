package com.bitdance.giveortake;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by nora on 7/11/13.
 */
public class UpdateLocationFragment extends Fragment {

    private LocationClient locationClient;
    private GoogleMap map;
    private Marker marker;
    private TextView errorMessageView;

    private boolean isNewUserFlow;

    BroadcastReceiver locationUpdated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UserService.LOCATION_UPDATED)) {
                String errorMessage = intent.getStringExtra(UserService.EXTRA_ERROR);
                if (errorMessage != null) {
                    displayErrorMessage(errorMessage);
                } else {
                    if (isNewUserFlow) {
                        ((WelcomeActivity)getActivity()).loadNextFragment();
                    } else {
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null && getArguments().getBoolean(Constants.NEW_USER)) {
            this.isNewUserFlow = true;
        } else {
            this.isNewUserFlow = false;
        }

        locationClient = new LocationClient(getActivity(), connectionCallbacks,
                onConnectionFailedListener);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.registerReceiver(locationUpdated,
                new IntentFilter(UserService.LOCATION_UPDATED));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_location, container, false);
        Button updateLocation = (Button)view.findViewById(R.id.update_button);
        errorMessageView = (TextView)view.findViewById(R.id.error_message);
        updateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (marker == null) {
                    displayErrorMessage(getString(R.string.error_no_location));
                    return;
                }
                hideErrorMessage();
                Intent intent = new Intent(getActivity(), UserService.class);
                intent.setAction(UserService.UPDATE_LOCATION);
                intent.putExtra(UserService.EXTRA_LATLNG, marker.getPosition());
                getActivity().startService(intent);
            }
        });
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SupportMapFragment mapFragment = ((SupportMapFragment) fm.findFragmentById(R.id.map));
        map = mapFragment.getMap();
        if (map == null) {
            return view;
        }
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (marker == null) {
                    marker = createMarker(latLng);
                } else {
                    marker.setPosition(latLng);
                }
            }
        });

        return view;
    }

    private GiveOrTakeApplication getGOTApplication() {
        return (GiveOrTakeApplication)getActivity().getApplication();
    }

    private void updateMapLocation() {
        ActiveUser activeUser = getGOTApplication().getActiveUser();
        LatLng centerPosition = null;
        if (activeUser != null && activeUser.getLatitude() != null &&
                activeUser.getLongitude() != null) {
            LatLng markerPosition = new LatLng(activeUser.getLatitude(), activeUser.getLongitude());
            centerPosition = markerPosition;
            marker = createMarker(markerPosition);
        } else {
            Location currentLocation = locationClient.getLastLocation();
            if (currentLocation != null) {
                centerPosition = new LatLng(currentLocation.getLatitude(),
                        currentLocation.getLongitude());
            }
        }
        // If there's no reasonable centerPosition, we'll just end up showing a map of the whole
        // world, which will be easier to navigate than a zoomed in map
        if (centerPosition != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(centerPosition, 14);
            map.animateCamera(cameraUpdate);
        }
    }

    private Marker createMarker(LatLng position) {
        return map.addMarker(new MarkerOptions()
                .position(position)
                .title(getString(R.string.my_location)));
    }

    @Override
    public void onStart() {
        super.onStart();
        locationClient.connect();
    }

    @Override
    public void onStop() {
        locationClient.disconnect();
        super.onStop();
    }

    private void displayErrorMessage(String errorMessage) {
        errorMessageView.setText(errorMessage);
        errorMessageView.setVisibility(View.VISIBLE);
    }

    private void hideErrorMessage() {
        errorMessageView.setText(null);
        errorMessageView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                .getInstance(getActivity().getApplicationContext());
        localBroadcastManager.unregisterReceiver(locationUpdated);
    }

    GooglePlayServicesClient.ConnectionCallbacks connectionCallbacks =
            new GooglePlayServicesClient.ConnectionCallbacks() {

                @Override
                public void onConnected(Bundle bundle) {
                    updateMapLocation();
                }

                @Override
                public void onDisconnected() {
                }
            };

    GooglePlayServicesClient.OnConnectionFailedListener onConnectionFailedListener =
            new GooglePlayServicesClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {

                }
            };
}
