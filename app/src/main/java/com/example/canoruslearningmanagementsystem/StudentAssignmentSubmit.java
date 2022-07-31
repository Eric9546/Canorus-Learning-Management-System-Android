package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
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

public class StudentAssignmentSubmit extends AppCompatActivity implements AdapterView.OnItemSelectedListener
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

    TextView mSubmitDate, mStatus, mGrade, mComment, mExtension;
    Button mQuestion, mView, mUpload;
    ProgressBar mProgress;
    Spinner mSpinner;

    String subId = "";
    String record_to_submit = "";
    String assignTitle = "";
    String exit = "false";
    String extension = "";
    String mime = "";
    String stuId = "";
    String newfilename = "";

    ArrayList<String> ext1 = new ArrayList<>();
    ArrayList<String> ext2 = new ArrayList<>();

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

                        Toast.makeText(StudentAssignmentSubmit.this, "Submission Failed", Toast.LENGTH_SHORT).show();

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

                        String record_to_submit = "Assignment/" + subId + "/Submit/" + assignTitle + "/" + stuId;

                        // Get time stamp //
                        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
                        SimpleDateFormat logTime = new SimpleDateFormat("hh:mma");
                        Date date = new Date ();

                        String submitDate = logDate.format(date) + ", " + logTime.format(date);

                       // Update the database

                        FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                        DatabaseReference stage3 = database3.getReference(record_to_submit);

                        stage3.child("id").setValue(stuId);
                        stage3.child("status").setValue("Submitted");
                        stage3.child("grade").setValue("N/A");
                        stage3.child("comment").setValue("N/A");
                        stage3.child("submitDate").setValue(submitDate);
                        stage3.child("fileName").setValue(newfilename);


                        Intent intent = new Intent(StudentAssignmentSubmit.this, StudentAssignmentSubmit.class);
                        intent.putExtra("subId", subId);
                        intent.putExtra("record_to_submit", record_to_submit);
                        intent.putExtra("assignTitle", assignTitle);
                        startActivity(intent);
                        finish();
                        mProgress.setVisibility(View.INVISIBLE);
                        mSpinner.setVisibility(View.INVISIBLE);
                        mExtension.setVisibility(View.INVISIBLE);

                        // Log the submission //
                        logAssignment(stuId, subId, assignTitle, newfilename);

                    }

                }

            });

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_assignment_submit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSubmitDate = findViewById(R.id.studentAssignmentSubmitText2);
        mStatus = findViewById(R.id.studentAssignmentSubmitText4);
        mGrade = findViewById(R.id.studentAssignmentSubmitText6);
        mComment = findViewById(R.id.studentAssignmentSubmitText8);
        mExtension = findViewById(R.id.studentAssignmentSubmitText9);

        mQuestion = findViewById(R.id.studentAssignmentSubmitQuestion);
        mView = findViewById(R.id.studentAssignmentSubmitView);
        mUpload = findViewById(R.id.studentAssignmentSubmitUpload);

        mProgress = findViewById(R.id.studentAssignmentSubmitProgress);
        mSpinner = findViewById(R.id.studentAssignmentSubmitExtension);
        mSpinner.setOnItemSelectedListener(this);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        stuId = spId;

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            subId = extras.getString("subId");
            record_to_submit = extras.getString("record_to_submit");
            assignTitle = extras.getString("assignTitle");

        }

        // Set up array to store info from database //
        ArrayList<String> row1 = new ArrayList<>();
        ArrayList<String> row2 = new ArrayList<>();

        // Set up the drop down menu //
        ext1.add("pdf");
        ext1.add("vnd.openxmlformats-officedocument.wordprocessingml.document");
        ext1.add("vnd.ms-powerpoint");
        ext2.add(".pdf");
        ext2.add(".docx");
        ext2.add(".pptx");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(StudentAssignmentSubmit.this, android.R.layout.simple_spinner_item, ext2);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Assignment/" + subId + "/Submit/" + assignTitle + "/" + spId);

        stage1.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                // Download the question //
                mQuestion.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {

                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference(record_to_submit);

                        stage2.addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2)
                            {

                                String fileName = snapshot2.child("fileName").getValue().toString();
                                downloadFile(fileName);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {

                            }

                        });

                    }
                });

                if (snapshot.getValue() == null)
                {

                    mSubmitDate.setText("Not Submitted");
                    mStatus.setText("N/A");
                    mGrade.setText("N/A");
                    mComment.setText("N/A");

                    mUpload.setVisibility(View.VISIBLE);
                    mExtension.setVisibility(View.VISIBLE);
                    mSpinner.setVisibility(View.VISIBLE);

                    mUpload.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {

                            mProgress.setVisibility(View.VISIBLE);

                            // Create link to firebase storage for submission file //
                            newfilename = "ASSIGNMENT_" + subId + "_" + assignTitle + "_" + stuId + extension;
                            mStorageReference = FirebaseStorage.getInstance().getReference(newfilename);

                            // Upload the file to storage //
                            Intent intent = new Intent();
                            intent.setType("application/" + mime);
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_CODE);

                            exit = "true";

                        }

                    });

                }

                else
                {

                    if (exit.equalsIgnoreCase("true"))
                    {

                    }

                    else
                    {

                        mSubmitDate.setText(snapshot.child("submitDate").getValue().toString());
                        mStatus.setText(snapshot.child("status").getValue().toString());
                        mGrade.setText(snapshot.child("grade").getValue().toString());
                        mComment.setText(snapshot.child("comment").getValue().toString());

                        mView.setVisibility(View.VISIBLE);

                        // View the submitted file //
                        mView.setOnClickListener(new View.OnClickListener()
                        {

                            @Override
                            public void onClick(View view)
                            {

                                String fileName = snapshot.child("fileName").getValue().toString();
                                downloadFile(fileName);

                            }

                        });

                    }

                }

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
                Intent intent = new Intent(StudentAssignmentSubmit.this, StudentAssignmentSubmit.class);
                intent.putExtra("subId", subId);
                intent.putExtra("record_to_submit", record_to_submit);
                intent.putExtra("assignTitle", assignTitle);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void downloadFile (String fileName)
    {

        // Download the notes //
        String fileLink = "https://firebasestorage.googleapis.com/v0/b/canorus-18990.appspot.com/o/" + fileName + "?alt=media&";

        Uri urifile = Uri.parse(fileLink);

        DownloadManager downloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(urifile);

        request.setTitle(fileName);
        request.setDescription(fileName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);


        Long reference = downloadManager.enqueue(request);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {

        mime = ext1.get(position).toString();
        extension = ext2.get(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    public void logAssignment (String dbId, String subId, String assignTitle, String newfilename)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Student with the ID: " + dbId.toUpperCase() + " submitted their file titled " + newfilename + " for " + subId + "-" + assignTitle +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-ASSIGNMENT")
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