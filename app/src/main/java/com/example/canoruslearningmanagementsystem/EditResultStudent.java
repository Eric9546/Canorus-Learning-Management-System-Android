package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

public class EditResultStudent extends AppCompatActivity implements TwoRowAdapter.ItemClickListener, AdapterView.OnItemSelectedListener
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

    String search = "";
    String program = "";
    String stuId = "";
    String finalId = "";
    String subId = "";
    String session = "";
    String exit = "false";

    TwoRowAdapter adapter;

    EditText mSearch;
    Button mSubmit, mResult;
    Spinner mSubject, mGrade;

    // Set up array to store info from database //
    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();
    ArrayList<String> row3 = new ArrayList<>();
    ArrayList<String> row4 = new ArrayList<>();
    ArrayList<String> row5 = new ArrayList<>();
    ArrayList<String> row6 = new ArrayList<>();
    ArrayList<String> row7 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_result_student);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        finalId = spId;

        mSearch = findViewById(R.id.editResultStudentName);
        mSubmit = findViewById(R.id.editResultStudentSubmit);
        mResult = findViewById(R.id.editResultStudentResult);
        mSubject = findViewById(R.id.editResultStudentSubject);
        mGrade = findViewById(R.id.editResultStudentGrade);
        mSubject.setOnItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {

            stuId = extras.getString("stuId");
            search = extras.getString("search");
            program = extras.getString("program");
            session = extras.getString("session");

        }

        // Set up the drop down menu //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Subject/");

        stage1.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    if (item.child("program").getValue().toString().equalsIgnoreCase(program))
                    {

                        row1.add(item.child("subId").getValue().toString() + " - " + item.child("subName").getValue().toString());
                        row2.add(item.child("subId").getValue().toString());

                    }

                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditResultStudent.this, android.R.layout.simple_spinner_item, row1);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSubject.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });

        // Set up the drop down menu //
        row3.add("A+");
        row3.add("A");
        row3.add("A-");
        row3.add("B+");
        row3.add("B");
        row3.add("B-");
        row3.add("C+");
        row3.add("C");
        row3.add("C-");
        row3.add("D");
        row3.add("F");

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(EditResultStudent.this, android.R.layout.simple_spinner_item, row3);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGrade.setAdapter(adapter2);

        // Add the grade for the subject //
        mResult.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Query to check if subject already has result //
                FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                DatabaseReference stage3 = database3.getReference("Result/" + stuId + "/" + subId);

                stage3.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        if (snapshot.getValue() == null)
                        {

                            // Get subject data //
                            FirebaseDatabase database4 = FirebaseDatabase.getInstance();
                            DatabaseReference stage4 = database4.getReference("Subject/" + subId);

                            stage4.addValueEventListener(new ValueEventListener()
                            {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot)
                                {

                                    SimpleDateFormat logDate = new SimpleDateFormat("dd/MM/yyyy");
                                    Date date = new Date ();

                                    String subName = snapshot.child("subName").getValue().toString();

                                    stage3.child("id").setValue(stuId);
                                    stage3.child("subId").setValue(subId);
                                    stage3.child("subName").setValue(subName);
                                    stage3.child("program").setValue(program);
                                    stage3.child("session").setValue(session);
                                    stage3.child("grade").setValue(mGrade.getSelectedItem().toString());
                                    stage3.child("date").setValue(logDate.format(date));

                                    // Log the result added //
                                    logAddResult(finalId, stuId, mGrade.getSelectedItem().toString(), program, subId);

                                    finish();
                                    overridePendingTransition(0, 0);
                                    Intent intent = new Intent(EditResultStudent.this, EditResultStudent.class);
                                    intent.putExtra("stuId", stuId);
                                    intent.putExtra("program", program);
                                    intent.putExtra("search", search);
                                    intent.putExtra("session", session);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error)
                                {

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

                                Toast.makeText(EditResultStudent.this, "This Subject Already Has Results!", Toast.LENGTH_SHORT).show();

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

        // Search subject ID //
        mSubmit.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                String search = mSearch.getText().toString().toUpperCase();

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditResultStudent.this, EditResultStudent.class);
                intent.putExtra("stuId", stuId);
                intent.putExtra("program", program);
                intent.putExtra("search", search);
                intent.putExtra("session", session);
                startActivity(intent);
                overridePendingTransition(0, 0);

            }

        });

        // Retrieve data from database //
        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference stage2 = database2.getReference("Result/" + stuId);

        stage2.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    if (item.child("subId").getValue().toString().toUpperCase().contains(search))
                    {

                        row4.add(item.child("subId").getValue().toString() + " - " + item.child("subName").getValue().toString());
                        row5.add(item.child("grade").getValue().toString());
                        row6.add(item.child("subId").getValue().toString());

                    }

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.editResultStudentRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(EditResultStudent.this));
                adapter = new TwoRowAdapter (EditResultStudent.this, row4, row5);
                adapter.setClickListener(EditResultStudent.this);
                recyclerView.setAdapter(adapter);

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
                Intent intent = new Intent(EditResultStudent.this, EditResultStudent.class);
                intent.putExtra("stuId", stuId);
                intent.putExtra("program", program);
                intent.putExtra("search", search);
                intent.putExtra("session", session);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {

        subId = row2.get(position);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    @Override
    public void onItemClick(View view, int position)
    {

        String finalSubId = row6.get(position);

        Spinner mGradePopup;
        Button mGradeSubmit, mRemove;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_edit_result_student, null);

        mGradeSubmit = popupView.findViewById(R.id.popup_edit_result_student_grade_submit);
        mRemove = popupView.findViewById(R.id.popup_edit_result_student_remove);
        mGradePopup = popupView.findViewById(R.id.popup_edit_result_student_grade);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                popupWindow.dismiss();
                return true;
            }

        });

        // Set up the drop down menu //
        row7.add("A+");
        row7.add("A");
        row7.add("A-");
        row7.add("B+");
        row7.add("B");
        row7.add("B-");
        row7.add("C+");
        row7.add("C");
        row7.add("C-");
        row7.add("D");
        row7.add("F");

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(EditResultStudent.this, android.R.layout.simple_spinner_item, row7);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGradePopup.setAdapter(adapter3);

        mGradeSubmit.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Query to update the record from the database table //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference("Result/" + stuId + "/" + finalSubId);

                SimpleDateFormat logDate = new SimpleDateFormat("dd/MM/yyyy");
                Date date = new Date ();

                stage1.child("grade").setValue(mGradePopup.getSelectedItem().toString());
                stage1.child("date").setValue(logDate.format(date));

                // Log the updated grade //
                logEditResult(finalId, stuId, mGradePopup.getSelectedItem().toString(), program, finalSubId);

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditResultStudent.this, EditResultStudent.class);
                intent.putExtra("stuId", stuId);
                intent.putExtra("program", program);
                intent.putExtra("search", search);
                intent.putExtra("session", session);
                startActivity(intent);
                overridePendingTransition(0, 0);

            }

        });

        mRemove.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Query to delete the record from the database table //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference("Result/" + stuId + "/" + finalSubId);

                stage1.removeValue();

                // Log the details //
                logRemoveResult(finalId, stuId, program, finalSubId);

                Toast.makeText(EditResultStudent.this, "Result Removed", Toast.LENGTH_SHORT).show();

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditResultStudent.this, EditResultStudent.class);
                intent.putExtra("stuId", stuId);
                intent.putExtra("program", program);
                intent.putExtra("search", search);
                intent.putExtra("session", session);
                startActivity(intent);
                overridePendingTransition(0, 0);

            }

        });

    }

    public void logRemoveResult (String dbId, String stuId, String program, String subId)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " removed the grade for the student: " + stuId.toUpperCase() + " in " + program + " " + subId +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-RESULT")
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

    public void logAddResult (String dbId, String stuId, String grade, String program, String subId)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " updated the grade to " + grade + " for the student: " + stuId + " in " + program + " " + subId +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-RESULT")
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

    public void logEditResult (String dbId, String stuId, String grade, String program, String subId)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " updated the grade to " + grade + " for the student: " + stuId + " in " + program + " " + subId +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-RESULT")
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