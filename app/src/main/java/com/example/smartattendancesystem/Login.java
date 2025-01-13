package com.example.smartattendancesystem;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartattendancesystem.Admin.AdminHome;
import com.example.smartattendancesystem.Student.Home;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "admin";

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView toRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        // Link UI elements
        emailEditText = findViewById(R.id.emailTxt);
        passwordEditText = findViewById(R.id.otpTxt);
        loginButton = findViewById(R.id.verifyButton);
        toRegister = findViewById(R.id.toRegister);

        // Set up login button click listener
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                if (email.equals(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
                    // Admin login
                    Log.d(TAG, "Admin login successful");
                    Intent intent = new Intent(Login.this, AdminHome.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Firebase user login
                    loginUser(email, password);
                }
            }
        });

        // Navigate to registration activity
        toRegister.setOnClickListener(v -> startActivity(new Intent(Login.this, Register.class)));
    }

    private void loginUser(String email, String password) {
        Log.d(TAG, "Attempting to login with email: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Authentication successful. User ID: " + user.getUid());
                            checkStudentDirectory(email);
                        }
                    } else {
                        Log.e(TAG, "Authentication failed", task.getException());
                        Toast.makeText(Login.this, "Authentication failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkStudentDirectory(String email) {
        Log.d(TAG, "Checking student directory for email: " + email);

        mDatabase.child("students")
                .orderByChild("email")
                .equalTo(email)
                .get()
                .addOnSuccessListener(studentSnapshot -> {
                    Log.d(TAG, "Student query completed. Exists: " + studentSnapshot.exists());

                    if (studentSnapshot.exists()) {
                        for (DataSnapshot child : studentSnapshot.getChildren()) {
                            String studentName = child.child("name").getValue(String.class);
                            String id = child.child("id").getValue(String.class);
                            String section = child.child("section").getValue(String.class);
                            String profileUrl = child.child("profileImageUrl").getValue(String.class);
                            String sid = child.getKey();
                            Log.d(TAG, "Found student: " + studentName);

                            Intent intent = new Intent(Login.this, Home.class);
                            intent.putExtra("studentname", studentName);
                            intent.putExtra("studentid", id);
                            intent.putExtra("sid", sid);
                            intent.putExtra("section", section);
                            if (profileUrl != null) {
                                intent.putExtra("profileUrl", profileUrl);
                            } else {
                                intent.putExtra("profileUrl", "");
                            }
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Log.d(TAG, "User not found in students database");
                        Toast.makeText(Login.this,
                                "User not found in database",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check student data", e);
                    Toast.makeText(Login.this,
                            "Failed to check student data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
