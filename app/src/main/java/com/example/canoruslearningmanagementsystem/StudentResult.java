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

public class StudentResult extends AppCompatActivity implements TwoRowAdapter.ItemClickListener
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

    String session = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_result);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
             session = extras.getString("session");

        }

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Result/" + spId.toUpperCase());

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

                    if (item.child("session").getValue().toString().equalsIgnoreCase(session))
                    {

                        row1.add(item.child("subId").getValue().toString() + " - " + item.child("subName").getValue().toString());
                        row2.add(item.child("program").getValue().toString() + " | " + item.child("grade").getValue().toString()
                                                                             + " | " + item.child("date").getValue().toString());

                    }

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.studentResultRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(StudentResult.this));
                adapter = new TwoRowAdapter (StudentResult.this, row1, row2);
                adapter.setClickListener(StudentResult.this);
                recyclerView.setAdapter(adapter);

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
        Toast.makeText(StudentResult.this, "Program | Grade | Date", Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(StudentResult.this, StudentResult.class);
                intent.putExtra("session", session);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

}