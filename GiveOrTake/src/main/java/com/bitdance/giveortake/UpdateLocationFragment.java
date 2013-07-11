package com.bitdance.giveortake;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by nora on 7/11/13.
 */
public class UpdateLocationFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_location, container, false);
        Button centerOnLocation = (Button)view.findViewById(R.id.center_on_current_location);
        Button updateLocation = (Button)view.findViewById(R.id.update_button);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SupportMapFragment mapFragment = ((SupportMapFragment) fm.findFragmentById(R.id.map));

        return view;
    }
}
