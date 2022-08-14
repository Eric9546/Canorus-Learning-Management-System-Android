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

public class ViewLecturerTimetableFiltered extends AppCompatActivity implements TwoRowAdapter.ItemClickListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    String lecId = "";
    String subName = "";

    TwoRowAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lecturer_timetable_filtered);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            lecId = extras.getString("lecId");

        }

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Class/");

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

                    FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                    DatabaseReference stage2 = database2.getReference("Class/" + item.getKey());

                    stage2.addValueEventListener(new ValueEventListener()
                    {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2)
                        {

                            for (DataSnapshot item2:snapshot2.getChildren())
                            {

                                FirebaseDatabase database4 = FirebaseDatabase.getInstance();
                                DatabaseReference stage4 = database4.getReference("Subject/" + item2.getKey());

                                stage4.addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot4)
                                    {

                                        subName = snapshot4.child("subName").getValue().toString();

                                        FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                                        DatabaseReference stage3 = database3.getReference("Class/" + item.getKey() + "/" + item2.getKey());

                                        stage3.addValueEventListener(new ValueEventListener()
                                        {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot3)
                                            {

                                                for (DataSnapshot item3:snapshot3.getChildren())
                                                {

                                                    if (item3.child("lecId").getValue().toString().equalsIgnoreCase(lecId))
                                                    {

                                                        // data to populate the first row with //
                                                        row1.add(item3.child("day").getValue().toString() + " | " + item3.child("timeStart").getValue().toString()
                                                                + " - " + item3.child("timeEnd").getValue().toString());

                                                        // data to populate the second row with //
                                                        row2.add(item3.child("subId").getValue().toString() + " | " + item3.child("section").getValue().toString()
                                                                + " | " + item3.child("room").getValue().toString()
                                                                + " | " + item3.child("type").getValue().toString());

                                                    }

                                                }

                                                // set up the RecyclerView
                                                RecyclerView recyclerView = findViewById(R.id.viewLecturerTimetableFilteredRecycler);
                                                recyclerView.setLayoutManager(new LinearLayoutManager(ViewLecturerTimetableFiltered.this));
                                                adapter = new TwoRowAdapter (ViewLecturerTimetableFiltered.this, row1, row2);
                                                adapter.setClickListener(ViewLecturerTimetableFiltered.this);
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

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error)
                        {

                        }

                    });

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

        Toast.makeText(ViewLecturerTimetableFiltered.this, "Subject ID | Section | Room | Type", Toast.LENGTH_SHORT).show();

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

                startActivity (new Intent(getApplicationContext(), AdminPanel.class));
                finish();

                return true;

            case R.id.refresh:

                finish();
                Intent intent = new Intent(ViewLecturerTimetableFiltered.this, ViewLecturerTimetableFiltered.class);
                intent.putExtra("lecId", lecId);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

}