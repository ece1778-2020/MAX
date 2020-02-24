package com.clarksoft.max;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements OnCompleteListener<AuthResult> {

    private static final String LOG_TAG = LoginActivity.class.getName();

    private TextInputLayout email;
    private TextInputLayout password;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);

        mAuth = FirebaseAuth.getInstance();
    }

    public void onButtonClick(View view) {
        Intent intent;

        switch (view.getId()){
            case R.id.login_btn_login:

                EditText emailEdt = email.getEditText();
                EditText passwordEdt = password.getEditText();

                // If null happens, something critical happened and
                // it is best to wait for the user to try again
                if (emailEdt != null && passwordEdt != null) {
                    String emailInput = emailEdt.getText().toString();
                    String passwordInput = passwordEdt.getText().toString();

                    if (emailInput.isEmpty() || passwordInput.isEmpty()) {
                        updateUI(null);
                        return;
                    }

                    mAuth.signInWithEmailAndPassword(emailInput, passwordInput).addOnCompleteListener(this);
                }

                intent = new Intent(this, NavigationActivity.class);
                startActivity(intent);
                break;
            case R.id.login_btn_sign_up:
                intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(this, NavigationActivity.class);
            startActivity(intent);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.login_err_login_failed), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onComplete(@NonNull Task task) {
        if (task.isSuccessful()) {
            // Sign in success, update UI with the signed-in user's information
            Log.d(LOG_TAG, "signInWithEmail:success");
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
        } else {
            // If sign in fails, display a message to the user.
            Log.w(LOG_TAG, "signInWithEmail:failure", task.getException());
            updateUI(null);
        }
    }
}
