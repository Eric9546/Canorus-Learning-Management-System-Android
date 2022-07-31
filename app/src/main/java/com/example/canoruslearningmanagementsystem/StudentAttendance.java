package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StudentAttendance extends AppCompatActivity implements TwoRowAdapter.ItemClickListener
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

    TwoRowAdapter adapter;

    String subId = "";
    String section = "";
    String program = "";
    String session = "";
    String stuId = "";

    ArrayList<String> attendPin = new ArrayList<>();
    ArrayList<String> record_to_view = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        session = spSession;
        stuId = spId;

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            subId = extras.getString("subId");
            program = extras.getString("program");
            session = extras.getString("session");
            section = extras.getString("section");

        }

        // Set up array to store info from database //
        ArrayList<String> row1 = new ArrayList<>();
        ArrayList<String> row2 = new ArrayList<>();

        // Retrieve the program value //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Attendance/" + session + "/" + subId + "/" + section);

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    if (item.child("attendStatus").getValue().toString().equalsIgnoreCase("Opened"))
                    {
                        attendPin.add(item.child("attendPin").getValue().toString());

                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference("Attendance/" + session + "/" + subId + "/" + section + "/" +
                                                                                item.child("classDateTime").getValue().toString() + "/" + spId);

                        record_to_view.add("Attendance/" + session + "/" + subId + "/" + section + "/" +
                                item.child("classDateTime").getValue().toString() + "/" + spId);

                        stage2.addValueEventListener(new ValueEventListener()
                        {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2)
                            {

                                row1.add(item.child("classDateTime").getValue().toString());
                                row2.add(snapshot2.child("attendStatus").getValue().toString());

                                // set up the RecyclerView
                                RecyclerView recyclerView = findViewById(R.id.studentAttendanceRecycler);
                                recyclerView.setLayoutManager(new LinearLayoutManager(StudentAttendance.this));
                                adapter = new TwoRowAdapter(StudentAttendance.this, row1, row2);
                                adapter.setClickListener(StudentAttendance.this);
                                recyclerView.setAdapter(adapter);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {

                            }

                        });

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });

    }

    @Override
    public void onItemClick(View view, int position)
    {

        EditText pin;
        Button submitPin, submitQR;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_student_attendance, null);

        pin = popupView.findViewById(R.id.popup_student_pin);
        submitPin = popupView.findViewById(R.id.popup_student_submit_pin);
        submitQR = popupView.findViewById(R.id.popup_student_qr);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                popupWindow.dismiss();
                return true;
            }
        });

        submitPin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                // Authenticate the pin //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference(record_to_view.get(position));

                if (pin.getText().toString().equalsIgnoreCase(attendPin.get(position)))
                {

                    // Update the database //
                    stage1.child("attendStatus").setValue("Present");

                    Toast.makeText(StudentAttendance.this, "Success!", Toast.LENGTH_SHORT).show();

                    // Log the attendance //
                    logAttendance (stuId, program, session, subId);

                    finish();
                    overridePendingTransition(0, 0);
                    Intent intent = new Intent(StudentAttendance.this, StudentAttendance.class);
                    intent.putExtra("subId", subId);
                    intent.putExtra("program", program);
                    intent.putExtra("session", session);
                    intent.putExtra("section", section);
                    startActivity(intent);
                    overridePendingTransition(0, 0);

                }

                else
                {

                    Toast.makeText(StudentAttendance.this, "Attendance Pin Is Incorrect!", Toast.LENGTH_SHORT).show();

                }

            }

        });

        submitQR.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(StudentAttendance.this, StudentAttendanceQR.class);
                intent.putExtra("subId", subId);
                intent.putExtra("program", program);
                intent.putExtra("session", session);
                intent.putExtra("section", section);
                intent.putExtra("record_to_view", record_to_view.get(position));
                intent.putExtra("attendPin", attendPin.get(position));
                startActivity(intent);

            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {

        getMenuInflater().inflate(R.menu.action_bar_function, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {

        switch (item.getItemId())
        {

            case R.id.home:

                startActivity (new Intent(getApplicationContext(), StudentPanel.class));
                finish();

                return true;

            case R.id.refresh:

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(StudentAttendance.this, StudentAttendance.class);
                intent.putExtra("subId", subId);
                intent.putExtra("program", program);
                intent.putExtra("session", session);
                intent.putExtra("section", section);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

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