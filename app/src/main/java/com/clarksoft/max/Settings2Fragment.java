package com.clarksoft.max;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Settings2Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Settings2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Settings2Fragment extends Fragment implements OnSuccessListener<Void>, OnFailureListener, OnCompleteListener<DocumentSnapshot> {
    //        implements AdapterView.OnItemSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private String userUUID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Button settings2_btn_save;
    private Switch settings2_reminder, settings2_vibration;
    private Spinner settings2_time_selector;
    private RadioGroup settings2_week_button_group;

    public Settings2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Settings1Fragment.
     */
    // TODO: Rename and change types and number of parameters
    private static Settings2Fragment newInstance(String param1, String param2) {
        Settings2Fragment fragment = new Settings2Fragment();
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
        View view = inflater.inflate(R.layout.fragment_settings2, container, false);

        settings2_time_selector = (Spinner) view.findViewById(R.id.settings2_time_selector);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.settings2_time_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_item);
        // Apply the adapter to the spinner
        settings2_time_selector.setAdapter(adapter);
//        spinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) getContext());

        settings2_btn_save = view.findViewById(R.id.settings2_btn_save);
        settings2_reminder = view.findViewById(R.id.settings2_reminder);
        settings2_vibration = view.findViewById(R.id.settings2_vibration);
        settings2_week_button_group = view.findViewById(R.id.settings2_week_button_group);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userUUID = user.getUid();
        }

        fetchSettings2Data();

        settings2_btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeSettings2Data();
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onSettings2FragmentInteraction(uri);
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

    private void fetchSettings2Data() {
        DocumentReference userData = db.collection("settings2").document(userUUID);
        userData.get().addOnCompleteListener(this);
    }

    private void storeSettings2Data() {
        try {

            int selected_day = 0, selected_time;
            boolean reminder = settings2_reminder.isChecked();
            boolean vibration = settings2_vibration.isChecked();
            switch (settings2_week_button_group.getCheckedRadioButtonId()) {
                case R.id.settings2_radio_mon:
                    selected_day = 1;
                    break;
                case R.id.settings2_radio_tue:
                    selected_day = 2;
                    break;
                case R.id.settings2_radio_wed:
                    selected_day = 3;
                    break;
                case R.id.settings2_radio_thu:
                    selected_day = 4;
                    break;
                case R.id.settings2_radio_fri:
                    selected_day = 5;
                    break;
                case R.id.settings2_radio_sat:
                    selected_day = 6;
                    break;
                case R.id.settings2_radio_sun:
                    selected_day = 7;
                    break;
                default:
                    break;
            }

            selected_time = settings2_time_selector.getSelectedItemPosition();

//            settings2_time_selector.setSelection(3);
//            RadioButton radio = getView().findViewById(R.id.settings2_radio_sun);
//            radio.setChecked(true);


            Map<String, Object> userData = new HashMap<>();
            userData.put("reminder", reminder);
            userData.put("vibration", vibration);
            userData.put("selected_day", selected_day);
            userData.put("selected_time", selected_time);

            db.collection("settings2").document(userUUID).set(userData)
                    .addOnSuccessListener(this).addOnFailureListener(this);

        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.settings1_toast_invalidInput, Toast.LENGTH_LONG).show();
        }
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
        void onSettings2FragmentInteraction(Uri uri);
    }

    @Override
    public void onSuccess(Void aVoid) {
        Toast.makeText(getContext(), R.string.settings2_toast_saved, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Toast.makeText(getContext(), R.string.settings1_toast_failedToSave, Toast.LENGTH_LONG).show();
        Log.e("DB", e.toString());
    }

    @Override
    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document != null && document.exists()) {
                boolean reminder = document.getBoolean("reminder");
                boolean vibration = document.getBoolean("vibration");
                Integer selected_day = Integer.parseInt(document.get("selected_day").toString());
                Integer selected_time = Integer.parseInt(document.get("selected_time").toString());

                settings2_reminder.setChecked(reminder);
                settings2_vibration.setChecked(vibration);
                settings2_time_selector.setSelection(selected_time);

//            RadioButton radio = getView().findViewById(R.id.R.id.settings2_radio_mon);
//            radio.setChecked(true);

                RadioButton radio;

                switch (selected_day) {
                    case 1:
                        radio = getView().findViewById(R.id.settings2_radio_mon);
                        radio.setChecked(true);
                        break;
                    case 2:
                        radio = getView().findViewById(R.id.settings2_radio_tue);
                        radio.setChecked(true);
                        break;
                    case 3:
                        radio = getView().findViewById(R.id.settings2_radio_wed);
                        radio.setChecked(true);
                        break;
                    case 4:
                        radio = getView().findViewById(R.id.settings2_radio_thu);
                        radio.setChecked(true);
                        break;
                    case 5:
                        radio = getView().findViewById(R.id.settings2_radio_fri);
                        radio.setChecked(true);
                        break;
                    case 6:
                        radio = getView().findViewById(R.id.settings2_radio_sat);
                        radio.setChecked(true);
                        break;
                    case 7:
                        radio = getView().findViewById(R.id.settings2_radio_sun);
                        radio.setChecked(true);
                        break;
                    default:
                        break;
                }



            } else {
                Log.e("DB", "Document does not exist.");
            }
        } else {
            Log.e("DB", "", task.getException());
        }
    }
}
