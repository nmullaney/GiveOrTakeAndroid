package com.bitdance.giveortake;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by nora on 6/19/13.
 */
public class ProfileFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ActiveUser activeUser = ActiveUser.getInstance();
        TextView usernameView = (TextView)v.findViewById(R.id.profile_username);
        usernameView.setText(activeUser.getUserName());
        TextView emailView = (TextView)v.findViewById(R.id.profile_email);
        emailView.setText(activeUser.getEmail());
        return v;
    }

}
