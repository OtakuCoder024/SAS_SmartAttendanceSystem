package com.example.smartattendancesystem.Admin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smartattendancesystem.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanQrCode extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_scanqrcode);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("attendance");

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        checkCameraPermission();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // Show rationale to the user
                Toast.makeText(this, "Camera permission is needed to scan QR codes.", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        String scannedData = rawResult.getText();
        markAttendance(scannedData);

        // Restart scanning after a brief delay
        scannerView.postDelayed(() -> scannerView.resumeCameraPreview(ScanQrCode.this), 2000);
    }

    private void markAttendance(String studentId) {
        // Get current date in YYYY-MM-DD format
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Save attendance under the classId (BSIT3A)
        databaseReference.child(currentDate)
                .child("BSIT3A")
                .child(studentId)
                .setValue(true)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Attendance marked for " + studentId, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to mark attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void startCamera() {
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scannerView != null) {
            startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scannerView != null) {
            scannerView.stopCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
