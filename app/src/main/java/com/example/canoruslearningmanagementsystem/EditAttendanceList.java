package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class EditAttendanceList extends AppCompatActivity implements ThreeRowAdapter.ItemClickListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    ThreeRowAdapter adapter;

    String subId = "";
    String section = "";
    String program = "";
    String session = "";
    String record_to_view = "";
    String search = "";

    EditText mSearch;
    Button mSubmit;

    // Set up array to store info from database //
    ArrayList<String> row1 = new ArrayList<>();
    ArrayList<String> row2 = new ArrayList<>();
    ArrayList<String> row3 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_attendance_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            subId = extras.getString("subId");
            program = extras.getString("program");
            session = extras.getString("session");
            section = extras.getString("section");
            record_to_view = extras.getString("record_to_view");
            search = extras.getString("search");

        }

        mSearch = findViewById(R.id.editAttendanceListId);
        mSubmit = findViewById(R.id.editAttendanceListSubmit);

        // Retrieve the program value //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference(record_to_view);

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    if ((!item.getKey().equalsIgnoreCase("attendPin")) && (!item.getKey().equalsIgnoreCase("attendStatus"))
                    && (!item.getKey().equalsIgnoreCase("classDateTime")) && (!item.getKey().equalsIgnoreCase("QRcode")) )
                    {

                        if (item.child("stuName").getValue().toString().toUpperCase().contains(search))
                        {

                            row1.add(item.child("stuId").getValue().toString());
                            row2.add(item.child("stuName").getValue().toString());
                            row3.add(item.child("attendStatus").getValue().toString());

                        }

                    }

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.editAttendanceListRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(EditAttendanceList.this));
                adapter = new ThreeRowAdapter (EditAttendanceList.this, row1, row2, row3);
                adapter.setClickListener(EditAttendanceList.this);
                recyclerView.setAdapter(adapter);

                mSubmit.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {

                        String search = mSearch.getText().toString().toUpperCase(Locale.ROOT);

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(EditAttendanceList.this, EditAttendanceList.class);
                        intent.putExtra("subId", subId);
                        intent.putExtra("program", program);
                        intent.putExtra("session", session);
                        intent.putExtra("section", section);
                        intent.putExtra("record_to_view", record_to_view);
                        intent.putExtra("search", search);
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
    public void onItemClick(View view, int position)
    {

        String record_to_update = record_to_view + "/" + row1.get(position);

        Button mPresent, mAbsent;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_edit_attendance_list, null);

        mPresent = popupView.findViewById(R.id.popup_edit_attendance_list_present);
        mAbsent = popupView.findViewById(R.id.popup_edit_attendance_list_absent);

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

        mPresent.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Set student as present //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference(record_to_update);

                stage1.child("attendStatus").setValue("Present");

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditAttendanceList.this, EditAttendanceList.class);
                intent.putExtra("subId", subId);
                intent.putExtra("program", program);
                intent.putExtra("session", session);
                intent.putExtra("section", section);
                intent.putExtra("record_to_view", record_to_view);
                intent.putExtra("search", search);
                startActivity(intent);
                overridePendingTransition(0, 0);

            }

        });

        mAbsent.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Set student as absent //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference(record_to_update);

                stage1.child("attendStatus").setValue("Absent");

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(EditAttendanceList.this, EditAttendanceList.class);
                intent.putExtra("subId", subId);
                intent.putExtra("program", program);
                intent.putExtra("session", session);
                intent.putExtra("section", section);
                intent.putExtra("record_to_view", record_to_view);
                intent.putExtra("search", search);
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
                Intent intent = new Intent(EditAttendanceList.this, EditAttendanceList.class);
                intent.putExtra("subId", subId);
                intent.putExtra("program", program);
                intent.putExtra("session", session);
                intent.putExtra("section", section);
                intent.putExtra("record_to_view", record_to_view);
                intent.putExtra("search", "");
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

}