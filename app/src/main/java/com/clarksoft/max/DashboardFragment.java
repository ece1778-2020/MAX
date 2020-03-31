package com.clarksoft.max;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


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

    private TextView dashboard_hr, dashboard_min_hr, dashboard_max_hr;
    private TextView dashboard_total_exercise, dashboard_target_exercise, dashboard_days;
    private ProgressBar sensorLoading;
    private int sensorPaired = 0, pairingProgress, bpm;
    private String bpm_str = "-- bpm";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private String userUUID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Integer min_hr = 0, max_hr = 0;

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
        } else {
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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userUUID = user.getUid();
        }

        dashboard_hr = view.findViewById(R.id.dashboard_HR);
        sensorLoading = view.findViewById(R.id.dashboard_sensor_loading);

        dashboard_max_hr = view.findViewById(R.id.dashboard_max_hr);
        dashboard_min_hr = view.findViewById(R.id.dashboard_min_hr);

        dashboard_total_exercise = view.findViewById(R.id.dashboard_total_exercise);
        dashboard_target_exercise = view.findViewById(R.id.dashboard_target_exercise);
        dashboard_days = view.findViewById(R.id.dashboard_days);

        fetchSettings1Data();
        fetchSessionData();

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

    private void fetchSettings1Data() {
        DocumentReference userData = db.collection("settings1").document(userUUID);
        userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        max_hr = Integer.parseInt(document.get("max_hr").toString());
                        min_hr = Integer.parseInt(document.get("min_hr").toString());

                        dashboard_max_hr.setText(max_hr.toString() + " bpm");
                        dashboard_min_hr.setText(min_hr.toString() + " bpm");
                    } else {
                        Log.e("DB", "Document does not exist.");
                    }
                } else {
                    Log.e("DB", "", task.getException());
                }
            }
        });
    }

    private void fetchSessionData() {

        int day_in_week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, (day_in_week * (-1)));
        String target_date = String.valueOf(calendar.get(Calendar.YEAR)) +
                String.format("%02d", calendar.get(Calendar.MONTH) + 1) +
                String.format("%02d", calendar.get(Calendar.DATE));

        int current_date = Integer.parseInt(new SimpleDateFormat("yyyyMMdd", Locale.CANADA).format((new Date())));
        int first_day_of_week = Integer.parseInt(target_date);


        Query userDataQuery = db.collection("session").whereEqualTo("uid", userUUID)
                .orderBy("date").startAt(first_day_of_week).endAt(current_date);
        userDataQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot session_data = task.getResult();
                    int total_days = 0;
                    float total_exercise = 0.0f;
                    float target_exercise = 0.0f;
                    for (QueryDocumentSnapshot document : session_data) {
                        total_days += 1;
                        int db_below_hr = Integer.parseInt(document.get("below_hr").toString());
                        int db_in_hr = Integer.parseInt(document.get("in_hr").toString());
                        int db_above_hr = Integer.parseInt(document.get("above_hr").toString());

                        total_exercise += (db_below_hr + db_in_hr + db_above_hr);
                        target_exercise += db_in_hr;
                    }
                    total_exercise /= 60;
                    target_exercise /= 60;
                    dashboard_days.setText(String.valueOf(total_days) + " days");
                    dashboard_total_exercise.setText(String.format("%.1f min", total_exercise));
                    dashboard_target_exercise.setText(String.format("%.1f min", target_exercise));

                } else {
                    Log.e("DB", "", task.getException());
                }
            }
        });
    }
}
