package com.example.smartattendancesystem.Student;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartattendancesystem.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCode extends AppCompatActivity {

    private ImageView qrCodeImage;
    private String studentName, studentId, sid, section, profileUrl;
    private Button toCamera;
    ImageView prevBtn;
    // Replace with the actual student ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_qrcode);

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

        qrCodeImage = findViewById(R.id.qrCodeContainer);
        //toCamera = findViewById(R.id.scanWithCamera);
        prevBtn = findViewById(R.id.prevBtn);

        Intent intent = getIntent();
        if (intent != null) {
            studentName = getIntent().getStringExtra("studentname");
            studentId = getIntent().getStringExtra("studentid");
            sid = getIntent().getStringExtra("sid");
            section = getIntent().getStringExtra("section");
            profileUrl = getIntent().getStringExtra("profileUrl");
        }

        /*toCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(QRCode.this, ScanQrCode.class));
            }
        });*/

        // Wait until the layout is ready to calculate dimensions
        qrCodeImage.post(() -> {
            int width = qrCodeImage.getWidth();
            int height = qrCodeImage.getHeight();
            generateQRCode(studentId, Math.min(width, height));
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QRCode.this, Home.class);
                intent.putExtra("studentid", studentId);
                intent.putExtra("studentname", studentName);
                intent.putExtra("sid",sid);
                intent.putExtra("section",section);
                intent.putExtra("profileUrl",profileUrl);
                startActivity(intent);
            }
        });
    }

    private void generateQRCode(String studentData, int size) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            Bitmap bitmap = toBitmap(qrCodeWriter.encode(studentData, BarcodeFormat.QR_CODE, size, size));
            qrCodeImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private Bitmap toBitmap(com.google.zxing.common.BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bitmap;
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(QRCode.this, Home.class);
        intent.putExtra("studentid", studentId);
        intent.putExtra("studentname", studentName);
        intent.putExtra("sid",sid);
        intent.putExtra("section",section);
        intent.putExtra("profileUrl",profileUrl);
        startActivity(intent);
    }
}
