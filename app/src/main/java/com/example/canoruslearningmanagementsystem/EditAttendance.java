package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class EditAttendance extends AppCompatActivity implements TwoRowAdapter.ItemClickListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    TwoRowAdapter adapter;

    String subId = "";
    String section = "";
    String program = "";
    String session = "";
    String stuId = "";
    String exit = "false";

    private DatePickerDialog mDatePickerDialog;
    private Button dateButton;

    Spinner mStart, mEnd;
    Button mSubmit;

    ArrayList<String> timeStart = new ArrayList<>();
    ArrayList<String> timeEnd = new ArrayList<>();

    // Set up array to store info from database //
    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_attendance);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        initDatePicker();
        dateButton = findViewById(R.id.editAttendanceDate);
        dateButton.setText(getTodaysDate());

        mStart = findViewById(R.id.editAttendanceStart);
        mEnd = findViewById(R.id.editAttendanceEnd);
        mSubmit = findViewById(R.id.editAttendanceSubmit);

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        session = spSession;
        stuId = spId;

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            subId = extras.getString("subId");
            program = extras.getString("program");
            session = extras.getString("session");
            section = extras.getString("section");
            exit = extras.getString("exit");

        }

        // Set up the drop down menu //
        timeStart.add("8:00am");
        timeStart.add("9:00am");
        timeStart.add("10:00am");
        timeStart.add("11:00am");
        timeStart.add("12:00pm");
        timeStart.add("1:00pm");
        timeStart.add("2:00pm");
        timeStart.add("3:00pm");
        timeStart.add("4:00pm");
        timeStart.add("5:00pm");

        timeEnd.add("9:00am");
        timeEnd.add("10:00am");
        timeEnd.add("11:00am");
        timeEnd.add("12:00pm");
        timeEnd.add("1:00pm");
        timeEnd.add("2:00pm");
        timeEnd.add("3:00pm");
        timeEnd.add("4:00pm");
        timeEnd.add("5:00pm");
        timeEnd.add("6:00pm");

        ArrayAdapter<String> adapterStart = new ArrayAdapter<String>(EditAttendance.this, android.R.layout.simple_spinner_item, timeStart);
        adapterStart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStart.setAdapter(adapterStart);

        ArrayAdapter<String> adapterEnd = new ArrayAdapter<String>(EditAttendance.this, android.R.layout.simple_spinner_item, timeEnd);
        adapterEnd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEnd.setAdapter(adapterEnd);

        // Retrieve the data //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Attendance/" + session + "/" + subId + "/" + section);

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                if (exit.equalsIgnoreCase("true"))
                {



                }

                else
                {

                    for (DataSnapshot item:snapshot.getChildren())
                    {

                        row1.add(item.getKey());
                        row2.add(item.child("attendStatus").getValue().toString() + " | " + item.child("attendPin").getValue().toString());

                    }

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.editAttendanceRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(EditAttendance.this));
                adapter = new TwoRowAdapter(EditAttendance.this, row1, row2);
                adapter.setClickListener(EditAttendance.this);
                recyclerView.setAdapter(adapter);

                mSubmit.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {

                        String date = dateButton.getText().toString();
                        String timeStart = mStart.getSelectedItem().toString();
                        String timeEnd = mEnd.getSelectedItem().toString();
                        String classDateTime = date + "-" + timeStart + "-" + timeEnd;

                        // Query to check if content name already exists //
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference("Attendance/" + session + "/" + subId + "/" + section + "/" + classDateTime);

                        stage2.addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2)
                            {

                                if (snapshot2.getValue() == null)
                                {

                                    // Generate 6 digit pin //
                                    Random rnd = new Random();
                                    int number = rnd.nextInt(999999);

                                    String attendPin = String.format("%06d", number);

                                    // Inserting the data into the database table //
                                    FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                                    DatabaseReference stage3 = database3.getReference("Attendance/" + session + "/" + subId + "/" + section + "/" + classDateTime);

                                    stage3.child("classDateTime").setValue(classDateTime);
                                    stage3.child("attendPin").setValue(attendPin);
                                    stage3.child("attendStatus").setValue("Opened");
                                    stage3.child("QRcode").setValue("images/no_image.jpg");

                                    studentList(classDateTime);

                                    finish();
                                    overridePendingTransition(0, 0);
                                    Intent intent = new Intent(EditAttendance.this, EditAttendance.class);
                                    intent.putExtra("subId", subId);
                                    intent.putExtra("program", program);
                                    intent.putExtra("session", session);
                                    intent.putExtra("section", section);
                                    intent.putExtra("exit", "true");
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);

                                    exit = "true";

                                }

                                else
                                {

                                    if (exit.equalsIgnoreCase("true"))
                                    {



                                    }

                                    else
                                    {

                                        Toast.makeText(EditAttendance.this, "Attendance Item Already Exists!", Toast.LENGTH_SHORT).show();

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
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });

    }

    private void studentList (String classDateTime)
    {

        // Inserting student list //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Enrolment/");

        stage1.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot item : snapshot.getChildren())
                {

                    FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                    DatabaseReference stage2 = database2.getReference("Enrolment/" + item.getKey());

                    stage2.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2)
                        {

                            for (DataSnapshot item2 : snapshot2.getChildren())
                            {

                                // Retrieve student personal details //
                                FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                                DatabaseReference stage3 = database3.getReference("Registration/" + item.getKey());

                                stage3.addValueEventListener(new ValueEventListener()
                                {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot3)
                                    {

                                        String stuName = snapshot3.child("name").getValue().toString();

                                        if (item2.getKey().equalsIgnoreCase(subId))
                                        {

                                            FirebaseDatabase database4 = FirebaseDatabase.getInstance();
                                            DatabaseReference stage4 = database4.getReference("Enrolment/" + item.getKey() + "/" + item2.getKey());

                                            stage4.addValueEventListener(new ValueEventListener()
                                            {

                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot4)
                                                {

                                                    if ((section.contains(snapshot4.child("section").getValue().toString())) &&
                                                    snapshot4.child("session").getValue().toString().equalsIgnoreCase(session))
                                                    {

                                                        FirebaseDatabase database5 = FirebaseDatabase.getInstance();
                                                        DatabaseReference stage5 = database5.getReference("Attendance/" + session + "/" + subId + "/" + section + "/" + classDateTime + "/" + item.getKey());

                                                        stage5.child("stuId").setValue(item.getKey());
                                                        stage5.child("stuName").setValue(stuName);
                                                        stage5.child("attendStatus").setValue("Absent");

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error)
                                                {

                                                }

                                            });

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error)
                                    {

                                    }

                                });

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error)
                        {

                        }

                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });

    }

    @Override
    public void onItemClick(View view, int position)
    {

        String record_to_view = "Attendance/" + session + "/" + subId + "/" + section + "/" + row1.get(position);

        Button mOpen, mClose, mView, mDelete;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_edit_attendance, null);

        mOpen = popupView.findViewById(R.id.popup_edit_attendance_open);
        mClose = popupView.findViewById(R.id.popup_edit_attendance_close);
        mView = popupView.findViewById(R.id.popup_edit_attendance_view);
        mDelete = popupView.findViewById(R.id.popup_edit_attendance_delete);

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

        mOpen.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                // Open the attendance //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference(record_to_view);

                stage1.child("attendStatus").setValue("Opened");

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditAttendance.this, EditAttendance.class);
                intent.putExtra("subId", subId);
                intent.putExtra("program", program);
                intent.putExtra("session", session);
                intent.putExtra("section", section);
                intent.putExtra("exit", "false");
                startActivity(intent);
                overridePendingTransition(0, 0);

            }
        });

        mClose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                // Close the attendance //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference(record_to_view);

                stage1.child("attendStatus").setValue("Closed");

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditAttendance.this, EditAttendance.class);
                intent.putExtra("subId", subId);
                intent.putExtra("program", program);
                intent.putExtra("session", session);
                intent.putExtra("section", section);
                intent.putExtra("exit", "false");
                startActivity(intent);
                overridePendingTransition(0, 0);

            }
        });

        mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(EditAttendance.this, EditAttendanceList.class);
                intent.putExtra("subId", subId);
                intent.putExtra("program", program);
                intent.putExtra("session", session);
                intent.putExtra("section", section);
                intent.putExtra("record_to_view", record_to_view);
                intent.putExtra("search", "");
                startActivity(intent);

            }
        });

        mDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                // Remove the attendance list //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference(record_to_view);

                stage1.removeValue();

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditAttendance.this, EditAttendance.class);
                intent.putExtra("subId", subId);
                intent.putExtra("program", program);
                intent.putExtra("session", session);
                intent.putExtra("section", section);
                intent.putExtra("exit", "false");
                startActivity(intent);
                overridePendingTransition(0, 0);

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

                startActivity (new Intent(getApplicationContext(), LecturerPanel.class));
                finish();

                return true;

            case R.id.refresh:

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditAttendance.this, EditAttendance.class);
                intent.putExtra("subId", subId);
                intent.putExtra("program", program);
                intent.putExtra("session", session);
                intent.putExtra("section", section);
                intent.putExtra("exit", "false");
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

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

    public void openDatePicker(View view)
    {

        mDatePickerDialog.show();

    }

}