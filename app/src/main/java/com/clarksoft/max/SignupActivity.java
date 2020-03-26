package com.clarksoft.max;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignupActivity
        extends AppCompatActivity
        implements View.OnFocusChangeListener,
        OnCompleteListener<AuthResult>,
        OnSuccessListener<Void>,
        OnFailureListener {

    private static final String LOG_TAG = SignupActivity.class.getName();
    private TextInputLayout email;
    private TextInputLayout password;
    private TextInputLayout passwordConf;
    private TextInputLayout age;
    private ProgressBar progressBar;
    private String userUUID;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        email = findViewById(R.id.input_reg_email);
        password = findViewById(R.id.input_reg_password);
        passwordConf = findViewById(R.id.input_reg_conf_password);
        age = findViewById(R.id.input_user_age);
        progressBar = findViewById(R.id.progressBar_cyclic);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerFocusListeners();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        if (hasFocus) {
            switch (v.getId()) {
                case R.id.input_reg_password_txt:
                    setHintForTextInputLayout(password, getString(R.string.signup_input_reg_password_hint_pattern));
                    break;
                case R.id.input_reg_conf_password_txt:
                    setHintForTextInputLayout(passwordConf, getString(R.string.signup_input_reg_conf_password_hint_pattern));
                    break;
                case R.id.input_user_age_txt:
                    setHintForTextInputLayout(age, getString(R.string.signup_age_hint));
                    break;
                default:
                    // Nothing special to do with received View
            }
        } else {
            switch (v.getId()) {
                case R.id.input_reg_email_txt:
                    setHintForTextInputLayout(email, "");
                    validate(email, Patterns.EMAIL_ADDRESS, getString(R.string.signup_err_input_email));
                    break;

                case R.id.input_reg_password_txt:
                    setHintForTextInputLayout(password, "");
                    validate(password, InputValidation.PWD_PATTERN, getString(R.string.signup_err_input_pwd));
                    break;

                case R.id.input_reg_conf_password_txt:
                    InputValidation inputValidation = new InputValidation();
                    Boolean valid;

                    setHintForTextInputLayout(passwordConf, "");
                    EditText passwordConfTxt = passwordConf.getEditText();
                    EditText passwordInputTxt = password.getEditText();

                    if (passwordInputTxt != null && passwordConfTxt != null) {
                        String passwordConfInput = passwordConfTxt.getText().toString();
                        String passwordInput = passwordInputTxt.getText().toString();

                        if (passwordConfInput.length() > 0) {
                            valid = inputValidation.comparePwd(passwordInput, passwordConfInput);
                            inputValidation.updateFieldError(valid, passwordConf, getString(R.string.signup_err_input_pwd_mismatch));
                        }
                    }
                    break;

                case R.id.input_user_age_txt:
                    setHintForTextInputLayout(age, "");
                    validate(age, InputValidation.AGE_PATTERN, getString(R.string.signup_error));
                    break;
                default:
                    // Nothing special to do with received View
            }
        }
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()) {
            // Sign in success, update UI with the signed-in user's information
            Log.d(LOG_TAG, "createUserWithEmail:success");
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                userUUID = user.getUid();
                storeUserData();
            }
        } else {
            Exception e = task.getException();
            Log.w(LOG_TAG, "createUserWithEmail:failure", e);

            if (e instanceof FirebaseAuthUserCollisionException) {
                Toast.makeText(SignupActivity.this, getString(R.string.signup_err_email_already_in_use),
                        Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
            else {
                Toast.makeText(SignupActivity.this, getString(R.string.signup_err_check_internet_conn),
                        Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        }
    }

    @Override
    public void onSuccess(Void aVoid) {
        Log.d(LOG_TAG, "DocumentSnapshot successfully written!");
        updateUI(mAuth.getCurrentUser());
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Log.w(LOG_TAG, "Error writing document", e);
        Toast.makeText(SignupActivity.this, getString(R.string.signup_err_try_again_later),
                Toast.LENGTH_SHORT).show();
        updateUI(null);
    }

    public void setHintForTextInputLayout(@NonNull TextInputLayout field, @NonNull String hint) {
        EditText fieldTxt = field.getEditText();
        if (fieldTxt != null) {
            fieldTxt.setHint(hint);
            //TODO: Change color below
            //fieldTxt.setHintTextColor(Color.parseColor("#FFFFFFFF"));
        }
    }

    private void validate(TextInputLayout inputView, Pattern pattern, String msg) {
        validate(inputView, pattern, msg, false);
    }

    private Boolean validate(TextInputLayout inputView, Pattern pattern, String msg, Boolean alertOnEmptyStr) {
        Boolean valid = false;

        EditText editTextInput = inputView.getEditText();
        if (editTextInput != null) {
            String txtInput = editTextInput.getText().toString();
            editTextInput.setHint("");

            if (txtInput.length() > 0 || alertOnEmptyStr) {
                InputValidation inputValidation = new InputValidation();
                valid = inputValidation.matchPattern(txtInput, pattern);
                inputValidation.updateFieldError(valid, inputView, msg);
            }
        }

        return valid;
    }

    private void safeSetFocusListener(TextInputLayout input, View.OnFocusChangeListener listener) {
        EditText inputTxt = input.getEditText();
        if (inputTxt != null) {
            inputTxt.setOnFocusChangeListener(listener);
        }
    }

    private void registerFocusListeners() {
        safeSetFocusListener(email, this);
        safeSetFocusListener(password, this);
        safeSetFocusListener(passwordConf, this);
        safeSetFocusListener(age, this);
    }

    public void signupClick(View view) {

        InputValidation inputValidation = new InputValidation();
        EditText emailInputTxt = email.getEditText();
        EditText passwordInputTxt = password.getEditText();
        EditText passwordConfInputTxt = passwordConf.getEditText();

        if (emailInputTxt != null && passwordInputTxt != null && passwordConfInputTxt != null) {
            String emailInput = emailInputTxt.getText().toString().trim();
            String passwordConfInput = passwordConfInputTxt.getText().toString();
            String passwordInput = passwordInputTxt.getText().toString();

            Boolean valid = validate(email, Patterns.EMAIL_ADDRESS, getString(R.string.signup_err_input_email), true);
            valid &= validate(password, InputValidation.PWD_PATTERN, getString(R.string.signup_err_input_pwd), true);
            valid &= validate(age, InputValidation.AGE_PATTERN, getString(R.string.signup_err_input_username), true);
            valid &= inputValidation.comparePwd(passwordInput, passwordConfInput);
            inputValidation.updateFieldError(valid, passwordConf, getString(R.string.signup_err_input_pwd_mismatch));

            if (valid) {
                // If all the data is valid, try to create account and store data
                createAccountAndStoreData(emailInput, passwordInput);
            }
        }
    }

    private void storeUserData() {
        EditText ageInputTxt = age.getEditText();

        if (ageInputTxt != null) {
            Integer ageInput = Integer.parseInt(ageInputTxt.getText().toString());

            Integer max_hr = (int) Math.round((206.9 - (0.67 * ageInput)) * 0.75);
            Integer min_hr = (int) Math.round((206.9 - (0.67 * ageInput)) * 0.65);

            Map<String, Object> userData = new HashMap<>();
            userData.put("age", ageInput);
            userData.put("max_hr", max_hr);
            userData.put("min_hr", min_hr);
            userData.put("target_time", 10);

            // Add a new document with a generated ID to Firebase
            db.collection("settings1").document(userUUID).set(userData)
                    .addOnSuccessListener(this).addOnFailureListener(this);
        }
    }

    private void createAccountAndStoreData(String email, String password) {

        progressBar.setVisibility(View.VISIBLE);
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this);
    }

    private void updateUI(FirebaseUser user) {
        progressBar.setVisibility(View.INVISIBLE);
        if (user != null) {
            Intent intent = new Intent(this, NavigationActivity.class);
            startActivity(intent);
        }
    }

}
