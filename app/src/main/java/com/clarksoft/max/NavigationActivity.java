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
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.Semaphore;

public class NavigationActivity extends AppCompatActivity
        implements InfoFragment.OnFragmentInteractionListener,
        SessionFragment.OnFragmentInteractionListener, DashboardFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        Settings1Fragment.OnFragmentInteractionListener,
        Settings2Fragment.OnFragmentInteractionListener,
        WahooServiceListener {

    private final String LOG_TAG = NavigationActivity.class.getName();
    private BottomNavigationView bottomNavigation;
    private WahooService sensorService = null;

    private int sensorPairing = 0;
    private int sensorPaired = 0;
    private int pairingProgress;
    private String bpm_str = "";
    private int bpm = -1;

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_info:
                            openFragment(InfoFragment.newInstance("", ""));
                            return true;
                        case R.id.navigation_settings:
                            openFragment(SettingsFragment.newInstance("", ""));
                            return true;
                        case R.id.navigation_dashboard:
                            openFragment(DashboardFragment.newInstance(bpm, bpm_str, sensorPaired, pairingProgress));
                            return true;
                        case R.id.navigation_session:
                            openFragment(SessionFragment.newInstance(bpm, bpm_str, sensorPaired, pairingProgress));
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
        WahooService service = WahooService.getInstance();
        service.addListener(this);

        startSensorDiscovery();
    }

    private void startSensorDiscovery() {
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

    @Override
    public void wahooEvent(String str) {

        bpm_str = "";
        bpm = -1;
        try {
            bpm_str = str.split(",")[1].split("/")[0];
            bpm = (int) Float.parseFloat(bpm_str);

            Log.e("TestinG", bpm_str );
            bpm_str = bpm_str.split("\\.")[0];
            Log.e("TestinG", bpm_str );
            bpm_str += " bpm";
            sensorPaired = 1;
        } catch (Exception e) {
            Log.d("Navigation Activity", "wahooEvent: Initializing");
        }

        String temp_str = str.split(" ")[0];

        switch (temp_str) {
            case "onDeviceDiscovered:TICKR":
                sensorPairing = 1;
                sensorPaired = 0;
                pairingProgress = 10;

                Log.e("filterMe", "onDeviceDiscovered :" + pairingProgress);
                break;
            case "onSensorConnectionStateChanged":
                sensorPaired = 0;
                if(sensorPairing == 1){
                    pairingProgress += 10;
                }

                Log.e("filterMe", "onSensorConnectionStateChanged :" + pairingProgress);
                break;
            case "onNewCapabilityDetectedConnection":
                sensorPairing = 1;
                sensorPaired = 0;
                pairingProgress = 30;

                Log.e("filterMe", "onNewCapabilityDetectedConnection :" + pairingProgress);
                break;
            case "registered":
                sensorPaired = 1;
                sensorPairing = 0;
                pairingProgress = 100;

                Log.e("filterMe", "registered HR listener :" + pairingProgress);
                break;
            case "onDiscoveredDeviceLost":
                sensorPaired = 0;
                sensorPairing = 0;
                pairingProgress = 0;

                Log.e("filterMe", "onDiscoveredDeviceLost :" + pairingProgress);
                break;
            default:
        }

        Intent intent = new Intent("sensor_broadcast");
        intent.putExtra("bpm", bpm);
        intent.putExtra("bpm_str", bpm_str);
        intent.putExtra("sensorPaired", sensorPaired);
        intent.putExtra("pairingProgress", pairingProgress);
        sendBroadcast(intent);
    }
}
