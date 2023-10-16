package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    EditText name, email, password, conpassword, phone;
    Button signBtn;
    TextView goLogIn;
    RadioGroup radioGroupGender;
    RadioButton radioMale, radioFemale;

    private ProgressBar progressBar;

    FirebaseAuth fAuth;
    FirebaseFirestore fstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        // Edit Text
        name = findViewById(R.id.signup_name);
        email = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);
        conpassword = findViewById(R.id.confirm_password);
        phone = findViewById(R.id.signup_phone);

        // Radio Buttons
        radioGroupGender = findViewById(R.id.radio_group_gender);
        radioMale = findViewById(R.id.radio_male);
        radioFemale = findViewById(R.id.radio_female);

        progressBar = findViewById(R.id.signup_progress_bar);


        // Button/TextView
        signBtn = findViewById(R.id.signup_button);
        goLogIn = findViewById(R.id.loginRedirectText);

        // Firebase
        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        signBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });

        goLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });
    }

    private void createAccount() {
        name.setError(null);
        email.setError(null);
        password.setError(null);
        conpassword.setError(null);
        phone.setError(null);

        String theName = name.getText().toString().trim();
        String theEmail = email.getText().toString().trim();
        String thePassword = password.getText().toString().trim();
        String theConfirmPassword = conpassword.getText().toString().trim();
        String thePhoneNumber = phone.getText().toString().trim();
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
        String gender = selectedGenderRadioButton != null ? selectedGenderRadioButton.getText().toString() : "";

        if (theName.isEmpty()) {
            name.setError("Please enter your name");
            name.requestFocus();
            return;
        }

        if (theEmail.isEmpty() || !isValidEmail(theEmail)) {
            email.setError("Please enter a valid email address");
            email.requestFocus();
            return;
        }

        // Password validation
        if (thePassword.isEmpty()) {
            password.setError("Please enter a password");
            password.requestFocus();
            return;
        }

        if (!thePassword.equals(theConfirmPassword)) {
            conpassword.setError("Passwords do not match");
            conpassword.requestFocus();
            return;
        }

        // Phone number validation
        if (thePhoneNumber.isEmpty() || !isValidPhoneNumber(thePhoneNumber)) {
            phone.setError("Please enter a valid phone number");
            phone.requestFocus();
            return;
        }

        if (selectedGenderId == -1) {
            // Set an error message next to the radio group
            ((TextView) findViewById(R.id.gender_error)).setText("Please select your gender");
            return;
        } else {
            ((TextView) findViewById(R.id.gender_error)).setText("");  // Clear any previous error message
        }

        progressBar.setVisibility(View.VISIBLE);

        fAuth.createUserWithEmailAndPassword(theEmail, thePassword)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = fAuth.getCurrentUser();
                        Toast.makeText(Register.this, "Account Created!", Toast.LENGTH_SHORT).show();

                        DocumentReference df = fstore.collection("Users").document(user.getUid());
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("fullName", name.getText().toString());
                        userInfo.put("email", email.getText().toString());
                        userInfo.put("phoneNumber", thePhoneNumber);
                        userInfo.put("gender", gender);

                        df.set(userInfo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        progressBar.setVisibility(View.GONE); // Hide the progress bar once registration is successful

                                        // Start the Login activity after successful registration
                                        startActivity(new Intent(getApplicationContext(), Login.class));
                                        finish();  // Finish the current activity to prevent going back to it

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        progressBar.setVisibility(View.GONE); // Hide the progress bar if registration fails

                                        Toast.makeText(Register.this, "Failed to create account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE); // Hide the progress bar if registration fails
                        Toast.makeText(Register.this, "Failed to create account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidEmail(String email) {
        // This is a simple email validation. You may want to implement a more comprehensive validation.
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // This is a simple phone number validation. You may want to implement a more comprehensive validation.
        return phoneNumber.matches("09\\d{9}"); // Assumes phone numbers should be 10 digits long
    }
}
