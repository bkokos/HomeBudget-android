package com.myapp.homebudget.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.myapp.homebudget.MainActivity;
import com.myapp.homebudget.R;

import org.springframework.web.client.RestTemplate;

import static com.myapp.homebudget.R.string.adduser;
import static com.myapp.homebudget.util.Constans.HOST_NAME;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passwordField;
    private Button loginButton;
    private CheckBox registerCB;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.emailLogin);
        passwordField = findViewById(R.id.passwordLogin);
        loginButton = findViewById(R.id.loginButton);
        registerCB = findViewById(R.id.registerCB);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
            }
        };

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (registerCB.isChecked()) {
                    startRegisterIn();
                } else {
                    startSignIn();
                }
            }
        });
    }

    private void startRegisterIn() {
        final String email = emailField.getText().toString();
        final String password = passwordField.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Pole nie może być puste", Toast.LENGTH_LONG).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        addUser(email);
                        startSignIn();
                    } else {
                        Toast.makeText(LoginActivity.this, "Błąd podczas rejestracji", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void addUser(String email) {
        user = User.of(email);
        RestTemplate restTemplate = new RestTemplate();

        Long id = restTemplate.postForObject(HOST_NAME + adduser, user, Long.class);
    }

    private void startSignIn() {

        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Pole nie może być puste", Toast.LENGTH_LONG).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                    } else {
                        Toast.makeText(LoginActivity.this, "Nieprawidłowy login lub hasło", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }
}
