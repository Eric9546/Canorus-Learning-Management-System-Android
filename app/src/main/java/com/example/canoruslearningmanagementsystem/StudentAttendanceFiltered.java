package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StudentAttendanceFiltered extends AppCompatActivity implements TwoRowAdapter.ItemClickListener
{

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

    // Set up array to store info from database //
    ArrayList<String> row3 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance_filtered);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        session = spSession;

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            subId = extras.getString("subId");

        }

        // Set up array to store info from database //
        ArrayList<String> row1 = new ArrayList<>();
        ArrayList<String> row2 = new ArrayList<>();

        // Retrieve the program value //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Subject/" + subId);

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                program = snapshot.child("program").getValue().toString();

                // Retrieve the section value //
                FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                DatabaseReference stage2 = database2.getReference("Enrolment/" + spId + "/" + subId);

                stage2.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot2)
                    {

                        section = snapshot2.child("section").getValue().toString();

                        // Retrieve the class attendance data //
                        FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                        DatabaseReference stage3 = database3.getReference("Class/" + program + "/" + subId);

                        stage3.addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot3)
                            {

                                for (DataSnapshot item:snapshot3.getChildren())
                                {

                                    if(item.child("section").getValue().toString().contains(section))
                                    {

                                        row1.add(item.child("day").getValue().toString() + " | " + item.child("timeStart").getValue().toString()
                                                                                         + " - " + item.child("timeEnd").getValue().toString());

                                        row2.add(item.child("room").getValue().toString() + " | " + item.child("type").getValue().toString()
                                                                                          + " | " + item.child("section").getValue().toString());

                                        row3.add(item.child("section").getValue().toString());

                                    }

                                }

                                // set up the RecyclerView
                                RecyclerView recyclerView = findViewById(R.id.studentAttendanceFilteredRecycler);
                                recyclerView.setLayoutManager(new LinearLayoutManager(StudentAttendanceFiltered.this));
                                adapter = new TwoRowAdapter(StudentAttendanceFiltered.this, row1, row2);
                                adapter.setClickListener(StudentAttendanceFiltered.this);
                                recyclerView.setAdapter(adapter);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {

                            }

                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }

    @Override
    public void onItemClick(View view, int position)
    {

        Intent intent = new Intent(StudentAttendanceFiltered.this, StudentAttendance.class);
        intent.putExtra("subId", subId);
        intent.putExtra("program", program);
        intent.putExtra("session", session);
        intent.putExtra("section", row3.get(position));
        startActivity(intent);

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
                Intent intent = new Intent(StudentAttendanceFiltered.this, StudentAttendanceFiltered.class);
                intent.putExtra("subId", subId);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

}