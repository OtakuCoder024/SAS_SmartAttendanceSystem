package com.example.smartattendancesystem.Student;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartattendancesystem.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Home extends AppCompatActivity {

    private Button qrBtn, attendanceBtn, scheduleBtn, settingsBtn;
    private TextView title, dateTxt, scheduleTxt, statusTxt,sectionTxt;

    private String studentName, studentId,sid,section,profileUrl;
    private final Handler timeHandler = new Handler();
    private Runnable timeUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_home);

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

        title = findViewById(R.id.title);
        qrBtn = findViewById(R.id.qrBtn);
        dateTxt = findViewById(R.id.dateTxt);
        scheduleTxt = findViewById(R.id.scheduleTxt);
        statusTxt = findViewById(R.id.statusTxt);
        attendanceBtn = findViewById(R.id.attendanceBtn);
        scheduleBtn = findViewById(R.id.scheduleBtn);
        settingsBtn = findViewById(R.id.settingsBtn);

        // Get the student name from intent
        studentName = getIntent().getStringExtra("studentname");
        studentId = getIntent().getStringExtra("studentid");
        sid = getIntent().getStringExtra("sid");
        section = getIntent().getStringExtra("section");
        profileUrl = getIntent().getStringExtra("profileUrl");

        // Trim to the first word of the name
        String firstName = studentName != null && studentName.contains(" ")
                ? studentName.split(" ")[0]
                : studentName;

        // Determine the time of day
        String greeting = getGreeting();

        // Set the greeting message
        title.setText(String.format("%s, %s", greeting, firstName));

        // Set the initial current date and time
        updateDateTime();

        // Schedule periodic updates for the time
        startPeriodicTimeUpdates();
        checkAndDisplayAttendance();


        statusTxt.setText("Section: "+section);

        // Add click listener for QR Button
        qrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, QRCode.class);
                intent.putExtra("studentid", studentId);
                intent.putExtra("studentname", studentName);
                intent.putExtra("sid",sid);
                intent.putExtra("section",section);
                intent.putExtra("profileUrl",profileUrl);
                startActivity(intent);
            }
        });

        attendanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, AttendanceSheet.class);
                intent.putExtra("studentid", studentId);
                intent.putExtra("studentname", studentName);
                intent.putExtra("sid",sid);
                intent.putExtra("section",section);
                intent.putExtra("profileUrl",profileUrl);
                startActivity(intent);
            }
        });

        scheduleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, Entertainment.class);
                intent.putExtra("studentid", studentId);
                intent.putExtra("studentname", studentName);
                intent.putExtra("sid",sid);
                intent.putExtra("section",section);
                intent.putExtra("profileUrl",profileUrl);
                startActivity(intent);
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, Settings.class);
                intent.putExtra("studentid", studentId);
                intent.putExtra("studentname", studentName);
                intent.putExtra("sid",sid);
                intent.putExtra("section",section);
                intent.putExtra("profileUrl",profileUrl);
                startActivity(intent);
            }
        });
    }
    private void checkAndDisplayAttendance() {
        // Get the current date dynamically
        String currentDate = getCurrentDate();

        // Firebase database reference
        DatabaseReference attendanceRef = FirebaseDatabase.getInstance()
                .getReference("attendance")
                .child(currentDate)
                .child(section)
                .child(studentId);

        // Debug: Log the full path being accessed
        String path = "attendance/" + currentDate + "/" + section + "/" + studentId;
        System.out.println("Accessing path: " + path);

        // Add listener to read data
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Debug: Log the snapshot data
                System.out.println("Snapshot exists: " + dataSnapshot.exists());
                System.out.println("Snapshot value: " + dataSnapshot.getValue());

                Log.d("FirebasePath", "Path: attendance/" + currentDate + "/" + section + "/" + studentId);

                if (dataSnapshot.exists()) {
                    Boolean isPresent = dataSnapshot.getValue(Boolean.class);

                    if (isPresent != null && isPresent) {
                        scheduleTxt.setText("Attendance: Present");
                    } else {
                        scheduleTxt.setText("Attendance: Absent");
                    }
                } else {
                    scheduleTxt.setText("Attendance: No record");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Home.this, "Failed to fetch attendance: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Method to get the current date in the format yyyy-MM-dd
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }

    private String getGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 0 && hour < 12) {
            return "Good Morning";
        } else if (hour >= 12 && hour < 18) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }

    private String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, hh:mm a, MMMM dd yyyy", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private void updateDateTime() {
        String currentDateTime = getCurrentDateTime();
        dateTxt.setText("Date: "+currentDateTime);
    }

    private void startPeriodicTimeUpdates() {
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                // Update the date and time
                updateDateTime();

                // Schedule the next update in one minute
                timeHandler.postDelayed(this, 1000); // 60,000 ms = 1 minute
            }
        };
        timeHandler.post(timeUpdater); // Start the first update
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop the periodic updates when the activity is destroyed
        if (timeUpdater != null) {
            timeHandler.removeCallbacks(timeUpdater);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Cannot go Back!", Toast.LENGTH_SHORT).show();
    }
}
