package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewAttendance extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    String subId = "";

    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();
    ArrayList<String> row3 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        Button mSubmit = findViewById(R.id.viewAttendanceSubmit);
        Spinner mSubId = findViewById(R.id.viewAttendanceSpinnerSubject);
        Spinner mSession = findViewById(R.id.viewAttendanceSpinnerSession);
        mSubId.setOnItemSelectedListener(this);
        mSession.setOnItemSelectedListener(this);

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Subject/");

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    if (item.child("lecId").getValue().toString().equalsIgnoreCase(spId))
                    {

                        row1.add(item.child("subId").getValue().toString() + " - " + item.child("subName").getValue().toString());
                        row2.add(item.child("subId").getValue().toString());

                        // Set up the drop down menu
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ViewAttendance.this, android.R.layout.simple_spinner_item, row1);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSubId.setAdapter(adapter);
                        mSession.setAdapter(adapter);

                    }

                }

                // Retrieve data from database //
                FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                DatabaseReference stage2 = database2.getReference("Session/");

                stage2.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot2)
                    {

                        for (DataSnapshot item2:snapshot2.getChildren())
                        {

                            row3.add(item2.child("session").getValue().toString());

                            // Set up the drop down menu
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ViewAttendance.this, android.R.layout.simple_spinner_item, row3);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mSession.setAdapter(adapter);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {



                    }

                });


                mSubmit.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {

                        Intent intent = new Intent(ViewAttendance.this, ViewAttendanceFiltered.class);
                        intent.putExtra("subId", subId);
                        intent.putExtra("session", mSession.getSelectedItem().toString());
                        startActivity(intent);

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

                startActivity (new Intent(getApplicationContext(), LecturerPanel.class));
                finish();

                return true;

            case R.id.refresh:

                finish();
                overridePendingTransition(0, 0);
                startActivity (new Intent(getApplicationContext(), ViewAttendance.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {

        subId = row2.get(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

}