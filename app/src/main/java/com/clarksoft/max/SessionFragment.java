package com.clarksoft.max;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SessionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SessionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SessionFragment extends Fragment implements WahooServiceListener {

    private boolean sensorPairing = false;
    private boolean sensorPaired = false;
    private String bpm_str = "";
    private String motivation_str = "";
    private View parentView;
    private WahooService mService;

    private TextView textBpm;
    private TextView textMotivation;
    private CardView card;
    private ProgressBar sensorLoading;

    private Button session_btn_start, session_btn_pause, session_btn_end;
    private int session = 0;
    private BottomNavigationView bottomNavigation;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SessionFragment() {
        // Required empty public constructor
    }

    @Override
    public void wahooEvent(String str) {

        setProgressBar(str);
        if (parentView != null) {
            if (!sensorPairing) {
                bpm_str = getString(R.string.session_str_waiting);
                textMotivation.setVisibility(View.INVISIBLE);
                sensorLoading.setVisibility(View.VISIBLE);
            }


            textBpm.setText(bpm_str);
            textMotivation.setText(motivation_str);

            try {
                bpm_str = str.split(",")[1].split("/")[0];
                int bpm = (int) Float.parseFloat(bpm_str);
                bpm_str += " bpm";



                if (bpm < 70) {
                    card.setCardBackgroundColor(Color.parseColor("#FFF1D346")); //yellow
                    motivation_str = getString(R.string.session_str_seed_up);
                }
                if (bpm >= 70 && bpm < 80) {
                    card.setCardBackgroundColor(Color.parseColor("#FF8BC34A")); // green
                    motivation_str = getString(R.string.session_str_keep_up);
                }
                if (bpm >= 80) {
                    card.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary)); //red
                    motivation_str = getString(R.string.session_str_slow_down);
                }

                textMotivation.setVisibility(View.VISIBLE);
                sensorLoading.setVisibility(View.INVISIBLE);
                textMotivation.setText(motivation_str);

            } catch (Exception e) {
                Log.d(TAG, "wahooEvent: Initializing");
            }

            textBpm.setText(bpm_str);
        }
    }

    private void setProgressBar(String str) {

        String temp_str = str.split(":")[0];

        switch (temp_str) {
            case "onDeviceDiscovered":
                sensorPairing = true;
                sensorLoading.setProgress(10);
                break;
            case "onSensorConnectionStateChanged":
                if(sensorPairing){
                    sensorLoading.incrementProgressBy(10);
                }
                break;
            case "onNewCapabilityDetectedConnection":
                sensorLoading.setProgress(30);
                break;
            case "registered HR listener":
                sensorPaired = true;
                sensorLoading.setProgress(100);
                break;
            case "onDiscoveredDeviceLost":
                sensorPaired = false;
                sensorPairing = false;
                sensorLoading.setProgress(0);
                break;
            default:
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SessionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SessionFragment newInstance(String param1, String param2) {
        SessionFragment fragment = new SessionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        WahooService service = WahooService.getInstance();
        service.addListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_session, container, false);
        // Inflate the layout for this fragment
        parentView = view;
        view.setBackgroundColor(Color.WHITE);

        textBpm = view.findViewById(R.id.session_lbl_bpm);
        textMotivation = view.findViewById(R.id.session_lbl_motivation);
        sensorLoading = view.findViewById(R.id.sensor_loading);
        card = view.findViewById(R.id.session_main_rectangle);

        session_btn_start = view.findViewById(R.id.session_btn_start);
        session_btn_pause = view.findViewById(R.id.session_btn_pause);
        session_btn_end = view.findViewById(R.id.session_btn_end);

        bottomNavigation = getActivity().findViewById(R.id.bottomNavigationView);

        session_btn_pause.setVisibility(View.INVISIBLE);
        session_btn_end.setVisibility(View.INVISIBLE);

        session_btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_reaction(view);
            }
        });

        session_btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_reaction(view);
            }
        });

        session_btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_reaction(view);
            }
        });

        if (!sensorPairing) {
            bpm_str = getString(R.string.session_str_waiting);
            textBpm.setText(bpm_str);
            textMotivation.setVisibility(View.INVISIBLE);
            sensorLoading.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private void button_reaction(View view){
        switch (view.getId()){
            case R.id.session_btn_start:
                if (!sensorPaired){
                    Toast.makeText(getContext(), R.string.session_lbl_still_awaiting, Toast.LENGTH_LONG).show();
                    break;
                }
                session_btn_start.setVisibility(View.INVISIBLE);
                session_btn_pause.setVisibility(View.VISIBLE);
                session_btn_end.setVisibility(View.VISIBLE);
                enableBottomBar(false);
                break;
            case R.id.session_btn_pause:
                session_btn_pause.setVisibility(View.INVISIBLE);
                session_btn_start.setVisibility(View.VISIBLE);
                session_btn_start.setText(R.string.session_lbl_resume);
                break;
            case R.id.session_btn_end:
                session_btn_start.setVisibility(View.VISIBLE);
                session_btn_end.setVisibility(View.INVISIBLE);
                session_btn_pause.setVisibility(View.INVISIBLE);
                session_btn_start.setText(R.string.session_lbl_start);
                enableBottomBar(true);

                break;
            default:
        }
    }

    private void enableBottomBar(boolean enable){
        for(int i = 0; i < bottomNavigation.getMenu().size(); i++){
            bottomNavigation.getMenu().getItem(i).setEnabled(enable);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onSessionFragmentInteraction(uri);
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
        void onSessionFragmentInteraction(Uri uri);
    }
}
