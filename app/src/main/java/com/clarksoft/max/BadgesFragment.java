package com.clarksoft.max;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BadgesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BadgesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    BadgesAdapter badgesAdapter;

    private String userUUID;
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

    private Map<String, Integer> badge_key = new HashMap<>();

    private ArrayList<Boolean> badge_enabled = new ArrayList<Boolean>() {
        {
            add(false);
            add(false);
            add(false);
            add(false);
            add(false);
            add(false);
            add(false);
            add(false);
            add(false);
        }
    };
    private ArrayList<Uri> badgeUri = new ArrayList<Uri>() {
        {
            add(Uri.parse("android.resource://com.clarksoft.max/drawable/bee_faded"));
            add(Uri.parse("android.resource://com.clarksoft.max/drawable/star_faded"));
            add(Uri.parse("android.resource://com.clarksoft.max/drawable/rising_star_faded"));
            add(Uri.parse("android.resource://com.clarksoft.max/drawable/medal_faded"));
            add(Uri.parse("android.resource://com.clarksoft.max/drawable/skeleton_faded"));
            add(Uri.parse("android.resource://com.clarksoft.max/drawable/cup_faded"));
            add(Uri.parse("android.resource://com.clarksoft.max/drawable/clap_faded"));
            add(Uri.parse("android.resource://com.clarksoft.max/drawable/leaves_faded"));
            add(Uri.parse("android.resource://com.clarksoft.max/drawable/xmas_faded"));
        }
    };
    private ArrayList<String> badgeTitle = new ArrayList<String>() {
        {
            add("Newbee: Welcome To The Hive");
            add("Exercise Star");
            add("Rising Star");
            add("Exercise Medal");
            add("Move That Body");
            add("Exercise Cup");
            add("I Live For The Applause");
            add("Olympic");
            add("Christmas");
        }
    };
    private ArrayList<String> badgeDescription = new ArrayList<String>() {
        {
            add("Completed your first session.");
            add("Exercised 3 days in a week.");
            add("Exercised every day in a week.");
            add("Exercised for 30min in one session.");
            add("Spent less than 10% of the exercise time in the low HR range.");
            add("Exercised for 60 min in the target HR range in one week.");
            add("Did a whole session in the target HR range (longer than 5 min).");
            add("Did a whole week in the target HR range (sessions longer than 5 min).");
            add("Exercised on the 25th of December.");
        }
    };


    public BadgesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BadgesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BadgesFragment newInstance(String param1, String param2) {
        BadgesFragment fragment = new BadgesFragment();
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
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_badges, container, false);

        badge_key.put("newbee", 0);
        badge_key.put("exercise_star", 1);
        badge_key.put("rising_star", 2);
        badge_key.put("exercise_medal", 3);
        badge_key.put("move_that_body", 4);
        badge_key.put("exercise_cup", 5);
        badge_key.put("i_live_for_the_applause", 6);
        badge_key.put("olympic", 7);
        badge_key.put("christmas", 8);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userUUID = user.getUid();
        }
        recyclerView = view.findViewById(R.id.badge_recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(getContext(), 1);
        badgesAdapter = new BadgesAdapter(badgeUri, badgeTitle, badgeDescription, badge_enabled, getContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(badgesAdapter);

        get_badge();

        return view;
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

                        update_badge();
                    } else {
                        Log.e("DB", "Document does not exist.");
                    }
                } else {
                    Log.e("DB", "", task.getException());
                }
            }
        });
    }

    private void update_badge(){
        if(newbee){
            int index = badge_key.get("newbee");
            badgeUri.set(index, Uri.parse("android.resource://com.clarksoft.max/drawable/bee"));
            badge_enabled.set(index, true);
            badgesAdapter.notifyItemChanged(index);
        }

        if(exercise_star){
            int index = badge_key.get("exercise_star");
            badgeUri.set(index, Uri.parse("android.resource://com.clarksoft.max/drawable/star"));
            badge_enabled.set(index, true);
            badgesAdapter.notifyItemChanged(index);
        }

        if(rising_star){
            int index = badge_key.get("rising_star");
            badgeUri.set(index, Uri.parse("android.resource://com.clarksoft.max/drawable/rising_star"));
            badge_enabled.set(index, true);
            badgesAdapter.notifyItemChanged(index);
        }

        if(exercise_medal){
            int index = badge_key.get("exercise_medal");
            badgeUri.set(index, Uri.parse("android.resource://com.clarksoft.max/drawable/medal"));
            badge_enabled.set(index, true);
            badgesAdapter.notifyItemChanged(index);
        }

        if(move_that_body){
            int index = badge_key.get("move_that_body");
            badgeUri.set(index, Uri.parse("android.resource://com.clarksoft.max/drawable/skeleton"));
            badge_enabled.set(index, true);
            badgesAdapter.notifyItemChanged(index);
        }

        if(exercise_cup){
            int index = badge_key.get("exercise_cup");
            badgeUri.set(index, Uri.parse("android.resource://com.clarksoft.max/drawable/cup"));
            badge_enabled.set(index, true);
            badgesAdapter.notifyItemChanged(index);
        }

        if(i_live_for_the_applause){
            int index = badge_key.get("i_live_for_the_applause");
            badgeUri.set(index, Uri.parse("android.resource://com.clarksoft.max/drawable/clap"));
            badge_enabled.set(index, true);
            badgesAdapter.notifyItemChanged(index);
        }

        if(olympic){
            int index = badge_key.get("olympic");
            badgeUri.set(index, Uri.parse("android.resource://com.clarksoft.max/drawable/leaves"));
            badge_enabled.set(index, true);
            badgesAdapter.notifyItemChanged(index);
        }

        if(christmas){
            int index = badge_key.get("christmas");
            badgeUri.set(index, Uri.parse("android.resource://com.clarksoft.max/drawable/xmas"));
            badge_enabled.set(index, true);
            badgesAdapter.notifyItemChanged(index);
        }
    }
}
