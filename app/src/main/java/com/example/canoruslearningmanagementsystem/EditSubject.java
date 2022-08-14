package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.Map;

public class EditSubject extends AppCompatActivity implements AdapterView.OnItemSelectedListener
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

    EditText mName, mSections, mFee;
    Spinner mProgram, mLecId;
    Button mSubmit;
    TextView mTitle;

    String subId = "";
    String lecId = "";

    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();
    ArrayList<String> row3 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_subject);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        mName = findViewById(R.id.editSubjectName);
        mSections = findViewById(R.id.editSubjectSection);
        mFee = findViewById(R.id.editSubjectFee);
        mProgram = findViewById(R.id.editSubjectProgram);
        mLecId = findViewById(R.id.editSubjectLec);
        mSubmit = findViewById(R.id.editSubjectSubmit);
        mTitle = findViewById(R.id.editSubjectText7);

        mLecId.setOnItemSelectedListener(this);

        mTitle.setText("Edited: " + subId);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {

            subId = extras.getString("subId");

        }

        // Set up the drop down menu //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Program/");

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    row1.add(item.child("progCode").getValue().toString());

                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditSubject.this, android.R.layout.simple_spinner_item, row1);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mProgram.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });

        // Set up the drop down menu //
        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference stage2 = database2.getReference("Lecturer/");

        stage2.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    row2.add(item.child("lecId").getValue().toString() + " - " + item.child("name").getValue().toString());
                    row3.add(item.child("lecId").getValue().toString());

                }

                row2.add("N/A");
                row3.add("N/A");

                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(EditSubject.this, android.R.layout.simple_spinner_item, row2);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mLecId.setAdapter(adapter2);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });

        // Display the current subject details in the form //
        FirebaseDatabase database3 = FirebaseDatabase.getInstance();
        DatabaseReference stage3 = database3.getReference("Subject/" + subId);

        stage3.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                mName.setHint(snapshot.child("subName").getValue().toString());
                mSections.setHint(snapshot.child("section").getValue().toString());
                mFee.setHint(snapshot.child("fee").getValue().toString());

                mSubmit.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {

                        String name = mName.getText().toString();
                        String sections = mSections.getText().toString();
                        String fee = mFee.getText().toString();

                        if (TextUtils.isEmpty(name))
                        {

                            name = snapshot.child("subName").getValue().toString();

                        }

                        if (TextUtils.isEmpty(sections))
                        {

                            sections = snapshot.child("section").getValue().toString();


                        }

                        if (TextUtils.isEmpty(fee))
                        {

                            fee = snapshot.child("fee").getValue().toString();


                        }

                        // Update the details in the database //
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference("Subject/" + subId);

                        stage2.child("subName").setValue(name);
                        stage2.child("program").setValue(mProgram.getSelectedItem().toString());
                        stage2.child("fee").setValue(fee);
                        stage2.child("lecId").setValue(lecId);
                        stage2.child("section").setValue(sections);

                        // Log the audit changes to database //
                        logEditSubject(spId, subId, name, mProgram.getSelectedItem().toString());

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(EditSubject.this, EditSubject.class);
                        intent.putExtra("subId", subId);
                        startActivity(intent);
                        overridePendingTransition(0, 0);

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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {

        lecId = row3.get(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

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
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditSubject.this, EditSubject.class);
                intent.putExtra("subId", subId);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void logEditSubject (String dbId, String subId, String subName, String program)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " updated the subject details for " + subId.toUpperCase() + " - " + subName +
                " under the program " + program +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-SUBJECT")
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