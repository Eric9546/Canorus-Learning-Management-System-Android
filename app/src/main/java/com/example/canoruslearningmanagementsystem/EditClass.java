package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditClass extends AppCompatActivity implements ThreeRowAdapter.ItemClickListener
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

    String program = "";
    String subId = "";
    String finalId = "";
    String lecId = "";
    String exit = "";
    String sectionFinal = "";

    ThreeRowAdapter adapterRecycler;

    EditText mRoom;
    Button mSubmit;
    Spinner mDay, mTimeStart, mTimeEnd, mType;
    CheckBox mSection1, mSection2, mSection3, mSection4, mSection5, mSection6;

    // Set up array to store info from database //
    ArrayList<String> arrayDay = new ArrayList<>();
    ArrayList<String> arrayTimeStart = new ArrayList<>();
    ArrayList<String> arrayTimeEnd = new ArrayList<>();
    ArrayList<String> arrayType = new ArrayList<>();
    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();
    ArrayList<String> row3 = new ArrayList<>();
    ArrayList<String> row4 = new ArrayList<>();
    ArrayList<String> row5 = new ArrayList<>();
    ArrayList<String> row6 = new ArrayList<>();
    ArrayList<String> row7 = new ArrayList<>();
    List<String> sectionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        finalId = spId;

        mRoom = findViewById(R.id.editClassRoom);
        mSubmit = findViewById(R.id.editClassSubmit);
        mDay = findViewById(R.id.editClassDay);
        mTimeStart = findViewById(R.id.editClassTimeStart);
        mTimeEnd = findViewById(R.id.editClassTimeEnd);
        mType = findViewById(R.id.editClassType);
        mSection1 = findViewById(R.id.editClassBox1);
        mSection2 = findViewById(R.id.editClassBox2);
        mSection3 = findViewById(R.id.editClassBox3);
        mSection4 = findViewById(R.id.editClassBox4);
        mSection5 = findViewById(R.id.editClassBox5);
        mSection6 = findViewById(R.id.editClassBox6);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {

            program = extras.getString("program");
            subId = extras.getString("subId");

        }

        // Set up the day drop down //
        arrayDay.add("Monday");
        arrayDay.add("Tuesday");
        arrayDay.add("Wednesday");
        arrayDay.add("Thursday");
        arrayDay.add("Friday");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditClass.this, android.R.layout.simple_spinner_item, arrayDay);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDay.setAdapter(adapter);

        // Set up the time start drop down //
        arrayTimeStart.add("8:00am");
        arrayTimeStart.add("9:00am");
        arrayTimeStart.add("10:00am");
        arrayTimeStart.add("11:00am");
        arrayTimeStart.add("12:00pm");
        arrayTimeStart.add("1:00pm");
        arrayTimeStart.add("2:00pm");
        arrayTimeStart.add("3:00pm");
        arrayTimeStart.add("4:00pm");
        arrayTimeStart.add("5:00pm");

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(EditClass.this, android.R.layout.simple_spinner_item, arrayTimeStart);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTimeStart.setAdapter(adapter2);

        // Set up the time end drop down //
        arrayTimeEnd.add("9:00am");
        arrayTimeEnd.add("10:00am");
        arrayTimeEnd.add("11:00am");
        arrayTimeEnd.add("12:00pm");
        arrayTimeEnd.add("1:00pm");
        arrayTimeEnd.add("2:00pm");
        arrayTimeEnd.add("3:00pm");
        arrayTimeEnd.add("4:00pm");
        arrayTimeEnd.add("5:00pm");
        arrayTimeEnd.add("6:00pm");

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(EditClass.this, android.R.layout.simple_spinner_item, arrayTimeEnd);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTimeEnd.setAdapter(adapter3);

        // Set up the type drop down //
        arrayType.add("Lecture");
        arrayType.add("Tutorial");
        arrayType.add("Practical");
        arrayType.add("Online");

        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(EditClass.this, android.R.layout.simple_spinner_item, arrayType);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mType.setAdapter(adapter4);

        // Set up the sections //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Subject/" + subId);

        stage1.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                String str = snapshot.child("section").getValue().toString();
                sectionList = Arrays.asList(str.split(","));
                lecId = snapshot.child("lecId").getValue().toString();

                switch (sectionList.size())
                {

                    case 1:

                        mSection1.setVisibility(View.VISIBLE);
                        mSection1.setText(sectionList.get(0));

                        return;

                    case 2:

                        mSection1.setVisibility(View.VISIBLE);
                        mSection1.setText(sectionList.get(0));
                        mSection2.setVisibility(View.VISIBLE);
                        mSection2.setText(sectionList.get(1));

                        return;

                    case 3:

                        mSection1.setVisibility(View.VISIBLE);
                        mSection1.setText(sectionList.get(0));
                        mSection2.setVisibility(View.VISIBLE);
                        mSection2.setText(sectionList.get(1));
                        mSection3.setVisibility(View.VISIBLE);
                        mSection3.setText(sectionList.get(2));

                        return;

                    case 4:

                        mSection1.setVisibility(View.VISIBLE);
                        mSection1.setText(sectionList.get(0));
                        mSection2.setVisibility(View.VISIBLE);
                        mSection2.setText(sectionList.get(1));
                        mSection3.setVisibility(View.VISIBLE);
                        mSection3.setText(sectionList.get(2));
                        mSection4.setVisibility(View.VISIBLE);
                        mSection4.setText(sectionList.get(3));

                        return;

                    case 5:

                        mSection1.setVisibility(View.VISIBLE);
                        mSection1.setText(sectionList.get(0));
                        mSection2.setVisibility(View.VISIBLE);
                        mSection2.setText(sectionList.get(1));
                        mSection3.setVisibility(View.VISIBLE);
                        mSection3.setText(sectionList.get(2));
                        mSection4.setVisibility(View.VISIBLE);
                        mSection4.setText(sectionList.get(3));
                        mSection5.setVisibility(View.VISIBLE);
                        mSection5.setText(sectionList.get(4));

                        return;

                    case 6:

                        mSection1.setVisibility(View.VISIBLE);
                        mSection1.setText(sectionList.get(0));
                        mSection2.setVisibility(View.VISIBLE);
                        mSection2.setText(sectionList.get(1));
                        mSection3.setVisibility(View.VISIBLE);
                        mSection3.setText(sectionList.get(2));
                        mSection4.setVisibility(View.VISIBLE);
                        mSection4.setText(sectionList.get(3));
                        mSection5.setVisibility(View.VISIBLE);
                        mSection5.setText(sectionList.get(4));
                        mSection6.setVisibility(View.VISIBLE);
                        mSection6.setText(sectionList.get(5));

                        return;

                    default:

                        mSection1.setVisibility(View.VISIBLE);
                        mSection1.setText(sectionList.get(0));
                        mSection2.setVisibility(View.VISIBLE);
                        mSection2.setText(sectionList.get(1));
                        mSection3.setVisibility(View.VISIBLE);
                        mSection3.setText(sectionList.get(2));
                        mSection4.setVisibility(View.VISIBLE);
                        mSection4.setText(sectionList.get(3));
                        mSection5.setVisibility(View.VISIBLE);
                        mSection5.setText(sectionList.get(4));
                        mSection6.setVisibility(View.VISIBLE);
                        mSection6.setText(sectionList.get(5));

                        return;

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });

        // Set up the classes //
        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference stage2 = database2.getReference("Class/" + program + "/" + subId);

        stage2.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    row1.add(item.child("day").getValue().toString() + " | " + item.child("timeStart").getValue().toString() + " - " + item.child("timeEnd").getValue().toString());
                    row2.add(item.child("room").getValue().toString() + " | " + item.child("type").getValue().toString());
                    row3.add(item.child("section").getValue().toString() + " | " + item.child("lecId").getValue().toString());
                    row4.add(item.child("uuid").getValue().toString());
                    row5.add(item.child("day").getValue().toString());
                    row6.add(item.child("timeStart").getValue().toString());
                    row7.add(item.child("timeEnd").getValue().toString());

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.editClassRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(EditClass.this));
                adapterRecycler = new ThreeRowAdapter (EditClass.this, row1, row2, row3);
                adapterRecycler.setClickListener(EditClass.this);
                recyclerView.setAdapter(adapterRecycler);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });


        mSubmit.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Retrieve the spinner inputs //
                String day = mDay.getSelectedItem().toString();
                String timeStart = mTimeStart.getSelectedItem().toString();
                String timeEnd = mTimeEnd.getSelectedItem().toString();
                String type = mType.getSelectedItem().toString();

                // Retrieve the section input //
                ArrayList<String> section = new ArrayList<>();

                switch (sectionList.size())
                {

                    case 1:

                        if (mSection1.isChecked())
                        {

                            section.add(mSection1.getText().toString());

                        }

                        break;

                    case 2:

                        if (mSection1.isChecked())
                        {

                            section.add(mSection1.getText().toString());

                        }

                        if (mSection2.isChecked())
                        {

                            section.add(mSection2.getText().toString());

                        }

                        break;

                    case 3:

                        if (mSection1.isChecked())
                        {

                            section.add(mSection1.getText().toString());

                        }

                        if (mSection2.isChecked())
                        {

                            section.add(mSection2.getText().toString());

                        }

                        if (mSection3.isChecked())
                        {

                            section.add(mSection3.getText().toString());

                        }

                        break;

                    case 4:

                        if (mSection1.isChecked())
                        {

                            section.add(mSection1.getText().toString());

                        }

                        if (mSection2.isChecked())
                        {

                            section.add(mSection2.getText().toString());

                        }

                        if (mSection3.isChecked())
                        {

                            section.add(mSection3.getText().toString());

                        }

                        if (mSection4.isChecked())
                        {

                            section.add(mSection4.getText().toString());

                        }

                        break;

                    case 5:

                        if (mSection1.isChecked())
                        {

                            section.add(mSection1.getText().toString());

                        }

                        if (mSection2.isChecked())
                        {

                            section.add(mSection2.getText().toString());

                        }

                        if (mSection3.isChecked())
                        {

                            section.add(mSection3.getText().toString());

                        }

                        if (mSection4.isChecked())
                        {

                            section.add(mSection4.getText().toString());

                        }

                        if (mSection5.isChecked())
                        {

                            section.add(mSection5.getText().toString());

                        }

                        break;

                    case 6:

                        if (mSection1.isChecked())
                        {

                            section.add(mSection1.getText().toString());

                        }

                        if (mSection2.isChecked())
                        {

                            section.add(mSection2.getText().toString());

                        }

                        if (mSection3.isChecked())
                        {

                            section.add(mSection3.getText().toString());

                        }

                        if (mSection4.isChecked())
                        {

                            section.add(mSection4.getText().toString());

                        }

                        if (mSection5.isChecked())
                        {

                            section.add(mSection5.getText().toString());

                        }

                        if (mSection6.isChecked())
                        {

                            section.add(mSection6.getText().toString());

                        }

                        break;

                    default:

                        if (mSection1.isChecked())
                        {

                            section.add(mSection1.getText().toString());

                        }

                        if (mSection2.isChecked())
                        {

                            section.add(mSection2.getText().toString());

                        }

                        if (mSection3.isChecked())
                        {

                            section.add(mSection3.getText().toString());

                        }

                        if (mSection4.isChecked())
                        {

                            section.add(mSection4.getText().toString());

                        }

                        if (mSection5.isChecked())
                        {

                            section.add(mSection5.getText().toString());

                        }

                        if (mSection6.isChecked())
                        {

                            section.add(mSection6.getText().toString());

                        }

                        break;

                }

                if (section.size() == 0)
                {

                    Toast.makeText(EditClass.this, "Minimum 1 Section Required!", Toast.LENGTH_SHORT).show();
                    return;

                }

                for (String x : section)
                {

                    sectionFinal = sectionFinal + x + "-";

                }

                sectionFinal = sectionFinal.substring(0, sectionFinal.length() - 1);

                // Retrieve the room //
                String room = mRoom.getText().toString();

                if (TextUtils.isEmpty(room))
                {

                    mRoom.setError("Room Cannot Be Empty!");
                    return;

                }

                // Find the timeStartIndex //
                int timeStartIndex = timeStartIndex(timeStart);
                int timeEndIndex = timeEndIndex(timeEnd);

                // Validate the time inputs //
                if (timeEndIndex <= timeStartIndex)
                {

                    Toast.makeText(EditClass.this, "End Time Cannot Be Equal Or Earlier Than Start Time!", Toast.LENGTH_SHORT).show();
                    return;

                }

                // Check for time clash //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage = database.getReference("Class/" + program + "/" + subId);

                stage.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        for (DataSnapshot item:snapshot.getChildren())
                        {

                            // Check the day //
                            if (item.child("day").getValue().toString().equalsIgnoreCase(day))
                            {

                                // Check the time //
                                if (timeStartIndex >= Integer.parseInt(item.child("timeStartIndex").getValue().toString()) &&
                                    timeStartIndex < Integer.parseInt(item.child("timeEndIndex").getValue().toString()))
                                {

                                    Toast.makeText(EditClass.this, "Clashes With The Class Start Time!", Toast.LENGTH_SHORT).show();

                                    exit = "true";

                                    return;

                                }

                                else if (timeEndIndex > Integer.parseInt(item.child("timeStartIndex").getValue().toString()) &&
                                        timeEndIndex <= Integer.parseInt(item.child("timeEndIndex").getValue().toString()))
                                {

                                    Toast.makeText(EditClass.this, "Clashes With The Class End Time!", Toast.LENGTH_SHORT).show();

                                    exit = "true";

                                    return;

                                }

                            }


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }

                });

                // Check for room and lecturer clash //
                FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database1.getReference("Class/");

                stage1.addValueEventListener(new ValueEventListener()
                {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1)
                    {

                        for (DataSnapshot item1:snapshot1.getChildren())
                        {

                            FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                            DatabaseReference stage2 = database2.getReference("Class/" + item1.getKey());

                            stage2.addValueEventListener(new ValueEventListener()
                            {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot2)
                                {

                                    for (DataSnapshot item2:snapshot2.getChildren())
                                    {

                                        if (!item2.getKey().equalsIgnoreCase(subId))
                                        {

                                            FirebaseDatabase database3 = FirebaseDatabase.getInstance();
                                            DatabaseReference stage3 = database3.getReference("Class/" + item1.getKey() + "/" + item2.getKey());

                                            stage3.addValueEventListener(new ValueEventListener()
                                            {

                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot3)
                                                {

                                                    for (DataSnapshot item3:snapshot3.getChildren())
                                                    {

                                                        // Check the day //
                                                        if (item3.child("day").getValue().toString().equalsIgnoreCase(day))
                                                        {

                                                            // Check the time //
                                                            if (timeStartIndex >= Integer.parseInt(item3.child("timeStartIndex").getValue().toString()) &&
                                                                timeStartIndex < Integer.parseInt(item3.child("timeEndIndex").getValue().toString()))
                                                            {

                                                                // Check the room //
                                                                if (item3.child("room").getValue().toString().equalsIgnoreCase(room))
                                                                {

                                                                    Toast.makeText(EditClass.this, "Clashes With Room Availability! (" + room + ")", Toast.LENGTH_SHORT).show();

                                                                    exit = "true";

                                                                    return;

                                                                }

                                                                // Check the lecturer //
                                                                if (item3.child("lecId").getValue().toString().equalsIgnoreCase(lecId))
                                                                {

                                                                    Toast.makeText(EditClass.this, "Clashes With Lecturer Availability! (" + lecId + ")", Toast.LENGTH_SHORT).show();

                                                                    exit = "true";

                                                                    return;

                                                                }


                                                            }

                                                            if (timeEndIndex > Integer.parseInt(item3.child("timeStartIndex").getValue().toString()) &&
                                                                timeEndIndex <= Integer.parseInt(item3.child("timeEndIndex").getValue().toString()))
                                                            {

                                                                // Check the room //
                                                                if (item3.child("room").getValue().toString().equalsIgnoreCase(room))
                                                                {

                                                                    Toast.makeText(EditClass.this, "Clashes With Room Availability! (" + room + ")", Toast.LENGTH_SHORT).show();

                                                                    exit = "true";

                                                                    return;

                                                                }

                                                                // Check the lecturer //
                                                                if (item3.child("lecId").getValue().toString().equalsIgnoreCase(lecId))
                                                                {

                                                                    Toast.makeText(EditClass.this, "Clashes With Lecturer Availability! (" + lecId + ")", Toast.LENGTH_SHORT).show();

                                                                    exit = "true";

                                                                    return;

                                                                }


                                                            }

                                                        }

                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error)
                                                {

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

                        if (exit.equalsIgnoreCase("true"))
                        {



                        }

                        else
                        {

                            // Inserting the data into the database table //
                            UUID uuid = UUID.randomUUID();
                            FirebaseDatabase database4 = FirebaseDatabase.getInstance();
                            DatabaseReference stage4 = database4.getReference("Class/" + program + "/" + subId + "/" + uuid.toString());

                            stage4.child("uuid").setValue(uuid.toString());
                            stage4.child("subId").setValue(subId);
                            stage4.child("program").setValue(program);
                            stage4.child("day").setValue(day);
                            stage4.child("timeStart").setValue(timeStart);
                            stage4.child("timeStartIndex").setValue(timeStartIndex);
                            stage4.child("timeEndIndex").setValue(timeEndIndex);
                            stage4.child("timeEnd").setValue(timeEnd);
                            stage4.child("room").setValue(room);
                            stage4.child("section").setValue(sectionFinal);
                            stage4.child("type").setValue(type);
                            stage4.child("lecId").setValue(lecId);

                            // Log the added class //
                            logAddClass (finalId, program, subId, day, timeStart, timeEnd);

                            SystemClock.sleep(3000);

                            finish();
                            overridePendingTransition(0, 0);
                            Intent intent = new Intent(EditClass.this, EditClass.class);
                            intent.putExtra("subId", subId);
                            intent.putExtra("program", program);
                            startActivity(intent);
                            overridePendingTransition(0, 0);

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
                Intent intent = new Intent(EditClass.this, EditClass.class);
                intent.putExtra("subId", subId);
                intent.putExtra("program", program);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onItemClick(View view, int position)
    {

        // Remove the class //
        String uuid = row4.get(position);
        String day = row5.get(position);
        String timeStart = row6.get(position);
        String timeEnd = row7.get(position);

        // Query to delete the record from the database table //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Class/" + program + "/" + subId + "/" + uuid);

        stage1.removeValue();

        // Log the removed class //
        logRemoveClass(finalId, program, subId, day, timeStart, timeEnd);

        finish();
        overridePendingTransition(0, 0);
        Intent intent = new Intent(EditClass.this, EditClass.class);
        intent.putExtra("subId", subId);
        intent.putExtra("program", program);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    public void logAddClass (String dbId, String program, String subId, String day, String timeStart, String timeEnd)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " added the class for " + program + "-" + subId + " for " + day +
                " " + timeStart + "-" + timeEnd +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-CLASS")
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

    public void logRemoveClass (String dbId, String program, String subId, String day, String timeStart, String timeEnd)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " removed the class for " + program + "-" + subId + " for " + day +
                " " + timeStart + "-" + timeEnd +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-CLASS")
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

    public int timeStartIndex (String timeStart) {

        int index = 0;

        if (timeStart.equalsIgnoreCase("8:00am")) {
            index = 8;
        } else if (timeStart.equalsIgnoreCase("9:00am")) {
            index = 9;
        } else if (timeStart.equalsIgnoreCase("10:00am")) {
            index = 10;
        } else if (timeStart.equalsIgnoreCase("11:00am")) {
            index = 11;
        } else if (timeStart.equalsIgnoreCase("12:00pm")) {
            index = 12;
        } else if (timeStart.equalsIgnoreCase("1:00pm")) {
            index = 13;
        } else if (timeStart.equalsIgnoreCase("2:00pm")) {
            index = 14;
        } else if (timeStart.equalsIgnoreCase("3:00pm")) {
            index = 15;
        } else if (timeStart.equalsIgnoreCase("4:00pm")) {
            index = 16;
        } else if (timeStart.equalsIgnoreCase("5:00pm")) {
            index = 17;
        }

        return index;

    }

    public int timeEndIndex (String timeEnd) {

        int index = 0;

        if (timeEnd.equalsIgnoreCase("9:00am")) {
            index = 9;
        } else if (timeEnd.equalsIgnoreCase("10:00am")) {
            index = 10;
        } else if (timeEnd.equalsIgnoreCase("11:00am")) {
            index = 11;
        } else if (timeEnd.equalsIgnoreCase("12:00pm")) {
            index = 12;
        } else if (timeEnd.equalsIgnoreCase("1:00pm")) {
            index = 13;
        } else if (timeEnd.equalsIgnoreCase("2:00pm")) {
            index = 14;
        } else if (timeEnd.equalsIgnoreCase("3:00pm")) {
            index = 15;
        } else if (timeEnd.equalsIgnoreCase("4:00pm")) {
            index = 16;
        } else if (timeEnd.equalsIgnoreCase("5:00pm")) {
            index = 17;
        } else if (timeEnd.equalsIgnoreCase("6:00pm")) {
            index = 18;
        }

        return index;

    }

}