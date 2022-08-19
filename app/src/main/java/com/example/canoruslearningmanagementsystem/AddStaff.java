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

public class AddStaff extends AppCompatActivity
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

    EditText mId, mPassword, mName, mEmail, mPhone, mIc, mAddress;
    Spinner mSpinner;
    Button mSubmit;

    ArrayList<String> row1 = new ArrayList<>();

    String exit = "false";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_staff);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        mId = findViewById(R.id.addStaffId);
        mPassword = findViewById(R.id.addStaffPassword);
        mName = findViewById(R.id.addStaffName);
        mEmail = findViewById(R.id.addStaffEmail);
        mPhone = findViewById(R.id.addStaffPhone);
        mIc = findViewById(R.id.addStaffIc);
        mAddress = findViewById(R.id.addStaffAddress);
        mSpinner = findViewById(R.id.addStaffAccessLevel);
        mSubmit = findViewById(R.id.addStaffSubmit);

        // Set up the drop down menu //
        row1.add("Lecturer");
        row1.add("Admin");
        row1.add("Program Officer");
        row1.add("Exam Unit");
        row1.add("Finance");
        row1.add("Registry");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddStaff.this, android.R.layout.simple_spinner_item, row1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        mSubmit.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                String id = mId.getText().toString();
                String password = mPassword.getText().toString();
                String name = mName.getText().toString();
                String email = mEmail.getText().toString();
                String phone = mPhone.getText().toString();
                String ic = mIc.getText().toString();
                String address = mAddress.getText().toString();

                if (TextUtils.isEmpty(id))
                {

                    mId.setError("User ID Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(password))
                {

                    mPassword.setError("Password Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(name))
                {

                    mName.setError("Name Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(email))
                {

                    mEmail.setError("Email Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(phone))
                {

                    mPhone.setError("Phone Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(ic))
                {

                    mIc.setError("IC/Password Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(address))
                {

                    mAddress.setError("Address Cannot Be Empty!");
                    return;

                }

                // Query to check if user ID already exists //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage = database.getReference("Registration/" + id.toUpperCase());

                stage.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        if (snapshot.getValue() == null)
                        {

                            stage.child("id").setValue(id.toUpperCase());
                            stage.child("password").setValue(password);
                            stage.child("access_level").setValue(mSpinner.getSelectedItem().toString());
                            stage.child("name").setValue(name);
                            stage.child("email").setValue(email);
                            stage.child("telno").setValue(phone);
                            stage.child("ic").setValue(ic);
                            stage.child("address").setValue(address);
                            stage.child("program").setValue("N/A");
                            stage.child("session").setValue("N/A");

                            FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                            DatabaseReference stage2 = database2.getReference("Lecturer/" + id.toUpperCase());

                            stage2.child("lecId").setValue(id.toUpperCase());
                            stage2.child("name").setValue(name);
                            stage2.child("email").setValue(email);
                            stage2.child("telno").setValue(phone);

                            // Log the details //
                            logAddStaff(spId, id.toUpperCase(), mSpinner.getSelectedItem().toString());

                            exit = "true";

                            finish();
                            overridePendingTransition(0, 0);
                            startActivity (new Intent(getApplicationContext(), AddStaff.class));
                            overridePendingTransition(0, 0);

                            Toast.makeText(AddStaff.this, "Staff Added!", Toast.LENGTH_SHORT).show();

                        }

                        else
                        {

                            if (exit.equalsIgnoreCase("true"))
                            {



                            }

                            else
                            {

                                Toast.makeText(AddStaff.this, "User ID Already Exists!", Toast.LENGTH_SHORT).show();

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
                startActivity (new Intent(getApplicationContext(), AddStaff.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void logAddStaff (String dbId, String userId, String accessLevel)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " added user with ID: " + userId.toUpperCase() + " with access level: " + accessLevel +
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