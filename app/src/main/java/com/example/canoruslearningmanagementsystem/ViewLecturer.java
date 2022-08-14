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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

public class ViewLecturer extends AppCompatActivity implements TwoRowAdapter.ItemClickListener
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
    String finalLecId = "";

    TwoRowAdapter adapter;

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
        setContentView(R.layout.activity_view_lecturer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");
        finalLecId = spId;

        mSearch = findViewById(R.id.viewLecturerName);
        mSubmit = findViewById(R.id.viewLecturerSubmit);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {

            search = extras.getString("search");

        }

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Lecturer/");

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    if (item.child("name").getValue().toString().toUpperCase().contains(search))
                    {

                        row1.add(item.child("lecId").getValue().toString() + " - " + item.child("name").getValue().toString());
                        row2.add(item.child("email").getValue().toString() + " | " + item.child("telno").getValue().toString());
                        row3.add(item.child("lecId").getValue().toString());

                    }

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.viewLecturerRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(ViewLecturer.this));
                adapter = new TwoRowAdapter (ViewLecturer.this, row1, row2);
                adapter.setClickListener(ViewLecturer.this);
                recyclerView.setAdapter(adapter);

                mSubmit.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {

                        String search = mSearch.getText().toString().toUpperCase();

                        finish();
                        overridePendingTransition(0, 0);
                        Intent intent = new Intent(ViewLecturer.this, ViewLecturer.class);
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
                Intent intent = new Intent(ViewLecturer.this, ViewLecturer.class);
                intent.putExtra("search", search);
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

        Toast.makeText(ViewLecturer.this, "Email | Phone", Toast.LENGTH_SHORT).show();

        String lecId = row3.get(position);

        Button mEdit, mRemove;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_view_lecturer, null);

        mEdit = popupView.findViewById(R.id.popup_view_lecturer_edit);
        mRemove = popupView.findViewById(R.id.popup_view_lecturer_remove);

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

        mEdit.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(ViewLecturer.this, EditLecturer.class);
                intent.putExtra("lecId", lecId);
                startActivity(intent);

            }

        });

        mRemove.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                // Query to delete the record from the database table //
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference stage1 = database.getReference("Lecturer/" + lecId);

                stage1.removeValue();

                // Log the removed lecturer //
                logRemoveLecturer(finalLecId, lecId);

                finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(ViewLecturer.this, ViewLecturer.class);
                intent.putExtra("search", search);
                startActivity(intent);
                overridePendingTransition(0, 0);

            }

        });

    }

    public void logRemoveLecturer (String dbId, String lecId)
    {

        // Get current date and time //
        SimpleDateFormat logDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat logTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date ();

        // Log the user login onto the logging database //
        String logMsg = "Staff with the ID: " + dbId.toUpperCase() + " removed the lecturer with ID: " + lecId.toUpperCase() +
                " at " + logTime.format(date) + " HRS using the ANDROID MOBILE platform";
        Map<String, Object> user = new HashMap<>();
        user.put("logMsg", logMsg);

        log.collection(logDate.format(date) + "-LECTURER")
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