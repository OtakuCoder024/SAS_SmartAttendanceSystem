package com.example.smartattendancesystem.Admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartattendancesystem.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminHome extends AppCompatActivity {

    Button qrcode, addSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_home);

        qrcode = findViewById(R.id.button);
        addSection = findViewById(R.id.button2);

        qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminHome.this, ScanQrCode.class));
            }
        });

        addSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddSectionDialog();
            }
        });
    }

    private void showAddSectionDialog() {
        // Inflate custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_section, null);

        // Initialize UI elements from the custom dialog layout
        TextView title = dialogView.findViewById(R.id.dialogTitle);
        EditText inputSection = dialogView.findViewById(R.id.editTextSection);
        Button submitButton = dialogView.findViewById(R.id.submitButton);

        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set up submit button listener
        submitButton.setOnClickListener(view -> {
            String sectionName = inputSection.getText().toString().trim();

            if (TextUtils.isEmpty(sectionName)) {
                Toast.makeText(AdminHome.this, "Please enter a course and section name.", Toast.LENGTH_SHORT).show();
            } else {
                // Save to Firebase Realtime Database
                saveSectionToDatabase(sectionName);
                dialog.dismiss(); // Close the dialog
            }
        });
    }

    private void saveSectionToDatabase(String sectionName) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("CourseSection");

        // Fetch the current count of sections to generate the next incrementing ID
        databaseReference.get().addOnSuccessListener(dataSnapshot -> {
            long count = dataSnapshot.getChildrenCount(); // Get the current count of child nodes
            String sectionId = "Section" + (count + 1); // Increment by 1 for the new ID

            // Save the section to the database
            databaseReference.child(sectionId).setValue(sectionName)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AdminHome.this, "Section added successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AdminHome.this, "Failed to add section: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(AdminHome.this, "Failed to fetch section count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
