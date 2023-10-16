package com.example.project;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    TextView gotoSignup;
    Button logIn;
    EditText email, password;

    boolean valid = true;

    private ProgressBar progressBar;

    FirebaseAuth fAuth;
    FirebaseFirestore fstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        //Firebase
        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        //EditText
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);

        //Button
        logIn = findViewById(R.id.login_button);

        //TextView
        gotoSignup = findViewById(R.id.signupRedirectText);

        // Initialize the ProgressBar
        progressBar = findViewById(R.id.login_progress_bar);

        gotoSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Register.class));
                finish();
            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                valid = true;  // Reset the valid flag

                boolean isEmailValid = checkEmailField(email);
                boolean isPasswordValid = checkPasswordField(password);

                if (isEmailValid && isPasswordValid) {
                    loginUser(email.getText().toString().trim(), password.getText().toString().trim());
                } else {
                    valid = false;  // Set valid to false if validation failed
                    Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Validate email format
    private boolean isValidEmail(CharSequence target) {
        return Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    // Validate the email field
    private boolean checkEmailField(EditText emailField) {
        String email = emailField.getText().toString().trim();
        if (email.isEmpty()) {
            emailField.setError("Please enter your email");
            return false;
        } else if (!isValidEmail(email)) {
            emailField.setError("Please enter a valid email address");
            return false;
        }
        return true;
    }

    // Validate the password field
    private boolean checkPasswordField(EditText passwordField) {
        String password = passwordField.getText().toString().trim();
        if (password.isEmpty()) {
            passwordField.setError("Please enter your password");
            return false;
        } else if (password.length() < 6) {
            passwordField.setError("Password should be at least 6 characters");
            return false;
        }
        return true;
    }

    // Sign in with email and password
    private void loginUser(String email, String password) {
        // Show the progress bar
        progressBar.setVisibility(View.VISIBLE);

        fAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Hide the progress bar
                        progressBar.setVisibility(View.GONE);

                        startActivity(new Intent(getApplicationContext(), HomeDashboard.class));
                        Toast.makeText(Login.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Hide the progress bar
                        progressBar.setVisibility(View.GONE);

                        if (valid) {
                            Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}