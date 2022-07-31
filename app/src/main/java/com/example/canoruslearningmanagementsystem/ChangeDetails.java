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
import java.util.Map;

public class ChangeDetails extends AppCompatActivity
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditText mPassword, mPassword2, mEmail, mPhone, mAddress;
        Button mSubmit;

        mPassword = findViewById(R.id.changeDetailsPassword);
        mPassword2 = findViewById(R.id.changeDetailsPassword2);
        mEmail = findViewById(R.id.changeDetailsEmail);
        mPhone = findViewById(R.id.changeDetailsPhone);
        mAddress = findViewById(R.id.changeDetailsAddress);
        mSubmit = findViewById(R.id.changeDetailsSubmit);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        // Display the current student details in the form //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Registration/" + spId);

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                mEmail.setHint(snapshot.child("email").getValue().toString());
                mPhone.setHint(snapshot.child("telno").getValue().toString());
                mAddress.setHint(snapshot.child("address").getValue().toString());

                mSubmit.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {

                        // Retrieve inputs from user //
                        String password = mPassword.getText().toString().trim();
                        String password2 = mPassword2.getText().toString().trim();
                        String email = mEmail.getText().toString().trim();
                        String phone = mPhone.getText().toString().trim();
                        String address = mAddress.getText().toString().trim();

                        // Check the inputs that need updating //
                        if (!TextUtils.isEmpty(password))
                        {

                            if (TextUtils.isEmpty(password2))
                            {

                                mPassword2.setError("Re-enter Password Cannot Be Empty!");

                            }

                        }

                        else if (!TextUtils.isEmpty(password2))
                        {

                            if (TextUtils.isEmpty(password))
                            {

                                mPassword.setError("Password Cannot Be Empty!");

                            }

                        }

                        else
                        {

                            password = snapshot.child("password").getValue().toString();
                            password2 = snapshot.child("password").getValue().toString();

                        }

                        if (TextUtils.isEmpty(email))
                        {

                            email = snapshot.child("email").getValue().toString();

                        }

                        if (TextUtils.isEmpty(phone))
                        {

                            phone = snapshot.child("telno").getValue().toString();

                        }

                        if (TextUtils.isEmpty(address))
                        {

                            address = snapshot.child("address").getValue().toString();

                        }

                        // Ensure the 2 passwords match //
                        if (password.equals(password2))
                        {

                            // Update the details in the database //
                            FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                            DatabaseReference stage2 = database2.getReference("Registration/" + spId);

                            stage2.child("password").setValue(password);
                            stage2.child("email").setValue(email);
                            stage2.child("telno").setValue(phone);
                            stage2.child("address").setValue(address);

                            // Log the audit changes to database //
                            logDetails(spId);

                            Toast.makeText(ChangeDetails.this, "Details Updated!", Toast.LENGTH_SHORT).show();

                        }

                        else
                        {

                            mPassword.setError("Passwords Do Not Match!");
                            mPassword2.setError("Passwords Do Not Match!");

                        }

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

                startActivity (new Intent(getApplicationContext(), StudentPanel.class));
                finish();

                return true;

            case R.id.refresh:

                finish();
                overridePendingTransition(0, 0);
                startActivity (new Intent(getApplicationContext(), ChangeDetails.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void logDetails (String dbId)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Student with the ID: " + dbId.toUpperCase() + " updated their personal details at " + logTime.format(date) +
                " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-DETAILS")
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