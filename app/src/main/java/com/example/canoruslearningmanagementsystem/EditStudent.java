package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EditStudent extends AppCompatActivity
{

    private static final int FILE_CODE = 69;

    FirebaseFirestore log = FirebaseFirestore.getInstance();

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    String id = "";
    String newfilename = "";
    String finalId = "";

    String password = "";
    String name = "";
    String email = "";
    String phone = "";
    String ic = "";
    String address = "";
    String program = "";
    String session = "";

    EditText mPassword, mName, mEmail, mPhone, mIc, mAddress;
    Spinner mProgram, mSession;
    Button mSubmit;
    CheckBox mCert;
    TextView mTitle;

    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();

    StorageReference mStorageReference;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CODE)
        {

            UploadTask uploadTask = mStorageReference.putFile(data.getData());

            // Create file retrieval task //
            Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
            {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {

                    if (!task.isSuccessful())
                    {

                        Toast.makeText(EditStudent.this, "Process Failed", Toast.LENGTH_SHORT).show();

                    }

                    return mStorageReference.getDownloadUrl();

                }

            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {

                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {

                    if (task.isSuccessful())
                    {

                        // Update the details in the database //
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference("Registration/" + id);

                        stage2.child("password").setValue(password);
                        stage2.child("name").setValue(name);
                        stage2.child("email").setValue(email);
                        stage2.child("telno").setValue(phone);
                        stage2.child("ic").setValue(ic);
                        stage2.child("address").setValue(address);
                        stage2.child("program").setValue(program);
                        stage2.child("session").setValue(session);
                        stage2.child("filename").setValue(newfilename);

                        // Log the audit changes to database //
                        logEditStudent (finalId, id, program, session);

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(EditStudent.this, EditStudent.class);
                        intent.putExtra("id", id);
                        startActivity(intent);
                        overridePendingTransition(0, 0);

                    }

                }

            });

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        finalId = spId;

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {

            id = extras.getString("id");

        }

        mPassword = findViewById(R.id.editStudentPassword);
        mName = findViewById(R.id.editStudentName);
        mEmail = findViewById(R.id.editStudentEmail);
        mPhone = findViewById(R.id.editStudentPhone);
        mIc = findViewById(R.id.editStudentIc);
        mAddress = findViewById(R.id.editStudentAddress);
        mProgram = findViewById(R.id.editStudentProgram);
        mSession = findViewById(R.id.editStudentSession);
        mSubmit = findViewById(R.id.editStudentSubmit);
        mTitle = findViewById(R.id.editStudentText11);
        mCert = findViewById(R.id.editStudentCert);

       mTitle.setText("Edited: " + id);

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

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditStudent.this, android.R.layout.simple_spinner_item, row1);
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
        DatabaseReference stage2 = database2.getReference("Session/");

        stage2.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    row2.add(item.child("session").getValue().toString());

                }

                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(EditStudent.this, android.R.layout.simple_spinner_item, row2);
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSession.setAdapter(adapter2);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });

        // Display the current student details in the form //
        FirebaseDatabase database3 = FirebaseDatabase.getInstance();
        DatabaseReference stage3 = database.getReference("Registration/" + id);

        stage3.addValueEventListener(new ValueEventListener()
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

                        password = mPassword.getText().toString().trim();
                        name = mName.getText().toString().trim();
                        email = mEmail.getText().toString().trim();
                        phone = mPhone.getText().toString().trim();
                        ic = mIc.getText().toString().trim();
                        address = mAddress.getText().toString().trim();
                        program = mProgram.getSelectedItem().toString().trim();
                        session = mSession.getSelectedItem().toString().trim();

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

                        if (mCert.isChecked())
                        {

                            // Create link to firebase storage for payment file //
                            newfilename = "STUDENT_" + id + "_" + program + "_" + session + ".pdf";
                            mStorageReference = FirebaseStorage.getInstance().getReference(newfilename);

                            // Upload the file to storage //
                            Intent intent = new Intent();
                            intent.setType("application/pdf");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select PDF File"), FILE_CODE);

                        }

                        else
                        {

                            // Update the details in the database //
                            FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                            DatabaseReference stage2 = database2.getReference("Registration/" + id);

                            stage2.child("password").setValue(password);
                            stage2.child("name").setValue(name);
                            stage2.child("email").setValue(email);
                            stage2.child("telno").setValue(phone);
                            stage2.child("ic").setValue(ic);
                            stage2.child("address").setValue(address);
                            stage2.child("program").setValue(program);
                            stage2.child("session").setValue(session);

                            // Log the audit changes to database //
                            logEditStudent (finalId, id, program, session);

                            finish();
                            overridePendingTransition(0, 0);
                            Intent intent = new Intent(EditStudent.this, EditStudent.class);
                            intent.putExtra("id", id);
                            startActivity(intent);
                            overridePendingTransition(0, 0);

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

                startActivity (new Intent(getApplicationContext(), AdminPanel.class));
                finish();

                return true;

            case R.id.refresh:

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditStudent.this, EditStudent.class);
                intent.putExtra("id", id);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void logEditStudent (String dbId, String stuId, String program, String session)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " updated the student details with ID: " + stuId.toUpperCase() + " enrolled under " + program + " " + session +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
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