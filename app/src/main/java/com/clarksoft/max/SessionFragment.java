package com.clarksoft.max;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SessionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SessionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SessionFragment extends Fragment implements OnCompleteListener<QuerySnapshot>, OnSuccessListener<DocumentReference>, OnFailureListener {

    private int sensorPaired = 0;
    private int pairingProgress;
    private String bpm_str = "";
    private int bpm;

    private String motivation_str = "";
    private View parentView;
    private WahooService mService;

    private TextView textBpm;
    private TextView textMotivation;
    private CardView card;
    private ProgressBar sensorLoading;

    private Chronometer session_chronometer;
    private long timer_base = 0;
    private boolean running = false;

    private long cmr_offset;
    private int in_hr = 0;
    private int above_hr = 0;
    private int below_hr = 0;

    private Integer min_hr = 0, max_hr = 0;
    private Integer current_min = Integer.MAX_VALUE, current_max = 0;

    private Button session_btn_start, session_btn_pause, session_btn_end;
    private int session = 0;
    private BottomNavigationView bottomNavigation;

    private String userUUID, db_key = null;
    private int date;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Boolean newbee = false;
    private Boolean exercise_star = false;
    private Boolean rising_star = false;
    private Boolean exercise_medal = false;
    private Boolean move_that_body = false;
    private Boolean exercise_cup = false;
    private Boolean i_live_for_the_applause = false;
    private Boolean olympic = false;
    private Boolean christmas = false;

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
        bpm = bpm_data;
        pairingProgress = pairingProgress_data;
        sensorPaired = sensorPaired_data;
        if (sensorPaired == 0 || str.contains("--")) {
            bpm_str = getString(R.string.session_str_waiting);
            textMotivation.setVisibility(View.INVISIBLE);
            sensorLoading.setVisibility(View.VISIBLE);
            card.setCardBackgroundColor(Color.parseColor("#FF999999")); // green
        } else {
            bpm_str = str;
        }

        sensorLoading.setProgress(pairingProgress);

        textBpm.setText(bpm_str);
        textMotivation.setText(motivation_str);


        if (bpm > -1) {

            current_max = Math.max(current_max, bpm);
            current_min = Math.min(current_min, bpm);

            if (min_hr == 0 && max_hr == 0) {
                card.setCardBackgroundColor(Color.parseColor("#FF999999")); // grey
                motivation_str = getString((R.string.session_lbl_fetchingData));
            } else if (bpm < min_hr) {
                card.setCardBackgroundColor(Color.parseColor("#FFF1D346")); //yellow
                motivation_str = getString(R.string.session_str_seed_up);
            } else if (bpm >= min_hr && bpm < max_hr) {
                card.setCardBackgroundColor(Color.parseColor("#FF8BC34A")); // green
                motivation_str = getString(R.string.session_str_keep_up);
            } else if (bpm >= max_hr) {
                card.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary)); //red
                motivation_str = getString(R.string.session_str_slow_down);
            }

            textMotivation.setVisibility(View.VISIBLE);
            sensorLoading.setVisibility(View.INVISIBLE);
            textMotivation.setText(motivation_str);

            textBpm.setText(bpm_str);
        } else {
            card.setCardBackgroundColor(Color.parseColor("#FF999999")); // grey
        }
    }


    public static SessionFragment newInstance(int bpm, String bpm_str, int sensorPaired, int pairingProgress) {
        SessionFragment fragment = new SessionFragment();
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

        View view = inflater.inflate(R.layout.fragment_session, container, false);
        // Inflate the layout for this fragment
        parentView = view;
        view.setBackgroundColor(Color.WHITE);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userUUID = user.getUid();
        }
        fetchSettings1Data();

        textBpm = view.findViewById(R.id.session_lbl_bpm);
        textMotivation = view.findViewById(R.id.session_lbl_motivation);
        sensorLoading = view.findViewById(R.id.sensor_loading);
        card = view.findViewById(R.id.session_main_rectangle);

        session_btn_start = view.findViewById(R.id.session_btn_start);
        session_btn_pause = view.findViewById(R.id.session_btn_pause);
        session_btn_end = view.findViewById(R.id.session_btn_end);

        bottomNavigation = getActivity().findViewById(R.id.bottomNavigationView);

        session_chronometer = view.findViewById(R.id.session_chronometer);
        session_chronometer.setBase(SystemClock.elapsedRealtime());

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

        if (sensorPaired == 0) {
            bpm_str = getString(R.string.session_str_waiting);
            textBpm.setText(bpm_str);
            textMotivation.setVisibility(View.INVISIBLE);
            sensorLoading.setVisibility(View.VISIBLE);
            sensorLoading.setProgress(pairingProgress);
        }

        IntentFilter filter = new IntentFilter("sensor_broadcast");
        getActivity().registerReceiver(receiverUpdateDownload, filter);


        session_chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                if (cmr_offset >= 0) {
                    if (bpm < min_hr) {
                        below_hr += 1;
                    } else if (bpm >= min_hr && bpm < max_hr) {
                        in_hr += 1;
                    } else if (bpm >= max_hr) {
                        above_hr += 1;
                    }

                    long total_time = below_hr + in_hr + above_hr;
                } else
                    cmr_offset += 1;
            }
        });

        return view;
    }

    private void button_reaction(View view) {

        switch (view.getId()) {
            case R.id.session_btn_start:
                if (sensorPaired == 0) {
                    Toast.makeText(getContext(), R.string.session_lbl_still_awaiting, Toast.LENGTH_LONG).show();
                    break;
                }
                session_btn_start.setVisibility(View.INVISIBLE);
                session_btn_pause.setVisibility(View.VISIBLE);
                session_btn_end.setVisibility(View.VISIBLE);
                enableBottomBar(false);
                cmr_offset = -2;
                session_chronometer.setBase(SystemClock.elapsedRealtime() - timer_base);
                session_chronometer.start();
                date = Integer.parseInt(new SimpleDateFormat("yyyyMMdd", Locale.CANADA).format((new Date())));
                break;
            case R.id.session_btn_pause:
                session_btn_pause.setVisibility(View.INVISIBLE);
                session_btn_start.setVisibility(View.VISIBLE);
                session_btn_start.setText(R.string.session_lbl_resume);

                timer_base = SystemClock.elapsedRealtime() - session_chronometer.getBase();
                session_chronometer.stop();
                break;
            case R.id.session_btn_end:
                session_btn_start.setVisibility(View.VISIBLE);
                session_btn_end.setVisibility(View.INVISIBLE);
                session_btn_pause.setVisibility(View.INVISIBLE);
                session_btn_start.setText(R.string.session_lbl_start);
                enableBottomBar(true);

                session_chronometer.setBase(SystemClock.elapsedRealtime());
                session_chronometer.stop();
                timer_base = 0;

                manageSessionData();
                set_badges();

                openFragment(EndSessionFragment.newInstance("", ""));

                // store the data

                break;
            default:
        }
    }

    private void enableBottomBar(boolean enable) {
        for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
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

    private void manageSessionData() {

        Query userDataQuery = db.collection("session").whereEqualTo("uid", userUUID).whereEqualTo("date", date);
        userDataQuery.get().addOnCompleteListener(this);
    }

    @Override
    public void onComplete(@NonNull Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            QuerySnapshot session_data = task.getResult();
            for (QueryDocumentSnapshot document : session_data) {
                Integer db_below_hr = Integer.parseInt(document.get("below_hr").toString());
                Integer db_in_hr = Integer.parseInt(document.get("in_hr").toString());
                Integer db_above_hr = Integer.parseInt(document.get("above_hr").toString());

                db_key = document.getId();
                below_hr += db_below_hr;
                in_hr += db_in_hr;
                above_hr += db_above_hr;
            }

            storeSessionData();

        } else {
            Log.e("DB", "", task.getException());
        }
    }

    private void storeSessionData() {

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("below_hr", below_hr);
        sessionData.put("in_hr", in_hr);
        sessionData.put("above_hr", above_hr);
        sessionData.put("uid", userUUID);
        sessionData.put("date", date);

        if (db_key == null) {

            db.collection("session").add(sessionData)
                    .addOnSuccessListener(this)
                    .addOnFailureListener(this);
        } else {
            db.collection("session").document(db_key).set(sessionData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db_key = null;
                            below_hr = 0;
                            in_hr = 0;
                            above_hr = 0;
                        }
                    }).addOnFailureListener(this);
        }
    }

    @Override
    public void onSuccess(DocumentReference documentReference) {
        db_key = null;
        below_hr = 0;
        in_hr = 0;
        above_hr = 0;
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Toast.makeText(getContext(), R.string.settings1_toast_failedToSave, Toast.LENGTH_LONG).show();
        Log.e("DB", e.toString());
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
                    } else {
                        Log.e("DB", "Document does not exist.");
                    }
                } else {
                    Log.e("DB", "", task.getException());
                }
            }
        });
    }

    private void openFragment(Fragment fragment) {

        Float total_exercise = (in_hr + above_hr + below_hr) / 60.0f;
        Float target_exercise = (in_hr) / 60.0f;

        Bundle args = new Bundle();
        args.putString("total_workout", String.format("%.2f min", total_exercise));
        args.putString("target_workout", String.format("%.2f min", target_exercise));
        args.putString("max", current_max.toString());
        args.putString("min", current_min.toString());
        fragment.setArguments(args);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void set_badges() {
        get_badge();
        badge_newbee();
        update_badge();
    }

    private void badge_newbee(){
        if(!newbee)
            newbee = true;
    }

    private void get_badge() {
        DocumentReference BadgeData = db.collection("badges")
                .document(userUUID);
        BadgeData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        newbee = document.getBoolean("newbee");
                        exercise_star = document.getBoolean("exercise_star");
                        rising_star = document.getBoolean("rising_star");
                        exercise_medal = document.getBoolean("exercise_medal");
                        move_that_body = document.getBoolean("move_that_body");
                        exercise_cup = document.getBoolean("exercise_cup");
                        i_live_for_the_applause = document.getBoolean("i_live_for_the_applause");
                        olympic = document.getBoolean("olympic");
                        christmas = document.getBoolean("christmas");
                    } else {
                        Log.e("DB", "Document does not exist.");
                        update_badge();
                    }
                } else {
                    Log.e("DB", "", task.getException());
                }
            }
        });
    }

    private void update_badge() {
        Map<String, Object> badgeData = new HashMap<>();
        badgeData.put("newbee", newbee);
        badgeData.put("exercise_star", exercise_star);
        badgeData.put("rising_star", rising_star);
        badgeData.put("exercise_medal", exercise_medal);
        badgeData.put("move_that_body", move_that_body);
        badgeData.put("exercise_cup", exercise_cup);
        badgeData.put("i_live_for_the_applause", i_live_for_the_applause);
        badgeData.put("olympic", olympic);
        badgeData.put("christmas", christmas);

        db.collection("session").document(userUUID).set(badgeData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Badges Updated.");
                    }
                }).addOnFailureListener(this);
    }
}
