package com.clarksoft.max;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.service.autofill.TextValueSanitizer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EndSessionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EndSessionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView total_exercise, target_exercise, max_hr, min_hr, date;

    public EndSessionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EndSessionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EndSessionFragment newInstance(String param1, String param2) {
        EndSessionFragment fragment = new EndSessionFragment();
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

        View view =  inflater.inflate(R.layout.fragment_end_session, container, false);

        Button btn_back;

        date = view.findViewById(R.id.date);
        total_exercise = view.findViewById(R.id.total_exercise);
        target_exercise = view.findViewById(R.id.time_in_hr);
        max_hr = view.findViewById(R.id.max_hr);
        min_hr = view.findViewById(R.id.min_hr);
        btn_back = view.findViewById(R.id.end_session_back_btn);

        //Retrieve the value
        String total_workout = getArguments().getString("total_workout");
        String target_workout = getArguments().getString("target_workout");
        String max = getArguments().getString("max");
        String min = getArguments().getString("min");

        String date_str = new SimpleDateFormat("dd/MMM/yyyy", Locale.CANADA).format((new Date()));

        date.setText(date_str);
        total_exercise.setText(total_workout);
        target_exercise.setText(target_workout);
        max_hr.setText(max);
        min_hr.setText(min);


        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main_frame, SessionFragment.newInstance(0,"",0,0));
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }
}
