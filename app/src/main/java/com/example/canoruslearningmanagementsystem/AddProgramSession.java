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
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddProgramSession extends AppCompatActivity
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

    EditText mCode, mName, mSession;
    Button mSubmitProgram, mSubmitSession;

    String programExit = "false";
    String sessionExit = "false";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_program_session);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        mCode = findViewById(R.id.addProgramSessionCode);
        mName = findViewById(R.id.addProgramSessionName);
        mSession = findViewById(R.id.addProgramSessionSession);
        mSubmitProgram = findViewById(R.id.addProgramSessionSubmitProgram);
        mSubmitSession = findViewById(R.id.addProgramSessionSubmitSession);

        // If user clicks submit program //
        mSubmitProgram.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                String code = mCode.getText().toString();
                String name = mName.getText().toString();

                if (TextUtils.isEmpty(code))
                {

                    mCode.setError("Program Code Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(name))
                {

                    mName.setError("Program Name Cannot Be Empty!");
                    return;

                }

                // Query to check if progCode already exists //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage = database.getReference("Program/" + code.toUpperCase());

                stage.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        if (snapshot.getValue() == null)
                        {

                            // Inserting the data into the database table //
                            FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                            DatabaseReference stage2 = database2.getReference("Program/" + code.toUpperCase());

                            stage2.child("progName").setValue(name);
                            stage2.child("progCode").setValue(code.toUpperCase());

                            // Log the details //
                            logAddProgram(spId, code.toUpperCase(), name);

                            programExit = "true";

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity (new Intent(getApplicationContext(), AddProgramSession.class));
                            overridePendingTransition(0, 0);

                        }

                        else
                        {

                            if (programExit.equalsIgnoreCase("true"))
                            {



                            }

                            else
                            {

                                Toast.makeText(AddProgramSession.this, "Program Code Already Exists!", Toast.LENGTH_SHORT).show();

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

        mSubmitSession.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                String session = mSession.getText().toString();

                if (TextUtils.isEmpty(session))
                {

                    mSession.setError("Session Cannot Be Empty!");
                    return;

                }

                // Query to check if session already exists //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage = database.getReference("Session/" + session.toUpperCase());

                stage.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        if (snapshot.getValue() == null)
                        {

                            // Inserting the data into the database table //
                            FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                            DatabaseReference stage2 = database2.getReference("Session/" + session.toUpperCase());

                            stage2.child("session").setValue(session.toUpperCase());

                            // Log the details //
                            logAddSession(spId, session.toUpperCase());

                            sessionExit = "true";

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity (new Intent(getApplicationContext(), AddProgramSession.class));
                            overridePendingTransition(0, 0);

                        }

                        else
                        {

                            if (sessionExit.equalsIgnoreCase("true"))
                            {



                            }

                            else
                            {

                                Toast.makeText(AddProgramSession.this, "Session Already Exists!", Toast.LENGTH_SHORT).show();

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
                startActivity (new Intent(getApplicationContext(), AddProgramSession.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void logAddProgram (String dbId, String code, String name)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " added the program " + code.toUpperCase() + "-" + name +
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

    public void logAddSession (String dbId, String session)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " added the session " + session.toUpperCase() +
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