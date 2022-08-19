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
import android.widget.TextView;
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

public class EditLecturer extends AppCompatActivity
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

    TextView mLecId;
    EditText mName, mEmail, mPhone;
    Button mSubmit;

    String lecId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lecturer);
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

        mName = findViewById(R.id.editLecturerName);
        mEmail = findViewById(R.id.editLecturerEmail);
        mPhone = findViewById(R.id.editLecturerPhone);
        mSubmit = findViewById(R.id.editLecturerSubmit);
        mLecId = findViewById(R.id.editLecturerText5);

        mLecId.setText("Edited: " + lecId);

        // Display the current lecturer details in the form //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Lecturer/" + lecId);

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                mName.setHint(snapshot.child("name").getValue().toString());
                mEmail.setHint(snapshot.child("email").getValue().toString());
                mPhone.setHint(snapshot.child("telno").getValue().toString());

                mSubmit.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {

                        String name = mName.getText().toString().trim();
                        String email = mEmail.getText().toString().trim();
                        String phone = mPhone.getText().toString().trim();

                        // Check the inputs that need updating //
                        if (TextUtils.isEmpty(name))
                        {

                            name = snapshot.child("name").getValue().toString();

                        }

                        if (TextUtils.isEmpty(email))
                        {

                            email = snapshot.child("email").getValue().toString();

                        }

                        if (TextUtils.isEmpty(phone))
                        {

                            phone = snapshot.child("telno").getValue().toString();

                        }

                        // Update the details in the database //
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference("Lecturer/" + lecId);

                        stage2.child("name").setValue(name);
                        stage2.child("email").setValue(email);
                        stage2.child("telno").setValue(phone);

                        // Update the details in the database //
                        FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                        DatabaseReference stage3 = database3.getReference("Registration/" + lecId);

                        stage3.child("name").setValue(name);
                        stage3.child("email").setValue(email);
                        stage3.child("telno").setValue(phone);

                        // Log the audit changes to database //
                        logEditLecturer(spId, lecId);

                        Toast.makeText(EditLecturer.this, "Details Updated!", Toast.LENGTH_SHORT).show();

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(EditLecturer.this, EditLecturer.class);
                        intent.putExtra("lecId", lecId);
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
                Intent intent = new Intent(EditLecturer.this, EditLecturer.class);
                intent.putExtra("lecId", lecId);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void logEditLecturer (String dbId, String lecId)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " updated the lecturer details with ID: " + lecId.toUpperCase() +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-LECTURER")
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