package com.clarksoft.max;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Switch;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

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

    private MediaPlayer mp_slow;
    private MediaPlayer mp_fast;
    private Boolean sessionRunning = false;
    private ReentrantLock audio_mutex = new ReentrantLock();

    private Boolean newbee = false;
    private Boolean exercise_star = false;
    private Boolean rising_star = false;
    private Boolean exercise_medal = false;
    private Boolean move_that_body = false;
    private Boolean exercise_cup = false;
    private Boolean i_live_for_the_applause = false;
    private Boolean olympic = false;
    private Boolean christmas = false;

    enum mediaPlayers {slow, fast};
    private Boolean sound_enabled = false;

    private final Integer num_days = 30;
    private Float total_in_hr = 0.0f;
    private Integer num_sessions = 0, target_time = 0, oldest_date = 0;
    ImageView session_notification_active;

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

                stopMedia(mediaPlayers.slow);
                stopMedia(mediaPlayers.fast);
            } else if (bpm < min_hr) {
                int base_color = Color.parseColor("#00F1D346");
                card.setCardBackgroundColor(cardColourManager(bpm, min_hr, max_hr, base_color)); //yellow
                motivation_str = getString(R.string.session_str_seed_up);

                stopMedia(mediaPlayers.fast);
                startMedia(mediaPlayers.slow);
            } else if (bpm >= min_hr && bpm < max_hr) {
                int base_color = Color.parseColor("#008BC34A");
                card.setCardBackgroundColor(cardColourManager(bpm, min_hr, max_hr, base_color)); // green
                motivation_str = getString(R.string.session_str_keep_up);

                stopMedia(mediaPlayers.slow);
                stopMedia(mediaPlayers.fast);
            } else if (bpm >= max_hr) {
                int base_color = Color.parseColor("#00EE5819");
                card.setCardBackgroundColor(cardColourManager(bpm, min_hr, max_hr, base_color)); //red
                motivation_str = getString(R.string.session_str_slow_down);

                stopMedia(mediaPlayers.slow);
                startMedia(mediaPlayers.fast);
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

        mp_slow = MediaPlayer.create(getContext(), R.raw.slow);
        mp_slow.setLooping(true);
        mp_fast = MediaPlayer.create(getContext(), R.raw.fast);
        mp_fast.setLooping(true);
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
        fetchSettings2Data();

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

        session_notification_active = view.findViewById(R.id.session_notification_active);
        session_notification_active.setVisibility(View.INVISIBLE);

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


        checkTimeIncreaseSuggestion();


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
                sessionRunning = true;
                break;
            case R.id.session_btn_pause:
                session_btn_pause.setVisibility(View.INVISIBLE);
                session_btn_start.setVisibility(View.VISIBLE);
                session_btn_start.setText(R.string.session_lbl_resume);

                stopMedia(mediaPlayers.slow);
                stopMedia(mediaPlayers.fast);


                timer_base = SystemClock.elapsedRealtime() - session_chronometer.getBase();
                session_chronometer.stop();
                sessionRunning = false;
                break;
            case R.id.session_btn_end:
                session_btn_start.setVisibility(View.VISIBLE);
                session_btn_end.setVisibility(View.INVISIBLE);
                session_btn_pause.setVisibility(View.INVISIBLE);
                session_btn_start.setText(R.string.session_lbl_start);

                stopMedia(mediaPlayers.slow, false);
                stopMedia(mediaPlayers.fast, false);

                // Should only be called after stopping the media players
                sessionRunning = false;

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
        sessionData.put("min", current_min);
        sessionData.put("max", current_max);

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
                            current_min = Integer.MAX_VALUE;
                            current_max = 0;
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
                        target_time = Integer.parseInt(document.get("target_time").toString());
                    } else {
                        Log.e("DB", "Document does not exist.");
                    }
                } else {
                    Log.e("DB", "", task.getException());
                }
            }
        });
    }

    private void fetchSettings2Data() {
        DocumentReference userData = db.collection("settings2").document(userUUID);
        userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        sound_enabled = document.getBoolean("sound");
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
        String date_str = new SimpleDateFormat("dd/MMM/yyyy", Locale.CANADA).format((new Date()));

        Bundle args = new Bundle();
        args.putString("total_workout", String.format("%.2f min", total_exercise));
        args.putString("target_workout", String.format("%.2f min", target_exercise));
        args.putString("max", current_max.toString());
        args.putString("min", current_min.toString());
        args.putString("date", date_str);
        args.putString("initiator", "session");
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

    private void badge_newbee() {
        if (!newbee)
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

        db.collection("badges").document(userUUID).set(badgeData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Badges Updated.");
                    }
                }).addOnFailureListener(this);
    }

    private void stopMedia(mediaPlayers mp) {
        stopMedia(mp, true);
    }

    private void stopMedia(mediaPlayers mp, Boolean recreate) {
        try {
            audio_mutex.lock();
            switch (mp) {
                case slow:

                    if (sound_enabled && sessionRunning && mp_slow.isPlaying()) {
                        mp_slow.stop();
                        mp_slow.release();

                        if (recreate) {
                            mp_slow = MediaPlayer.create(getContext(), R.raw.slow);
                            mp_slow.setLooping(true);
                        }
                    }

                    break;
                case fast:
                    if (sound_enabled && sessionRunning && mp_fast.isPlaying()) {
                        mp_fast.stop();
                        mp_fast.release();

                        if (recreate) {
                            mp_fast = MediaPlayer.create(getContext(), R.raw.fast);
                            mp_fast.setLooping(true);
                        }
                    }

                    break;
            }
        } finally {
            audio_mutex.unlock();
        }
    }

    private void startMedia(mediaPlayers mp) {
        try {
            audio_mutex.lock();
            switch (mp) {
                case slow:
                    if (sound_enabled && sessionRunning && !mp_slow.isPlaying()) {
                        mp_slow.start();
                    }

                    break;
                case fast:
                    if (sound_enabled && sessionRunning && !mp_fast.isPlaying()) {
                        mp_fast.start();
                    }
                    break;
            }
        } finally {
            audio_mutex.unlock();
        }
    }

    private void checkTimeIncreaseSuggestion(){

        Log.e("avg_time", "Here" );

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, num_days * (-1));
        String target_date = String.valueOf(calendar.get(Calendar.YEAR)) +
                String.format("%02d", calendar.get(Calendar.MONTH) + 1) +
                String.format("%02d", calendar.get(Calendar.DATE));

        int current_date = Integer.parseInt(new SimpleDateFormat("yyyyMMdd", Locale.CANADA).format((new Date())));
        int one_month_back = Integer.parseInt(target_date);

        Query userDataQuery = db.collection("session")
                .whereEqualTo("uid", userUUID)
                .orderBy("date")
                .limit(1);
        userDataQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot session_data = task.getResult();

                    for (QueryDocumentSnapshot document : session_data) {
                        oldest_date = Integer.parseInt(document.get("date").toString());
                    }

                    Log.e("avg_time", "oldest: " + oldest_date + " month_back: " + one_month_back);



                    if(oldest_date <= one_month_back) {

                        Query userDataQuery = db.collection("session")
                                .whereEqualTo("uid", userUUID)
                                .orderBy("date")
                                .startAt(one_month_back)
                                .endAt(current_date);
                        userDataQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot session_data = task.getResult();

                                    for (QueryDocumentSnapshot document : session_data) {
                                        num_sessions++;
                                        total_in_hr += Integer.parseInt(document.get("in_hr").toString()) / 60.0f;
                                    }

                                    num_sessions = (num_sessions>4)?num_sessions:4;

                                    Float avg_session_time = total_in_hr / num_sessions;
                                    Log.e("avg_time", avg_session_time.toString() );

                                    DocumentReference userData = db.collection("settings1").document(userUUID);
                                    userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document != null && document.exists()) {
                                                    target_time = Integer.parseInt(document.get("target_time").toString());
                                                } else {
                                                    Log.e("DB", "Document does not exist.");
                                                }
                                            } else {
                                                Log.e("DB", "", task.getException());
                                            }


                                            if (avg_session_time >= target_time && avg_session_time < 15) {
                                                checkExerciseDays("Think about setting your \"Exercise goal\" in the User Settings page to 15 minutes in target HR range per week");
                                            } else if (avg_session_time >= target_time && avg_session_time >= 15 && avg_session_time < 30) {
                                                checkExerciseDays("Think about setting your \"Exercise goal\" in the User Settings page to 30 minutes in target HR range per week");
                                            } else if (avg_session_time >= target_time && avg_session_time >= 30 && avg_session_time < 45) {
                                                checkExerciseDays("Think about setting your \"Exercise goal\" in the User Settings page to 45 minutes in target HR range per week");
                                            } else if (avg_session_time >= target_time && avg_session_time >= 45 && avg_session_time < 60) {
                                                checkExerciseDays("Think about setting your \"Exercise goal\" in the User Settings page to 60 minutes in target HR range per week");
                                            } else if (avg_session_time >= target_time && avg_session_time >= 60 && avg_session_time < 75) {
                                                checkExerciseDays("Think about setting your \"Exercise goal\" in the User Settings page to 75 minutes in target HR range per week");
                                            } else if (avg_session_time >= target_time && avg_session_time >= 75 && avg_session_time < 90) {
                                                checkExerciseDays("Think about setting your \"Exercise goal\" in the User Settings page to 90 minutes in target HR range per week");
                                            }
                                            else{
                                                checkExerciseDays("");
                                            }

                                        }
                                    });

                                } else {
                                    Log.e("DB", "", task.getException());
                                }
                            }
                        });
                    }
                    Log.e("avg_time", "Now here" );
                } else {
                    Log.e("DB", "", task.getException());
                }
            }
        });
    }

    private void checkExerciseDays(String target_suggestion){


        if(!target_suggestion.isEmpty()) {
            session_notification_active.setVisibility(View.VISIBLE);
            session_notification_active.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage(target_suggestion);
                    alertDialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            session_notification_active.setVisibility(View.INVISIBLE);
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }
            });
        }

    }

    private Integer cardColourManager(Integer bpm, Integer min, Integer max, Integer base_colour){

        final Integer range_offset = 10;
        final Integer center = (min + max)/2;
        Integer alpha_constant = 255;
        Integer min_alpha_clip = 100;

        if (bpm < min) {
            alpha_constant = ((255 / range_offset) * (min - bpm)) + min_alpha_clip;
            alpha_constant = alpha_constant > 255 ? 255 : alpha_constant;
        }
        else if (bpm >= min && bpm <= center) {
            alpha_constant = ((255/(center - min)) * (bpm - min)) + min_alpha_clip;
            alpha_constant = alpha_constant > 255 ? 255 : alpha_constant;
        }
        else if (bpm > center && bpm <= max) {
            alpha_constant = ((255/(max-center)) * (max - bpm)) + min_alpha_clip;
            alpha_constant = alpha_constant > 255 ? 255 : alpha_constant;
        }
        else if (bpm > max) {
            alpha_constant = ((255/range_offset) * (bpm - max)) + min_alpha_clip;
            alpha_constant = alpha_constant > 255 ? 255 : alpha_constant;
        }

        return base_colour + (alpha_constant << 24);

    }
}
