package com.clarksoft.max;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationActivity extends AppCompatActivity
        implements InfoFragment.OnFragmentInteractionListener,
        SessionFragment.OnFragmentInteractionListener, DashboardFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        Settings1Fragment.OnFragmentInteractionListener,
        Settings2Fragment.OnFragmentInteractionListener{

    private final String LOG_TAG = NavigationActivity.class.getName();
    private BottomNavigationView bottomNavigation;
    private WahooService sensorService = null;

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_info:
                            openFragment(InfoFragment.newInstance("", ""));
                            return true;
                        case R.id.navigation_settings:
                            openFragment(SettingsFragment.newInstance("", ""));
                            return true;
                        case R.id.navigation_dashboard:
                            openFragment(DashboardFragment.newInstance("", ""));
                            return true;
                        case R.id.navigation_session:
                            openFragment(SessionFragment.newInstance("", ""));
                            return true;
                        case R.id.navigation_log:
                            //openFragment(NotificationFragment.newInstance("", ""));
                            return true;
                    }
                    return false;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigation);

        bottomNavigation = findViewById(R.id.bottomNavigationView);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        openFragment(InfoFragment.newInstance("", ""));

        startSensorDiscovery();
    }

    private void startSensorDiscovery(){
        if (sensorService == null && WahooService.getInstance() != null) {
            sensorService = WahooService.getInstance();
            sensorService.startDiscovery(); // This is the earliest we can possibly start the discovery
        }
    }

    public void openFragment(Fragment fragment) {
        startSensorDiscovery(); // Plan B in case the earliest discovery happened before the Wahoo service was ready
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof InfoFragment) {
            InfoFragment infoFragment = (InfoFragment) fragment;
            infoFragment.setOnFragmentInteractionListener(this);
        }
    }

    @Override
    public void onInfoFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSessionFragmentInteraction(Uri uri) {

    }

    @Override
    public void onDashboardFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSettingsFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSettings1FragmentInteraction(Uri uri) {

    }

    @Override
    public void onSettings2FragmentInteraction(Uri uri) {

    }
}
