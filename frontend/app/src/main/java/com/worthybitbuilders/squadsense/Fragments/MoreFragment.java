package com.worthybitbuilders.squadsense.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.worthybitbuilders.squadsense.activities.LogInActivity;
import com.worthybitbuilders.squadsense.activities.page_inbox;
import com.worthybitbuilders.squadsense.activities.page_myteam;
import com.worthybitbuilders.squadsense.activities.page_notification_setting;
import com.worthybitbuilders.squadsense.activities.page_profile;
import com.worthybitbuilders.squadsense.activities.page_search_everywhere;
import com.worthybitbuilders.squadsense.R;
import com.worthybitbuilders.squadsense.utils.SwitchActivity;

public class MoreFragment extends Fragment {
    LinearLayout btnNotificationSetting = null;
    LinearLayout btnInbox = null;
    LinearLayout btnMyteam = null;

    LinearLayout btnSearchEverywhere = null;
    LinearLayout Profile = null;
    TextView btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_more, container, false);

        //init variables here
        btnNotificationSetting = (LinearLayout) v.findViewById(R.id.btn_notification_settings);
        btnInbox = (LinearLayout) v.findViewById(R.id.btn_inbox);
        btnMyteam = (LinearLayout) v.findViewById(R.id.btn_myteam);
        btnSearchEverywhere = (LinearLayout) v.findViewById(R.id.btn_search_everywhere);
        Profile = (LinearLayout) v.findViewById(R.id.profile);
        btnLogout = (TextView) v.findViewById(R.id.btn_logout);

        //set onclick of buttons here
        btnNotificationSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnNotificationSetting_showActivity();
            }
        });

        btnInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnInbox_showActivity();
            }
        });

        btnMyteam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnMyteam_showActivity();
            }
        });

        btnSearchEverywhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSearchEverywhere_showActivity();
            }
        });
        Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnProfile_showActivity();
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SwitchActivity.switchToActivity(getContext(), LogInActivity.class);
                getActivity().finish();
            }
        });
        return v;
    }
// define function here
    private void btnSearchEverywhere_showActivity() {
        SwitchActivity.switchToActivity(getContext(), page_search_everywhere.class);
    }

    private void btnProfile_showActivity() {
        SwitchActivity.switchToActivity(getContext(), page_profile.class);
    }

    private void btnMyteam_showActivity() {
        SwitchActivity.switchToActivity(getContext(), page_myteam.class);
    }

    private void btnInbox_showActivity() {
        SwitchActivity.switchToActivity(getContext(), page_inbox.class);
    }

    private void btnNotificationSetting_showActivity() {
        SwitchActivity.switchToActivity(getContext(), page_notification_setting.class);
    }

}