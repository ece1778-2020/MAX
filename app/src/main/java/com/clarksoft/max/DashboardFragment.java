package com.clarksoft.max;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DashboardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextView dashboard_hr;
    private ProgressBar sensorLoading;
    private int sensorPaired = 0, pairingProgress, bpm;
    private String bpm_str = "-- bpm";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DashboardFragment() {
        // Required empty public constructor
    }

    BroadcastReceiver receiverUpdateDownload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String bpm_str = intent.getStringExtra("bpm_str");
            int bpm = intent.getIntExtra("bpm", 0);
            sensorPaired = intent.getIntExtra("sensorPaired", 0);
            int pairingProgress = intent.getIntExtra("pairingProgress", 0);

            sensorHandler(bpm_str, bpm, sensorPaired, pairingProgress);
        }
    };

    public void sensorHandler(String str, int bpm_data, int sensorPaired_data, int pairingProgress_data) {

        pairingProgress = pairingProgress_data;
        sensorPaired = sensorPaired_data;
        bpm = bpm_data;
        if (sensorPaired == 0) {
            bpm_str = "-- bpm";
            sensorLoading.setVisibility(View.VISIBLE);
        }
        else{
            bpm_str = str;
        }

        sensorLoading.setProgress(pairingProgress);

        dashboard_hr.setText(bpm_str);

        if (bpm > -1) {
            sensorLoading.setVisibility(View.INVISIBLE);
            dashboard_hr.setText(bpm_str);
        }
    }

    public static DashboardFragment newInstance(int bpm, String bpm_str, int sensorPaired, int pairingProgress) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putInt("bpm", bpm);
        args.putString("bpm_str", bpm_str);
        args.putInt("sensorPaired", sensorPaired);
        args.putInt("pairingProgress", pairingProgress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bpm = getArguments().getInt("bpm");
            bpm_str = getArguments().getString("bpm_str");
            sensorPaired = getArguments().getInt("sensorPaired");
            pairingProgress = getArguments().getInt("pairingProgress");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        dashboard_hr = view.findViewById(R.id.dashboard_HR);
        sensorLoading = view.findViewById(R.id.dashboard_sensor_loading);

        if (sensorPaired == 0) {
            bpm_str = "-- bpm";
            dashboard_hr.setText(bpm_str);
            sensorLoading.setVisibility(View.VISIBLE);
            sensorLoading.setProgress(pairingProgress);
        }

        IntentFilter filter = new IntentFilter("sensor_broadcast");
        getActivity().registerReceiver(receiverUpdateDownload, filter);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onDashboardFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onDashboardFragmentInteraction(Uri uri);
    }
}
