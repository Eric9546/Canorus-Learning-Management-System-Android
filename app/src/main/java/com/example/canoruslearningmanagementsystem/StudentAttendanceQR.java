package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StudentAttendanceQR extends AppCompatActivity
{

    FirebaseFirestore log = FirebaseFirestore.getInstance();

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    private CodeScanner mCodeScanner;

    String stuId = "";
    String subId = "";
    String section = "";
    String program = "";
    String session = "";
    String record_to_view = "";
    String attendPin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance_qr);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        stuId = spId;

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            subId = extras.getString("subId");
            program = extras.getString("program");
            session = extras.getString("session");
            section = extras.getString("section");
            record_to_view = extras.getString("record_to_view");
            attendPin = extras.getString("attendPin");

        }

        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);

        mCodeScanner.setDecodeCallback(new DecodeCallback()
        {
            @Override
            public void onDecoded(@NonNull final Result result)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {

                        // Authenticate the pin //
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference stage1 = database.getReference(record_to_view);

                        if (attendPin.equalsIgnoreCase(result.getText()))
                        {

                            // Update the database //
                            stage1.child("attendStatus").setValue("Present");

                            Toast.makeText(StudentAttendanceQR.this, "Success!", Toast.LENGTH_SHORT).show();

                            // Log the attendance //
                            logAttendance (stuId, program, session, subId);

                            Intent intent = new Intent(StudentAttendanceQR.this, StudentAttendance.class);
                            intent.putExtra("subId", subId);
                            intent.putExtra("program", program);
                            intent.putExtra("session", session);
                            intent.putExtra("section", section);
                            startActivity(intent);
                            finish();

                        }

                        else
                        {

                            Toast.makeText(StudentAttendanceQR.this, "Error! Try Again!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(StudentAttendanceQR.this, StudentAttendance.class);
                            intent.putExtra("subId", subId);
                            intent.putExtra("program", program);
                            intent.putExtra("session", session);
                            intent.putExtra("section", section);
                            startActivity(intent);
                            finish();

                        }

                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mCodeScanner.startPreview();
            }
        });

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause()
    {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    public void logAttendance (String dbId, String program, String session, String subId)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Student with the ID: " + dbId.toUpperCase() + " enrolled under " + program + " " + session + " " + subId +
                " is present in class " +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-ATTENDANCE")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                {
                    @Override
                    public void onSuccess(DocumentReference documentReference)
                    {

                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {

                    }
                });

    }

}