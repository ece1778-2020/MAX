package com.clarksoft.max;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
 * {@link Settings1Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Settings1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Settings1Fragment extends Fragment implements OnSuccessListener<Void>, OnFailureListener, OnCompleteListener<DocumentSnapshot> {
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

    private EditText settings1_input_age, settings1_input_max_hr, settings1_input_min_hr, settings1_input_time;
    private Button settings1_btn_save;

    public Settings1Fragment() {
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
    public static Settings1Fragment newInstance(String param1, String param2) {
        Settings1Fragment fragment = new Settings1Fragment();
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
        View view = inflater.inflate(R.layout.fragment_settings1, container, false);

        settings1_input_age = view.findViewById(R.id.settings1_input_age);
        settings1_input_max_hr = view.findViewById(R.id.settings1_input_max_hr);
        settings1_input_min_hr = view.findViewById(R.id.settings1_input_min_hr);
        settings1_input_time = view.findViewById(R.id.settings1_input_time);
        settings1_btn_save = view.findViewById(R.id.settings1_btn_save);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userUUID = user.getUid();
        }

        fetchSettings1Data();

        settings1_btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeSettings1Data();
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onSettings1FragmentInteraction(uri);
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

    @Override
    public void onSuccess(Void aVoid) {
        Toast.makeText(getContext(), R.string.settings1_toast_saved, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Toast.makeText(getContext(), R.string.settings1_toast_failedToSave, Toast.LENGTH_LONG).show();
        Log.e("DB", e.toString());
    }

    @Override
    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if(task.isSuccessful()){
            DocumentSnapshot document = task.getResult();
            if(document != null && document.exists()){
                String age = document.get("age").toString();
                String max_hr = document.get("max_hr").toString();
                String min_hr = document.get("min_hr").toString();
                String target_time = document.get("target_time").toString();

                if(!age.isEmpty())
                    settings1_input_age.setText(age);
                if(!max_hr.isEmpty())
                    settings1_input_max_hr.setText(max_hr);
                if(!min_hr.isEmpty())
                    settings1_input_min_hr.setText(min_hr);
                if(!target_time.isEmpty())
                    settings1_input_time.setText(target_time);

            }
            else {
                Log.e("DB", "Document does not exist.");
            }
        }
        else{
            Log.e("DB", "", task.getException());
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
        void onSettings1FragmentInteraction(Uri uri);
    }

    private void storeSettings1Data() {

        try {
            Integer age = Integer.parseInt(settings1_input_age.getText().toString().trim());
            Integer max_hr = Integer.parseInt(settings1_input_max_hr.getText().toString().trim());
            Integer min_hr = Integer.parseInt(settings1_input_min_hr.getText().toString().trim());
            Integer target_time = Integer.parseInt(settings1_input_time.getText().toString().trim());

            if((max_hr <= min_hr) || (max_hr > 150) || (min_hr < 60)) {
                Toast.makeText(getContext(), "Invalid minimum or maximum HR range! Fix and try again.", Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("age", age);
            userData.put("max_hr", max_hr);
            userData.put("min_hr", min_hr);
            userData.put("target_time", target_time);

            db.collection("settings1").document(userUUID).set(userData)
                    .addOnSuccessListener(this).addOnFailureListener(this);

        }
        catch (Exception e){
            Toast.makeText(getContext(), R.string.settings1_toast_invalidInput, Toast.LENGTH_LONG).show();
        }
    }

    private void fetchSettings1Data() {
        DocumentReference userData = db.collection("settings1").document(userUUID);
        userData.get().addOnCompleteListener(this);
    }
}
