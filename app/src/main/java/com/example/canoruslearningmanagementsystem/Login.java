package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.canoruslearningmanagementsystem.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Login extends AppCompatActivity
{

    FirebaseFirestore log = FirebaseFirestore.getInstance();

    EditText mUserId, mPassword;
    Button mSubmit;
    ProgressBar mProgress;

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the default push notification //
        FirebaseMessaging.getInstance().deleteToken();
        FirebaseMessaging.getInstance().subscribeToTopic("default");

        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        //Check the user login status //
        if (mPreferences.contains(LOGIN_STATUS_KEY))
        {

            if (mPreferences.getString(LOGIN_STATUS_KEY, "").equals("TRUE"))
            {

                String spAccessLevel = mPreferences.getString(ACCESS_LEVEL_KEY, "");
                String spId = mPreferences.getString(ID_KEY, "");

                // Update the user session //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Registration/" + spId.toUpperCase());

                myRef.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        String dbId = snapshot.child("id").getValue().toString();
                        String dbPassword = snapshot.child("password").getValue().toString();
                        String dbAccessLevel = snapshot.child("access_level").getValue().toString();
                        String dbSession = snapshot.child("session").getValue().toString();
                        String sbProgram = snapshot.child("program").getValue().toString();

                        // Set session data //
                        spEditor.putString(LOGIN_STATUS_KEY, "TRUE");
                        spEditor.putString(ID_KEY, dbId);
                        spEditor.putString(ACCESS_LEVEL_KEY, dbAccessLevel);
                        spEditor.putString(SESSION_KEY, dbSession);
                        spEditor.putString(PROGRAM_KEY, sbProgram);
                        spEditor.apply();

                        if (spAccessLevel.equals("Student"))
                        {

                            // Set up push notification for announcements //
                            setUpAnnouncementPush(spId);

                            // Log the login data //
                            logLogin(spAccessLevel, spId);

                            // Redirect user to respective panels //
                            startActivity (new Intent(getApplicationContext(), StudentPanel.class));
                            finish();

                        }

                        else if (spAccessLevel.equals("Lecturer"))
                        {

                            // Log the login data //
                            logLogin(spAccessLevel, spId);

                            // Redirect user to respective panels //
                            startActivity (new Intent(getApplicationContext(), LecturerPanel.class));
                            finish();

                        }

                        else
                        {

                            // Log the login data //
                            logLogin(spAccessLevel, spId);

                            // Redirect user to respective panels //
                            startActivity (new Intent(getApplicationContext(), AdminPanel.class));
                            finish();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }

                });

            }

        }

        else
        {

            spEditor.putString(LOGIN_STATUS_KEY, "FALSE");
            spEditor.apply();

        }

        mUserId = findViewById(R.id.userId);
        mPassword = findViewById(R.id.password);
        mSubmit = findViewById(R.id.submit);
        mProgress = findViewById(R.id.progressBar);

        mSubmit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                // Retrieve inputs from user //
                String id = mUserId.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                // Check to make sure inputs are not empty //
                if (TextUtils.isEmpty(id))
                {

                    mUserId.setError("User ID Is Required!");
                    return;

                }

                if (TextUtils.isEmpty(password))
                {

                    mPassword.setError("Password Is Required!");
                    return;

                }

                // Show progress //
                mProgress.setVisibility(View.VISIBLE);

                // Authenticate the login //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Registration/" + id.toUpperCase());

                myRef.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        // Check Login ID //
                        if (snapshot.getValue() == null)
                        {

                            Toast.makeText(Login.this, "User ID Does Not Exist. Try Again", Toast.LENGTH_SHORT).show();
                            mProgress.setVisibility(View.INVISIBLE);

                        }

                        else
                        {

                            String dbId = snapshot.child("id").getValue().toString();
                            String dbPassword = snapshot.child("password").getValue().toString();
                            String dbAccessLevel = snapshot.child("access_level").getValue().toString();
                            String dbSession = snapshot.child("session").getValue().toString();
                            String sbProgram = snapshot.child("program").getValue().toString();

                            // Check the password //
                            if (dbPassword.equals(password))
                            {

                                if (dbAccessLevel.equals("Student"))
                                {

                                    mProgress.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();

                                    // Set session data //
                                    spEditor.putString(LOGIN_STATUS_KEY, "TRUE");
                                    spEditor.putString(ID_KEY, dbId);
                                    spEditor.putString(ACCESS_LEVEL_KEY, dbAccessLevel);
                                    spEditor.putString(SESSION_KEY, dbSession);
                                    spEditor.putString(PROGRAM_KEY, sbProgram);
                                    spEditor.apply();

                                    // Set up push notification for announcements //
                                    setUpAnnouncementPush(dbId);

                                    // Log the login data //
                                    logLogin(dbAccessLevel, dbId);

                                    // Redirect user to respective panels //
                                    startActivity (new Intent(getApplicationContext(), StudentPanel.class));
                                    finish();

                                }

                                else if (dbAccessLevel.equals("Lecturer"))
                                {

                                    mProgress.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();

                                    // Set session data //
                                    spEditor.putString(LOGIN_STATUS_KEY, "TRUE");
                                    spEditor.putString(ID_KEY, dbId);
                                    spEditor.putString(ACCESS_LEVEL_KEY, dbAccessLevel);
                                    spEditor.putString(SESSION_KEY, dbSession);
                                    spEditor.putString(PROGRAM_KEY, sbProgram);
                                    spEditor.apply();

                                    // Log the login data //
                                    logLogin(dbAccessLevel, dbId);

                                    // Redirect user to respective panels //
                                    startActivity (new Intent(getApplicationContext(), LecturerPanel.class));
                                    finish();

                                }

                                else
                                {

                                    mProgress.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();

                                    // Set session data //
                                    spEditor.putString(LOGIN_STATUS_KEY, "TRUE");
                                    spEditor.putString(ID_KEY, dbId);
                                    spEditor.putString(ACCESS_LEVEL_KEY, dbAccessLevel);
                                    spEditor.putString(SESSION_KEY, dbSession);
                                    spEditor.putString(PROGRAM_KEY, sbProgram);
                                    spEditor.apply();

                                    // Log the login data //
                                    logLogin(dbAccessLevel, dbId);

                                    // Redirect user to respective panels //
                                    startActivity (new Intent(getApplicationContext(), AdminPanel.class));
                                    finish();

                                }

                            }

                            else
                            {

                                Toast.makeText(Login.this, "Password Incorrect. Try Again", Toast.LENGTH_SHORT).show();
                                mProgress.setVisibility(View.INVISIBLE);

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                        Toast.makeText(Login.this, "Login Failure. Try Again", Toast.LENGTH_SHORT).show();
                        mProgress.setVisibility(View.INVISIBLE);

                    }
                });

            }
        });

    }

    public void logLogin (String dbAccessLevel, String dbId)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "User with the ID: " + dbId.toUpperCase() + " with the access level of " + dbAccessLevel.toUpperCase() +
                        " logged in at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-LOGIN")
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

    public void setUpAnnouncementPush (String id)
    {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Enrolment/" + id.toUpperCase());

        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    FirebaseMessaging.getInstance().subscribeToTopic(item.child("subId").getValue().toString());

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

                Toast.makeText(Login.this, "Push Notification Setup Error", Toast.LENGTH_SHORT).show();
                mProgress.setVisibility(View.INVISIBLE);

            }

        });

    }

}