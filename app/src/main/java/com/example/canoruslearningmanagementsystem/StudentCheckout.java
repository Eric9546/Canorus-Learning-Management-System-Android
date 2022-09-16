package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StudentCheckout extends AppCompatActivity
{

    private static final int FILE_CODE = 69;

    String newfilename = "";
    String stuId = "";
    String stuProgram = "";
    String stuSession = "";

    FirebaseFirestore log = FirebaseFirestore.getInstance();

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    Button mUpload, mCancel, mOnline;
    ProgressBar mProgress;

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

                        Toast.makeText(StudentCheckout.this, "Payment Failed", Toast.LENGTH_SHORT).show();

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

                        // Gather student data //
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference stage1 = database.getReference("Registration/" + stuId);

                        stage1.addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {

                                String name = snapshot.child("name").getValue().toString();
                                String program = snapshot.child("program").getValue().toString();
                                String payStatus = "Unapproved";

                                // Update the details in the database //
                                FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                                DatabaseReference stage2 = database2.getReference("Payment/" + stuSession + "/" + stuId);

                                stage2.child("id").setValue(stuId);
                                stage2.child("name").setValue(name);
                                stage2.child("program").setValue(program);
                                stage2.child("session").setValue(stuSession);
                                stage2.child("filename").setValue(newfilename);
                                stage2.child("payStatus").setValue(payStatus);
                                stage2.child("payMode").setValue("Bank In");

                                // Log the payment details //
                                logPayment (stuId, program, stuSession, newfilename);

                                startActivity (new Intent(getApplicationContext(), StudentPanel.class));
                                finish();
                                mProgress.setVisibility(View.INVISIBLE);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {

                            }

                        });

                    }

                }

            });

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_checkout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        String spProgram = mPreferences.getString(PROGRAM_KEY, "");

        stuId = spId;
        stuProgram = spProgram;
        stuSession = spSession;

        mUpload = findViewById(R.id.studentCheckoutUpload);
        mCancel = findViewById(R.id.studentCheckoutCancel);
        mOnline = findViewById(R.id.studentCheckoutOnline);
        mProgress = findViewById(R.id.studentCheckoutProgress);

        mUpload.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Set progress //
                mProgress.setVisibility(View.VISIBLE);

                // Create link to firebase storage for payment file //
                newfilename = "PAYMENT_" + spId + "_" + spProgram + "_" + spSession + ".pdf";
                mStorageReference = FirebaseStorage.getInstance().getReference(newfilename);

                // Upload the file to storage //
                Intent intent = new Intent();
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select PDF File"), FILE_CODE);

            }

        });

        mOnline.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                startActivity (new Intent(getApplicationContext(), StudentOnlinePayment.class));
                finish();

            }

        });

        mCancel.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                startActivity (new Intent(getApplicationContext(), StudentPayment.class));
                finish();

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
                startActivity (new Intent(getApplicationContext(), StudentCheckout.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void logPayment (String dbId, String program, String session, String newfilename)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Student with the ID: " + dbId.toUpperCase() + " enrolled under " + program + " " + session +
                " submitted their payment file titled " + newfilename + " at " +
                logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-PAYMENT")
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