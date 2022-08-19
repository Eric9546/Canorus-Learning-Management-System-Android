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

public class EditStaff extends AppCompatActivity
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

    TextView mTitle;
    EditText mPassword, mName, mEmail, mPhone, mIc, mAddress;
    Spinner mSpinner;
    Button mSubmit;

    String id = "";

    ArrayList<String> row1 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_staff);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {

            id = extras.getString("id");

        }

        mPassword = findViewById(R.id.editStaffPassword);
        mName = findViewById(R.id.editStaffName);
        mEmail = findViewById(R.id.editStaffEmail);
        mPhone = findViewById(R.id.editStaffPhone);
        mIc = findViewById(R.id.editStaffIc);
        mAddress = findViewById(R.id.editStaffAddress);
        mSpinner = findViewById(R.id.editStaffAccessLevel);
        mSubmit = findViewById(R.id.editStaffSubmit);
        mTitle = findViewById(R.id.editStaffText9);

        mTitle.setText("Edited: " + id);

        // Set up the drop down menu //
        row1.add("Lecturer");
        row1.add("Admin");
        row1.add("Program Officer");
        row1.add("Exam Unit");
        row1.add("Finance");
        row1.add("Registry");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditStaff.this, android.R.layout.simple_spinner_item, row1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        // Display the current lecturer details in the form //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Registration/" + id);

        stage1.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                mPassword.setHint(snapshot.child("password").getValue().toString());
                mName.setHint(snapshot.child("name").getValue().toString());
                mEmail.setHint(snapshot.child("email").getValue().toString());
                mPhone.setHint(snapshot.child("telno").getValue().toString());
                mIc.setHint(snapshot.child("ic").getValue().toString());
                mAddress.setHint(snapshot.child("address").getValue().toString());

                mSubmit.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {

                        String password = mPassword.getText().toString().trim();
                        String name = mName.getText().toString().trim();
                        String email = mEmail.getText().toString().trim();
                        String phone = mPhone.getText().toString().trim();
                        String ic = mIc.getText().toString().trim();
                        String address = mAddress.getText().toString().trim();

                        if (TextUtils.isEmpty(password))
                        {

                            password = snapshot.child("password").getValue().toString();

                        }

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

                        if (TextUtils.isEmpty(ic))
                        {

                            ic = snapshot.child("ic").getValue().toString();

                        }

                        if (TextUtils.isEmpty(address))
                        {

                            address = snapshot.child("address").getValue().toString();

                        }

                        // Update the details in the database //
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference("Registration/" + id);

                        stage2.child("password").setValue(password);
                        stage2.child("access_level").setValue(mSpinner.getSelectedItem().toString());
                        stage2.child("name").setValue(name);
                        stage2.child("email").setValue(email);
                        stage2.child("telno").setValue(phone);
                        stage2.child("ic").setValue(ic);
                        stage2.child("address").setValue(address);

                        FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                        DatabaseReference stage3 = database3.getReference("Lecturer/" + id.toUpperCase());

                        stage3.child("name").setValue(name);
                        stage3.child("email").setValue(email);
                        stage3.child("telno").setValue(phone);

                        // Log the audit changes to database //
                        logEditStaff(spId, id, mSpinner.getSelectedItem().toString());

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(EditStaff.this, EditStaff.class);
                        intent.putExtra("id", id);
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
                Intent intent = new Intent(EditStaff.this, EditStaff.class);
                intent.putExtra("id", id);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void logEditStaff (String dbId, String userId, String accessLevel)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " updated the user details with ID: " + userId.toUpperCase() + " with access level: " + accessLevel +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-STAFF")
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