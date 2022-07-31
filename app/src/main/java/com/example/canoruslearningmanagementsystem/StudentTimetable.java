package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudentTimetable extends AppCompatActivity implements TwoRowAdapter.ItemClickListener
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_timetable);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Enrolment/" + spId.toUpperCase());

        // Set up array to store info from database //
        ArrayList<String> row1 = new ArrayList<>();
        ArrayList<String> row2 = new ArrayList<>();

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    if (item.child("session").getValue().toString().equalsIgnoreCase(spSession))
                    {

                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference("Class/" + item.child("program").getValue().toString() + "/" + item.child("subId").getValue().toString());

                        stage2.addValueEventListener(new ValueEventListener()
                        {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2)
                            {

                                for (DataSnapshot item2: snapshot2.getChildren())
                                {

                                    if (item2.child("section").getValue().toString().contains(item.child("section").getValue().toString()))
                                    {

                                        // data to populate the first row with //
                                        row1.add(item2.child("day").getValue().toString() + " | " + item2.child("timeStart").getValue().toString()
                                                                                           + " - " + item2.child("timeEnd").getValue().toString());

                                        // data to populate the second row with //
                                        row2.add(item2.child("subId").getValue().toString() + " | " + item2.child("section").getValue().toString()
                                                                                             + " | " + item2.child("room").getValue().toString()
                                                                                             + " | " + item2.child("type").getValue().toString());


                                    }

                                }

                                // set up the RecyclerView
                                RecyclerView recyclerView = findViewById(R.id.studentTimetableRecycler);
                                recyclerView.setLayoutManager(new LinearLayoutManager(StudentTimetable.this));
                                adapter = new TwoRowAdapter (StudentTimetable.this, row1, row2);
                                adapter.setClickListener(StudentTimetable.this);
                                recyclerView.setAdapter(adapter);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {

                                Toast.makeText(StudentTimetable.this, "Data Retrieval Error", Toast.LENGTH_SHORT).show();

                            }

                        });

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

                Toast.makeText(StudentTimetable.this, "Data Retrieval Error", Toast.LENGTH_SHORT).show();

            }

        });

    }

    @Override
    public void onItemClick(View view, int position)
    {
        Toast.makeText(StudentTimetable.this, "Subject ID | Section | Room | Type", Toast.LENGTH_SHORT).show();
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
                startActivity (new Intent(getApplicationContext(), StudentTimetable.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

}