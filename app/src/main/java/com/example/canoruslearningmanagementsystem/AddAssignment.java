package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddAssignment extends AppCompatActivity implements AdapterView.OnItemSelectedListener
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

    String subId = "";
    String newfilename = "";
    String extension = "";
    String mime = "";
    String assignAdded = "false";
    String title = "";
    String desc = "";
    String date = "";
    String time = "";
    String dbId = "";

    int hour = 0;
    int minute = 0;

    private DatePickerDialog mDatePickerDialog;
    private Button dateButton;

    Button mAdd, mTime;
    EditText mTitle, mDesc;
    Spinner mSpinner;

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

                        Toast.makeText(AddAssignment.this, "Submission Failed", Toast.LENGTH_SHORT).show();

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

                        String record_to_submit = "Assignment/" + subId + "/Question/" + title;
                        String dueDate = date + ", " + time;

                        // Update the database //
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference(record_to_submit);

                        stage2.child("assignTitle").setValue(title);
                        stage2.child("assignDesc").setValue(desc);
                        stage2.child("dueDate").setValue(dueDate);
                        stage2.child("fileName").setValue(newfilename);

                        // Log the assignment //
                        logAddAssignment(dbId, subId, title);

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(AddAssignment.this, ViewAssignmentFiltered.class);
                        intent.putExtra("subId", subId);
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
        setContentView(R.layout.activity_add_assignment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initDatePicker();
        dateButton = findViewById(R.id.addAssignmentDate);
        dateButton.setText(getTodaysDate());
        mAdd = findViewById(R.id.addAssignmentSubmit);
        mTime = findViewById(R.id.addAssignmentTime);
        mTitle = findViewById(R.id.addAssignmentTitle);
        mDesc = findViewById(R.id.addAssignmentDesc);

        mSpinner = findViewById(R.id.addAssignmentExtension);
        mSpinner.setOnItemSelectedListener(this);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        dbId = spId;

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            subId = extras.getString("subId");

        }

        // Set up the drop down menu //
        ext1.add("pdf");
        ext1.add("vnd.openxmlformats-officedocument.wordprocessingml.document");
        ext1.add("vnd.ms-powerpoint");
        ext2.add(".pdf");
        ext2.add(".docx");
        ext2.add(".pptx");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddAssignment.this, android.R.layout.simple_spinner_item, ext2);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        mAdd.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                title = mTitle.getText().toString();
                desc = mDesc.getText().toString();
                date = dateButton.getText().toString();
                time = mTime.getText().toString();

                if (TextUtils.isEmpty(title))
                {

                    mTitle.setError("Assignment Title Cannot Be Empty!");
                    return;

                }

                if (TextUtils.isEmpty(desc))
                {

                    mDesc.setError("Assignment Description Cannot Be Empty!");
                    return;

                }

                // Query to check if assignment title already exists //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference("Assignment/" + subId + "/Question/" + title);

                stage1.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        if (snapshot.getValue() == null)
                        {

                            // Create link to firebase storage for submission file //
                            newfilename = "ASSIGNMENT_" + subId + "_" + title + extension;
                            mStorageReference = FirebaseStorage.getInstance().getReference(newfilename);

                            // Upload the file to storage //
                            Intent intent = new Intent();
                            intent.setType("application/" + mime);
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_CODE);

                            assignAdded = "true";

                        }

                        else
                        {

                            if (assignAdded.equalsIgnoreCase("true"))
                            {



                            }

                            else
                            {

                                Toast.makeText(AddAssignment.this, "Assignment Title Already Exists!", Toast.LENGTH_SHORT).show();

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

    private String getTodaysDate()
    {

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return makeDateString(day, month, year);

    }

    private void initDatePicker()
    {

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {

            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day)
            {

                month = month + 1;
                String date =   makeDateString (day, month, year);
                dateButton.setText(date);

            }

        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        mDatePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);

    }

    private String makeDateString (int day, int month, int year)
    {

        return day + "-" + month + "-" + year;

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

                startActivity (new Intent(getApplicationContext(), LecturerPanel.class));
                finish();

                return true;

            case R.id.refresh:

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(AddAssignment.this, AddAssignment.class);
                intent.putExtra("subId", subId);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    public void openDatePicker(View view)
    {

        mDatePickerDialog.show();

    }


    public void popTimePicker(View view)
    {

        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener()
        {

            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
            {

                hour = selectedHour;
                minute = selectedMinute;

                String dateString3 = selectedHour + ":" + selectedMinute;

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                Date date3 = null;
                try
                {

                    date3 = sdf.parse(dateString3);

                }

                catch (ParseException e)
                {
                    e.printStackTrace();
                }

                SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mmaa");

                mTime.setText(sdf2.format(date3));

            }

        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();

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

    public void logAddAssignment (String dbId, String subId, String assignTitle)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Lecturer with the ID: " + dbId.toUpperCase() + " added the assignment titled " + assignTitle + " for the subject " + subId +
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