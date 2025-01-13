package com.example.smartattendancesystem;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartattendancesystem.HelperClass.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class OTP extends AppCompatActivity {
    private EditText emailTxt, otpTxt;
    private Button sendBtn, verifyBtn;
    private TextView countdownTimer;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String generatedOTP;
    private static final long COUNTDOWN_TIME = 60000; // 60 seconds
    private CountDownTimer timer;
    private boolean canResendOTP = true;
    private String email, password, id, name, section, number, dob, age, sex; // Changed address to section

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_otp);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI elements
        emailTxt = findViewById(R.id.emailTxt);
        otpTxt = findViewById(R.id.otpTxt);
        sendBtn = findViewById(R.id.sendBtn);
        verifyBtn = findViewById(R.id.verifyBtns);
        countdownTimer = findViewById(R.id.countdownTimer);

        // Get data from Register class
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        id = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");
        section = getIntent().getStringExtra("section"); // Changed from address
        number = getIntent().getStringExtra("number");
        dob = getIntent().getStringExtra("dob");
        age = getIntent().getStringExtra("age");
        sex = getIntent().getStringExtra("sex");

        emailTxt.setText(email);

        // Set click listeners
        sendBtn.setOnClickListener(v -> {
            if (canResendOTP) {
                sendOTP();
            } else {
                Toast.makeText(OTP.this, "Please wait before requesting new OTP", Toast.LENGTH_SHORT).show();
            }
        });

        verifyBtn.setOnClickListener(v -> verifyOTP());

        // Send OTP automatically when activity starts
        sendOTP();
    }

    private void sendOTP() {
        if (email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        generatedOTP = generateOTP();

        String sanitizedEmail = sanitizeEmail(email);

        // Store OTP temporarily in Firebase Realtime Database
        mDatabase.child("otpVerification").child(sanitizedEmail).child("otp").setValue(generatedOTP);
        mDatabase.child("otpVerification").child(sanitizedEmail).child("timestamp").setValue(System.currentTimeMillis());

        // Send email using EmailSender
        try {
            new com.example.smartattendancesystem.HelperClass.EmailSender().execute(email, generatedOTP);
            Toast.makeText(this, "OTP sent to your email", Toast.LENGTH_SHORT).show();
            startCountdownTimer();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    private void startCountdownTimer() {
        canResendOTP = false;
        sendBtn.setEnabled(false);

        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                countdownTimer.setText(String.format("00:%02d", seconds));
                sendBtn.setText("Wait");
            }

            @Override
            public void onFinish() {
                canResendOTP = true;
                sendBtn.setEnabled(true);
                sendBtn.setText("Resend OTP");
                countdownTimer.setText("00:00");
            }
        }.start();
    }

    private void verifyOTP() {
        String enteredOTP = otpTxt.getText().toString().trim();

        if (enteredOTP.isEmpty()) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        String sanitizedEmail = sanitizeEmail(email);

        mDatabase.child("otpVerification").child(sanitizedEmail).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String storedOTP = task.getResult().child("otp").getValue(String.class);
                        Long timestamp = task.getResult().child("timestamp").getValue(Long.class);

                        if (storedOTP == null || timestamp == null) {
                            Toast.makeText(OTP.this, "Failed to retrieve OTP. Please resend.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        long currentTime = System.currentTimeMillis();
                        if (currentTime - timestamp <= 300000) { // 5 minutes in milliseconds
                            if (enteredOTP.equals(storedOTP)) {
                                registerUser();
                            } else {
                                Toast.makeText(OTP.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(OTP.this, "OTP expired. Please request new OTP", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(OTP.this, "Failed to verify OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        // Query to get the total number of students
        mDatabase.child("students").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                long nextId = task.getResult().getChildrenCount() + 1; // Increment the count

                // Register user with Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(authTask -> {
                            if (authTask.isSuccessful()) {
                                // Save user data in Firebase Realtime Database
                                User user = new User(id, name, email, section, number, dob, age, sex, true); // Changed address to section
                                mDatabase.child("students").child(String.valueOf(nextId)).setValue(user)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(OTP.this, "Registration and verification successful", Toast.LENGTH_SHORT).show();

                                                // Clean up OTP data
                                                mDatabase.child("otpVerification").child(sanitizeEmail(email)).removeValue();

                                                // Redirect to main activity
                                                startActivity(new Intent(OTP.this, Login.class));
                                                finish();
                                            } else {
                                                Toast.makeText(OTP.this, "Failed to save user data in database", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(OTP.this, "Failed to register user: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(OTP.this, "Failed to fetch student count: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String sanitizeEmail(String email) {
        return email.replace(".", "_");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}