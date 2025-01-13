package com.example.smartattendancesystem.Student;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartattendancesystem.Login;
import com.example.smartattendancesystem.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Settings extends AppCompatActivity {

    Button profileBtn, entBtn, inventorsBtn, logoutBtn;
    private String studentName, studentId, sid, section, profileUrl;
    TextView statusTxt;
    ImageView profilePic,prevBtn;
    DatabaseReference studentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_settings);

        // UI fullscreen handling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        profileBtn = findViewById(R.id.profileBtn);
        entBtn = findViewById(R.id.entBtn);
        inventorsBtn = findViewById(R.id.inventorsBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        statusTxt = findViewById(R.id.statusTxt);
        profilePic = findViewById(R.id.profilePic);
        prevBtn = findViewById(R.id.prevBtn);


        // Get data from Intent
        studentName = getIntent().getStringExtra("studentname");
        studentId = getIntent().getStringExtra("studentid");
        sid = getIntent().getStringExtra("sid");
        section = getIntent().getStringExtra("section");
        profileUrl = getIntent().getStringExtra("profileUrl");

        studentRef = FirebaseDatabase.getInstance().getReference("students").child(sid);

        fetchStudentData();

        statusTxt.setText(studentName);


        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this, Home.class);
                intent.putExtra("studentid", studentId);
                intent.putExtra("studentname", studentName);
                intent.putExtra("sid",sid);
                intent.putExtra("section",section);
                intent.putExtra("profileUrl",profileUrl);
                startActivity(intent);
            }
        });

        profileBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Settings.this, Profile.class);
            intent.putExtra("studentid", studentId);
            intent.putExtra("studentname", studentName);
            intent.putExtra("sid", sid);
            intent.putExtra("section", section);
            startActivity(intent);
        });
        entBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Settings.this, Entertainment.class);
            intent.putExtra("studentid", studentId);
            intent.putExtra("studentname", studentName);
            intent.putExtra("sid", sid);
            intent.putExtra("section", section);
            startActivity(intent);
        });
        inventorsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Settings.this, Inventors.class);
            intent.putExtra("studentid", studentId);
            intent.putExtra("studentname", studentName);
            intent.putExtra("sid", sid);
            intent.putExtra("section", section);
            startActivity(intent);
        });

        logoutBtn.setOnClickListener(view -> showLogoutConfirmationDialog());
    }

    private void fetchStudentData() {
        studentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    // Load profile picture if available
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(Settings.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ai)
                                .circleCrop()
                                .into(profilePic);
                    } else {
                        profilePic.setImageResource(R.drawable.ai); // Default image if no URL
                    }
                } else {
                    Toast.makeText(Settings.this, "Student data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Settings.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Settings.this, Home.class);
        intent.putExtra("studentid", studentId);
        intent.putExtra("studentname", studentName);
        intent.putExtra("sid",sid);
        intent.putExtra("section",section);
        intent.putExtra("profileUrl",profileUrl);
        startActivity(intent);
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Exit");
        builder.setMessage("Are you sure you want to Logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            startActivity(new Intent(Settings.this, Login.class));
            finish();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
