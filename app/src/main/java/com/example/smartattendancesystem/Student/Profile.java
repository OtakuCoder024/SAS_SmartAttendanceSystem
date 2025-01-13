package com.example.smartattendancesystem.Student;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartattendancesystem.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    EditText nameR, sectionR, numR, dobR, ageR, sexR, emailR;
    TextView nameTxt, emailTxt;
    ImageView profileCircle,prevBtn;
    Button editBtn, uploadBtn;
    private String studentName, studentId, sid, section, profileUrl;
    DatabaseReference studentRef;
    StorageReference storageRef;
    Uri profileImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_profile);

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

        nameR = findViewById(R.id.nameP);
        sectionR = findViewById(R.id.sectionP);
        numR = findViewById(R.id.numP);
        dobR = findViewById(R.id.dobP);
        ageR = findViewById(R.id.ageP);
        sexR = findViewById(R.id.sexP);
        emailR = findViewById(R.id.emailP);

        nameTxt = findViewById(R.id.nameTxt);
        emailTxt = findViewById(R.id.emailTxt);
        profileCircle = findViewById(R.id.profileCircle);
        editBtn = findViewById(R.id.editBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        prevBtn = findViewById(R.id.prevBtn);

        // Get the student data from intent
        studentName = getIntent().getStringExtra("studentname");
        studentId = getIntent().getStringExtra("studentid");
        sid = getIntent().getStringExtra("sid");
        section = getIntent().getStringExtra("section");
        profileUrl = getIntent().getStringExtra("profileUrl");

        // Initialize Firebase references
        studentRef = FirebaseDatabase.getInstance().getReference("students").child(sid);
        storageRef = FirebaseStorage.getInstance().getReference("ProfilePic").child(section).child(studentName);

        // Fetch and display data
        fetchStudentData();

        // Update profile
        editBtn.setOnClickListener(v -> updateStudentProfile());

        // Upload profile picture
        profileCircle.setOnClickListener(v -> selectProfilePicture());
        uploadBtn.setOnClickListener(v -> uploadProfilePicture());

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, Home.class);
                intent.putExtra("studentid", studentId);
                intent.putExtra("studentname", studentName);
                intent.putExtra("sid",sid);
                intent.putExtra("section",section);
                intent.putExtra("profileUrl",profileUrl);
                startActivity(intent);
            }
        });
    }

    private void fetchStudentData() {
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fetchedStudentName = snapshot.child("name").getValue(String.class);
                    String fetchedSection = snapshot.child("section").getValue(String.class);
                    String fetchedDob = snapshot.child("dob").getValue(String.class);
                    String fetchedAge = snapshot.child("age").getValue(String.class);
                    String fetchedSex = snapshot.child("sex").getValue(String.class);
                    String fetchedEmail = snapshot.child("email").getValue(String.class);
                    String fetchedNum = snapshot.child("number").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    nameR.setText(fetchedStudentName != null ? fetchedStudentName : "");
                    sectionR.setText(fetchedSection != null ? fetchedSection : "");
                    dobR.setText(fetchedDob != null ? fetchedDob : "");
                    ageR.setText(fetchedAge != null ? fetchedAge : "");
                    sexR.setText(fetchedSex != null ? fetchedSex : "");
                    numR.setText(fetchedNum != null ? fetchedNum : "");
                    emailR.setText(fetchedEmail != null ? fetchedEmail : "");
                    emailTxt.setText(fetchedEmail);
                    nameTxt.setText(fetchedStudentName);

                    // Load profile picture if available
                    if (profileImageUrl != null) {
                        Glide.with(Profile.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ai)
                                .circleCrop() // Transform the image into a circle
                                .into(profileCircle);
                    }
                } else {
                    Toast.makeText(Profile.this, "Student data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStudentProfile() {
        String updatedName = nameR.getText().toString();
        String updatedSection = sectionR.getText().toString();
        String updatedDob = dobR.getText().toString();
        String updatedAge = ageR.getText().toString();
        String updatedSex = sexR.getText().toString();
        String updatedEmail = emailR.getText().toString();

        studentRef.child("name").setValue(updatedName);
        studentRef.child("section").setValue(updatedSection);
        studentRef.child("dob").setValue(updatedDob);
        studentRef.child("age").setValue(updatedAge);
        studentRef.child("sex").setValue(updatedSex);
        studentRef.child("email").setValue(updatedEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Profile.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Profile.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            profileCircle.setImageURI(profileImageUri);
        }
    }

    private void uploadProfilePicture() {
        if (profileImageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profileImageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = storageRef.putBytes(data);
                uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    studentRef.child("profileImageUrl").setValue(downloadUrl).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Profile.this, "Profile picture updated.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })).addOnFailureListener(e -> Toast.makeText(Profile.this, "Failed to upload picture: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                Toast.makeText(Profile.this, "Error uploading picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Profile.this, Home.class);
        intent.putExtra("studentid", studentId);
        intent.putExtra("studentname", studentName);
        intent.putExtra("sid",sid);
        intent.putExtra("section",section);
        intent.putExtra("profileUrl",profileUrl);
        startActivity(intent);
    }
}
