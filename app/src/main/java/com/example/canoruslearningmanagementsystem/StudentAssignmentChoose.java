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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StudentAssignmentChoose extends AppCompatActivity implements AdapterView.OnItemSelectedListener
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_assignment_choose);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        Button mSubmit = findViewById(R.id.studentAssignmentChooseSubmit);
        Spinner mSpinner = findViewById(R.id.studentAssignmentChooseSpinner);
        mSpinner.setOnItemSelectedListener(this);

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Enrolment/" + spId);

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    row1.add(item.child("subId").getValue().toString() + " - " + item.child("subName").getValue().toString());
                    row2.add(item.child("subId").getValue().toString());

                    // Set up the drop down menu
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(StudentAssignmentChoose.this, android.R.layout.simple_spinner_item, row1);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mSpinner.setAdapter(adapter);

                    mSubmit.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {

                            Intent intent = new Intent(StudentAssignmentChoose.this, StudentAssignmentFiltered.class);
                            intent.putExtra("subId", subId);
                            startActivity(intent);

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
                startActivity (new Intent(getApplicationContext(), StudentAssignmentChoose.class));
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