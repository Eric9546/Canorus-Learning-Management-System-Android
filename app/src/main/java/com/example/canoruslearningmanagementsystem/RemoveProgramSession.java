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

public class RemoveProgramSession extends AppCompatActivity implements AdapterView.OnItemSelectedListener
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

    String progCode = "";
    String progName = "";

    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();
    ArrayList<String> row3 = new ArrayList<>();
    ArrayList<String> row4 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_program_session);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        Button mSubmitProgram = findViewById(R.id.removeProgramSessionSubmit1);
        Button mSubmitSession = findViewById(R.id.removeProgramSessionSubmit2);
        Spinner mSpinnerProgram = findViewById(R.id.removeProgramSessionSpinner1);
        Spinner mSpinnerSession = findViewById(R.id.removeProgramSessionSpinner2);

        mSpinnerProgram.setOnItemSelectedListener(this);

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Program/");

        stage1.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    row1.add(item.child("progCode").getValue().toString() + " (" + item.child("progName").getValue().toString() + ")");
                    row2.add(item.child("progCode").getValue().toString());
                    row3.add(item.child("progName").getValue().toString());

                    // Set up the drop down menu
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(RemoveProgramSession.this, android.R.layout.simple_spinner_item, row1);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mSpinnerProgram.setAdapter(adapter);

                    mSubmitProgram.setOnClickListener(new View.OnClickListener()
                    {

                        @Override
                        public void onClick(View view)
                        {

                            // Remove the program //
                            FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                            DatabaseReference stage3 = database3.getReference("Program/" + progCode);

                            stage3.removeValue();

                            // Log the details //
                            logRemoveProgram (spId, progName, progCode);

                            Toast.makeText(RemoveProgramSession.this, "Program Removed!", Toast.LENGTH_SHORT).show();

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity (new Intent(getApplicationContext(), RemoveProgramSession.class));
                            overridePendingTransition(0, 0);

                        }

                    });

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

                            row4.add(item2.child("session").getValue().toString());

                            // Set up the drop down menu
                            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(RemoveProgramSession.this, android.R.layout.simple_spinner_item, row4);
                            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mSpinnerSession.setAdapter(adapter2);

                            mSubmitSession.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {

                                    // Remove the session //
                                    FirebaseDatabase database4 = FirebaseDatabase.getInstance();
                                    DatabaseReference stage4 = database4.getReference("Session/" + mSpinnerSession.getSelectedItem().toString());

                                    stage4.removeValue();

                                    // Log the details //
                                    logRemoveSession(spId, mSpinnerSession.getSelectedItem().toString());

                                    Toast.makeText(RemoveProgramSession.this, "Session Removed!", Toast.LENGTH_SHORT).show();

                                    finish();
                                    overridePendingTransition(0, 0);
                                    startActivity (new Intent(getApplicationContext(), RemoveProgramSession.class));
                                    overridePendingTransition(0, 0);

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

                startActivity (new Intent(getApplicationContext(), AdminPanel.class));
                finish();

                return true;

            case R.id.refresh:

                finish();
                overridePendingTransition(0, 0);
                startActivity (new Intent(getApplicationContext(), RemoveProgramSession.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {

        progCode = row2.get(position).toString();
        progName = row3.get(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    public void logRemoveProgram (String dbId, String progName, String progCode)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " removed the program " + progCode.toUpperCase() + "-" + progName +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-PROGRAM")
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

    public void logRemoveSession (String dbId, String session)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " removed " + session.toUpperCase() +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-SESSION")
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