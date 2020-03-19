package com.clarksoft.max;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;


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
            add("Exercised for 60 min in the target HR range range in one week.");
            add("Did a whole session in the target HR range (longer than 5 min).");
            add("Did a whole week in the target HR range (sessions longer than 5 min).");
            add("Exercise on the 25th of December.");
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_badges, container, false);

        recyclerView = view.findViewById(R.id.badge_recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(getContext(), 1);
        badgesAdapter = new BadgesAdapter(badgeUri, badgeTitle, badgeDescription);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(badgesAdapter);


        return view;
    }
}
