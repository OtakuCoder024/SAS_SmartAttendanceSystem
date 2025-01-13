package com.example.smartattendancesystem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartattendancesystem.HelperClass.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;

public class Register extends AppCompatActivity {

    private EditText nameR, numR, dobR, ageR, sexR, emailR, passR, conpassR, idR;
    private Spinner sectionSpinner;
    private Button signupButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private boolean isOtpVerified = false;
    private ArrayList<String> sectionsList;
    private ArrayAdapter<String> sectionAdapter;
    TextView toLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("students");

        idR = findViewById(R.id.idR);
        nameR = findViewById(R.id.nameR);
        sectionSpinner = findViewById(R.id.sectionR);
        numR = findViewById(R.id.numR);
        dobR = findViewById(R.id.dobR);
        ageR = findViewById(R.id.ageR);
        sexR = findViewById(R.id.sexR);
        emailR = findViewById(R.id.emailR);
        passR = findViewById(R.id.passR);
        conpassR = findViewById(R.id.conpassR);
        signupButton = findViewById(R.id.signupButton);
        toLogin = findViewById(R.id.toLogin);

        sectionsList = new ArrayList<>();
        sectionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sectionsList);
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionSpinner.setAdapter(sectionAdapter);

        loadSectionsFromDatabase();

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        toLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });

        dobR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    private void loadSectionsFromDatabase() {
        DatabaseReference sectionRef = FirebaseDatabase.getInstance().getReference("CourseSection");
        sectionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sectionsList.clear();
                for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                    String section = sectionSnapshot.getValue(String.class);
                    sectionsList.add(section);
                }
                // Use custom layout for the spinner
                sectionAdapter = new ArrayAdapter<>(Register.this, R.layout.spinner_new, sectionsList);
                sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sectionSpinner.setAdapter(sectionAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Register.this, "Failed to load sections", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser() {
        String id = idR.getText().toString().trim();
        String name = nameR.getText().toString().trim();
        String section = sectionSpinner.getSelectedItem() != null ? sectionSpinner.getSelectedItem().toString() : "";
        String number = numR.getText().toString().trim();
        String dob = dobR.getText().toString().trim();
        String age = ageR.getText().toString().trim();
        String sex = sexR.getText().toString().trim();
        String email = emailR.getText().toString().trim();
        String password = passR.getText().toString().trim();
        String confirmPassword = conpassR.getText().toString().trim();

        if (id.length() != 9) {
            Toast.makeText(Register.this, "Student ID must be exactly 9 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (number.length() != 11) {
            Toast.makeText(Register.this, "Phone number must be exactly 11 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@gmail.com")) {
            Toast.makeText(Register.this, "Email must be a valid @gmail.com address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6 || !password.matches(".*\\d.*")) {
            Toast.makeText(Register.this, "Password must be at least 6 characters and contain at least one number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty() || section.isEmpty() || number.isEmpty() || dob.isEmpty() ||
                age.isEmpty() || sex.isEmpty() || email.isEmpty() || id.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(Register.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start OTP Activity before creating user in Firebase Auth
        Intent intent = new Intent(Register.this, OTP.class);
        intent.putExtra("id", id);
        intent.putExtra("email", email);
        intent.putExtra("name", name);
        intent.putExtra("password", password);
        intent.putExtra("section", section);
        intent.putExtra("number", number);
        intent.putExtra("dob", dob);
        intent.putExtra("age", age);
        intent.putExtra("sex", sex);
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            isOtpVerified = true;

            // Retrieve user details again after OTP verification
            String id = idR.getText().toString().trim();
            String name = nameR.getText().toString().trim();
            String section = sectionSpinner.getSelectedItem() != null ? sectionSpinner.getSelectedItem().toString() : "";
            String number = numR.getText().toString().trim();
            String dob = dobR.getText().toString().trim();
            String age = ageR.getText().toString().trim();
            String sex = sexR.getText().toString().trim();
            String email = emailR.getText().toString().trim();
            String password = passR.getText().toString().trim();

            // Proceed with Firebase Auth and database write only after OTP is verified
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                generateAndSaveUserId(id, name, section, number, dob, age, sex, email);
                            } else {
                                Toast.makeText(Register.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "OTP Verification failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateAndSaveUserId(String id, String name, String section, String number, String dob, String age, String sex, String email) {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long userId = snapshot.getChildrenCount() + 1;
                saveUserToDatabase(String.valueOf(userId), id, name, section, number, dob, age, sex, email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Register.this, "Failed to generate user ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToDatabase(String userId, String id, String name, String section, String number, String dob, String age, String sex, String email) {
        User user = new User(id, name, section, number, dob, age, sex, email);

        mDatabase.child(userId).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Register.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(Register.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        String formattedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        dobR.setText(formattedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
}
