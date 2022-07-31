package com.example.canoruslearningmanagementsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StudentPayment extends AppCompatActivity implements TwoRowAdapter.ItemClickListener
{

    // Set up the session variables //
    private final String ID_KEY = "id";
    private final String ACCESS_LEVEL_KEY = "access_level";
    private final String LOGIN_STATUS_KEY = "login_status";
    private final String SESSION_KEY = "session";
    private final String PROGRAM_KEY = "program";
    private SharedPreferences mPreferences;
    private String spFileName = "com.example.session";

    int total = 0;

    TextView mTotal;
    Button mSubmit;

    TwoRowAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_payment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get session details //
        mPreferences = getSharedPreferences(spFileName, MODE_PRIVATE);
        SharedPreferences.Editor spEditor = mPreferences.edit();

        String spId = mPreferences.getString(ID_KEY, "");
        String spSession = mPreferences.getString(SESSION_KEY, "");

        mTotal = findViewById(R.id.studentPaymentText2);
        mSubmit = findViewById(R.id.studentPaymentButton);

        // Retrieve data from database //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference stage1 = database.getReference("Enrolment/" + spId.toUpperCase());

        // Set up array to store info from database //
        ArrayList<String> row1 = new ArrayList<>();
        ArrayList<String> row2 = new ArrayList<>();

        stage1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot item:snapshot.getChildren())
                {

                    if (item.child("payStatus").getValue().toString().equalsIgnoreCase("Unpaid"))
                    {

                        String temp = item.child("fee").getValue().toString();

                        total = total + Integer.parseInt(temp);

                        row1.add(item.child("subId").getValue().toString() + " - " + item.child("subName").getValue().toString());
                        row2.add(item.child("program").getValue().toString() + " | " + item.child("session").getValue().toString()
                                                                             + " | RM" + item.child("fee").getValue().toString()
                                                                             + " | " + item.child("payStatus").getValue().toString());

                    }

                }

                // set up the RecyclerView
                RecyclerView recyclerView = findViewById(R.id.studentPaymentRecycler);
                recyclerView.setLayoutManager(new LinearLayoutManager(StudentPayment.this));
                adapter = new TwoRowAdapter (StudentPayment.this, row1, row2);
                adapter.setClickListener(StudentPayment.this);
                recyclerView.setAdapter(adapter);

                mTotal.setText("RM " + String.valueOf(total) + ".00");

                mSubmit.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {

                        // Check if payment already submitted //
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference stage2 = database2.getReference("Payment/" + spSession + "/" + spId.toUpperCase());

                        stage2.addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {

                                if (snapshot.getValue() == null)
                                {

                                    startActivity (new Intent(getApplicationContext(), StudentCheckout.class));
                                    finish();

                                }

                                else
                                {

                                    Toast.makeText(StudentPayment.this, "Your Payment Has Been Sent/Completed!", Toast.LENGTH_SHORT).show();

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

    @Override
    public void onItemClick(View view, int position)
    {
        Toast.makeText(StudentPayment.this, "Program | Session | Fee | Payment Status", Toast.LENGTH_SHORT).show();
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
                startActivity (new Intent(getApplicationContext(), StudentPayment.class));
                overridePendingTransition(0, 0);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

}