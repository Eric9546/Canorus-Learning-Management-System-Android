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

public class AddSubject extends AppCompatActivity implements AdapterView.OnItemSelectedListener
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

    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();
    ArrayList<String> row3 = new ArrayList<>();

    EditText mSubId, mName, mSections, mFee;
    Spinner mProgram, mLecId;
    Button mSubmit;

    String exit = "false";
    String lecId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subject);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        mSubId = findViewById(R.id.addSubjectId);
        mName = findViewById(R.id.addSubjectName);
        mSections = findViewById(R.id.addSubjectSection);
        mFee = findViewById(R.id.addSubjectFee);
        mProgram = findViewById(R.id.addSubjectProgram);
        mLecId = findViewById(R.id.addSubjectLec);
        mSubmit = findViewById(R.id.addSubjectSubmit);

        mLecId.setOnItemSelectedListener(this);

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

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddSubject.this, android.R.layout.simple_spinner_item, row1);
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

                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(AddSubject.this, android.R.layout.simple_spinner_item, row2);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mLecId.setAdapter(adapter2);

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

                String subId = mSubId.getText().toString();
                String name = mName.getText().toString();
                String sections = mSections.getText().toString();
                String fee = mFee.getText().toString();

                if (TextUtils.isEmpty(subId))
                {

                    mSubId.setError("Subject ID Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(name))
                {

                    mName.setError("Subject Name Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(sections))
                {

                    mSections.setError("Sections Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(fee))
                {

                    mFee.setError("Subject Fee Cannot Be Empty!");
                    return;

                }

                // Query to check if subject ID already exists //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage = database.getReference("Subject/" + subId.toUpperCase());

                stage.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        if (snapshot.getValue() == null)
                        {

                            stage.child("subId").setValue(subId.toUpperCase());
                            stage.child("subName").setValue(name);
                            stage.child("program").setValue(mProgram.getSelectedItem().toString());
                            stage.child("fee").setValue(fee);
                            stage.child("lecId").setValue(lecId);
                            stage.child("section").setValue(sections);

                            // Log the details //
                            logAddSubject(spId, subId.toUpperCase(), name, mProgram.getSelectedItem().toString());

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity (new Intent(getApplicationContext(), AddSubject.class));
                            overridePendingTransition(0, 0);

                            Toast.makeText(AddSubject.this, "Subject Added!", Toast.LENGTH_SHORT).show();

                        }

                        else
                        {

                            if (exit.equalsIgnoreCase("true"))
                            {



                            }

                            else
                            {

                                Toast.makeText(AddSubject.this, "Subject ID Already Exists!", Toast.LENGTH_SHORT).show();

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }

                });

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
                startActivity (new Intent(getApplicationContext(), AddSubject.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void logAddSubject (String dbId, String subId, String subName, String program)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " added the subject " + subId.toUpperCase() + " - " + subName +
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