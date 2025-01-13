package com.example.smartattendancesystem.Student;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartattendancesystem.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;

public class AttendanceSheet extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView attendanceDetails;
    private String studentName, studentId, sid, section, profileUrl;
    private String schoolYearStarts, schoolYearEnds;
    ImageView prevBtn;

    private DatabaseReference attendanceRef;
    private HashSet<CalendarDay> presentDates = new HashSet<>();
    private HashSet<CalendarDay> absentDates = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance_sheet);

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

        Intent intent = getIntent();
        if (intent != null) {
            studentName = getIntent().getStringExtra("studentname");
            studentId = getIntent().getStringExtra("studentid");
            sid = getIntent().getStringExtra("sid");
            section = getIntent().getStringExtra("section");
            profileUrl = getIntent().getStringExtra("profileUrl");
        }
        attendanceRef = FirebaseDatabase.getInstance().getReference("attendance");
        schoolYearStarts = "2024-09-01";// Format: "yyyy-MM-dd"
        schoolYearEnds = "2025-07-01";     // Format: "yyyy-MM-dd"
        prevBtn = findViewById(R.id.prevBtn);

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AttendanceSheet.this, Home.class);
                intent.putExtra("studentid", studentId);
                intent.putExtra("studentname", studentName);
                intent.putExtra("sid",sid);
                intent.putExtra("section",section);
                intent.putExtra("profileUrl",profileUrl);
                startActivity(intent);
            }
        });

        // Initialize UI components
        calendarView = findViewById(R.id.calendarView);
        attendanceDetails = findViewById(R.id.attendanceDetails);

        // Mark past dates as absent if not recorded
        markPastDatesAsAbsent();

        // Fetch attendance and update calendar colors
        fetchAttendanceData();

        // Set listener for date selection
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", date.getYear(), date.getMonth() + 1, date.getDay());
            fetchAttendanceForDate(selectedDate);
        });
    }

    private void fetchAttendanceForDate(String date) {
        if (!isDateInRange(date, schoolYearStarts, schoolYearEnds)) {
            attendanceDetails.setText("Date is outside the school year range.");
            return;
        }

        attendanceRef.child(date).child(section).child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean isPresent = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    attendanceDetails.setText("Attendance on " + date + ":\n" +
                            studentId + ": " + (isPresent ? "Present" : "Absent"));
                } else {
                    attendanceDetails.setText("No attendance data available for " + date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AttendanceSheet.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markPastDatesAsAbsent() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Start date of the school year
        Calendar startCalendar = Calendar.getInstance();
        try {
            startCalendar.setTime(dateFormat.parse(schoolYearStarts));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Today's date
        Calendar todayCalendar = Calendar.getInstance();

        // Iterate through all dates from the start of the school year to today
        while (!startCalendar.after(todayCalendar)) {
            String currentDate = dateFormat.format(startCalendar.getTime());

            attendanceRef.child(currentDate).child(section).child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // Mark as absent if no record exists
                        attendanceRef.child(currentDate).child(section).child(studentId).setValue(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AttendanceSheet.this, "Failed to mark past dates: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Move to the next date
            startCalendar.add(Calendar.DATE, 1);
        }
    }


    private void fetchAttendanceData() {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashSet<String> allDates = new HashSet<>();

                // Iterate over all dates in Firebase
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    if (date != null && isDateInRange(date, schoolYearStarts, schoolYearEnds)) {
                        allDates.add(date);
                        DataSnapshot studentSnapshot = dateSnapshot.child(section).child(studentId);
                        boolean isPresent = Boolean.TRUE.equals(studentSnapshot.getValue(Boolean.class));

                        CalendarDay day = getCalendarDayFromDate(date);
                        if (day != null) {
                            if (isPresent) {
                                presentDates.add(day);
                            } else {
                                absentDates.add(day);
                            }
                        }
                    }
                }

                // Update the calendar with decorators
                calendarView.addDecorator(new AttendanceDecorator(Color.GREEN, presentDates));
                calendarView.addDecorator(new AttendanceDecorator(Color.RED, absentDates));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AttendanceSheet.this, "Failed to fetch attendance data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private CalendarDay getCalendarDayFromDate(String date) {
        try {
            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // CalendarDay month is 0-based
            int day = Integer.parseInt(parts[2]);
            return CalendarDay.from(year, month, day);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isDateInRange(String date, String start, String end) {
        return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
    }

    private static class AttendanceDecorator implements DayViewDecorator {
        private final int color;
        private final HashSet<CalendarDay> dates;

        public AttendanceDecorator(int color, HashSet<CalendarDay> dates) {
            this.color = color;
            this.dates = dates;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(10, color)); // Adds a colored dot below the day
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AttendanceSheet.this, Home.class);
        intent.putExtra("studentid", studentId);
        intent.putExtra("studentname", studentName);
        intent.putExtra("sid",sid);
        intent.putExtra("section",section);
        intent.putExtra("profileUrl",profileUrl);
        startActivity(intent);
    }
}
