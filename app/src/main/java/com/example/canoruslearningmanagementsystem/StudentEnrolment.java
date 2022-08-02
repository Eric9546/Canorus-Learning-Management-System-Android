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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class StudentEnrolment extends AppCompatActivity implements TwoRowAdapter.ItemClickListener, AdapterView.OnItemSelectedListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    Button mSubmit;

    TwoRowAdapter adapter;

    String subId = "";
    String section = "";
    String payStatus = "Unpaid";
    String program = "";
    String session = "";
    String record_to_remove = "";
    String stuId = "";
    String finalDate = "";
    String exit = "false";

    // Set up array to store info from database //
    ArrayList<String> listSubId = new ArrayList<>();
    ArrayList<String> listSection = new ArrayList<>();

    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();
    ArrayList<String> row3 = new ArrayList<>();
    ArrayList<String> row4 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_enrolment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        String spProgram = mPreferences.getString(PROGRAM_KEY, "");

        stuId = spId;

        mSubmit = findViewById(R.id.viewAnnouncementsFilteredSubmit);
        Spinner mSpinner = findViewById(R.id.studentEnrolmentDropdown);
        mSpinner.setOnItemSelectedListener(this);

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

                    if (item.child("program").getValue().toString().equalsIgnoreCase(spProgram))
                    {

                        String str = item.child("section").getValue().toString();
                        List<String> sectionList = Arrays.asList(str.split(","));

                        for (String x : sectionList)
                        {

                            listSubId.add(item.child("subId").getValue().toString());
                            listSection.add(x);
                            row1.add(item.child("subId").getValue().toString() + " - " + item.child("subName").getValue().toString() + " (" + x + ")");

                        }

                        // Set up the drop down menu
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(StudentEnrolment.this, android.R.layout.simple_spinner_item, row1);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinner.setAdapter(adapter);

                    }

                }

                mSubmit.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {

                        // Generate date //
                        SimpleDateFormat logDate = new SimpleDateFormat("dd/MM/yyyy");
                        Date date = new Date ();
                        finalDate = logDate.format(date);

                        // Query to check if subject already enrolled //
                        FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                        DatabaseReference stage3 = database3.getReference("Enrolment/" + spId + "/" + subId);

                        stage3.addValueEventListener(new ValueEventListener()
                        {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {

                                if (snapshot.getValue() == null)
                                {

                                    // Check for clashes //
                                    FirebaseDatabase database4 = FirebaseDatabase.getInstance();
                                    DatabaseReference stage4 = database4.getReference("Class/" + spProgram + "/" + subId);

                                    stage4.addValueEventListener(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot)
                                        {

                                            for (DataSnapshot item:snapshot.getChildren())
                                            {

                                                if (item.child("section").getValue().toString().contains(section))
                                                {

                                                    FirebaseDatabase database5 = FirebaseDatabase.getInstance();
                                                    DatabaseReference stage5 = database5.getReference("Enrolment/" + spId);

                                                    stage5.addValueEventListener(new ValueEventListener()
                                                    {

                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot2)
                                                        {

                                                            if (snapshot2.getValue() == null)
                                                            {

                                                                // Get data from subject table //
                                                                FirebaseDatabase database7 = FirebaseDatabase.getInstance();
                                                                DatabaseReference stage7 = database7.getReference("Subject/" + subId);

                                                                stage7.addValueEventListener(new ValueEventListener()
                                                                {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                                                    {

                                                                        String subName = snapshot.child("subName").getValue().toString();
                                                                        String fee = snapshot.child("fee").getValue().toString();
                                                                        String lecId = snapshot.child("lecId").getValue().toString();

                                                                        // Get data from lecturer table //
                                                                        FirebaseDatabase database8 = FirebaseDatabase.getInstance();
                                                                        DatabaseReference stage8 = database8.getReference("Lecturer/" + lecId);

                                                                        stage8.addValueEventListener(new ValueEventListener()
                                                                        {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot snapshot)
                                                                            {

                                                                                String lecName = snapshot.child("name").getValue().toString();

                                                                                // Inserting the data into the database table //
                                                                                FirebaseDatabase database9 = FirebaseDatabase.getInstance();
                                                                                DatabaseReference stage9 = database9.getReference("Enrolment/" + spId + "/" + subId);

                                                                                stage9.child("id").setValue(spId);
                                                                                stage9.child("subId").setValue(subId);
                                                                                stage9.child("subName").setValue(subName);
                                                                                stage9.child("fee").setValue(fee);
                                                                                stage9.child("payStatus").setValue(payStatus);
                                                                                stage9.child("program").setValue(spProgram);
                                                                                stage9.child("session").setValue(spSession);
                                                                                stage9.child("date").setValue(finalDate);
                                                                                stage9.child("section").setValue(section);
                                                                                stage9.child("lecName").setValue(lecName);

                                                                                finish();
                                                                                overridePendingTransition(0, 0);
                                                                                startActivity (new Intent(getApplicationContext(), StudentPanel.class));
                                                                                overridePendingTransition(0, 0);

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

                                                            for (DataSnapshot item2:snapshot2.getChildren())
                                                            {

                                                                if (!item2.child("subId").getValue().toString().equalsIgnoreCase(subId))
                                                                {

                                                                    FirebaseDatabase database6 = FirebaseDatabase.getInstance();
                                                                    DatabaseReference stage6 = database6.getReference("Class/" + spProgram + "/" + item2.child("subId").getValue().toString());

                                                                    stage6.addValueEventListener(new ValueEventListener()
                                                                    {

                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot3)
                                                                        {

                                                                            for (DataSnapshot item3:snapshot3.getChildren())
                                                                            {

                                                                                if (item3.child("section").getValue().toString().contains(item2.child("section").getValue().toString()))
                                                                                {

                                                                                    if (item.child("day").getValue().toString().equalsIgnoreCase(item3.child("day").getValue().toString()))
                                                                                    {

                                                                                        if (Integer.parseInt(item.child("timeStartIndex").getValue().toString()) >= Integer.parseInt(item3.child("timeStartIndex").getValue().toString()) &&
                                                                                                Integer.parseInt(item.child("timeStartIndex").getValue().toString()) < Integer.parseInt(item3.child("timeEndIndex").getValue().toString()))
                                                                                        {

                                                                                            Toast.makeText(StudentEnrolment.this, "Class Clash!", Toast.LENGTH_SHORT).show();

                                                                                            finish();
                                                                                            overridePendingTransition(0, 0);
                                                                                            startActivity (new Intent(getApplicationContext(), StudentEnrolment.class));
                                                                                            overridePendingTransition(0, 0);

                                                                                            exit = "true";

                                                                                        }

                                                                                        else if (Integer.parseInt(item.child("timeStartIndex").getValue().toString()) > Integer.parseInt(item3.child("timeStartIndex").getValue().toString()) &&
                                                                                                Integer.parseInt(item.child("timeStartIndex").getValue().toString()) <= Integer.parseInt(item3.child("timeEndIndex").getValue().toString()))
                                                                                        {

                                                                                            Toast.makeText(StudentEnrolment.this, "Class Clash!", Toast.LENGTH_SHORT).show();

                                                                                            finish();
                                                                                            overridePendingTransition(0, 0);
                                                                                            startActivity (new Intent(getApplicationContext(), StudentEnrolment.class));
                                                                                            overridePendingTransition(0, 0);

                                                                                            exit = "true";

                                                                                        }

                                                                                    }

                                                                                }

                                                                            }

                                                                            if (exit.equalsIgnoreCase("true"))
                                                                            {



                                                                            }

                                                                            else
                                                                            {

                                                                                // Get data from subject table //
                                                                                FirebaseDatabase database7 = FirebaseDatabase.getInstance();
                                                                                DatabaseReference stage7 = database7.getReference("Subject/" + subId);

                                                                                stage7.addValueEventListener(new ValueEventListener()
                                                                                {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                                                                    {

                                                                                        String subName = snapshot.child("subName").getValue().toString();
                                                                                        String fee = snapshot.child("fee").getValue().toString();
                                                                                        String lecId = snapshot.child("lecId").getValue().toString();

                                                                                        // Get data from lecturer table //
                                                                                        FirebaseDatabase database8 = FirebaseDatabase.getInstance();
                                                                                        DatabaseReference stage8 = database8.getReference("Lecturer/" + lecId);

                                                                                        stage8.addValueEventListener(new ValueEventListener()
                                                                                        {
                                                                                            @Override
                                                                                            public void onDataChange(@NonNull DataSnapshot snapshot)
                                                                                            {

                                                                                                String lecName = snapshot.child("name").getValue().toString();

                                                                                                // Inserting the data into the database table //
                                                                                                FirebaseDatabase database9 = FirebaseDatabase.getInstance();
                                                                                                DatabaseReference stage9 = database9.getReference("Enrolment/" + spId + "/" + subId);

                                                                                                stage9.child("id").setValue(spId);
                                                                                                stage9.child("subId").setValue(subId);
                                                                                                stage9.child("subName").setValue(subName);
                                                                                                stage9.child("fee").setValue(fee);
                                                                                                stage9.child("payStatus").setValue(payStatus);
                                                                                                stage9.child("program").setValue(spProgram);
                                                                                                stage9.child("session").setValue(spSession);
                                                                                                stage9.child("date").setValue(finalDate);
                                                                                                stage9.child("section").setValue(section);
                                                                                                stage9.child("lecName").setValue(lecName);

                                                                                                finish();
                                                                                                overridePendingTransition(0, 0);
                                                                                                startActivity (new Intent(getApplicationContext(), StudentPanel.class));
                                                                                                overridePendingTransition(0, 0);

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

                                else
                                {

                                    Toast.makeText(StudentEnrolment.this, "You Already Enrolled This Subject!", Toast.LENGTH_SHORT).show();
                                    finish();
                                    overridePendingTransition(0, 0);
                                    startActivity (new Intent(getApplicationContext(), StudentEnrolment.class));
                                    overridePendingTransition(0, 0);

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {

                            }

                        });

                    }
                });

                // Retrieve data from database //
                FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                DatabaseReference stage2 = database2.getReference("Enrolment/" + spId.toUpperCase());

                stage2.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot2)
                    {

                        for (DataSnapshot item2:snapshot2.getChildren())
                        {

                            if (item2.child("session").getValue().toString().equalsIgnoreCase(spSession))
                            {

                                row2.add(item2.child("subId").getValue().toString() + " - " + item2.child("subName").getValue().toString());
                                row3.add("RM" + item2.child("fee").getValue().toString() + " | " + item2.child("date").getValue().toString()
                                        + " | " + item2.child("section").getValue().toString());
                                row4.add(item2.child("subId").getValue().toString());

                            }

                        }

                        // set up the RecyclerView
                        RecyclerView recyclerView = findViewById(R.id.viewAnnouncementFilteredRecycler);
                        recyclerView.setLayoutManager(new LinearLayoutManager(StudentEnrolment.this));
                        adapter = new TwoRowAdapter (StudentEnrolment.this, row2, row3);
                        adapter.setClickListener(StudentEnrolment.this);
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
    public void onItemClick(View view, int position)
    {

        record_to_remove = row4.get(position);

        // Drop the subject //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Enrolment/" + stuId + "/" + record_to_remove);

        stage1.removeValue();

        Toast.makeText(StudentEnrolment.this, "Subject Dropped", Toast.LENGTH_SHORT).show();

        finish();
        overridePendingTransition(0, 0);
        startActivity (new Intent(getApplicationContext(), StudentEnrolment.class));
        overridePendingTransition(0, 0);

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
                startActivity (new Intent(getApplicationContext(), StudentEnrolment.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {

        // Get the inputs //
        subId = listSubId.get(position);
        section = listSection.get(position);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

}